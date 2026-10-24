package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.ui.components.BrickLogo
import com.example.ui.theme.*

@Composable
fun OnboardingScreen(
    initialUser: UserProfile?,
    onComplete: (user: UserProfile, cashLiquide: Double, cashMobile: Double, cashFamily: Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableIntStateOf(1) }

    // Step 1: Identity & Currency
    var name by remember { mutableStateOf(initialUser?.name ?: "Alexandre") }
    var currency by remember { mutableStateOf(initialUser?.currency ?: "FCFA") }

    // Step 2: Caisse & Argent disponible au départ (PAS de salaire forcé !)
    var cashLiquideStr by remember { mutableStateOf("0") }
    var cashMobileStr by remember { mutableStateOf("0") }
    var cashFamilyStr by remember { mutableStateOf("0") }

    // Step 3: Charges fixes & Reminder
    var fixedChargesStr by remember { mutableStateOf(initialUser?.fixedCharges?.toInt()?.toString() ?: "0") }
    var reminderHourStr by remember { mutableStateOf(initialUser?.dailyReminderHour?.toString() ?: "20") }

    val cashLiquide = cashLiquideStr.toDoubleOrNull() ?: 0.0
    val cashMobile = cashMobileStr.toDoubleOrNull() ?: 0.0
    val cashFamily = cashFamilyStr.toDoubleOrNull() ?: 0.0
    val totalCaisse = cashLiquide + cashMobile + cashFamily

    val fixedCharges = fixedChargesStr.toDoubleOrNull() ?: 0.0
    val reminderHour = reminderHourStr.toIntOrNull()?.coerceIn(0, 23) ?: 20

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BrickBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp)
                .fillMaxHeight()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with Stepper
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (step > 1) {
                    IconButton(
                        onClick = { step-- },
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("onboarding_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = BrickTextPrimary
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(44.dp))
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..4) {
                        val isActive = step == i
                        val isDone = step > i
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(if (isActive) 28.dp else 14.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    when {
                                        isActive -> BrickPrimary
                                        isDone -> EmeraldSuccess
                                        else -> BrickSurfaceBorder
                                    }
                                )
                        )
                    }
                }

                Text(
                    text = "Étape $step/4",
                    style = MaterialTheme.typography.labelSmall,
                    color = BrickTextMuted,
                    fontSize = 11.sp
                )
            }

            // Scrollable Content Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (step) {
                        1 -> StepOneIdentity(
                            name = name,
                            onNameChange = { name = it },
                            currency = currency,
                            onCurrencyChange = { currency = it },
                            onNext = { if (name.isNotBlank()) step = 2 }
                        )

                        2 -> StepTwoCaisseInitiale(
                            currency = currency,
                            cashLiquideStr = cashLiquideStr,
                            onCashLiquideChange = { cashLiquideStr = it },
                            cashMobileStr = cashMobileStr,
                            onCashMobileChange = { cashMobileStr = it },
                            cashFamilyStr = cashFamilyStr,
                            onCashFamilyChange = { cashFamilyStr = it },
                            totalCaisse = totalCaisse,
                            onNext = { step = 3 }
                        )

                        3 -> StepThreeChargesEtAlertes(
                            currency = currency,
                            fixedChargesStr = fixedChargesStr,
                            onFixedChargesChange = { fixedChargesStr = it },
                            reminderHourStr = reminderHourStr,
                            onReminderHourChange = { reminderHourStr = it },
                            onNext = { step = 4 }
                        )

                        4 -> StepFourConfirmationCaisse(
                            name = name,
                            currency = currency,
                            totalCaisse = totalCaisse,
                            cashLiquide = cashLiquide,
                            cashMobile = cashMobile,
                            cashFamily = cashFamily,
                            fixedCharges = fixedCharges,
                            onFinish = {
                                val updatedUser = (initialUser ?: UserProfile()).copy(
                                    name = name.trim().ifEmpty { "Alexandre" },
                                    currency = currency,
                                    monthlySalary = 0.0, // Pas de salaire forcé, les paiements s'ajoutent en caisse !
                                    fixedCharges = fixedCharges,
                                    dailyReminderHour = reminderHour,
                                    dailyReminderMinute = 0,
                                    isOnboarded = true
                                )
                                onComplete(updatedUser, cashLiquide, cashMobile, cashFamily)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StepOneIdentity(
    name: String,
    onNameChange: (String) -> Unit,
    currency: String,
    onCurrencyChange: (String) -> Unit,
    onNext: () -> Unit
) {
    val currencies = listOf("FCFA", "EUR", "USD", "GNF", "NGN", "CDF")

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        BrickLogo(size = 72.dp)
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Bienvenue dans BRICK",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = BrickTextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "La forteresse de vos finances personnelles et de votre caisse",
            style = MaterialTheme.typography.bodyMedium,
            color = BrickTextMuted,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Name input
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("onboarding_name_input"),
            label = { Text("Votre prénom ou nom", color = BrickTextMuted) },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = BrickPrimary) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrickPrimary,
                unfocusedBorderColor = BrickSurfaceBorder,
                focusedTextColor = BrickTextPrimary,
                unfocusedTextColor = BrickTextPrimary
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Currency selection
        Text(
            text = "Votre devise principale",
            style = MaterialTheme.typography.labelMedium,
            color = BrickTextMuted,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            currencies.take(3).forEach { cur ->
                CurrencyPill(
                    label = cur,
                    isSelected = currency == cur,
                    onClick = { onCurrencyChange(cur) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            currencies.drop(3).forEach { cur ->
                CurrencyPill(
                    label = cur,
                    isSelected = currency == cur,
                    onClick = { onCurrencyChange(cur) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        Button(
            onClick = onNext,
            enabled = name.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("onboarding_step1_next_btn"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrickPrimary)
        ) {
            Text("Continuer vers ma caisse", color = Color(0xFF0A0E1A), fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color(0xFF0A0E1A))
        }
    }
}

@Composable
private fun CurrencyPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(44.dp)
            .clickable { onClick() },
        color = if (isSelected) BrickPrimary.copy(alpha = 0.2f) else BrickSurfaceElevated,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) BrickPrimary else BrickSurfaceBorder
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                color = if (isSelected) BrickPrimary else BrickTextPrimary,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun StepTwoCaisseInitiale(
    currency: String,
    cashLiquideStr: String,
    onCashLiquideChange: (String) -> Unit,
    cashMobileStr: String,
    onCashMobileChange: (String) -> Unit,
    cashFamilyStr: String,
    onCashFamilyChange: (String) -> Unit,
    totalCaisse: Double,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        Surface(
            color = BrickSurfaceElevated,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BrickPrimary.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "💡", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Pas de salaire fixe demandé ici !",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = BrickPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Chaque paiement reçu s'ajoutera au fur et à mesure. Pour démarrer, faites simplement le point sur votre caisse actuelle.",
                        style = MaterialTheme.typography.bodySmall,
                        color = BrickTextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Combien as-tu dans la caisse aujourd'hui ?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = BrickTextPrimary,
            fontSize = 17.sp
        )
        Text(
            text = "Indiquez l'argent réellement disponible maintenant",
            style = MaterialTheme.typography.bodySmall,
            color = BrickTextMuted,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Field 1: Espèces / Liquide
        CaisseInputField(
            title = "💵 Espèces / En poche & caisse",
            subtitle = "Billets et pièces à portée de main",
            value = cashLiquideStr,
            onValueChange = onCashLiquideChange,
            currency = currency,
            testTag = "input_cash_liquide"
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Field 2: Mobile Money & Banques
        CaisseInputField(
            title = "📱 Mobile Money & Comptes bancaires",
            subtitle = "Wave, Orange Money, Moov, MTN, Banque",
            value = cashMobileStr,
            onValueChange = onCashMobileChange,
            currency = currency,
            testTag = "input_cash_mobile"
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Field 3: Tontine & Famille / Cercles
        CaisseInputField(
            title = "🤝 Tontines & Argent placé / Famille",
            subtitle = "Fonds en réserve chez des proches ou cotisations",
            value = cashFamilyStr,
            onValueChange = onCashFamilyChange,
            currency = currency,
            testTag = "input_cash_family"
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Live Total Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF131B2E),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, CyberGold)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "TOTAL DISPONIBLE EN CAISSE",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberGold,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${String.format("%,.0f", totalCaisse)} $currency",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = BrickTextPrimary
                    )
                }

                Text(text = "🛡️", fontSize = 28.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("onboarding_step2_next_btn"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrickPrimary)
        ) {
            Text("Enregistrer ma caisse", color = Color(0xFF0A0E1A), fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color(0xFF0A0E1A))
        }
    }
}

@Composable
private fun CaisseInputField(
    title: String,
    subtitle: String,
    value: String,
    onValueChange: (String) -> Unit,
    currency: String,
    testTag: String
) {
    Surface(
        color = BrickSurface,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BrickSurfaceBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = BrickTextPrimary)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = BrickTextMuted, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = if (value == "0") "" else value,
                onValueChange = { newVal ->
                    val filtered = newVal.filter { it.isDigit() }
                    onValueChange(if (filtered.isEmpty()) "0" else filtered)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(testTag),
                placeholder = { Text("0 $currency", color = BrickTextMuted) },
                trailingIcon = { Text(currency, color = BrickPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 12.dp)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrickPrimary,
                    unfocusedBorderColor = BrickSurfaceBorder,
                    focusedTextColor = BrickTextPrimary,
                    unfocusedTextColor = BrickTextPrimary
                )
            )
        }
    }
}

@Composable
private fun StepThreeChargesEtAlertes(
    currency: String,
    fixedChargesStr: String,
    onFixedChargesChange: (String) -> Unit,
    reminderHourStr: String,
    onReminderHourChange: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Charges fixes & Heure du bilan",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = BrickTextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Facultatif : anticipez vos dépenses incompressibles du mois",
            style = MaterialTheme.typography.bodySmall,
            color = BrickTextMuted,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = if (fixedChargesStr == "0") "" else fixedChargesStr,
            onValueChange = { newVal ->
                val filtered = newVal.filter { it.isDigit() }
                onFixedChargesChange(if (filtered.isEmpty()) "0" else filtered)
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("onboarding_fixed_charges_input"),
            label = { Text("Total des charges fixes mensuelles ($currency)", color = BrickTextMuted) },
            placeholder = { Text("Ex: 50000 (loyer, factures)", color = BrickTextMuted) },
            leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = BrickSecondary) },
            trailingIcon = { Text(currency, color = BrickTextMuted, modifier = Modifier.padding(end = 12.dp)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrickPrimary,
                unfocusedBorderColor = BrickSurfaceBorder,
                focusedTextColor = BrickTextPrimary,
                unfocusedTextColor = BrickTextPrimary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = reminderHourStr,
            onValueChange = { newVal ->
                val filtered = newVal.filter { it.isDigit() }
                if (filtered.length <= 2) {
                    onReminderHourChange(filtered)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("onboarding_reminder_hour_input"),
            label = { Text("Heure du point de caisse du soir (0-23h)", color = BrickTextMuted) },
            leadingIcon = { Icon(Icons.Default.Notifications, contentDescription = null, tint = CyberGold) },
            trailingIcon = { Text("h00", color = BrickTextMuted, modifier = Modifier.padding(end = 12.dp)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrickPrimary,
                unfocusedBorderColor = BrickSurfaceBorder,
                focusedTextColor = BrickTextPrimary,
                unfocusedTextColor = BrickTextPrimary
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 50/30/20 summary reminder
        Surface(
            color = BrickSurfaceElevated,
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BrickSurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "🏛️ Principe de gestion BRICK 50/30/20",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = BrickTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "• 50% Besoins vitaux (Alimentation, transport, toit)\n• 30% Envies (Plaisirs maîtrisés)\n• 20% Épargne & Tontine (Sécurité et investissement)",
                    style = MaterialTheme.typography.bodySmall,
                    color = BrickTextMuted,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("onboarding_step3_next_btn"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrickPrimary)
        ) {
            Text("Voir le récapitulatif de ma caisse", color = Color(0xFF0A0E1A), fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color(0xFF0A0E1A))
        }
    }
}

@Composable
private fun StepFourConfirmationCaisse(
    name: String,
    currency: String,
    totalCaisse: Double,
    cashLiquide: Double,
    cashMobile: Double,
    cashFamily: Double,
    fixedCharges: Double,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Big Celebration & Confirmation Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF131B2E),
            border = androidx.compose.foundation.BorderStroke(
                width = 2.dp,
                brush = Brush.horizontalGradient(listOf(BrickPrimary, CyberGold))
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "🛡️", fontSize = 44.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Voilà ce qui est dans ta caisse au départ !",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrickPrimary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "${String.format("%,.0f", totalCaisse)} $currency",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = CyberGold,
                    fontSize = 28.sp
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = BrickSurfaceBorder)
                Spacer(modifier = Modifier.height(14.dp))

                // Breakdown of starting caisse
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "💵 Espèces / En poche :", style = MaterialTheme.typography.bodySmall, color = BrickTextMuted)
                    Text(text = "${String.format("%,.0f", cashLiquide)} $currency", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = BrickTextPrimary)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "📱 Mobile Money & Banque :", style = MaterialTheme.typography.bodySmall, color = BrickTextMuted)
                    Text(text = "${String.format("%,.0f", cashMobile)} $currency", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = BrickTextPrimary)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "🤝 Tontines & Famille :", style = MaterialTheme.typography.bodySmall, color = BrickTextMuted)
                    Text(text = "${String.format("%,.0f", cashFamily)} $currency", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = BrickTextPrimary)
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Pedagogical reminder card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = BrickSurfaceElevated,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BrickSurfaceBorder)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(text = "✨", fontSize = 22.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Votre caisse de départ est prête !",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldSuccess
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Dès qu'un paiement, salaire ou vente arrive, cliquez sur ➕ pour l'ajouter en 2 secondes. Votre caisse restera toujours exacte et votre reste à vivre calculé en temps réel !",
                        style = MaterialTheme.typography.bodySmall,
                        color = BrickTextMuted,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("onboarding_complete_btn"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
        ) {
            Text(
                text = "Valider et entrer dans ma forteresse 🚀",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}
