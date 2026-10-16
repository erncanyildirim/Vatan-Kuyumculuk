package com.suer.vatankuyumculuk.core

import kotlin.math.abs
import kotlin.math.roundToLong

/** Turkish number formatting: thousands '.', decimals ','. */
object TrFormat {
    private val POW10 = longArrayOf(1, 10, 100, 1_000, 10_000, 100_000, 1_000_000)

    /** 6630.594 -> "6.630,59" */
    fun number(value: Double, decimals: Int, grouping: Boolean = true): String {
        if (value.isNaN() || value.isInfinite()) return "—"
        val d = decimals.coerceIn(0, POW10.lastIndex)
        val factor = POW10[d]
        val scaled = (abs(value) * factor).roundToLong()
        val intPart = scaled / factor
        val fracPart = scaled % factor
        return buildString {
            if (value < 0 && scaled != 0L) append('-')
            append(if (grouping) group(intPart) else intPart.toString())
            if (d > 0) {
                append(',')
                append(fracPart.toString().padStart(d, '0'))
            }
        }
    }

    /** 1.3412 -> "%1,34" (Turkish style, sign handled by caller via arrows/colors). */
    fun percentAbs(value: Double): String = "%" + number(abs(value), 2)

    /** 1.3412 -> "+%1,34", -0.5 -> "-%0,50" */
    fun percentSigned(value: Double): String {
        val body = percentAbs(value)
        return when {
            value > 0.0049 -> "+$body"
            value < -0.0049 -> "-$body"
            else -> body
        }
    }

    private fun group(n: Long): String {
        val s = n.toString()
        if (s.length <= 3) return s
        val sb = StringBuilder(s.length + s.length / 3)
        val head = s.length % 3
        if (head > 0) sb.append(s, 0, head)
        var i = head
        while (i < s.length) {
            if (sb.isNotEmpty()) sb.append('.')
            sb.append(s, i, i + 3)
            i += 3
        }
        return sb.toString()
    }

    /**
     * Parses user input such as "1,5", "1.5", "1.500" (thousands), "10.000,25".
     * Returns null for empty/invalid input.
     */
    fun parseInput(input: String): Double? {
        var s = input.trim().replace(" ", "").replace("₺", "")
        if (s.isEmpty()) return null
        if (s.contains(',')) {
            s = s.replace(".", "").replace(',', '.')
        } else {
            val dots = s.count { it == '.' }
            if (dots > 1) {
                s = s.replace(".", "")
            } else if (dots == 1) {
                val after = s.substringAfter('.')
                // "1.500" in Turkish means one thousand five hundred
                if (after.length == 3 && s.substringBefore('.').isNotEmpty()) s = s.replace(".", "")
            }
        }
        if (s.count { it == '.' } > 1) return null
        return s.toDoubleOrNull()?.takeIf { !it.isNaN() && !it.isInfinite() && it >= 0 }
    }
}
