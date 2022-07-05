package com.arychagov.w40k

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arychagov.w40k.data.decrease
import com.arychagov.w40k.data.increase
import com.arychagov.w40k.ui.theme.W40kTheme
import com.arychagov.w40k.view.Spinner

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = W40KViewModel().also {
            it.attackerVM.updateAttacker()
        }
        setContent {
            W40kTheme {
                Scaffold(
                    bottomBar = {
                        BottomAppBar(backgroundColor = MaterialTheme.colors.background) {
                            val damage = remember { viewModel.resultDamage }
                            Text(
                                text = "Total wounds: ${damage.value}",
                                fontSize = 16.sp,
                                modifier = Modifier.fillMaxWidth(),
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                ) {
                    MainFrame(viewModel)
                }
            }
        }
    }
}

@Composable
fun MainFrame(viewModel: W40KViewModel = W40KViewModel()) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        AttackerFrame(viewModel.attackerVM)
        Spacer(modifier = Modifier.height(16.dp))
        DefenderFrame(viewModel.defenderVM)
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun AttackerFrame(attackerVM: AttackerViewModel) {
    val attackerExpanded = remember {
        mutableStateOf(true)
    }
    Text(
        text = "ATTACKER",
        color = if (attackerExpanded.value) Color.Black else Color.Gray,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { attackerExpanded.value = !attackerExpanded.value },
    )
    if (!attackerExpanded.value) {
        return
    }
    Spacer(modifier = Modifier.height(4.dp))
    CreateIncreaseDecreaseLine(
        text = "Attacks",
        value = attackerVM.attacks,
        updater = { attackerVM.updateAttacker() }
    )
    Spacer(modifier = Modifier.height(4.dp))
    CreateIncreaseDecreaseLine(
        text = "Skill",
        value = attackerVM.skill,
        updater = { attackerVM.updateAttacker() }
    )
    Spacer(modifier = Modifier.height(4.dp))
    CreateIncreaseDecreaseLine(
        text = "Strength",
        value = attackerVM.strength,
        updater = { attackerVM.updateAttacker() }
    )
    Spacer(modifier = Modifier.height(4.dp))
    CreateIncreaseDecreaseLine(
        text = "Penetration",
        value = attackerVM.penetration,
        updater = { attackerVM.updateAttacker() },
        belowOne = true,
    )
    Spacer(modifier = Modifier.height(4.dp))
    CreateIncreaseDecreaseLine(
        text = "Damage",
        value = attackerVM.damage,
        updater = { attackerVM.updateAttacker() }
    )
    val extras = remember {
        mutableStateOf(false)
    }
    Text(
        text = "> Customize",
        fontSize = 16.sp,
        color = if (extras.value) Color.Black else Color.Gray,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.clickable {
            extras.value = !extras.value
        }
    )
    if (!extras.value) {
        return
    }
    Spacer(modifier = Modifier.height(4.dp))
    CreateCheckedOnRule(
        ruleDescription = "Additional hits",
        conditionDescription = "On",
        conditionResult = "Hits",
        rule = attackerVM.additionalHitRule,
        ruleOn = attackerVM.additionalHitOn,
        ruleResult = attackerVM.additionalHits,
        updater = { attackerVM.updateAttacker() }
    )
    Spacer(modifier = Modifier.height(4.dp))
    CreateChecked(
        text = "Auto hit",
        rule = attackerVM.autoHit,
        updater = { attackerVM.updateAttacker() }
    )
    Spacer(modifier = Modifier.height(4.dp))
    CreateSelectable(
        text = "Hit reroll",
        rule = attackerVM.hRerollRule,
        values = RerollRules.values(),
        updater = { attackerVM.updateAttacker() }
    )
    Spacer(modifier = Modifier.height(4.dp))
    CreateChecked(
        text = "+1 to hit",
        rule = attackerVM.plusOneToH,
        updater = { attackerVM.updateAttacker() }
    )
    Spacer(modifier = Modifier.height(4.dp))
    CreateModificationRule(
        ruleDescription = "+D on hit",
        conditionDescription = "On",
        conditionResult = "Damage",
        rule = attackerVM.damageOnHRule,
        ruleOn = attackerVM.damageOnHOn,
        ruleResult = attackerVM.damageOnH,
        updater = { attackerVM.updateAttacker() }
    )
    Spacer(modifier = Modifier.height(4.dp))
    CreateCheckedOnRule(
        ruleDescription = "MW on hit",
        conditionDescription = "On",
        conditionResult = "MW",
        rule = attackerVM.mortalsOnHRule,
        ruleOn = attackerVM.mortalsOnHOn,
        ruleResult = attackerVM.mortalsOnH,
        updater = { attackerVM.updateAttacker() }
    )
    Spacer(modifier = Modifier.height(4.dp))
    CreateCheckedOnRule(
        ruleDescription = "Auto wound",
        conditionDescription = "On",
        conditionResult = "",
        rule = attackerVM.hasAutoWoundRule,
        ruleOn = attackerVM.autoWoundOn,
        ruleResult = null,
        updater = { attackerVM.updateAttacker() }
    )
    Spacer(modifier = Modifier.height(4.dp))
    CreateModificationRule(
        ruleDescription = "-AP on hit",
        conditionDescription = "On",
        conditionResult = "-AP",
        rule = attackerVM.apOnHitRule,
        ruleOn = attackerVM.apOnHitOn,
        ruleResult = attackerVM.apOnHit,
        updater = { attackerVM.updateAttacker() }
    )
    Spacer(modifier = Modifier.height(4.dp))
    CreateModificationRule(
        ruleDescription = "+S on hit",
        conditionDescription = "On",
        conditionResult = "+S",
        rule = attackerVM.strengthOnHRule,
        ruleOn = attackerVM.strengthOnHOn,
        ruleResult = attackerVM.strengthOnH,
        updater = { attackerVM.updateAttacker() }
    )
    Spacer(modifier = Modifier.height(4.dp))
    CreateSelectable(
        text = "Wound reroll",
        rule = attackerVM.wRerollRule,
        values = RerollRules.values(),
        updater = { attackerVM.updateAttacker() }
    )
    Spacer(modifier = Modifier.height(4.dp))
    CreateChecked(
        text = "+1 to wound",
        rule = attackerVM.plusOneToW,
        updater = { attackerVM.updateAttacker() }
    )
    Spacer(modifier = Modifier.height(4.dp))
    CreateModificationRule(
        ruleDescription = "+D on wound",
        conditionDescription = "On",
        conditionResult = "+D",
        rule = attackerVM.damageOnWRule,
        ruleOn = attackerVM.damageOnWOn,
        ruleResult = attackerVM.damageOnW,
        updater = { attackerVM.updateAttacker() }
    )
    Spacer(modifier = Modifier.height(4.dp))
    CreateCheckedOnRule(
        ruleDescription = "MW on wound",
        conditionDescription = "On",
        conditionResult = "MW",
        rule = attackerVM.mortalsOnWRule,
        ruleOn = attackerVM.mortalsOnWOn,
        ruleResult = attackerVM.mortalsOnW,
        updater = { attackerVM.updateAttacker() }
    )
    Spacer(modifier = Modifier.height(4.dp))
    CreateModificationRule(
        ruleDescription = "-AP on wound",
        conditionDescription = "On",
        conditionResult = "-AP",
        rule = attackerVM.apOnWRule,
        ruleOn = attackerVM.apOnWOn,
        ruleResult = attackerVM.apOnW,
        updater = { attackerVM.updateAttacker() }
    )
}

@Composable
fun <T : Enum<T>> CreateSelectable(
    text: String,
    rule: MutableState<T>,
    values: Array<T>,
    updater: () -> Unit,
) {
    val selected = remember { rule }
    Row {
        Text(
            text = text,
            fontSize = 16.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(alignment = Alignment.CenterVertically)
                .fillMaxWidth(fraction = 0.25f)
        )
        Spinner(
            items = values.asList(),
            selectedItem = rule.value,
            onItemSelected = { v ->
                selected.value = v
                updater()
            },
            selectedItemFactory = { modifier, item ->
                Row(
                    modifier = modifier
                        .padding(8.dp)
                        .wrapContentSize()
                ) {
                    Text(text = item.name)
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_arrow_drop_down_24),
                        contentDescription = "Drop down arrow",
                    )
                }
            },
            dropdownItemFactory = { item, _ ->
                Text(text = item.name)
            }
        )
    }
}

@Composable
fun CreateCheckedOnRule(
    ruleDescription: String,
    conditionDescription: String,
    conditionResult: String,
    rule: MutableState<Boolean>,
    ruleOn: MutableState<String>,
    ruleResult: MutableState<String>?,
    updater: () -> Unit,
    belowOne: Boolean = false,
) {
    val enabled = remember { rule }
    Column {
        Row {
            Text(
                text = ruleDescription,
                fontSize = 16.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(alignment = Alignment.CenterVertically)
                    .fillMaxWidth(fraction = 0.25f)
            )
            Checkbox(
                checked = enabled.value,
                onCheckedChange = { v ->
                    enabled.value = v
                    updater()
                }
            )
        }
        if (enabled.value) {
            CreateIncreaseDecreaseLine(
                text = conditionDescription,
                value = ruleOn,
                updater = updater,
                belowOne = belowOne,
            )
            if (ruleResult != null) {
                CreateIncreaseDecreaseLine(
                    text = conditionResult,
                    value = ruleResult,
                    updater = updater,
                    belowOne = belowOne,
                )
            }
        }
    }
}

@Composable
fun CreateModificationRule(
    ruleDescription: String,
    conditionDescription: String,
    conditionResult: String,
    rule: MutableState<ModificationRule>,
    ruleOn: MutableState<String>,
    ruleResult: MutableState<String>,
    belowOne: Boolean = false,
    updater: () -> Unit,
) {
    val status = remember { rule }
    Column {
        Row {
            Text(
                text = ruleDescription,
                fontSize = 16.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(alignment = Alignment.CenterVertically)
                    .fillMaxWidth(fraction = 0.25f)
            )
            Spinner(
                items = ModificationRule.values().asList(),
                selectedItem = rule.value,
                onItemSelected = { v ->
                    status.value = v
                    updater()
                },
                selectedItemFactory = { modifier, item ->
                    Row(
                        modifier = modifier
                            .padding(8.dp)
                            .wrapContentSize()
                    ) {
                        Text(text = item.name)
                        Icon(
                            painter = painterResource(id = R.drawable.ic_baseline_arrow_drop_down_24),
                            contentDescription = "Drop down arrow",
                        )
                    }
                },
                dropdownItemFactory = { item, _ ->
                    Text(text = item.name)
                }
            )
        }
        if (status.value != ModificationRule.NO) {
            CreateIncreaseDecreaseLine(
                text = conditionDescription,
                value = ruleOn,
                updater = updater,
                belowOne = belowOne,
            )
            CreateIncreaseDecreaseLine(
                text = conditionResult,
                value = ruleResult,
                updater = updater,
                belowOne = belowOne,
            )
        }
    }
}

@Composable
fun CreateChecked(
    text: String,
    rule: MutableState<Boolean>,
    updater: () -> Unit,
) {
    val enabled = remember { rule }
    Row {
        Text(
            text = text,
            fontSize = 16.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(alignment = Alignment.CenterVertically)
                .fillMaxWidth(fraction = 0.25f)
        )
        Checkbox(
            checked = enabled.value,
            onCheckedChange = { v ->
                enabled.value = v
                updater()
            }
        )
    }
}

@Composable
private fun CreateIncreaseDecreaseLine(
    text: String,
    value: MutableState<String>,
    belowOne: Boolean = false,
    updater: () -> Unit,
) {
    Row {
        Text(
            text = text,
            fontSize = 16.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(alignment = Alignment.CenterVertically)
                .fillMaxWidth(fraction = 0.25f)
        )
        IncreaseDecrease(remember { value }, updater, belowOne)
    }
}

@Composable
fun IncreaseDecrease(
    state: MutableState<String>,
    updater: () -> Unit,
    belowOne: Boolean = false,
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Image(
            painter = painterResource(id = R.drawable.increase),
            contentDescription = "Decrease",
            modifier = Modifier
                .align(alignment = Alignment.CenterVertically)
                .padding(10.dp)
                .clickable {
                    state.value = state.value.decrease(belowOne)
                    updater()
                }
        )
        OutlinedTextField(
            value = state.value,
            onValueChange = { text ->
                state.value = text
                updater()
            },
            textStyle = TextStyle(fontSize = 20.sp),
            modifier = Modifier
                .align(alignment = Alignment.CenterVertically)
                .fillMaxWidth(fraction = 0.65f)
        )
        Image(
            painter = painterResource(id = R.drawable.decrease),
            contentDescription = "Increase",
            modifier = Modifier
                .align(alignment = Alignment.CenterVertically)
                .padding(10.dp)
                .clickable {
                    state.value = state.value.increase()
                    updater()
                }
        )
    }
}

@Composable
fun DefenderFrame(defenderVM: DefenderViewModel) {
    val defenderExpanded = remember {
        mutableStateOf(true)
    }
    Text(
        text = "DEFENDER",
        color = if (defenderExpanded.value) Color.Black else Color.Gray,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { defenderExpanded.value = !defenderExpanded.value },
    )
    if (!defenderExpanded.value) {
        return
    }

    Spacer(modifier = Modifier.height(4.dp))
    CreateIncreaseDecreaseLine(
        text = "Toughness",
        value = defenderVM.toughness,
        updater = { defenderVM.updateDefender() }
    )
    Spacer(modifier = Modifier.height(4.dp))
    CreateIncreaseDecreaseLine(
        text = "Save",
        value = defenderVM.save,
        updater = { defenderVM.updateDefender() }
    )
    Spacer(modifier = Modifier.height(4.dp))
    CreateChecked(
        text = "Has inv. save",
        rule = defenderVM.hasInvulnerableSave,
        updater = { defenderVM.updateDefender() }
    )
    if (defenderVM.hasInvulnerableSave.value) {
        Spacer(modifier = Modifier.height(4.dp))
        CreateIncreaseDecreaseLine(
            text = "Inv. Save",
            value = defenderVM.invulnerableSave,
            updater = { defenderVM.updateDefender() }
        )
    }
    Spacer(modifier = Modifier.height(4.dp))
    CreateChecked(
        text = "Has FNP",
        rule = defenderVM.hasFeelNoPain,
        updater = { defenderVM.updateDefender() }
    )
    if (defenderVM.hasFeelNoPain.value) {
        Spacer(modifier = Modifier.height(4.dp))
        CreateIncreaseDecreaseLine(
            text = "FNP",
            value = defenderVM.feelNoPain,
            updater = { defenderVM.updateDefender() }
        )
    }
    Spacer(modifier = Modifier.height(4.dp))
    CreateChecked(
        text = "Hit trans",
        rule = defenderVM.hitTranshuman,
        updater = { defenderVM.updateDefender() }
    )
    Spacer(modifier = Modifier.height(4.dp))
    CreateChecked(
        text = "Wound trans",
        rule = defenderVM.woundTranshuman,
        updater = { defenderVM.updateDefender() }
    )
    Spacer(modifier = Modifier.height(4.dp))
    CreateChecked(
        text = "Has -Damage",
        rule = defenderVM.hasDamageDecrease,
        updater = { defenderVM.updateDefender() }
    )
    if (defenderVM.hasDamageDecrease.value) {
        Spacer(modifier = Modifier.height(4.dp))
        CreateIncreaseDecreaseLine(
            text = "-Damage",
            value = defenderVM.damageDecrease,
            updater = { defenderVM.updateDefender() }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    W40kTheme {
        MainFrame()
    }
}