package com.suer.vatankuyumculuk

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.browser.window

internal actual val platformKind: PlatformKind = PlatformKind.WEB

private fun webShare(text: String): Boolean =
    js("(navigator.share ? (navigator.share({ text: text }).catch(function () {}), true) : (navigator.clipboard ? (navigator.clipboard.writeText(text), false) : false))")

@Composable
internal actual fun rememberShareText(): (String) -> Unit = remember {
    { text: String -> webShare(text); Unit }
}

internal actual fun platformConfigEndpoints(): List<String> {
    // The site serves its own /api/config; the public endpoints are the fallback.
    val origin = runCatching { window.location.origin }.getOrNull().orEmpty()
    return if (origin.startsWith("http")) (listOf("$origin/api/config") + AppLinks.configEndpoints).distinct()
    else AppLinks.configEndpoints
}
