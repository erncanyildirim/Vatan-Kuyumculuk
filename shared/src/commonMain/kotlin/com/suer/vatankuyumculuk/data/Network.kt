package com.suer.vatankuyumculuk.data

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.websocket.WebSockets
import kotlinx.serialization.json.Json

val AppJson: Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
    explicitNulls = false
    encodeDefaults = true
}

/** Creates a client with the platform engine (OkHttp, Darwin or browser fetch/WebSocket). */
internal expect fun platformHttpClient(block: HttpClientConfig<*>.() -> Unit): HttpClient

fun createHttpClient(): HttpClient = platformHttpClient {
    install(WebSockets)
    // Timeouts are set per request: a global request timeout would also cut long-lived websockets.
    install(HttpTimeout)
    expectSuccess = false
}
