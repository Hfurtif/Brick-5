package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.FinancialSnapshot
import com.example.data.model.TransactionEntity
import com.example.data.model.UserProfile
import com.example.ui.components.DonutBreakdownChart
import com.example.ui.components.ExplanationCalloutCard
import com.example.ui.components.NeonGlassCard
import com.example.ui.components.SpendingTrendCurve
import com.example.ui.components.WeeklyHeatmap
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BrickBackground
import com.example.ui.theme.BrickSurfaceBorder
import com.example.ui.theme.BrickSurfaceElevated
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.CyberGold
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import kotlin.math.max

@Composable
fun BudgetAnalyticsScreen(
    user: UserProfile,
    snapshot: FinancialSnapshot,
    transactions: List<TransactionEntity>,
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
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Budgets & Analyses",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextWhite
            )
            Text(
                text = "Comprendre où part chaque euro sans jargon",
                fontSize = 12.sp,
                color = TextMuted
            )
        }

        // Donut Chart: 50/30/20 breakdown
        item {
            NeonGlassCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "RÉPARTITION GLOBALE (50 / 30 / 20)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    DonutBreakdownChart(
                        needsAmount = snapshot.needsSpent,
                        wantsAmount = snapshot.wantsSpent,
                        savingsAmount = snapshot.savingsSpent,
                        currency = user.currency
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "💡 Explication : Tes besoins occupent ${snapshot.needsPercentageActual}% de ton budget, tes envies ${snapshot.wantsPercentageActual}%, et ton épargne ${snapshot.savingsPercentageActual}%.",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }
        }

        // Spending vs Budget Curve
        item {
            NeonGlassCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "ÉVOLUTION DES DÉPENSES CUMULÉES",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberGold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Comparaison en direct avec ton plafond mensuel de revenus",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    SpendingTrendCurve(
                        transactions = transactions,
                        monthlyBudget = user.monthlySalary,
                        currency = user.currency
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "📈 Explication : Tant que la courbe néon reste sous la ligne rouge, ton mois est équilibré. Si la trajectoire monte trop vite, ralentis les envies.",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }
        }

        // Weekly Spending Heatmap
        item {
            NeonGlassCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "CARTE DE CHALEUR HEBDOMADAIRE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Quels sont les jours où tu dépenses le plus ?",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    WeeklyHeatmap(transactions = transactions)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "🗓️ Explication : Les couleurs chaudes indiquent tes pics de sorties. Prévois un budget fixe avant le week-end pour éviter les dérapages.",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }
        }

        // Category Budgets Gauges (80% and 100% alerts)
        item {
            NeonGlassCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "LIMITES PAR CATÉGORIE & ALERTES",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Aggregate spent by category
                    val categorySums = transactions.groupBy { it.category }
                        .mapValues { entry -> entry.value.sumOf { it.amount } }

                    // Example realistic limits based on salary
                    val categoriesWithLimits = listOf(
                        Triple("Logement", categorySums["Logement"] ?: 0.0, user.monthlySalary * 0.35),
                        Triple("Alimentation", categorySums["Alimentation"] ?: 0.0, user.monthlySalary * 0.20),
                        Triple("Sorties & Loisirs", categorySums["Sorties & Loisirs"] ?: 0.0, user.monthlySalary * 0.12),
                        Triple("Transports", categorySums["Transports"] ?: 0.0, user.monthlySalary * 0.10),
                        Triple("Shopping", categorySums["Shopping"] ?: 0.0, user.monthlySalary * 0.08),
                        Triple("Abonnements", categorySums["Abonnements"] ?: 0.0, 50.0)
                    )

                    categoriesWithLimits.forEach { (cat, spent, limit) ->
                        CategoryGaugeItem(
                            category = cat,
                            spent = spent,
                            limit = limit,
                            currency = user.currency
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }

        // "Ce que ça veut dire" Block
        item {
            ExplanationCalloutCard(text = snapshot.plainFrenchSummary)
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
fun CategoryGaugeItem(
    category: String,
    spent: Double,
    limit: Double,
    currency: String
) {
    val ratio = (spent / max(1.0, limit)).toFloat()
    val progress = ratio.coerceIn(0f, 1f)
    val isOver = ratio >= 1.0f
    val isWarning = ratio >= 0.8f && ratio < 1.0f

    val statusColor = when {
        isOver -> CoralDanger
        isWarning -> AmberWarning
        else -> EmeraldSuccess
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = category,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextWhite
                )
                if (isOver) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Dépassement",
                        tint = CoralDanger,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Text(
                text = "${spent.toInt()} / ${limit.toInt()} $currency (${(ratio * 100).toInt()}%)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = statusColor
            )
        }

        Spacer(modifier = Modifier.height(5.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = statusColor,
            trackColor = BrickSurfaceBorder
        )
    }
}
