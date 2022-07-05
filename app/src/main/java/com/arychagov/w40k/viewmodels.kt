package com.arychagov.w40k

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arychagov.w40k.data.*
import com.arychagov.w40k.util.percentiles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class RerollRules {
    NO,
    ONES,
    SINGLE,
    ALL,
}

enum class ModificationRule {
    NO,
    ADD,
    REPLACE,
}

class W40KViewModel : ViewModel() {
    private var attacker = Attacker()
    private var defender = Defender()
    private var currentJob: Job? = null

    val attackerVM = AttackerViewModel { attacker ->
        this.attacker = attacker
        doUpdate()
    }
    val defenderVM = DefenderViewModel { defender ->
        this.defender = defender
        doUpdate()
    }

    val resultDamage = mutableStateOf("")

    // Let's pretend I do care.
    private fun doUpdate() {
        currentJob?.cancel()
        currentJob = viewModelScope.launch(Dispatchers.Default) {
            fun analyzeDamage(wounds: List<Int>): String {
                val percentiles = percentiles(wounds)
                val total = wounds.fold(0) { acc, i -> acc + i }
                val mean = total / wounds.size.toDouble()
                return "mean=${mean.format()}, p50 = ${percentiles.p50}"
            }

            val wounds = mutableListOf<Int>()
            repeat(10000) {
                if (!isActive) {
                    return@launch
                }
                val attackResult = attacker.doHit(defender)
                val woundResult = attacker.doWound(attackResult, defender)
                wounds += defender.doSave(woundResult)
            }
            resultDamage.value = analyzeDamage(wounds)
        }
    }
}

class AttackerViewModel(private val onUpdate: (Attacker) -> Unit) : ViewModel() {
    private var attacker: Attacker = Attacker()

    val attacks = mutableStateOf("1")
    val skill = mutableStateOf("3")
    val strength = mutableStateOf("4")
    val penetration = mutableStateOf("0")
    val damage = mutableStateOf("1")

    val additionalHitRule = mutableStateOf(false)
    val additionalHitOn = mutableStateOf("6")
    val additionalHits = mutableStateOf("1")

    val autoHit = mutableStateOf(false)

    val hRerollRule = mutableStateOf(RerollRules.NO)
    val plusOneToH = mutableStateOf(false)

    val damageOnHRule = mutableStateOf(ModificationRule.NO)
    val damageOnHOn = mutableStateOf("6")
    val damageOnH = mutableStateOf("1")

    val mortalsOnHRule = mutableStateOf(false)
    val mortalsOnHOn = mutableStateOf("6")
    val mortalsOnH = mutableStateOf("1")

    val hasAutoWoundRule = mutableStateOf(false)
    val autoWoundOn = mutableStateOf("6")

    val apOnHitRule = mutableStateOf(ModificationRule.NO)
    val apOnHitOn = mutableStateOf("6")
    val apOnHit = mutableStateOf("1")

    val strengthOnHRule = mutableStateOf(ModificationRule.NO)
    val strengthOnHOn = mutableStateOf("6")
    val strengthOnH = mutableStateOf("1")

    val wRerollRule = mutableStateOf(RerollRules.NO)
    val plusOneToW = mutableStateOf(false)

    val damageOnWRule = mutableStateOf(ModificationRule.NO)
    val damageOnWOn = mutableStateOf("6")
    val damageOnW = mutableStateOf("1")

    val mortalsOnWRule = mutableStateOf(false)
    val mortalsOnWOn = mutableStateOf("6")
    val mortalsOnW = mutableStateOf("1")

    val apOnWRule = mutableStateOf(ModificationRule.NO)
    val apOnWOn = mutableStateOf("6")
    val apOnW = mutableStateOf("1")

    fun updateAttacker() {
        try {
            updateAttackerUnsafe()
            onUpdate(attacker)
        } catch (e: Exception) {
            Log.e("W40K", "Exception during attacker update", e)
        }
    }

    private fun updateAttackerUnsafe() {
        val attacks: Value = attacks.value.parseValue()
        val skill: Int = skill.value.toInt()
        val strength: Value = strength.value.parseValue()
        val penetration: Value = penetration.value.parseValue()
        val damage: Value = damage.value.parseValue()
        val additionalHitRule: AdditionalHitRule =
            if (additionalHitRule.value) {
                AdditionalHitRule.OnResult(
                    additionalHitOn.value.toInt(),
                    additionalHits.value.parseValue(),
                )
            } else {
                AdditionalHitRule.No
            }
        val autoHitRule: AutoHitRule =
            if (autoHit.value) {
                AutoHitRule.Always
            } else {
                AutoHitRule.No
            }
        val hitRerollRule: RerollRule =
            when (hRerollRule.value) {
                RerollRules.NO -> RerollRule.No
                RerollRules.ONES -> RerollRule.Ones
                RerollRules.ALL -> RerollRule.Full
                RerollRules.SINGLE -> RerollRule.Single
            }
        val damageCharacteristicHitRule: DamageCharacteristicRule =
            when (damageOnHRule.value) {
                ModificationRule.NO -> DamageCharacteristicRule.No
                ModificationRule.ADD -> DamageCharacteristicRule.AdditionalOnResult(
                    damageOnHOn.value.toInt(),
                    damageOnH.value.parseValue(),
                )
                ModificationRule.REPLACE -> DamageCharacteristicRule.OnResult(
                    damageOnHOn.value.toInt(),
                    damageOnH.value.parseValue(),
                )
            }
        val damageHitRule: DamageOnRollRule =
            if (mortalsOnHRule.value) {
                DamageOnRollRule.OnHit(
                    mortalsOnHOn.value.toInt(),
                    mortalsOnH.value.parseValue()
                )
            } else {
                DamageOnRollRule.No
            }
        val autoWoundRule: AutoWoundRule =
            if (hasAutoWoundRule.value) {
                AutoWoundRule.OnResult(autoWoundOn.value.toInt())
            } else {
                AutoWoundRule.No
            }
        val penetrationHitRule: PenetrationRule =
            when (apOnHitRule.value) {
                ModificationRule.NO -> PenetrationRule.No
                ModificationRule.ADD -> PenetrationRule.AdditionalOnResult(
                    apOnHitOn.value.toInt(),
                    apOnHit.value.parseValue(),
                )
                ModificationRule.REPLACE -> PenetrationRule.OnResult(
                    apOnHitOn.value.toInt(),
                    apOnHit.value.parseValue(),
                )
            }
        val strengthHitRule: StrengthHitRule =
            when (strengthOnHRule.value) {
                ModificationRule.NO -> StrengthHitRule.No
                ModificationRule.ADD -> StrengthHitRule.AdditionalOnResult(
                    strengthOnHOn.value.toInt(),
                    strengthOnH.value.parseValue(),
                )
                ModificationRule.REPLACE -> StrengthHitRule.OnResult(
                    strengthOnHOn.value.toInt(),
                    strengthOnH.value.parseValue(),
                )
            }

        val woundRerollRule: RerollRule =
            when (wRerollRule.value) {
                RerollRules.NO -> RerollRule.No
                RerollRules.ONES -> RerollRule.Ones
                RerollRules.ALL -> RerollRule.Full
                RerollRules.SINGLE -> RerollRule.Single
            }
        val damageCharacteristicWoundRule: DamageCharacteristicRule =
            when (damageOnWRule.value) {
                ModificationRule.NO -> DamageCharacteristicRule.No
                ModificationRule.ADD -> DamageCharacteristicRule.AdditionalOnResult(
                    damageOnWOn.value.toInt(),
                    damageOnW.value.parseValue(),
                )
                ModificationRule.REPLACE -> DamageCharacteristicRule.OnResult(
                    damageOnWOn.value.toInt(),
                    damageOnW.value.parseValue(),
                )
            }
        val damageWoundRule: DamageOnRollRule =
            if (mortalsOnWRule.value) {
                DamageOnRollRule.OnHit(
                    mortalsOnWOn.value.toInt(),
                    mortalsOnW.value.parseValue()
                )
            } else {
                DamageOnRollRule.No
            }
        val penetrationWoundRule: PenetrationRule =
            when (apOnWRule.value) {
                ModificationRule.NO -> PenetrationRule.No
                ModificationRule.ADD -> PenetrationRule.AdditionalOnResult(
                    apOnWOn.value.toInt(),
                    apOnW.value.parseValue(),
                )
                ModificationRule.REPLACE -> PenetrationRule.OnResult(
                    apOnWOn.value.toInt(),
                    apOnW.value.parseValue(),
                )
            }

        attacker = Attacker(
            attacks, skill, strength, penetration, damage,
            additionalHitRule, autoHitRule, hitRerollRule,
            this.plusOneToH.value, damageCharacteristicHitRule,
            damageHitRule, autoWoundRule, penetrationHitRule, strengthHitRule,
            woundRerollRule, this.plusOneToW.value,
            damageCharacteristicWoundRule, damageWoundRule, penetrationWoundRule,
        )
    }
}

class DefenderViewModel(private val onUpdate: (Defender) -> Unit) : ViewModel() {
    private var defender = Defender()

    val hitTranshuman = mutableStateOf(false)
    val woundTranshuman = mutableStateOf(false)
    val toughness = mutableStateOf("4")
    val save = mutableStateOf("4")
    val hasInvulnerableSave = mutableStateOf(false)
    val invulnerableSave = mutableStateOf("6")
    val hasFeelNoPain = mutableStateOf(false)
    val feelNoPain = mutableStateOf("6")
    val hasDamageDecrease = mutableStateOf(false)
    val damageDecrease = mutableStateOf("1")

    fun updateDefender() {
        try {
            updateDefenderUnsafe()
            onUpdate(defender)
        } catch (e: Exception) {
            Log.e("W40K", "Exception during defender update", e)
        }
    }

    private fun updateDefenderUnsafe() {
        val hitTranshuman: Boolean = hitTranshuman.value
        val woundTranshuman: Boolean = woundTranshuman.value
        val toughness: Value = toughness.value.parseValue()
        val save: Value = save.value.parseValue()
        val invulnerableSave: Value = if (hasInvulnerableSave.value) {
            invulnerableSave.value.parseValue()
        } else {
            7.toValue()
        }
        val feelNoPain: Int = if (hasFeelNoPain.value) {
            feelNoPain.value.toInt()
        } else {
            7
        }
        val damageDecrease: Value = if (hasDamageDecrease.value) {
            damageDecrease.value.parseValue()
        } else {
            none
        }

        defender = Defender(
            hitTranshuman, woundTranshuman, toughness, save,
            invulnerableSave, feelNoPain, damageDecrease
        )
    }
}