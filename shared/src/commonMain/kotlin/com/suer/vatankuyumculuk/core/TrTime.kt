package com.suer.vatankuyumculuk.core

import kotlin.time.Clock

/**
 * Time helpers for Europe/Istanbul. Türkiye has used UTC+3 all year round since 2016,
 * so a fixed offset is exact and avoids shipping a timezone database to the web build.
 */
object TrTime {
    const val OFFSET_MS: Long = 3L * 60 * 60 * 1000
    private const val DAY_MS: Long = 24L * 60 * 60 * 1000

    data class Parts(
        val year: Int,
        val month: Int,
        val day: Int,
        val hour: Int,
        val minute: Int,
        val second: Int,
        /** 1 = Monday ... 7 = Sunday */
        val dayOfWeek: Int,
    ) {
        val minuteOfDay: Int get() = hour * 60 + minute
    }

    fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()

    fun parts(epochMillis: Long): Parts {
        val local = epochMillis + OFFSET_MS
        val days = floorDiv(local, DAY_MS)
        val msOfDay = local - days * DAY_MS
        val (y, m, d) = civilFromDays(days)
        val secOfDay = (msOfDay / 1000).toInt()
        // 1970-01-01 was a Thursday (ISO 4)
        val dow = (floorMod(days + 3, 7) + 1).toInt()
        return Parts(y, m, d, secOfDay / 3600, (secOfDay / 60) % 60, secOfDay % 60, dow)
    }

    fun hhmm(epochMillis: Long): String = parts(epochMillis).let { "${two(it.hour)}:${two(it.minute)}" }

    fun hhmmss(epochMillis: Long): String =
        parts(epochMillis).let { "${two(it.hour)}:${two(it.minute)}:${two(it.second)}" }

    /** "11.10 09:15" */
    fun dayMonthTime(epochMillis: Long): String =
        parts(epochMillis).let { "${two(it.day)}.${two(it.month)} ${two(it.hour)}:${two(it.minute)}" }

    fun dateTime(epochMillis: Long): String =
        parts(epochMillis).let { "${two(it.day)}.${two(it.month)}.${it.year} ${two(it.hour)}:${two(it.minute)}" }

    /** Parses Harem's "09-10-2026 12:55:30" (Istanbul local time) into epoch millis. */
    fun parseHarem(text: String?): Long? {
        if (text.isNullOrBlank()) return null
        val m = HAREM_PATTERN.matchEntire(text.trim()) ?: return null
        val (d, mo, y, h, mi, s) = m.destructured
        val day = d.toInt(); val month = mo.toInt(); val year = y.toInt()
        if (month !in 1..12 || day !in 1..31) return null
        val days = daysFromCivil(year, month, day)
        val local = days * DAY_MS + (h.toLong() * 3600 + mi.toLong() * 60 + s.toLong()) * 1000
        return local - OFFSET_MS
    }

    /** Parses "HH:mm" into minute of day. */
    fun parseHourMinute(text: String): Int? {
        val parts = text.trim().split(':', '.')
        if (parts.size != 2) return null
        val h = parts[0].toIntOrNull() ?: return null
        val m = parts[1].toIntOrNull() ?: return null
        if (h !in 0..24 || m !in 0..59) return null
        return h * 60 + m
    }

    private val HAREM_PATTERN = Regex("""(\d{1,2})-(\d{1,2})-(\d{4})\s+(\d{1,2}):(\d{2}):(\d{2})""")

    private fun two(v: Int) = if (v < 10) "0$v" else v.toString()

    private fun floorDiv(a: Long, b: Long): Long {
        val q = a / b
        return if ((a % b != 0L) && ((a < 0) != (b < 0))) q - 1 else q
    }

    private fun floorMod(a: Long, b: Long): Long = a - floorDiv(a, b) * b

    // Howard Hinnant's civil date algorithms
    private fun civilFromDays(z0: Long): Triple<Int, Int, Int> {
        val z = z0 + 719468
        val era = floorDiv(z, 146097)
        val doe = z - era * 146097
        val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
        val y = yoe + era * 400
        val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
        val mp = (5 * doy + 2) / 153
        val d = doy - (153 * mp + 2) / 5 + 1
        val m = if (mp < 10) mp + 3 else mp - 9
        return Triple((if (m <= 2) y + 1 else y).toInt(), m.toInt(), d.toInt())
    }

    private fun daysFromCivil(y0: Int, m: Int, d: Int): Long {
        val y = (if (m <= 2) y0 - 1 else y0).toLong()
        val era = floorDiv(y, 400)
        val yoe = y - era * 400
        val mp = if (m > 2) m - 3 else m + 9
        val doy = (153L * mp + 2) / 5 + d - 1
        val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
        return era * 146097 + doe - 719468
    }
}
