package com.arychagov.w40k.data

import junit.framework.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DamageTest {
    @Test
    fun regular() {
        val attacker = Attacker(
            attacks = SimpleValue(16),
            skill = 4,
            strength = 8.toValue(),
            penetration = none,
            damage = 1.toValue(),
        )
        val defender = Defender(
            hitTranshuman = false,
            woundTranshuman = false,
            toughness = 8.toValue(),
            save = 4.toValue(),
            invulnerableSave = 7.toValue(),
            feelNoPain = 7
        )

        var wounds = 0.0
        repeat(10000) {
            wounds += attackSequence(attacker, defender)
        }

        val mean = wounds / 10000
        assertTrue(mean >= 1.9)
        assertTrue(mean <= 2.1)
    }


    @Test
    fun invulnerableSave() {
        val attacker = Attacker(
            attacks = SimpleValue(16),
            skill = 4,
            strength = 8.toValue(),
            penetration = 1none,
            damage = 1.toValue(),
        )
        val defender = Defender(
            hitTranshuman = false,
            woundTranshuman = false,
            toughness = 8.toValue(),
            save = 6.toValue(),
            invulnerableSave = 4.toValue(),
            feelNoPain = 7
        )

        var wounds = 0.0
        repeat(10000) {
            wounds += attackSequence(attacker, defender)
        }

        val mean = wounds / 10000
        assertTrue(mean >= 1.9)
        assertTrue(mean <= 2.1)
    }

    @Test
    fun feelNoPain() {
        val attacker = Attacker(
            attacks = SimpleValue(16),
            skill = 4,
            strength = 8.toValue(),
            penetration = none,
            damage = 6.toValue(),
        )
        val defender = Defender(
            hitTranshuman = false,
            woundTranshuman = false,
            toughness = 8.toValue(),
            save = 4.toValue(),
            invulnerableSave = 7.toValue(),
            feelNoPain = 4
        )

        var wounds = 0.0
        repeat(10000) {
            wounds += attackSequence(attacker, defender)
        }

        val mean = wounds / 10000
        assertTrue(mean >= 5.9)
        assertTrue(mean <= 6.1)
    }

    @Test
    fun playground() {
        val attacker = Attacker(
            attacks = SimpleValue(20),
            skill = 2,
            rerollRule = RerollRule.Ones,
            autoWoundRule = AutoWoundRule.OnResult(6),
            strength = 1none,
            penetration = 1.toValue(),
            damage = "d6".parseValue(),
        )
        val defender = Defender(
            hitTranshuman = false,
            woundTranshuman = false,
            toughness = 5.toValue(),
            save = 2.toValue(),
            invulnerableSave = 7.toValue(),
            feelNoPain = 7,
        )

        var wounds = 0.0
        repeat(10000) {
            wounds += attackSequence(attacker, defender)
        }

        val mean = wounds / 10000
    }
}
