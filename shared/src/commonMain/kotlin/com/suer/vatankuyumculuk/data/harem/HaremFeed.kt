package com.suer.vatankuyumculuk.data.harem

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.withTimeoutOrNull

class FeedClosedException(message: String) : Exception(message)

/**
 * Live price feed over Harem Altın's Socket.IO endpoint.
 *
 * Each collection opens one websocket. The first emission is the full snapshot, later
 * emissions are deltas (only changed instruments). The flow throws when the connection
 * drops or goes silent, so callers can reconnect with backoff.
 */
class HaremFeed(private val client: HttpClient) {

    private companion object {
        const val CONNECT_TIMEOUT_MS = 20_000L
    }

    fun prices(baseUrl: String): Flow<Map<String, RawQuote>> = channelFlow {
        val url = HaremProtocol.socketUrl(baseUrl)
        val opened = CompletableDeferred<Unit>()
        launch {
            delay(CONNECT_TIMEOUT_MS)
            if (!opened.isCompleted) throw FeedClosedException("Bağlantı zaman aşımına uğradı")
        }
        client.webSocket(urlString = url) {
            opened.complete(Unit)
            // Until the open packet arrives, use the documented defaults.
            var silenceLimit = HaremProtocol.DEFAULT_PING_INTERVAL_MS + HaremProtocol.DEFAULT_PING_TIMEOUT_MS
            while (true) {
                val frame = try {
                    withTimeoutOrNull(silenceLimit) { incoming.receive() }
                } catch (e: ClosedReceiveChannelException) {
                    throw FeedClosedException("Bağlantı kapandı")
                } ?: throw FeedClosedException("Sunucudan yanıt gelmedi")

                if (frame !is Frame.Text) continue
                when (val packet = HaremProtocol.parse(frame.readText())) {
                    is EioPacket.Open -> {
                        silenceLimit = packet.pingIntervalMs + packet.pingTimeoutMs + 5_000
                        send(Frame.Text("40"))
                    }
                    EioPacket.Ping -> send(Frame.Text("3"))
                    is EioPacket.Event -> if (packet.name == HaremProtocol.PRICE_EVENT) {
                        val quotes = HaremProtocol.parsePrices(packet.payload)
                        if (quotes.isNotEmpty()) this@channelFlow.send(quotes)
                    }
                    EioPacket.Close, EioPacket.SioDisconnect -> throw FeedClosedException("Sunucu bağlantıyı kapattı")
                    is EioPacket.SioConnectError -> throw FeedClosedException("Bağlantı reddedildi")
                    else -> Unit
                }
            }
        }
    }
}
