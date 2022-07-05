package com.arychagov.w40k.data

import java.lang.Integer.min

interface RerollRule {
    fun canReroll(result: Int): Boolean

    object No : RerollRule {
        override fun canReroll(result: Int): Boolean {
            return false
        }
    }

    object Full : RerollRule {
        override fun canReroll(result: Int): Boolean {
            return true
        }
    }

    object Ones : RerollRule {
        override fun canReroll(result: Int): Boolean {
            return result == 1
        }
    }

    object Single : RerollRule {
        private var rerolled = false

        override fun canReroll(result: Int): Boolean {
            if (rerolled) return false
            rerolled = true
            return true
        }
    }
}

interface DamageOnRollRule {
    fun getMortalWounds(result: Int): Value

    object No : DamageOnRollRule {
        override fun getMortalWounds(result: Int): Value {
            return none
        }
    }

    class OnHit(
        private val onResult: Int = 6,
        private val mortalWounds: Value,
    ) : DamageOnRollRule {
        override fun getMortalWounds(result: Int): Value {
            return if (result >= onResult) {
                mortalWounds
            } else {
                none
            }
        }

    }
}

interface AutoWoundRule {
    fun isAutoWound(result: Int): Boolean

    object No : AutoWoundRule {
        override fun isAutoWound(result: Int): Boolean {
            return false
        }
    }

    class OnResult(
        private val onResult: Int = 6,
    ) : AutoWoundRule {
        override fun isAutoWound(result: Int): Boolean {
            return result >= onResult
        }
    }
}


interface AutoHitRule {
    fun isAutoHit(): Boolean

    object No : AutoHitRule {
        override fun isAutoHit(): Boolean {
            return false
        }
    }

    object Always : AutoHitRule {
        override fun isAutoHit(): Boolean {
            return true
        }
    }
}


interface AdditionalHitRule {
    fun getAdditionalHits(result: Int): Value

    object No : AdditionalHitRule {
        override fun getAdditionalHits(result: Int): Value {
            return none
        }
    }

    class OnResult(
        private val onResult: Int = 6,
        private val additionalHits: Value,
    ) : AdditionalHitRule {
        override fun getAdditionalHits(result: Int): Value {
            return if (result >= onResult) {
                additionalHits
            } else {
                none
            }
        }
    }
}

interface PenetrationRule {
    fun getPenetrationIncrease(result: Int, penetration: Value): Value

    object No : PenetrationRule {
        override fun getPenetrationIncrease(result: Int, penetration: Value): Value {
            return penetration
        }
    }

    class AdditionalOnResult(
        private val onResult: Int = 6,
        private val additionalPenetration: Value,
    ) : PenetrationRule {
        override fun getPenetrationIncrease(result: Int, penetration: Value): Value {
            return if (result >= onResult) {
                penetration.add(additionalPenetration)
            } else {
                penetration
            }
        }
    }

    class OnResult(
        private val onResult: Int = 6,
        private val newPenetration: Value,
    ) : PenetrationRule {
        override fun getPenetrationIncrease(result: Int, penetration: Value): Value {
            return if (result >= onResult) {
                newPenetration
            } else {
                penetration
            }
        }
    }
}

interface DamageCharacteristicRule {
    fun getResultDamage(result: Int, damageCharacteristic: Value): Value

    object No : DamageCharacteristicRule {
        override fun getResultDamage(result: Int, damageCharacteristic: Value): Value {
            return damageCharacteristic
        }
    }

    class AdditionalOnResult(
        private val onResult: Int = 6,
        private val additionalDamage: Value,
    ) : DamageCharacteristicRule {
        override fun getResultDamage(result: Int, damageCharacteristic: Value): Value {
            return if (onResult >= result) {
                damageCharacteristic.add(additionalDamage)
            } else {
                damageCharacteristic
            }
        }
    }

    class OnResult(
        private val onResult: Int = 6,
        private val damage: Value,
    ) : DamageCharacteristicRule {
        override fun getResultDamage(result: Int, damageCharacteristic: Value): Value {
            return if (onResult >= result) {
                damage
            } else {
                damageCharacteristic
            }
        }
    }
}

interface StrengthHitRule {
    fun getResultStrength(result: Int, strength: Value): Value

    object No : StrengthHitRule {
        override fun getResultStrength(result: Int, strength: Value): Value {
            return strength
        }
    }

    class AdditionalOnResult(
        private val onResult: Int = 6,
        private val additionalStrength: Value,
    ) : StrengthHitRule {
        override fun getResultStrength(result: Int, strength: Value): Value {
            return if (onResult >= result) {
                strength.add(additionalStrength)
            } else {
                strength
            }
        }
    }

    class OnResult(
        private val onResult: Int = 6,
        private val newStrength: Value,
    ) : StrengthHitRule {
        override fun getResultStrength(result: Int, strength: Value): Value {
            return if (onResult >= result) {
                newStrength
            } else {
                strength
            }
        }
    }
}

data class Hit(
    internal val strength: Value,
    internal val penetration: Value,
    internal val damage: Value,
)

data class AttackResult(
    val mortalWounds: List<Value> = emptyList(),
    val hits: List<Hit> = emptyList(),
    val autoWound: List<Hit> = emptyList(),
)


data class WoundResult(
    val mortalWounds: List<Value> = emptyList(),
    val hits: List<Hit> = emptyList(),
)

data class Defender(
    val hitTranshuman: Boolean = false,
    val woundTranshuman: Boolean = false,
    val toughness: Value = none,
    val save: Value = none,
    val invulnerableSave: Value = 7.toValue(),
    val feelNoPain: Int = 7,
    val damageDecrease: Value = none
) {
    fun doSave(woundResult: WoundResult): Int {
        val wounds = mutableListOf<Value>()
        wounds += woundResult.mortalWounds
        val saveCharacteristic = save.eval()
        for (hit in woundResult.hits) {
            val rollToSave = min(
                saveCharacteristic + hit.penetration.eval(),
                invulnerableSave.eval()
            )
            val saveRoll = d6.eval()
            if (saveRoll == 1 || saveRoll < rollToSave) {
                wounds += hit.damage
            }
        }

        val totalWounds = wounds.fold(0) { acc, value ->
            acc + (value.eval() - damageDecrease.eval()).coerceAtLeast(1)
        }
        return if (feelNoPain > 6) {
            totalWounds
        } else {
            var resultWounds = 0
            repeat(totalWounds) {
                val fnpRoll = d6.eval()
                if (fnpRoll < feelNoPain) {
                    resultWounds += 1
                }
            }
            resultWounds
        }
    }
}

data class Attacker(
    val attacks: Value = none,
    val skill: Int = 4,
    val strength: Value = none,
    val penetration: Value = none,
    val damage: Value = none,

    val additionalHitRule: AdditionalHitRule = AdditionalHitRule.No,
    val autoHitRule: AutoHitRule = AutoHitRule.No,
    val rerollRule: RerollRule = RerollRule.No,
    val plusOneToHit: Boolean = false,
    val damageCharacteristicHitRule: DamageCharacteristicRule = DamageCharacteristicRule.No,
    val damageHitRule: DamageOnRollRule = DamageOnRollRule.No,
    val autoWoundRule: AutoWoundRule = AutoWoundRule.No,
    val penetrationHitRule: PenetrationRule = PenetrationRule.No,
    val strengthHitRule: StrengthHitRule = StrengthHitRule.No,

    val woundRerollRule: RerollRule = RerollRule.No,
    val plusOneToWound: Boolean = false,
    val damageCharacteristicWoundRule: DamageCharacteristicRule = DamageCharacteristicRule.No,
    val damageWoundRule: DamageOnRollRule = DamageOnRollRule.No,
    val penetrationWoundRule: PenetrationRule = PenetrationRule.No,
) {
    fun doHit(defender: Defender): AttackResult {
        var numberOfAttacks = attacks.eval()
        val hits = mutableListOf<Hit>()
        val autoWounds = mutableListOf<Hit>()
        val wounds = mutableListOf<Value>()
        val failedRolls = mutableListOf<Int>()
        var firstRoll = true
        do {
            repeat(numberOfAttacks) {
                if (autoHitRule.isAutoHit()) {
                    hits += Hit(
                        strength, penetration, damage
                    )
                } else {
                    val result = d6.eval()
                    if (defender.hitTranshuman && result <= 4) {
                        failedRolls += result
                        return@repeat
                    }
                    val additionalToHit = if (plusOneToHit) 1 else 0
                    if (result == 1 || (result != 6 && result + additionalToHit < skill)) {
                        failedRolls += result
                        return@repeat
                    }

                    val damageCharacteristic = damageCharacteristicHitRule.getResultDamage(
                        result, damage
                    )
                    val strengthCharacteristic = strengthHitRule.getResultStrength(
                        result, strength
                    )
                    val penetrationCharacteristic = penetrationHitRule.getPenetrationIncrease(
                        result, penetration
                    )
                    val additionalHits = additionalHitRule.getAdditionalHits(result)
                    wounds += damageHitRule.getMortalWounds(result)
                    if (autoWoundRule.isAutoWound(result)) {
                        autoWounds += Hit(
                            strengthCharacteristic,
                            penetrationCharacteristic,
                            damageCharacteristic
                        )
                        return@repeat
                    }

                    val resultHits = additionalHits.eval() + 1
                    repeat(resultHits) {
                        hits += Hit(
                            strengthCharacteristic,
                            penetrationCharacteristic,
                            damageCharacteristic
                        )
                    }
                }
            }

            numberOfAttacks = 0
            if (firstRoll) {
                for (failedRoll in failedRolls) {
                    if (rerollRule.canReroll(failedRoll)) {
                        numberOfAttacks += 1
                    }
                }
                firstRoll = false
            }
        } while (numberOfAttacks > 0)

        return AttackResult(
            mortalWounds = wounds.filter { it != none },
            hits = hits,
            autoWound = autoWounds,
        )
    }

    fun doWound(attackResult: AttackResult, defender: Defender): WoundResult {
        val mortalWounds = mutableListOf<Value>()
        mortalWounds += attackResult.mortalWounds
        val hits = mutableListOf<Hit>()
        hits += attackResult.autoWound

        val toughness = defender.toughness.eval()
        attackResult.hits.forEach { hit ->
            val result = d6.eval()
            val strength = hit.strength.eval()
            val pass = passValue(toughness, strength)
            val additionalToWound = if (plusOneToWound) 1 else 0
            if (result != 1 && (result == 6 || result + additionalToWound >= pass)) {
                val penetration = penetrationWoundRule.getPenetrationIncrease(
                    result, hit.penetration
                )
                val damage = damageCharacteristicWoundRule.getResultDamage(result, hit.damage)
                mortalWounds += damageWoundRule.getMortalWounds(result)
                hits += Hit(hit.strength, penetration, damage)
            } else {
                if (woundRerollRule.canReroll(result)) {
                    val newResult = d6.eval()
                    if (newResult >= pass) {
                        val penetration = penetrationWoundRule.getPenetrationIncrease(
                            newResult, hit.penetration
                        )
                        val damage = damageCharacteristicWoundRule.getResultDamage(
                            newResult, hit.damage
                        )
                        mortalWounds += damageWoundRule.getMortalWounds(newResult)
                        hits += Hit(hit.strength, penetration, damage)
                    }
                }
            }
        }
        return WoundResult(mortalWounds.filter { it != none }, hits)
    }

    private fun passValue(toughness: Int, strength: Int) = when {
        toughness * 2 <= strength -> 2
        toughness < strength -> 3
        toughness == strength -> 4
        toughness >= strength * 2 -> 6
        else -> 5
    }
}

fun attackSequence(attacker: Attacker, defender: Defender): Int {
    val attackResult = attacker.doHit(defender)
    val woundResult = attacker.doWound(attackResult, defender)
    return defender.doSave(woundResult)
}
