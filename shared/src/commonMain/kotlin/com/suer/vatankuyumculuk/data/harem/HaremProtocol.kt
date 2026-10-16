package com.suer.vatankuyumculuk.data.harem

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

/** One instrument as published by Harem Altın. Prices are null when missing or zero. */
data class RawQuote(
    val code: String,
    val buy: Double?,
    val sell: Double?,
    val low: Double?,
    val high: Double?,
    val close: Double?,
    /** Harem's local timestamp, e.g. "09-10-2026 12:55:30". */
    val time: String?,
)

/** Minimal Engine.IO v4 / Socket.IO v5 packet model (only what the price feed uses). */
sealed interface EioPacket {
    data class Open(val pingIntervalMs: Long, val pingTimeoutMs: Long) : EioPacket
    data object Ping : EioPacket
    data object Pong : EioPacket
    data object Close : EioPacket
    data object SioConnected : EioPacket
    data object SioDisconnect : EioPacket
    data class SioConnectError(val message: String) : EioPacket
    data class Event(val name: String, val payload: JsonElement?) : EioPacket
    data class Other(val raw: String) : EioPacket
}

object HaremProtocol {
    const val PRICE_EVENT = "price_changed"
    const val DEFAULT_PING_INTERVAL_MS = 25_000L
    const val DEFAULT_PING_TIMEOUT_MS = 20_000L

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    /** Builds the websocket URL from a base such as "wss://hrmsocketonly.haremaltin.com". */
    fun socketUrl(base: String): String {
        val trimmed = base.trim().trimEnd('/')
        if (trimmed.contains("/socket.io/")) return trimmed
        val withScheme = when {
            trimmed.startsWith("wss://") || trimmed.startsWith("ws://") -> trimmed
            trimmed.startsWith("https://") -> "wss://" + trimmed.removePrefix("https://")
            trimmed.startsWith("http://") -> "ws://" + trimmed.removePrefix("http://")
            else -> "wss://$trimmed"
        }
        return "$withScheme/socket.io/?EIO=4&transport=websocket"
    }

    fun parse(text: String): EioPacket {
        if (text.isEmpty()) return EioPacket.Other(text)
        return when (text[0]) {
            '0' -> parseOpen(text.substring(1))
            '1' -> EioPacket.Close
            '2' -> EioPacket.Ping
            '3' -> EioPacket.Pong
            '4' -> parseSocketIo(text.substring(1))
            else -> EioPacket.Other(text)
        }
    }

    private fun parseOpen(body: String): EioPacket {
        val obj = runCatching { json.parseToJsonElement(body).jsonObject }.getOrNull()
        val interval = obj?.get("pingInterval")?.jsonPrimitive?.longOrNull ?: DEFAULT_PING_INTERVAL_MS
        val timeout = obj?.get("pingTimeout")?.jsonPrimitive?.longOrNull ?: DEFAULT_PING_TIMEOUT_MS
        return EioPacket.Open(interval, timeout)
    }

    private fun parseSocketIo(body: String): EioPacket {
        if (body.isEmpty()) return EioPacket.Other("4")
        return when (body[0]) {
            '0' -> EioPacket.SioConnected
            '1' -> EioPacket.SioDisconnect
            '2' -> parseEvent(body.substring(1))
            '4' -> EioPacket.SioConnectError(body.substring(1))
            else -> EioPacket.Other("4$body")
        }
    }

    private fun parseEvent(body0: String): EioPacket {
        // Optional namespace ("/ns,") and ack id digits may precede the JSON array.
        var body = body0
        if (body.startsWith("/")) body = body.substringAfter(',', "")
        body = body.dropWhile { it.isDigit() }
        val arr = runCatching { json.parseToJsonElement(body) as? JsonArray }.getOrNull()
            ?: return EioPacket.Other("42$body0")
        val name = (arr.getOrNull(0) as? JsonPrimitive)?.contentOrNull ?: return EioPacket.Other("42$body0")
        return EioPacket.Event(name, arr.getOrNull(1))
    }

    /** Extracts quotes from a price_changed payload: {"meta":{...},"data":{"CODE":{...}}}. */
    fun parsePrices(payload: JsonElement?): Map<String, RawQuote> {
        val data = (payload as? JsonObject)?.get("data") as? JsonObject ?: return emptyMap()
        val out = LinkedHashMap<String, RawQuote>(data.size)
        for ((key, value) in data) {
            val o = value as? JsonObject ?: continue
            val code = (o["code"] as? JsonPrimitive)?.contentOrNull?.takeIf { it.isNotBlank() } ?: key
            out[code] = RawQuote(
                code = code,
                buy = o.price("alis"),
                sell = o.price("satis"),
                low = o.price("dusuk"),
                high = o.price("yuksek"),
                close = o.price("kapanis"),
                time = (o["tarih"] as? JsonPrimitive)?.contentOrNull?.takeIf { it.isNotBlank() },
            )
        }
        return out
    }

    private fun JsonObject.price(key: String): Double? {
        val p = this[key] as? JsonPrimitive ?: return null
        val v = p.contentOrNull?.trim()?.replace(',', '.')?.toDoubleOrNull() ?: return null
        return v.takeIf { it > 0.0 && !it.isNaN() && !it.isInfinite() }
    }
}
