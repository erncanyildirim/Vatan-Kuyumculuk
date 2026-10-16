package com.suer.vatankuyumculuk.data.market

import com.suer.vatankuyumculuk.core.TrTime
import com.suer.vatankuyumculuk.data.config.ProductOverride
import com.suer.vatankuyumculuk.data.harem.RawQuote

enum class PriceSource { LIVE, ADJUSTED, FIXED }

enum class Tick { NONE, UP, DOWN }

/** A price ready for display, after the jeweler's overrides were applied. */
data class PricedQuote(
    val product: Product,
    val buy: Double?,
    val sell: Double?,
    /** Daily change in percent, based on the previous close. Null for fixed prices. */
    val changePct: Double?,
    val low: Double?,
    val high: Double?,
    val close: Double?,
    val source: PriceSource,
    /** Harem's quote time, null for fixed prices. */
    val updatedAtMillis: Long?,
    val buyTick: Tick = Tick.NONE,
    val sellTick: Tick = Tick.NONE,
    /** Increments on every price change; drives the flash animation. */
    val tickSeq: Long = 0,
) {
    val code: String get() = product.code
}

object PricingEngine {

    fun price(product: Product, raw: RawQuote?, override: ProductOverride?): PricedQuote? {
        val o = override ?: ProductOverride()
        if (!o.visible) return null

        if (o.mode == ProductOverride.MODE_FIXED) {
            val buy = o.buy?.takeIf { it > 0 }
            val sell = o.sell?.takeIf { it > 0 }
            if (buy != null || sell != null) {
                return PricedQuote(
                    product = product,
                    buy = buy,
                    sell = sell,
                    changePct = null,
                    low = null,
                    high = null,
                    close = null,
                    source = PriceSource.FIXED,
                    updatedAtMillis = null,
                )
            }
            // A fixed mode without prices falls back to the live price.
        }

        if (raw == null || (raw.buy == null && raw.sell == null)) return null
        val time = TrTime.parseHarem(raw.time)

        if (o.mode == ProductOverride.MODE_ADJUST && (o.buyAdj != 0.0 || o.sellAdj != 0.0)) {
            val percent = o.adjUnit == ProductOverride.UNIT_PERCENT
            val sell = adjust(raw.sell, o.sellAdj, percent)
            val close = adjust(raw.close, o.sellAdj, percent)
            return PricedQuote(
                product = product,
                buy = adjust(raw.buy, o.buyAdj, percent),
                sell = sell,
                changePct = changePct(sell, close),
                low = adjust(raw.low, o.sellAdj, percent),
                high = adjust(raw.high, o.sellAdj, percent),
                close = close,
                source = PriceSource.ADJUSTED,
                updatedAtMillis = time,
            )
        }

        return PricedQuote(
            product = product,
            buy = raw.buy,
            sell = raw.sell,
            changePct = changePct(raw.sell, raw.close),
            low = raw.low,
            high = raw.high,
            close = raw.close,
            source = PriceSource.LIVE,
            updatedAtMillis = time,
        )
    }

    fun adjust(value: Double?, adj: Double, percent: Boolean): Double? {
        value ?: return null
        val out = if (percent) value * (1 + adj / 100.0) else value + adj
        return out.takeIf { it > 0 }
    }

    fun changePct(current: Double?, close: Double?): Double? {
        if (current == null || close == null || close <= 0) return null
        return (current - close) / close * 100.0
    }
}

/** Remembers the previous displayed prices to derive up/down ticks for animations. */
class TickTracker {
    private data class Entry(val buy: Double?, val sell: Double?, val buyTick: Tick, val sellTick: Tick, val seq: Long)

    private val entries = HashMap<String, Entry>()

    fun track(q: PricedQuote): PricedQuote {
        val prev = entries[q.code]
        if (prev == null) {
            entries[q.code] = Entry(q.buy, q.sell, Tick.NONE, Tick.NONE, 0)
            return q
        }
        val bt = direction(prev.buy, q.buy)
        val st = direction(prev.sell, q.sell)
        val entry = if (bt != Tick.NONE || st != Tick.NONE) {
            Entry(q.buy, q.sell, if (bt != Tick.NONE) bt else prev.buyTick, if (st != Tick.NONE) st else prev.sellTick, prev.seq + 1)
        } else {
            prev
        }
        entries[q.code] = entry
        return q.copy(buyTick = entry.buyTick, sellTick = entry.sellTick, tickSeq = entry.seq)
    }

    private fun direction(old: Double?, new: Double?): Tick = when {
        old == null || new == null -> Tick.NONE
        new > old -> Tick.UP
        new < old -> Tick.DOWN
        else -> Tick.NONE
    }
}
