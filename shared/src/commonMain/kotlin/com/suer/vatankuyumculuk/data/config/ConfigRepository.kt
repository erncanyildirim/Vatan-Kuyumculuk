package com.suer.vatankuyumculuk.data.config

import com.suer.vatankuyumculuk.data.AppJson
import com.suer.vatankuyumculuk.data.storage.LocalStore
import com.suer.vatankuyumculuk.data.storage.StoreKeys
import io.ktor.client.HttpClient
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Loads the jeweler's configuration (price overrides, store info, announcement).
 * Falls back to the last cached copy and finally to built-in defaults.
 */
class ConfigRepository(
    private val client: HttpClient,
    private val store: LocalStore,
    /** Absolute URLs of GET /api/config, tried in order. */
    private val endpoints: List<String>,
) {
    private val _config = MutableStateFlow(loadCached() ?: RemoteConfig())
    val config: StateFlow<RemoteConfig> = _config.asStateFlow()

    private var etag: String? = store.getString(StoreKeys.CONFIG_ETAG)

    /** Returns true when the server answered (200 or 304). */
    suspend fun refresh(): Boolean {
        for (url in endpoints) {
            try {
                val response = client.get(url) {
                    timeout { requestTimeoutMillis = 15_000 }
                    etag?.let { header(HttpHeaders.IfNoneMatch, it) }
                    header(HttpHeaders.Accept, "application/json")
                }
                when (response.status) {
                    HttpStatusCode.NotModified -> return true
                    HttpStatusCode.OK -> {
                        val text = response.bodyAsText()
                        val parsed = AppJson.decodeFromString(RemoteConfig.serializer(), text)
                        _config.value = parsed
                        store.putString(StoreKeys.CONFIG_JSON, text)
                        etag = response.headers[HttpHeaders.ETag]
                        store.putString(StoreKeys.CONFIG_ETAG, etag)
                        return true
                    }
                    else -> Unit
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Throwable) {
                // try the next endpoint
            }
        }
        return false
    }

    /** Refreshes periodically; cancelled together with the caller's scope. */
    suspend fun poll(intervalMs: Long = 120_000) {
        while (true) {
            val ok = refresh()
            delay(if (ok) intervalMs else intervalMs / 2)
        }
    }

    private fun loadCached(): RemoteConfig? {
        val text = store.getString(StoreKeys.CONFIG_JSON) ?: return null
        return runCatching { AppJson.decodeFromString(RemoteConfig.serializer(), text) }.getOrNull()
    }
}
