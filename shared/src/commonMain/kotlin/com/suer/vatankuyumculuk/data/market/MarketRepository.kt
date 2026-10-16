package com.suer.vatankuyumculuk.data.market

import com.suer.vatankuyumculuk.core.TrTime
import com.suer.vatankuyumculuk.data.AppJson
import com.suer.vatankuyumculuk.data.config.Announcement
import com.suer.vatankuyumculuk.data.config.ConfigRepository
import com.suer.vatankuyumculuk.data.config.RemoteConfig
import com.suer.vatankuyumculuk.data.config.StoreInfo
import com.suer.vatankuyumculuk.data.harem.HaremFeed
import com.suer.vatankuyumculuk.data.harem.RawQuote
import com.suer.vatankuyumculuk.data.storage.LocalStore
import com.suer.vatankuyumculuk.data.storage.StoreKeys
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.math.min
import kotlin.random.Random

enum class ConnectionStatus { CONNECTING, LIVE, RECONNECTING, OFFLINE, PAUSED }

data class MarketState(
    /** Visible quotes in catalog order. */
    val quotes: List<PricedQuote> = emptyList(),
    val connection: ConnectionStatus = ConnectionStatus.CONNECTING,
    /** Device time of the last message from the live feed. */
    val lastMessageAtMillis: Long? = null,
    /** Newest quote time reported by the source. */
    val dataTimeMillis: Long? = null,
    /** True while showing prices restored from the previous session. */
    val fromCache: Boolean = false,
    val store: StoreInfo = StoreInfo(),
    val announcement: Announcement = Announcement(),
) {
    private val byCode: Map<String, PricedQuote> by lazy { quotes.associateBy { it.code } }

    operator fun get(code: String): PricedQuote? = byCode[code]

    val hasData: Boolean get() = quotes.isNotEmpty()

    fun inGroup(group: ProductGroup): List<PricedQuote> = quotes.filter { it.product.group == group }

    /** The source publishes every few seconds while markets are open. */
    fun isMarketQuiet(nowMillis: Long): Boolean {
        val t = dataTimeMillis ?: return false
        return nowMillis - t > QUIET_AFTER_MS
    }

    companion object {
        const val QUIET_AFTER_MS = 15L * 60 * 1000
    }
}

@Serializable
private data class CachedQuote(
    val code: String,
    val buy: Double? = null,
    val sell: Double? = null,
    val low: Double? = null,
    val high: Double? = null,
    val close: Double? = null,
    val time: String? = null,
)

/**
 * Combines Harem's live feed with the jeweler's remote configuration.
 * [start]/[stop] follow the app's foreground state so no connection is kept in background.
 */
class MarketRepository(
    private val feed: HaremFeed,
    private val configRepository: ConfigRepository,
    private val store: LocalStore,
    private val scope: CoroutineScope,
    private val clock: () -> Long = TrTime::nowMillis,
) {
    private val raw = MutableStateFlow<Map<String, RawQuote>>(loadCachedQuotes())
    private val connection = MutableStateFlow(ConnectionStatus.CONNECTING)
    private val lastMessageAt = MutableStateFlow<Long?>(null)
    private val fromCache = MutableStateFlow(raw.value.isNotEmpty())
    private val tickTracker = TickTracker()

    private var job: Job? = null
    private var feedJob: Job? = null
    private var lastPersistAt = 0L

    val state: StateFlow<MarketState> = combine(
        raw, configRepository.config, connection, lastMessageAt, fromCache,
    ) { quotes, config, status, lastAt, cached ->
        build(quotes, config, status, lastAt, cached)
    }.stateIn(scope, SharingStarted.Eagerly, MarketState())

    val isRunning: Boolean get() = job?.isActive == true

    fun start() {
        if (isRunning) return
        job = scope.launch {
            launch { configRepository.poll() }
            feedJob = launch { runFeed() }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
        feedJob = null
        persist(force = true)
    }

    /** Pull-to-refresh / "Tekrar dene": reconnect immediately and reload the config. */
    fun refreshNow() {
        if (!isRunning) {
            start()
            return
        }
        scope.launch { configRepository.refresh() }
        feedJob?.cancel()
        feedJob = job?.let { parent -> scope.launch(parent) { runFeed() } } ?: scope.launch { runFeed() }
    }

    /** Keeps the live connection open; restarts it whenever the admin switches the feed or its address. */
    private suspend fun runFeed() {
        configRepository.config.map { it.feed }.distinctUntilChanged().collectLatest { feedConfig ->
            if (!feedConfig.enabled) {
                connection.value = ConnectionStatus.PAUSED
                return@collectLatest
            }
            var attempt = 0
            connection.value = ConnectionStatus.CONNECTING
            while (true) {
                try {
                    feed.prices(feedConfig.url).collect { delta ->
                        attempt = 0
                        raw.update { it + delta }
                        lastMessageAt.value = clock()
                        connection.value = ConnectionStatus.LIVE
                        fromCache.value = false
                        persist(force = false)
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Throwable) {
                    // fall through to reconnect
                }
                attempt++
                connection.value = if (attempt >= OFFLINE_AFTER_ATTEMPTS) ConnectionStatus.OFFLINE else ConnectionStatus.RECONNECTING
                delay(backoffMs(attempt))
            }
        }
    }

    private fun build(
        quotes: Map<String, RawQuote>,
        config: RemoteConfig,
        status: ConnectionStatus,
        lastAt: Long?,
        cached: Boolean,
    ): MarketState {
        val priced = ArrayList<PricedQuote>(Catalog.products.size)
        var newest: Long? = null
        // With the feed switched off only the jeweler's fixed prices are shown.
        val live = if (config.feed.enabled) quotes else emptyMap()
        for (product in Catalog.products) {
            val q = PricingEngine.price(product, live[product.code], config.products[product.code]) ?: continue
            priced += tickTracker.track(q)
            val t = q.updatedAtMillis
            if (t != null && (newest == null || t > newest)) newest = t
        }
        return MarketState(
            quotes = priced,
            connection = status,
            lastMessageAtMillis = lastAt,
            dataTimeMillis = newest,
            fromCache = cached,
            store = config.store,
            announcement = config.announcement,
        )
    }

    private fun persist(force: Boolean) {
        val now = clock()
        if (!force && now - lastPersistAt < PERSIST_EVERY_MS) return
        val snapshot = raw.value
        if (snapshot.isEmpty()) return
        lastPersistAt = now
        val list = snapshot.values.map { CachedQuote(it.code, it.buy, it.sell, it.low, it.high, it.close, it.time) }
        runCatching {
            store.putString(StoreKeys.QUOTES_JSON, AppJson.encodeToString(kotlinx.serialization.builtins.ListSerializer(CachedQuote.serializer()), list))
            store.putString(StoreKeys.QUOTES_SAVED_AT, now.toString())
        }
    }

    private fun loadCachedQuotes(): Map<String, RawQuote> {
        val text = store.getString(StoreKeys.QUOTES_JSON) ?: return emptyMap()
        return runCatching {
            AppJson.decodeFromString(kotlinx.serialization.builtins.ListSerializer(CachedQuote.serializer()), text)
                .associate { it.code to RawQuote(it.code, it.buy, it.sell, it.low, it.high, it.close, it.time) }
        }.getOrDefault(emptyMap())
    }

    companion object {
        private const val OFFLINE_AFTER_ATTEMPTS = 3
        private const val PERSIST_EVERY_MS = 15_000L

        fun backoffMs(attempt: Int): Long {
            val base = min(30_000L, 1_000L shl min(attempt - 1, 5).coerceAtLeast(0))
            val jitter = (base * 0.2 * (Random.nextDouble() * 2 - 1)).toLong()
            return (base + jitter).coerceAtLeast(500)
        }
    }
}
