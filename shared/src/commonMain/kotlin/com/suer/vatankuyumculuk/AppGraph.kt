package com.suer.vatankuyumculuk

import com.suer.vatankuyumculuk.data.Preferences
import com.suer.vatankuyumculuk.data.config.ConfigRepository
import com.suer.vatankuyumculuk.data.createHttpClient
import com.suer.vatankuyumculuk.data.harem.HaremFeed
import com.suer.vatankuyumculuk.data.market.MarketRepository
import com.suer.vatankuyumculuk.data.storage.SettingsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Public URLs of the project. The site is served by GitHub Pages until the shop's own domain
 * is bought; then add the domain in front of [configEndpoints] and point [SITE_URL] at it.
 * Never list a domain here before it is registered: whoever owns it would control /api/config.
 */
object AppLinks {
    /** Public site: privacy policy, support page and the published config. */
    const val SITE_URL = "https://erncanyildirim.github.io/Vatan-Kuyumculuk"

    /** Cloudflare Pages site with the admin panel; its /api/config holds the jeweler's settings. */
    const val ADMIN_SITE_URL = "https://vatankuyumculuk.pages.dev"

    const val PRIVACY_PATH = "/gizlilik/"
    const val SUPPORT_PATH = "/destek/"

    val configEndpoints: List<String> = listOf("$ADMIN_SITE_URL/api/config", "$SITE_URL/api/config")

    fun privacyUrl(base: String = SITE_URL) = base + PRIVACY_PATH
    fun supportUrl(base: String = SITE_URL) = base + SUPPORT_PATH
}

/** Process-wide singletons (survive Android configuration changes). */
object AppGraph {
    val scope: CoroutineScope by lazy { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    private val httpClient by lazy { createHttpClient() }
    private val store by lazy { SettingsStore() }

    val preferences: Preferences by lazy { Preferences(store) }

    val configRepository: ConfigRepository by lazy {
        ConfigRepository(httpClient, store, platformConfigEndpoints())
    }

    val market: MarketRepository by lazy {
        MarketRepository(HaremFeed(httpClient), configRepository, store, scope)
    }
}

/** Web uses the page's own origin first; apps use [AppLinks.configEndpoints]. */
internal expect fun platformConfigEndpoints(): List<String>
