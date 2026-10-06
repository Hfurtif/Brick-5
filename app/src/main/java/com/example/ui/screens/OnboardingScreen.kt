package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.ui.components.BrickLogo
import com.example.ui.components.NeonGlassCard
import com.example.ui.theme.BrickBackground
import com.example.ui.theme.BrickSurfaceBorder
import com.example.ui.theme.BrickSurfaceElevated
import com.example.ui.theme.CyberGold
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.NeonVioletGlow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun OnboardingScreen(
    initialUser: UserProfile,
    onComplete: (UserProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableIntStateOf(1) }

    var name by remember { mutableStateOf(initialUser.name) }
    var currency by remember { mutableStateOf(initialUser.currency) }
    var monthlySalaryStr by remember { mutableStateOf(initialUser.monthlySalary.toInt().toString()) }
    var payDayStr by remember { mutableStateOf(initialUser.payDayOfMonth.toString()) }
    var reminderHourStr by remember { mutableStateOf(initialUser.dailyReminderHour.toString()) }
    var fixedChargesStr by remember { mutableStateOf(initialUser.fixedCharges.toInt().toString()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BrickBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            BrickLogo(size = 54.dp, showText = true)
            Spacer(modifier = Modifier.height(16.dp))

            // Step Indicator Dots
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..4).forEach { i ->
                    Box(
                        modifier = Modifier
                            .size(if (i == step) 28.dp else 10.dp, 10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(
                                if (i == step) ElectricCyan
                                else if (i < step) EmeraldSuccess
                                else BrickSurfaceBorder
                            )
                    )
                }
            }
        }

        // Animated Content per Step
        AnimatedContent(
            targetState = step,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier.weight(1f)
        ) { targetStep ->
            when (targetStep) {
                1 -> StepOne(
                    name = name,
                    onNameChange = { name = it },
                    currency = currency,
                    onCurrencyChange = { currency = it }
                )
                2 -> StepTwo(
                    salary = monthlySalaryStr,
                    onSalaryChange = { monthlySalaryStr = it },
                    payDay = payDayStr,
                    onPayDayChange = { payDayStr = it },
                    currency = currency
                )
                3 -> StepThree(
                    fixedCharges = fixedChargesStr,
                    onFixedChargesChange = { fixedChargesStr = it },
                    reminderHour = reminderHourStr,
                    onReminderHourChange = { reminderHourStr = it },
                    currency = currency
                )
                4 -> StepFour(
                    name = name,
                    salary = monthlySalaryStr.toDoubleOrNull() ?: 2400.0,
                    fixedCharges = fixedChargesStr.toDoubleOrNull() ?: 950.0,
                    currency = currency
                )
            }
        }

        // Navigation bottom bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (step > 1) {
                IconButton(
                    onClick = { step-- },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(BrickSurfaceElevated)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Précédent",
                        tint = TextWhite
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(48.dp))
            }

            Button(
                onClick = {
                    if (step < 4) {
                        step++
                    } else {
                        val sal = monthlySalaryStr.toDoubleOrNull() ?: 2500.0
                        val pd = payDayStr.toIntOrNull() ?: 28
                        val rh = reminderHourStr.toIntOrNull() ?: 20
                        val fc = fixedChargesStr.toDoubleOrNull() ?: 950.0
                        onComplete(
                            initialUser.copy(
                                name = name.ifBlank { "Alexandre" },
                                currency = currency.ifBlank { "€" },
                                monthlySalary = sal,
                                payDayOfMonth = pd,
                                dailyReminderHour = rh,
                                fixedCharges = fc,
                                isOnboarded = true
                            )
                        )
                    }
                },
                modifier = Modifier
                    .height(50.dp)
                    .testTag("onboarding_next_btn"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
            ) {
                Text(
                    text = if (step < 4) "Continuer" else "Bâtir mes fondations",
                    color = BrickBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = if (step < 4) Icons.AutoMirrored.Filled.ArrowForward else Icons.Default.Check,
                    contentDescription = null,
                    tint = BrickBackground
                )
            }
        }
    }
}

@Composable
private fun StepOne(
    name: String,
    onNameChange: (String) -> Unit,
    currency: String,
    onCurrencyChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Bienvenue sur BRICK",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextWhite
        )
        Text(
            text = "Pour ceux qui ne savent pas gérer leur argent et dépensent sans contrôle. Posons ta première brique.",
            fontSize = 13.sp,
            color = TextMuted,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Comment t'appelles-tu ?") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                focusedBorderColor = ElectricCyan,
                unfocusedBorderColor = BrickSurfaceBorder
            ),
            modifier = Modifier.fillMaxWidth().testTag("onboard_name_input")
        )

        // Currency Picker
        Text(
            text = "Choisis ta devise principale :",
            fontSize = 12.sp,
            color = TextMuted,
            modifier = Modifier.align(Alignment.Start)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf("€", "$", "FCFA", "CHF", "£").forEach { cur ->
                val isSel = currency == cur
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSel) ElectricCyan else BrickSurfaceElevated)
                        .border(1.dp, if (isSel) ElectricCyan else BrickSurfaceBorder, RoundedCornerShape(10.dp))
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.TextButton(onClick = { onCurrencyChange(cur) }) {
                        Text(
                            text = cur,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSel) BrickBackground else TextWhite
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StepTwo(
    salary: String,
    onSalaryChange: (String) -> Unit,
    payDay: String,
    onPayDayChange: (String) -> Unit,
    currency: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Tes Revenus Réguliers",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite
        )
        Text(
            text = "Le point de départ de ta liberté financière. Combien rentre chaque mois dans ta poche ?",
            fontSize = 13.sp,
            color = TextMuted,
            lineHeight = 18.sp
        )

        OutlinedTextField(
            value = salary,
            onValueChange = onSalaryChange,
            label = { Text("Salaire mensuel net ($currency)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                focusedBorderColor = ElectricCyan,
                unfocusedBorderColor = BrickSurfaceBorder
            ),
            modifier = Modifier.fillMaxWidth().testTag("onboard_salary_input")
        )

        OutlinedTextField(
            value = payDay,
            onValueChange = onPayDayChange,
            label = { Text("Jour du virement de la paie (1 - 31)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                focusedBorderColor = ElectricCyan,
                unfocusedBorderColor = BrickSurfaceBorder
            ),
            modifier = Modifier.fillMaxWidth().testTag("onboard_payday_input")
        )
    }
}

@Composable
private fun StepThree(
    fixedCharges: String,
    onFixedChargesChange: (String) -> Unit,
    reminderHour: String,
    onReminderHourChange: (String) -> Unit,
    currency: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Charges & Rappel Quotidien",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite
        )
        Text(
            text = "Loyer, courses, électricité, abonnements… et l'heure à laquelle BRICK te rappelle de noter tes dépenses.",
            fontSize = 13.sp,
            color = TextMuted,
            lineHeight = 18.sp
        )

        OutlinedTextField(
            value = fixedCharges,
            onValueChange = onFixedChargesChange,
            label = { Text("Total des charges fixes mensuelles ($currency)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                focusedBorderColor = ElectricCyan,
                unfocusedBorderColor = BrickSurfaceBorder
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = reminderHour,
            onValueChange = onReminderHourChange,
            label = { Text("Heure du rappel chaque soir (ex: 20 pour 20h)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                focusedBorderColor = ElectricCyan,
                unfocusedBorderColor = BrickSurfaceBorder
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun StepFour(
    name: String,
    salary: Double,
    fixedCharges: Double,
    currency: String
) {
    val needsTarget = salary * 0.50
    val wantsTarget = salary * 0.30
    val savingsTarget = salary * 0.20

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Ton Plan 50 / 30 / 20 est Prêt !",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = CyberGold
        )
        Text(
            text = "$name, voici ton nouveau cadre de vie financière :",
            fontSize = 13.sp,
            color = TextMuted
        )

        NeonGlassCard(borderColor = ElectricCyan) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PlanRow(label = "🏠 Besoins (50% max)", amount = needsTarget, currency = currency, color = ElectricCyan)
                PlanRow(label = "🍸 Envies (30% max)", amount = wantsTarget, currency = currency, color = NeonVioletGlow)
                PlanRow(label = "👑 Se Payer en Premier (20% min)", amount = savingsTarget, currency = currency, color = CyberGold)
            }
        }

        Text(
            text = "💡 'Ce n'est pas combien tu gagnes qui compte, c'est combien tu gardes et comment cet argent travaille pour toi.' — Robert Kiyosaki",
            fontSize = 11.sp,
            color = TextMuted,
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun PlanRow(label: String, amount: Double, currency: String, color: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = TextWhite, fontWeight = FontWeight.Medium)
        Text(text = "${amount.toInt()} $currency / mois", fontSize = 13.sp, color = color, fontWeight = FontWeight.Bold)
    }
}
