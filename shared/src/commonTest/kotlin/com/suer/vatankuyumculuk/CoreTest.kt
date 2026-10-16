package com.suer.vatankuyumculuk

import com.suer.vatankuyumculuk.core.TrFormat
import com.suer.vatankuyumculuk.core.TrTime
import com.suer.vatankuyumculuk.data.AppJson
import com.suer.vatankuyumculuk.data.config.ProductOverride
import com.suer.vatankuyumculuk.data.config.RemoteConfig
import com.suer.vatankuyumculuk.data.harem.EioPacket
import com.suer.vatankuyumculuk.data.harem.HaremProtocol
import com.suer.vatankuyumculuk.data.harem.RawQuote
import com.suer.vatankuyumculuk.data.market.Catalog
import com.suer.vatankuyumculuk.data.market.PriceSource
import com.suer.vatankuyumculuk.data.market.PricingEngine
import com.suer.vatankuyumculuk.data.market.Tick
import com.suer.vatankuyumculuk.data.market.TickTracker
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TrFormatTest {
    @Test
    fun formatsTurkishNumbers() {
        assertEquals("6.630,59", TrFormat.number(6630.594, 2))
        assertEquals("10.874", TrFormat.number(10874.0, 0))
        assertEquals("49,1170", TrFormat.number(49.117, 4))
        assertEquals("-1,50", TrFormat.number(-1.5, 2))
        assertEquals("+%1,34", TrFormat.percentSigned(1.3412))
    }

    @Test
    fun parsesUserInput() {
        assertEquals(1.5, TrFormat.parseInput("1,5"))
        assertEquals(1.5, TrFormat.parseInput("1.5"))
        assertEquals(1500.0, TrFormat.parseInput("1.500"))
        assertEquals(10000.25, TrFormat.parseInput("10.000,25"))
        assertNull(TrFormat.parseInput(""))
        assertNull(TrFormat.parseInput("abc"))
    }
}

class TrTimeTest {
    @Test
    fun parsesHaremTimestampsAsIstanbulTime() {
        val t = assertNotNull(TrTime.parseHarem("09-10-2026 12:55:30"))
        assertEquals("09.10.2026 12:55", TrTime.dateTime(t))
        assertEquals("09.10 12:55", TrTime.dayMonthTime(t))
        assertEquals(5, TrTime.parts(t).dayOfWeek) // Friday
        assertNull(TrTime.parseHarem("bozuk"))
    }

    @Test
    fun parsesHourMinute() {
        assertEquals(9 * 60, TrTime.parseHourMinute("09:00"))
        assertNull(TrTime.parseHourMinute("25:00"))
    }
}

class HaremProtocolTest {
    @Test
    fun buildsSocketUrl() {
        assertEquals(
            "wss://hrmsocketonly.haremaltin.com/socket.io/?EIO=4&transport=websocket",
            HaremProtocol.socketUrl("https://hrmsocketonly.haremaltin.com/"),
        )
    }

    @Test
    fun parsesHandshakeAndPriceEvent() {
        val open = HaremProtocol.parse("""0{"sid":"x","pingInterval":25000,"pingTimeout":20000}""")
        assertEquals(EioPacket.Open(25_000, 20_000), open)
        assertEquals(EioPacket.Ping, HaremProtocol.parse("2"))

        val event = HaremProtocol.parse(
            """42["price_changed",{"meta":{"time":1},"data":{"ALTIN":{"code":"ALTIN","alis":"6608.880",""" +
                """"satis":"6636.120","tarih":"09-10-2026 20:06:47","dusuk":"6581.100","yuksek":"6688.020","kapanis":6542.58},""" +
                """"PASIF":{"alis":0,"satis":0}}}]""",
        )
        val e = assertIs<EioPacket.Event>(event)
        assertEquals(HaremProtocol.PRICE_EVENT, e.name)
        val quotes = HaremProtocol.parsePrices(e.payload)
        val altin = assertNotNull(quotes["ALTIN"])
        assertEquals(6608.88, altin.buy)
        assertEquals(6542.58, altin.close)
        assertNull(quotes["PASIF"]?.buy)
    }
}

class PricingEngineTest {
    private val has = Catalog[Catalog.HAS_ALTIN]!!
    private val raw = RawQuote("ALTIN", buy = 6600.0, sell = 6650.0, low = 6500.0, high = 6700.0, close = 6500.0, time = null)

    @Test
    fun liveByDefault() {
        val q = assertNotNull(PricingEngine.price(has, raw, null))
        assertEquals(PriceSource.LIVE, q.source)
        assertEquals(6650.0, q.sell)
        assertEquals((6650.0 - 6500.0) / 6500.0 * 100, q.changePct)
    }

    @Test
    fun adjustsByAmountAndPercent() {
        val amount = PricingEngine.price(has, raw, ProductOverride(mode = "adjust", buyAdj = -38.0, sellAdj = 50.0))!!
        assertEquals(6562.0, amount.buy)
        assertEquals(6700.0, amount.sell)
        assertEquals(PriceSource.ADJUSTED, amount.source)

        val percent = PricingEngine.price(has, raw, ProductOverride(mode = "adjust", sellAdj = 1.0, adjUnit = "percent"))!!
        assertEquals(6716.5, percent.sell!!, 1e-9)
    }

    @Test
    fun fixedAndHidden() {
        val fixed = PricingEngine.price(has, null, ProductOverride(mode = "fixed", sell = 7000.0))!!
        assertEquals(PriceSource.FIXED, fixed.source)
        assertNull(fixed.buy)
        assertEquals(7000.0, fixed.sell)

        assertNull(PricingEngine.price(has, raw, ProductOverride(visible = false)))
        // Fixed without prices falls back to the live price.
        assertEquals(PriceSource.LIVE, PricingEngine.price(has, raw, ProductOverride(mode = "fixed"))!!.source)
    }

    @Test
    fun tickTrackerFlagsDirection() {
        val tracker = TickTracker()
        val first = tracker.track(PricingEngine.price(has, raw, null)!!)
        assertEquals(Tick.NONE, first.sellTick)
        val up = tracker.track(PricingEngine.price(has, raw.copy(sell = 6660.0), null)!!)
        assertEquals(Tick.UP, up.sellTick)
        assertTrue(up.tickSeq > first.tickSeq)
    }
}

class RemoteConfigTest {
    /** The document shape written by the admin panel (cloudflare/functions/_lib/config.js). */
    @Test
    fun parsesAdminDocument() {
        val text = """
            {"schema":1,"version":3,"updatedAt":"2026-10-11T10:00:00Z",
             "feed":{"url":"wss://hrmsocketonly.haremaltin.com","enabled":true},
             "store":{"name":"Vatan Kuyumculuk","city":"Osmaniye","phone":"+905054348592","whatsapp":"+905054348592",
                      "address":"Adres","mapsQuery":"q","lat":null,"lng":null,"hoursText":"Hafta içi",
                      "openingHours":[{"days":[1,2,3,4,5],"open":"09:00","close":"19:00"}],
                      "instagram":"","email":"","website":""},
             "announcement":{"enabled":true,"text":"Duyuru","style":"warning"},
             "products":{"CEYREK_YENI":{"mode":"fixed","visible":true,"buy":null,"sell":10900,"buyAdj":0,"sellAdj":0,"adjUnit":"amount"}},
             "unknownField":1}
        """.trimIndent()
        val config = AppJson.decodeFromString(RemoteConfig.serializer(), text)
        assertEquals(3, config.version)
        assertEquals("warning", config.announcement.style)
        assertEquals(10900.0, config.products["CEYREK_YENI"]?.sell)
        assertEquals(listOf(1, 2, 3, 4, 5), config.store.openingHours.single().days)
    }

    @Test
    fun emptyDocumentUsesDefaults() {
        val config = AppJson.decodeFromString(RemoteConfig.serializer(), """{"schema":1,"version":0}""")
        assertEquals("Vatan Kuyumculuk", config.store.name)
        assertEquals("vatankuyumcu.80", config.store.instagram)
        assertTrue(config.store.openingHours.isEmpty())
        assertTrue(config.feed.enabled)
    }
}
