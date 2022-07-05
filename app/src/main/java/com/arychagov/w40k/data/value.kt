package com.arychagov.w40k.data

import androidx.core.text.isDigitsOnly
import kotlin.random.Random

val d6 = RandomValue(to = 6)
val none = SimpleValue(0)

interface Value {
    fun eval(): Int
}

data class SimpleValue(
    internal val value: Int,
) : Value {
    override fun eval(): Int {
        return value
    }
}

data class RandomValue(
    internal val from: Int = 1,
    internal val to: Int = 6,
) : Value {
    override fun eval(): Int {
        return Random.nextInt(from, to + 1)
    }
}

data class CombinedValue(
    internal val values: List<Value>,
) : Value {
    override fun eval(): Int {
        return values.fold(0) { acc: Int, i: Value -> acc + i.eval() }
    }
}

fun Int.toValue(): Value {
    return SimpleValue(this)
}

fun Value.add(another: Value): Value {
    return CombinedValue(listOf(this, another))
}

@Throws(IllegalArgumentException::class)
fun String.parseValue(): Value {
    val trimmed = this.trim().lowercase()
    if (trimmed.isDigitsOnly()) {
        return SimpleValue(trimmed.toInt())
    }
    // Could be something like (2d6 + 1)
    if (trimmed.contains('+')) {
        val split = trimmed.split('+')
        return CombinedValue(
            split.map { it.parseValue() }
        )
    } else if (trimmed.contains('d')) {
        val split = trimmed.split('d')
        if (split.size != 2) throw IllegalArgumentException("Cannot parse value $this")
        val max = split[1].toInt()
        if (split[0].isEmpty()) {
            return RandomValue(to = max)
        } else {
            val dicesCount = split[0].toInt()
            val dices = mutableListOf<RandomValue>()
            repeat(dicesCount) {
                dices.add(RandomValue(to = max))
            }
            return CombinedValue(dices)
        }
    }

    throw IllegalArgumentException("Cannot parse value $this")
}

fun String.decrease(belowOne: Boolean = false): String {
    val trimmed = this.trim().lowercase()
    return when {
        trimmed.isDigitsOnly() -> {
            val intValue = trimmed.toInt()
            if (intValue < 2) {
                if (belowOne) "0" else "1"
            } else {
                (intValue - 1).toString()
            }
        }
        trimmed.contains('+') -> {
            val addition = trimmed.split('+')[1].trim()
            if (addition == "1") {
                trimmed.split('+')[0].trim()
            } else {
                trimmed.split('+')[0].trim() + " + " + addition.decrease()
            }
        }
        else -> {
            this
        }
    }
}

fun String.increase(): String {
    val trimmed = this.trim().lowercase()
    return when {
        trimmed.isDigitsOnly() -> {
            val intValue = trimmed.toInt()
            (intValue + 1).toString()
        }
        trimmed.contains('+') -> {
            val addition = trimmed.split('+')[1].trim()
            trimmed.split('+')[0].trim() + " + " + addition.increase()
        }
        else -> {
            "${this.trim()} + 1"
        }
    }
}

fun Double.format(digits: Int = 2) = "%.${digits}f".format(this)
fun Float.format(digits: Int = 2) = "%.${digits}f".format(this)
