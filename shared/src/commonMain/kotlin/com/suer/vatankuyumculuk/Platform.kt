package com.suer.vatankuyumculuk

import androidx.compose.runtime.Composable
import io.ktor.http.encodeURLParameter

enum class PlatformKind { ANDROID, IOS, WEB }

internal expect val platformKind: PlatformKind

/** Returns a function that opens the system share sheet (or copies the text on web). */
@Composable
internal expect fun rememberShareText(): (String) -> Unit

object ExternalLinks {
    fun phone(number: String) = "tel:" + number.filter { it.isDigit() || it == '+' }

    fun whatsapp(number: String, text: String? = null): String {
        // wa.me needs the full international number: 0505… and 505… both become 90505….
        val digits = number.filter { it.isDigit() }.let {
            when {
                it.startsWith("0") -> "9$it"
                it.length == 10 && it.startsWith("5") -> "90$it"
                else -> it
            }
        }
        return "https://wa.me/$digits" + (text?.let { "?text=" + it.encodeURLParameter() } ?: "")
    }

    fun maps(query: String, lat: Double?, lng: Double?): String {
        val q = query.encodeURLParameter()
        return if (platformKind == PlatformKind.IOS) {
            if (lat != null && lng != null) "https://maps.apple.com/?ll=$lat,$lng&q=$q" else "https://maps.apple.com/?q=$q"
        } else {
            if (lat != null && lng != null) "https://www.google.com/maps/search/?api=1&query=$lat,$lng"
            else "https://www.google.com/maps/search/?api=1&query=$q"
        }
    }

    fun instagram(handle: String): String {
        val h = handle.trim().removePrefix("@").substringAfterLast("instagram.com/").trim('/')
        return "https://www.instagram.com/$h/"
    }
}
