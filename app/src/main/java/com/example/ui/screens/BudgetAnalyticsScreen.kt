package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.Donut503020Chart
import com.example.ui.components.Donut503020Mode
import com.example.ui.components.FinancialProgressBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.*

@Composable
fun BudgetAnalyticsScreen(
    viewModel: Budget503020ViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var chartMode by remember { mutableStateOf(Donut503020Mode.ACTUAL_SPENT) }
    var showCustomIncomeDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BrickBackground),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 640.dp)
                .fillMaxHeight(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Règle 50/30/20",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = BrickTextPrimary
                        )
                        Text(
                            text = "Besoins (50%) • Envies (30%) • Épargne (20%)",
                            style = MaterialTheme.typography.bodySmall,
                            color = BrickTextMuted
                        )
                    }

                    Surface(
                        modifier = Modifier.clickable { showCustomIncomeDialog = true },
                        color = BrickSurfaceElevated,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, BrickSurfaceBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = BrickPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (uiState.isCustomIncome) "Revenu ajusté" else "Ajuster base",
                                style = MaterialTheme.typography.labelSmall,
                                color = BrickPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Alerts Banners
            if (uiState.activeAlerts.isNotEmpty()) {
                items(uiState.activeAlerts, key = { it.id }) { alert ->
                    val isExceeded = alert.severity == BudgetAlertSeverity.EXCEEDED
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = if (isExceeded) CoralDanger.copy(alpha = 0.15f) else AmberWarning.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, if (isExceeded) CoralDanger else AmberWarning)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(text = if (isExceeded) "🚨" else "⚠️", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = alert.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isExceeded) CoralDanger else AmberWarning
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = alert.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = BrickTextPrimary,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "💡 ${alert.recommendation}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BrickTextMuted,
                                    fontSize = 11.sp
                                )
                            }
                            IconButton(
                                onClick = { viewModel.dismissAlert(alert.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Masquer", tint = BrickTextMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // Donut Chart Container
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = BrickSurfaceElevated,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, BrickSurfaceBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Toggle Chart Mode (Dépensé Réel vs Règle Cible)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(BrickSurface)
                                .padding(3.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable { chartMode = Donut503020Mode.ACTUAL_SPENT },
                                color = if (chartMode == Donut503020Mode.ACTUAL_SPENT) BrickPrimary else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "Dépensé Réel",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (chartMode == Donut503020Mode.ACTUAL_SPENT) Color(0xFF0A0E1A) else BrickTextMuted
                                    )
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable { chartMode = Donut503020Mode.TARGET_ALLOCATION },
                                color = if (chartMode == Donut503020Mode.TARGET_ALLOCATION) BrickPrimary else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "Règle 50/30/20 Cible",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (chartMode == Donut503020Mode.TARGET_ALLOCATION) Color(0xFF0A0E1A) else BrickTextMuted
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Donut503020Chart(
                            needs = uiState.needs,
                            wants = uiState.wants,
                            savings = uiState.savings,
                            currency = uiState.currency,
                            mode = chartMode
                        )
                    }
                }
            }

            // Buckets Breakdown Details (Needs, Wants, Savings)
            item {
                Text(
                    text = "Détail des Enveloppes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrickTextPrimary
                )
            }

            // Needs 50%
            item {
                BucketCard(
                    breakdown = uiState.needs,
                    title = "Besoins Vitaux (50%)",
                    description = "Nourriture, loyer, transport, électricité, santé",
                    color = NeedColor,
                    currency = uiState.currency
                )
            }

            // Wants 30%
            item {
                BucketCard(
                    breakdown = uiState.wants,
                    title = "Envies & Confort (30%)",
                    description = "Sorties, restaurants, shopping, loisirs",
                    color = WantColor,
                    currency = uiState.currency
                )
            }

            // Savings 20%
            item {
                BucketCard(
                    breakdown = uiState.savings,
                    title = "Épargne & Dettes (20%)",
                    description = "Tontine, réserve de sécurité, investissement, remboursement",
                    color = SaveColor,
                    currency = uiState.currency
                )
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    if (showCustomIncomeDialog) {
        CustomIncomeDialog(
            currentIncome = uiState.monthlyIncome,
            currency = uiState.currency,
            onDismiss = { showCustomIncomeDialog = false },
            onConfirm = { newIncome ->
                viewModel.setCustomIncome(newIncome)
                showCustomIncomeDialog = false
            },
            onReset = {
                viewModel.clearCustomIncome()
                showCustomIncomeDialog = false
            }
        )
    }
}

@Composable
private fun BucketCard(
    breakdown: BudgetBucketBreakdown,
    title: String,
    description: String,
    color: Color,
    currency: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = BrickSurfaceElevated,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (breakdown.isExceeded) CoralDanger else BrickSurfaceBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(color)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = BrickTextPrimary)
                }

                if (breakdown.isExceeded) {
                    Surface(
                        color = CoralDanger.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "DÉPASSÉ 🚨",
                            color = CoralDanger,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(description, style = MaterialTheme.typography.bodySmall, color = BrickTextMuted, fontSize = 11.sp)

            Spacer(modifier = Modifier.height(12.dp))

            FinancialProgressBar(
                ratio = breakdown.ratio,
                color = if (breakdown.isExceeded) CoralDanger else color,
                height = 8.dp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Dépensé", style = MaterialTheme.typography.labelSmall, color = BrickTextMuted, fontSize = 10.sp)
                    Text(
                        "${String.format("%,.0f", breakdown.spentAmount)} $currency",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (breakdown.isExceeded) CoralDanger else BrickTextPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Plafond alloué", style = MaterialTheme.typography.labelSmall, color = BrickTextMuted, fontSize = 10.sp)
                    Text(
                        "${String.format("%,.0f", breakdown.targetAmount)} $currency",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = BrickTextMuted
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        if (breakdown.isExceeded) "Dépassement" else "Reste disponible",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (breakdown.isExceeded) CoralDanger else EmeraldSuccess,
                        fontSize = 10.sp
                    )
                    Text(
                        "${String.format("%,.0f", if (breakdown.isExceeded) -breakdown.remainingAmount else breakdown.remainingAmount)} $currency",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (breakdown.isExceeded) CoralDanger else EmeraldSuccess
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomIncomeDialog(
    currentIncome: Double,
    currency: String,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit,
    onReset: () -> Unit
) {
    var inputStr by remember { mutableStateOf(if (currentIncome > 0) currentIncome.toInt().toString() else "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Ajuster la base de calcul", fontWeight = FontWeight.Bold, color = BrickTextPrimary)
        },
        text = {
            Column {
                Text(
                    "Définissez un montant de base mensuel pour projeter les allocations 50/30/20, ou réinitialisez pour utiliser automatiquement le cash réel de votre caisse.",
                    style = MaterialTheme.typography.bodySmall,
                    color = BrickTextMuted
                )
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = inputStr,
                    onValueChange = { newVal ->
                        if (newVal.all { it.isDigit() }) inputStr = newVal
                    },
                    label = { Text("Base de calcul mensuelle ($currency)", color = BrickTextMuted) },
                    trailingIcon = { Text(currency, color = BrickPrimary, modifier = Modifier.padding(end = 12.dp)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrickPrimary,
                        unfocusedBorderColor = BrickSurfaceBorder,
                        focusedTextColor = BrickTextPrimary,
                        unfocusedTextColor = BrickTextPrimary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = inputStr.toDoubleOrNull() ?: 0.0
                    onConfirm(amount)
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrickPrimary)
            ) {
                Text("Valider", color = Color(0xFF0A0E1A), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onReset) {
                Text("Réinitialiser (Auto-Caisse)", color = BrickTextMuted)
            }
        },
        containerColor = BrickSurface
    )
}
