package com.example.ui.screens

import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.fragment.app.FragmentActivity
import com.example.security.BiometricAuthManager
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.BrickBadge
import com.example.ai.FinancialSnapshot
import com.example.data.model.TransactionEntity
import com.example.data.model.UserProfile
import com.example.ui.components.NeonGlassCard
import com.example.ui.theme.BrickBackground
import com.example.ui.theme.BrickSurfaceBorder
import com.example.ui.theme.BrickSurfaceElevated
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.CyberGold
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.NeonVioletGlow
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun ProfileSecurityScreen(
    user: UserProfile,
    snapshot: FinancialSnapshot,
    transactions: List<TransactionEntity>,
    badges: List<BrickBadge>,
    onUpdateProfile: (UserProfile) -> Unit,
    onResetData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showResetDialog by remember { mutableStateOf(false) }

    var name by remember(user.name) { mutableStateOf(user.name) }
    var salaryStr by remember(user.monthlySalary) { mutableStateOf(user.monthlySalary.toInt().toString()) }
    var currency by remember(user.currency) { mutableStateOf(user.currency) }
    var payDayStr by remember(user.payDayOfMonth) { mutableStateOf(user.payDayOfMonth.toString()) }
    var reminderHourStr by remember(user.dailyReminderHour) { mutableStateOf(user.dailyReminderHour.toString()) }
    var pinCode by remember(user.pinCode) { mutableStateOf(user.pinCode) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BrickBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Profil & Sécurité",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextWhite
            )
            Text(
                text = "Gère tes paramètres, tes badges et la sécurité de tes données",
                fontSize = 12.sp,
                color = TextMuted
            )
        }

        // Badges Section
        item {
            NeonGlassCard(borderColor = CyberGold.copy(alpha = 0.4f)) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "BADGES D'ACCOMPLISSEMENT",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberGold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${badges.count { it.isUnlocked }} / ${badges.size} débloqués",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    badges.forEach { badge ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (badge.isUnlocked) CyberGold.copy(alpha = 0.2f)
                                        else BrickSurfaceElevated
                                    )
                                    .border(
                                        1.dp,
                                        if (badge.isUnlocked) CyberGold else BrickSurfaceBorder,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (badge.isUnlocked) badge.icon else "🔒",
                                    fontSize = 16.sp
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = badge.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (badge.isUnlocked) TextWhite else TextMuted
                                )
                                Text(
                                    text = badge.description,
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        // Financial Profile Configuration
        item {
            NeonGlassCard {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "CONFIGURATION FINANCIÈRE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan,
                        letterSpacing = 1.sp
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Prénom") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = BrickSurfaceBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = salaryStr,
                            onValueChange = { salaryStr = it },
                            label = { Text("Salaire mensuel") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = BrickSurfaceBorder
                            ),
                            modifier = Modifier.weight(2f)
                        )

                        OutlinedTextField(
                            value = currency,
                            onValueChange = { currency = it },
                            label = { Text("Devise") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = BrickSurfaceBorder
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Quick currency selection chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("FCFA", "€", "$", "CHF", "£").forEach { cur ->
                            val isSel = currency.trim().equals(cur, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) ElectricCyan else BrickSurfaceElevated)
                                    .border(1.dp, if (isSel) ElectricCyan else BrickSurfaceBorder, RoundedCornerShape(8.dp))
                                    .clickable { currency = cur },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cur,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) BrickBackground else TextWhite,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = payDayStr,
                            onValueChange = { payDayStr = it },
                            label = { Text("Jour de paie (1-31)") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = BrickSurfaceBorder
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = reminderHourStr,
                            onValueChange = { reminderHourStr = it },
                            label = { Text("Rappel dépenses (ex: 20h)") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = BrickSurfaceBorder
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Button(
                        onClick = {
                            val sal = salaryStr.toDoubleOrNull() ?: user.monthlySalary
                            val pd = payDayStr.toIntOrNull() ?: user.payDayOfMonth
                            val rh = reminderHourStr.toIntOrNull() ?: user.dailyReminderHour
                            onUpdateProfile(
                                user.copy(
                                    name = name,
                                    monthlySalary = sal,
                                    currency = currency,
                                    payDayOfMonth = pd,
                                    dailyReminderHour = rh
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_profile_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, tint = BrickBackground)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Enregistrer les modifications", color = BrickBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Appearance & Dynamic Theme Card
        item {
            NeonGlassCard(borderColor = ElectricCyan.copy(alpha = 0.4f)) {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "APPARENCE & THÈME VISUEL",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "BRICK s'adapte automatiquement au thème clair ou sombre de votre smartphone tout en préservant son esthétique néon distinctive.",
                        fontSize = 12.sp,
                        color = TextMuted,
                        lineHeight = 16.sp
                    )

                    val themeOptions = listOf(
                        Triple("SYSTEM", "📱 Auto", "Suit le système"),
                        Triple("DARK", "🌙 Sombre", "Cyber-Dark"),
                        Triple("LIGHT", "☀️ Clair", "Cyber-Light")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        themeOptions.forEach { (mode, label, desc) ->
                            val isSelected = user.themeMode == mode
                            val cardBg = if (isSelected) ElectricCyan.copy(alpha = 0.2f) else BrickSurfaceElevated
                            val borderCol = if (isSelected) ElectricCyan else BrickSurfaceBorder

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(cardBg)
                                    .border(1.dp, borderCol, RoundedCornerShape(12.dp))
                                    .clickable {
                                        onUpdateProfile(user.copy(themeMode = mode))
                                    }
                                    .padding(vertical = 10.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) ElectricCyan else TextWhite
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = desc,
                                    fontSize = 10.sp,
                                    color = if (isSelected) ElectricCyan else TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        // Security & Privacy
        item {
            NeonGlassCard(borderColor = NeonViolet.copy(alpha = 0.4f)) {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "SÉCURITÉ & CONFIDENTIALITÉ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonVioletGlow,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Masquer les montants", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextWhite)
                            Text(text = "Protège des regards indiscrets en public", fontSize = 11.sp, color = TextMuted)
                        }
                        Switch(
                            checked = user.hideAmounts,
                            onCheckedChange = { checked ->
                                onUpdateProfile(user.copy(hideAmounts = checked))
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ElectricCyan,
                                checkedTrackColor = BrickSurfaceBorder
                            )
                        )
                    }

                    // Biometric Authentication Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(ElectricCyan.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = null,
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Empreinte & Code Téléphone",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextWhite
                                )
                                Text(
                                    text = "Verrouillage avec les identifiants du smartphone",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }
                        Switch(
                            checked = user.biometricEnabled,
                            onCheckedChange = { checked ->
                                onUpdateProfile(user.copy(biometricEnabled = checked))
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ElectricCyan,
                                checkedTrackColor = BrickSurfaceBorder
                            )
                        )
                    }

                    if (user.biometricEnabled) {
                        Button(
                            onClick = {
                                val activity = context as? FragmentActivity
                                if (activity != null) {
                                    BiometricAuthManager.promptBiometric(
                                        activity = activity,
                                        title = "Test Biométrie BRICK",
                                        subtitle = "Vérification de ton empreinte ou code",
                                        onSuccess = {},
                                        onError = {}
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrickSurfaceElevated),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(ElectricCyan)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(42.dp)
                        ) {
                            Icon(Icons.Default.Fingerprint, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tester l'empreinte / code", color = ElectricCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedTextField(
                        value = pinCode,
                        onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) pinCode = it },
                        label = { Text("Code PIN de verrouillage (4 chiffres)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = NeonVioletGlow,
                            unfocusedBorderColor = BrickSurfaceBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            onUpdateProfile(user.copy(pinCode = pinCode))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrickSurfaceElevated),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(NeonVioletGlow)
                        )
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = NeonVioletGlow)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (pinCode.isBlank()) "Désactiver le PIN" else "Définir le PIN", color = TextWhite)
                    }
                }
            }
        }

        // Android Widgets Info & Quick Action
        item {
            NeonGlassCard(borderColor = ElectricCyan.copy(alpha = 0.4f)) {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "WIDGETS ÉCRAN D'ACCUEIL ANDROID",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Installe les 2 widgets BRICK sur ton écran d'accueil pour suivre tes finances sans ouvrir l'application :",
                        fontSize = 12.sp,
                        color = TextMuted
                    )

                    // Widget 1: Ajout Rapide
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(BrickSurfaceElevated)
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("⚡", fontSize = 20.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text("1. Ajout Dépense Rapide", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            Text("Bouton direct ouvrant la saisie éclair en mode dialogue", fontSize = 11.sp, color = TextMuted)
                        }
                    }

                    // Widget 2: Reste à Vivre du Jour
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(BrickSurfaceElevated)
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("💰", fontSize = 20.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text("2. Reste à Vivre du Jour", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            Text("Affiche ton solde journalier restant, actualisé en continu", fontSize = 11.sp, color = TextMuted)
                        }
                    }

                    Button(
                        onClick = {
                            com.example.widget.BrickResteAVivreWidgetProvider.updateAllWidgets(context)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrickSurfaceElevated),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(ElectricCyan)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Text("Forcer l'actualisation des widgets", color = ElectricCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Export and Reset Section
        item {
            NeonGlassCard {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "EXPORTATION & GESTION DES DONNÉES",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        letterSpacing = 1.sp
                    )

                    Button(
                        onClick = {
                            exportFinancialSummary(context, user, snapshot, transactions)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrickSurfaceElevated)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = ElectricCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Partager le Bilan Financier (Texte / CSV)", color = TextWhite)
                    }

                    Button(
                        onClick = { showResetDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CoralDanger.copy(alpha = 0.15f))
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, tint = CoralDanger)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Réinitialiser toutes les données", color = CoralDanger, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            containerColor = BrickSurfaceElevated,
            title = {
                Text(text = "Confirmer la réinitialisation ?", color = CoralDanger, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "Toutes les transactions, objectifs et profils seront remis à zéro. Cette action est irréversible.",
                    color = TextWhite,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetData()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralDanger)
                ) {
                    Text("Oui, tout effacer", color = TextWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Annuler", color = TextMuted)
                }
            }
        )
    }
}

private fun exportFinancialSummary(
    context: Context,
    user: UserProfile,
    snapshot: FinancialSnapshot,
    transactions: List<TransactionEntity>
) {
    val report = buildString {
        appendLine("=== BILAN FINANCIER BRICK ===")
        appendLine("Utilisateur : ${user.name}")
        appendLine("Salaire mensuel : ${user.monthlySalary} ${user.currency}")
        appendLine("Total Dépenses : ${snapshot.totalExpenses} ${user.currency}")
        appendLine("Reste à Vivre : ${snapshot.remainingBudget} ${user.currency} (${snapshot.dailyAllowanceRemaining.toInt()} ${user.currency}/jour)")
        appendLine("Score Financier : ${snapshot.healthScore}/100 (${snapshot.scoreGrade})")
        appendLine("Répartition 50/30/20 :")
        appendLine(" - Besoins : ${snapshot.needsPercentageActual}% (Cible 50%)")
        appendLine(" - Envies : ${snapshot.wantsPercentageActual}% (Cible 30%)")
        appendLine(" - Épargne & Actifs : ${snapshot.savingsPercentageActual}% (Cible 20%)")
        appendLine("\n--- TRANSACTIONS (CSV) ---")
        appendLine("Titre,Montant,Catégorie,Type,Priorité")
        transactions.forEach {
            appendLine("${it.title},${it.amount},${it.category},${it.type},${it.priority}")
        }
    }

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, report)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Exporter les données BRICK")
    context.startActivity(shareIntent)
}
