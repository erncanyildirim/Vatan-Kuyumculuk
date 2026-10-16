package com.suer.vatankuyumculuk.data.config

import kotlinx.serialization.Serializable

/**
 * Configuration published by the admin panel at GET /api/config.
 * The contract is documented in docs/api-contract.md; every field has a safe default so
 * the apps keep working with an empty or partial document.
 */
@Serializable
data class RemoteConfig(
    val schema: Int = 1,
    val version: Long = 0,
    val updatedAt: String? = null,
    val feed: FeedConfig = FeedConfig(),
    val store: StoreInfo = StoreInfo(),
    val announcement: Announcement = Announcement(),
    val products: Map<String, ProductOverride> = emptyMap(),
)

@Serializable
data class FeedConfig(
    val url: String = DEFAULT_FEED_URL,
    val enabled: Boolean = true,
) {
    companion object {
        const val DEFAULT_FEED_URL = "wss://hrmsocketonly.haremaltin.com"
    }
}

@Serializable
data class StoreInfo(
    val name: String = "Vatan Kuyumculuk",
    val city: String = "Osmaniye",
    val phone: String = "+905054348592",
    val whatsapp: String = "+905054348592",
    /** Street address; the row and the directions button stay hidden while it is blank. */
    val address: String = "",
    /** Free-text search used for map links when lat/lng are not set. */
    val mapsQuery: String = "Vatan Kuyumculuk Osmaniye",
    val lat: Double? = null,
    val lng: Double? = null,
    val hoursText: String = "",
    val openingHours: List<OpeningHours> = emptyList(),
    val instagram: String = "vatankuyumcu.80",
    val email: String = "",
    val website: String = "",
)

/** [days]: 1 = Monday … 7 = Sunday. Times are "HH:mm" in Istanbul time. */
@Serializable
data class OpeningHours(
    val days: List<Int> = emptyList(),
    val open: String = "09:00",
    val close: String = "19:00",
)

@Serializable
data class Announcement(
    val enabled: Boolean = false,
    val text: String = "",
    /** "info" | "warning" */
    val style: String = "info",
)

@Serializable
data class ProductOverride(
    /** "live" (Harem price), "adjust" (Harem price + adjustment) or "fixed" (manual price). */
    val mode: String = MODE_LIVE,
    val visible: Boolean = true,
    /** Manual prices for [MODE_FIXED]. */
    val buy: Double? = null,
    val sell: Double? = null,
    /** Adjustments for [MODE_ADJUST]; may be negative. */
    val buyAdj: Double = 0.0,
    val sellAdj: Double = 0.0,
    /** "amount" (TL added) or "percent". */
    val adjUnit: String = UNIT_AMOUNT,
) {
    companion object {
        const val MODE_LIVE = "live"
        const val MODE_ADJUST = "adjust"
        const val MODE_FIXED = "fixed"
        const val UNIT_AMOUNT = "amount"
        const val UNIT_PERCENT = "percent"
    }
}
