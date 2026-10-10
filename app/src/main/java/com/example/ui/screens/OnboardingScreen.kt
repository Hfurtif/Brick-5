package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.ImeAction
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

    val handleNext: () -> Unit = {
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
                    currency = currency.ifBlank { "FCFA" },
                    monthlySalary = sal,
                    payDayOfMonth = pd,
                    dailyReminderHour = rh,
                    fixedCharges = fc,
                    isOnboarded = true
                )
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BrickBackground)
            .systemBarsPadding()
            .imePadding()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
        ) {
            BrickLogo(size = 42.dp, showText = true)
            Spacer(modifier = Modifier.height(10.dp))

            // Step Indicator Dots
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..4).forEach { i ->
                    Box(
                        modifier = Modifier
                            .size(if (i == step) 28.dp else 10.dp, 8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (i == step) ElectricCyan
                                else if (i < step) EmeraldSuccess
                                else BrickSurfaceBorder
                            )
                    )
                }
            }
        }

        // Scrollable Content per Step with Continue button directly inside the view
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter
        ) {
            AnimatedContent(
                targetState = step,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                modifier = Modifier.fillMaxWidth()
            ) { targetStep ->
                when (targetStep) {
                    1 -> StepOne(
                        name = name,
                        onNameChange = { name = it },
                        currency = currency,
                        onCurrencyChange = { currency = it },
                        onNext = handleNext
                    )
                    2 -> StepTwo(
                        salary = monthlySalaryStr,
                        onSalaryChange = { monthlySalaryStr = it },
                        payDay = payDayStr,
                        onPayDayChange = { payDayStr = it },
                        currency = currency,
                        onBack = {
                            if (step > 1) {
                                step--
                            }
                        },
                        onNext = handleNext
                    )
                    3 -> StepThree(
                        fixedCharges = fixedChargesStr,
                        onFixedChargesChange = { fixedChargesStr = it },
                        reminderHour = reminderHourStr,
                        onReminderHourChange = { reminderHourStr = it },
                        currency = currency,
                        onBack = {
                            if (step > 1) {
                                step--
                            }
                        },
                        onNext = handleNext
                    )
                    4 -> StepFour(
                        name = name,
                        salary = monthlySalaryStr.toDoubleOrNull() ?: 2400.0,
                        fixedCharges = fixedChargesStr.toDoubleOrNull() ?: 950.0,
                        currency = currency,
                        onBack = {
                            if (step > 1) {
                                step--
                            }
                        },
                        onComplete = handleNext
                    )
                }
            }
        }
    }
}

@Composable
private fun StepOne(
    name: String,
    onNameChange: (String) -> Unit,
    currency: String,
    onCurrencyChange: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Bienvenue sur BRICK",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextWhite
        )
        Text(
            text = "L'application pour stopper les dépenses compulsives et bâtir tes fondations financières solides.",
            fontSize = 13.sp,
            color = TextMuted,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Comment t'appelles-tu ?") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onNext() }),
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
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("FCFA", "€", "$", "CHF", "£").forEach { cur ->
                val isSel = currency == cur
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSel) ElectricCyan else BrickSurfaceElevated)
                        .border(1.dp, if (isSel) ElectricCyan else BrickSurfaceBorder, RoundedCornerShape(10.dp))
                        .clickable { onCurrencyChange(cur) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = cur,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSel) BrickBackground else TextWhite,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Direct Continue Button moved up right below fields
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("onboarding_next_btn"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
        ) {
            Text(
                text = "Continuer",
                color = BrickBackground,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = BrickBackground
            )
        }
    }
}

@Composable
private fun StepTwo(
    salary: String,
    onSalaryChange: (String) -> Unit,
    payDay: String,
    onPayDayChange: (String) -> Unit,
    currency: String,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Tes Revenus Réguliers",
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite
        )
        Text(
            text = "Combien rentre chaque mois dans ta poche ? C'est la base pour calculer ton Reste à Vivre.",
            fontSize = 13.sp,
            color = TextMuted,
            lineHeight = 18.sp
        )

        OutlinedTextField(
            value = salary,
            onValueChange = onSalaryChange,
            label = { Text("Salaire mensuel net ($currency)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
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
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onNext() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                focusedBorderColor = ElectricCyan,
                unfocusedBorderColor = BrickSurfaceBorder
            ),
            modifier = Modifier.fillMaxWidth().testTag("onboard_payday_input")
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Direct Buttons Row moved up
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(BrickSurfaceElevated)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Précédent",
                    tint = TextWhite
                )
            }

            Button(
                onClick = onNext,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("onboarding_next_btn"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
            ) {
                Text(
                    text = "Continuer",
                    color = BrickBackground,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = BrickBackground
                )
            }
        }
    }
}

@Composable
private fun StepThree(
    fixedCharges: String,
    onFixedChargesChange: (String) -> Unit,
    reminderHour: String,
    onReminderHourChange: (String) -> Unit,
    currency: String,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Charges & Rappel Quotidien",
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite
        )
        Text(
            text = "Loyer, factures, électricité, abonnements… et l'heure du rappel quotidien chaque soir.",
            fontSize = 13.sp,
            color = TextMuted,
            lineHeight = 18.sp
        )

        OutlinedTextField(
            value = fixedCharges,
            onValueChange = onFixedChargesChange,
            label = { Text("Total des charges fixes mensuelles ($currency)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
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
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onNext() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                focusedBorderColor = ElectricCyan,
                unfocusedBorderColor = BrickSurfaceBorder
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Direct Buttons Row moved up
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(BrickSurfaceElevated)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Précédent",
                    tint = TextWhite
                )
            }

            Button(
                onClick = onNext,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("onboarding_next_btn"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
            ) {
                Text(
                    text = "Continuer",
                    color = BrickBackground,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = BrickBackground
                )
            }
        }
    }
}

@Composable
private fun StepFour(
    name: String,
    salary: Double,
    fixedCharges: Double,
    currency: String,
    onBack: () -> Unit,
    onComplete: () -> Unit
) {
    val needsTarget = salary * 0.50
    val wantsTarget = salary * 0.30
    val savingsTarget = salary * 0.20

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Ton Plan 50 / 30 / 20 est Prêt !",
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            color = CyberGold
        )
        Text(
            text = "$name, voici ton nouveau cadre de liberté financière :",
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
            fontSize = 12.sp,
            color = TextMuted,
            lineHeight = 16.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Complete Button Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(BrickSurfaceElevated)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Précédent",
                    tint = TextWhite
                )
            }

            Button(
                onClick = onComplete,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("onboarding_next_btn"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
            ) {
                Text(
                    text = "Bâtir mes fondations",
                    color = BrickBackground,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = BrickBackground
                )
            }
        }
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
