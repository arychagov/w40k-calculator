package com.arychagov.w40k.data

import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ValueParseTest {
    @Test
    fun singleValue() {
        val value = "2".parseValue()
        assertTrue(value is SimpleValue)
        assertEquals(2, value.eval())
    }

    @Test
    fun diceValue() {
        val value = "D6".parseValue()
        assertTrue(value is RandomValue)
    }

    @Test
    fun doubleDiceValue() {
        val value = "2D6".parseValue()
        assertTrue(value is CombinedValue)
        val values = (value as CombinedValue).values
        assertTrue(values[0] is RandomValue)
        assertTrue(values[1] is RandomValue)
    }

    @Test
    fun doubleDiceValuePlusSomething() {
        val value = "2D6 + 2".parseValue()
        assertTrue(value is CombinedValue)
        val values = (value as CombinedValue).values
        assertEquals(2, values.size)
        assertTrue(values[0] is CombinedValue)
        assertTrue(values[1] is SimpleValue)
        assertTrue((values[0] as CombinedValue).values[0] is RandomValue)
        assertTrue((values[0] as CombinedValue).values[1] is RandomValue)
    }

    @Test
    fun randomness() {
        val value = "2D6 + 2".parseValue()
        var sum = 0.0
        repeat(10000) {
            sum += value.eval()
        }
        sum /= 10000
        assertTrue(sum >= 8.9)
        assertTrue(sum <= 9.1)
    }

    @Test
    fun increase() {
        assertEquals("2", "1".increase())
        assertEquals("d6 + 1", "d6".increase())
        assertEquals("2d6 + 2", "2d6 + 1".increase())
    }

    @Test
    fun decrease() {
        assertEquals("1", "1".decrease())
        assertEquals("d6", "d6 + 1".decrease())
        assertEquals("2d6 + 1", "2d6 + 2".decrease())
    }
}
