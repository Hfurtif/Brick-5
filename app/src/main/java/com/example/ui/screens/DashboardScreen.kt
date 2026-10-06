package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.FinancialSnapshot
import com.example.data.model.TransactionEntity
import com.example.data.model.UserProfile
import com.example.ui.components.BrickLogo
import com.example.ui.components.ExplanationCalloutCard
import com.example.ui.components.HealthScoreGauge
import com.example.ui.components.NeonGlassCard
import com.example.ui.components.OnlineStatusBadge
import com.example.ui.components.PrivacyAmountText
import com.example.ui.components.StreakPill
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BrickBackground
import com.example.ui.theme.BrickSurfaceBorder
import com.example.ui.theme.BrickSurfaceElevated
import com.example.ui.theme.CoralDanger
import kotlin.math.max
import com.example.ui.theme.CyberGold
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NeedColor
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.NeonVioletGlow
import com.example.ui.theme.SaveColor
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.WantColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    user: UserProfile,
    snapshot: FinancialSnapshot,
    transactions: List<TransactionEntity>,
    todayTotal: Double = 0.0,
    isOnline: Boolean,
    onToggleHideAmounts: () -> Unit,
    onOpenQuickAdd: () -> Unit,
    onOpenAddExpense: () -> Unit,
    onOpenEveningRecap: () -> Unit,
    onOpenSimulator: () -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BrickBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BrickLogo(size = 38.dp, showText = true)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OnlineStatusBadge(isOnline = isOnline)
                    StreakPill(streakDays = user.streakDays)
                    IconButton(
                        onClick = onToggleHideAmounts,
                        modifier = Modifier.testTag("toggle_privacy_btn")
                    ) {
                        Icon(
                            imageVector = if (user.hideAmounts) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Masquer montants",
                            tint = TextMuted
                        )
                    }
                }
            }
        }

        // Hero Card: Reste à vivre & Solde
        item {
            NeonGlassCard(
                borderColor = if (snapshot.isOverBudget) CoralDanger else ElectricCyan
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "RESTE À VIVRE (CE MOIS)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (snapshot.isOverBudget) CoralDanger else ElectricCyan,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            PrivacyAmountText(
                                amount = snapshot.remainingBudget,
                                currency = user.currency,
                                hideAmounts = user.hideAmounts,
                                fontSize = 32,
                                color = if (snapshot.isOverBudget) CoralDanger else TextWhite
                            )
                        }

                        // Badge status
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (snapshot.isOverBudget) CoralDanger.copy(alpha = 0.2f)
                                    else EmeraldSuccess.copy(alpha = 0.2f)
                                )
                                .border(
                                    1.dp,
                                    if (snapshot.isOverBudget) CoralDanger else EmeraldSuccess,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (snapshot.isOverBudget) "Dépassement" else "Dans le vert",
                                color = if (snapshot.isOverBudget) CoralDanger else EmeraldSuccess,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Daily allowance row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(BrickSurfaceElevated)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Budget quotidien conseillé",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                            Text(
                                text = if (user.hideAmounts) "•• ${user.currency}/jour" else "${snapshot.dailyAllowanceRemaining.toInt()} ${user.currency} / jour",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberGold
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Dépensé ce mois",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                            Text(
                                text = if (user.hideAmounts) "••••" else "${snapshot.totalExpenses.toInt()} ${user.currency} / ${user.monthlySalary.toInt()} ${user.currency}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextWhite
                            )
                        }
                    }
                }
            }
        }

        // Ultra-Fast 5-Second Saisie Éclair Button
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(ElectricCyan, NeonViolet)
                        )
                    )
                    .clickable { onOpenQuickAdd() }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .testTag("ultra_fast_add_banner")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(BrickBackground)
                                .border(1.dp, ElectricCyan, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "⚡ SAISIE ÉCLAIR (5 SEC)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BrickBackground,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Dis ou tape : 'piment 50', 'taxi 500'...",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BrickBackground.copy(alpha = 0.85f)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(BrickBackground.copy(alpha = 0.25f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Auto-IA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrickBackground
                        )
                    }
                }
            }
        }

        // Secondary Action Row (Bilan Soir, Saisie Détail, Besoin ou Envie)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpenEveningRecap,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("open_evening_recap_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrickSurfaceElevated),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.horizontalGradient(listOf(CyberGold, NeonVioletGlow))
                    )
                ) {
                    Icon(Icons.Default.NightsStay, contentDescription = null, tint = CyberGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Bilan Soir",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                Button(
                    onClick = onOpenSimulator,
                    modifier = Modifier
                        .weight(1.2f)
                        .height(46.dp)
                        .testTag("open_simulator_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrickSurfaceElevated)
                ) {
                    Icon(Icons.Default.Psychology, contentDescription = null, tint = NeonVioletGlow, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Besoin/Envie ?",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                Button(
                    onClick = onOpenAddExpense,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("add_expense_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrickSurfaceElevated)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Détaillé",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Financial Health Score Card
        item {
            NeonGlassCard(borderColor = NeonViolet.copy(alpha = 0.4f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HealthScoreGauge(
                        score = snapshot.healthScore,
                        grade = snapshot.scoreGrade
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "SCORE FINANCIER BRICK",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonVioletGlow,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = snapshot.scoreGrade,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = snapshot.scoreSummary,
                            fontSize = 12.sp,
                            color = TextMuted,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // 50/30/20 Rule Progress
        item {
            NeonGlassCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RÈGLE 50 / 30 / 20",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberGold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Besoins vs Envies",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Needs Bar (50%)
                    BudgetProgressBar(
                        title = "Besoins indispensables",
                        current = snapshot.needsSpent,
                        target = snapshot.needsTarget,
                        actualPercent = snapshot.needsPercentageActual,
                        targetPercent = 50,
                        color = NeedColor,
                        currency = user.currency
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Wants Bar (30%)
                    BudgetProgressBar(
                        title = "Envies & Loisirs",
                        current = snapshot.wantsSpent,
                        target = snapshot.wantsTarget,
                        actualPercent = snapshot.wantsPercentageActual,
                        targetPercent = 30,
                        color = WantColor,
                        currency = user.currency
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Savings Bar (20%)
                    BudgetProgressBar(
                        title = "Épargne & Actifs (Se payer en 1er)",
                        current = snapshot.savingsSpent,
                        target = snapshot.savingsTarget,
                        actualPercent = snapshot.savingsPercentageActual,
                        targetPercent = 20,
                        color = SaveColor,
                        currency = user.currency
                    )
                }
            }
        }

        // "Ce que ça veut dire"
        item {
            ExplanationCalloutCard(text = snapshot.plainFrenchSummary)
        }

        // Recent Transactions Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dernières Dépenses",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                Text(
                    text = "${transactions.size} opérations",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        }

        // Transactions list items
        if (transactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aucune dépense enregistrée. Clique sur 'Saisie Dépense' !",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            items(transactions.take(8), key = { it.id }) { tx ->
                TransactionCardItem(
                    transaction = tx,
                    currency = user.currency,
                    hideAmounts = user.hideAmounts,
                    onDelete = { onDeleteTransaction(tx) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
fun BudgetProgressBar(
    title: String,
    current: Double,
    target: Double,
    actualPercent: Int,
    targetPercent: Int,
    color: Color,
    currency: String
) {
    val progress = (current / max(1.0, target)).toFloat().coerceIn(0f, 1f)
    val isOver = current > target

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextWhite
            )
            Text(
                text = "${current.toInt()} / ${target.toInt()} $currency ($actualPercent%)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isOver) CoralDanger else color
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = if (isOver) CoralDanger else color,
            trackColor = BrickSurfaceBorder
        )
    }
}

@Composable
fun TransactionCardItem(
    transaction: TransactionEntity,
    currency: String,
    hideAmounts: Boolean,
    onDelete: () -> Unit
) {
    val typeColor = when (transaction.type.uppercase()) {
        "BESOIN" -> NeedColor
        "ENVIE" -> WantColor
        "EPARGNE", "ACTIF" -> SaveColor
        else -> TextMuted
    }

    val typeLabel = when (transaction.type.uppercase()) {
        "BESOIN" -> "Besoin"
        "ENVIE" -> "Envie"
        "EPARGNE" -> "Épargne"
        else -> transaction.type
    }

    val dateFormat = SimpleDateFormat("dd MMM", Locale.FRENCH)
    val dateStr = dateFormat.format(Date(transaction.timestamp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(BrickSurfaceElevated.copy(alpha = 0.7f))
            .border(1.dp, BrickSurfaceBorder, RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(typeColor.copy(alpha = 0.15f))
                    .border(1.dp, typeColor.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (transaction.category) {
                        "Alimentation" -> "🛒"
                        "Logement" -> "🏠"
                        "Transports" -> "🚗"
                        "Sorties & Loisirs" -> "🍸"
                        "Shopping" -> "🛍️"
                        "Abonnements" -> "📱"
                        "Santé" -> "💊"
                        "Investissement" -> "📈"
                        else -> "💳"
                    },
                    fontSize = 18.sp
                )
            }

            Column {
                Text(
                    text = transaction.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextWhite
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "$dateStr • ${transaction.category}",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(typeColor.copy(alpha = 0.2f))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = typeLabel,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = typeColor
                        )
                    }
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = if (hideAmounts) "•• $currency" else "-${String.format("%.2f", transaction.amount)} $currency",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (transaction.type == "ENVIE") WantColor else TextWhite
            )

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Supprimer",
                    tint = TextDark,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
