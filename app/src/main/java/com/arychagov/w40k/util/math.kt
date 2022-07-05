// math is hard
package com.arychagov.w40k.util

import kotlin.math.ceil

fun percentiles(values: List<Int>): Percentiles {
    return Percentiles(
        p50 = percentile(values, 50),
        p95 = percentile(values, 95),
    )
}

fun percentile(values: List<Int>, percentile: Int): Int {
    val sorted = values.sortedDescending()
    val index = ceil(percentile / 100.0 * sorted.size).toInt()
    return sorted[index - 1]
}

data class Percentiles(
    val p50: Int,
    val p95: Int,
)