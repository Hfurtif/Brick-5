package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.FinancialSnapshot
import com.example.data.model.TransactionEntity
import com.example.data.model.UserProfile
import com.example.ui.components.FormattedAmountText
import com.example.ui.components.HealthScoreGauge
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    user: UserProfile?,
    snapshot: FinancialSnapshot,
    transactions: List<TransactionEntity>,
    todayTotalSpent: Double,
    onToggleHideAmounts: () -> Unit,
    onQuickAdd: () -> Unit,
    onAddExpense: () -> Unit,
    onAddIncome: () -> Unit,
    onOpenEveningRecap: () -> Unit,
    onOpenSimulator: () -> Unit,
    onNavigateToBudget: () -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val currency = user?.currency ?: "FCFA"
    val hideAmounts = user?.hideAmounts ?: false

    // Calculate total cash in caisse (all incomes - all expenses)
    val totalCaisse = remember(transactions) {
        val totalIn = transactions.filter { it.type == "REVENU" }.sumOf { it.amount }
        val totalOut = transactions.filter { it.type == "DEPENSE" }.sumOf { it.amount }
        totalIn - totalOut
    }

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
            // Header: User welcome + Privacy toggle
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Bonjour, ${user?.name ?: "Bâtisseur"} 🛡️",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = BrickTextPrimary,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Forteresse financière • 🔥 ${user?.streakDays ?: 1} jours de discipline",
                            style = MaterialTheme.typography.bodySmall,
                            color = BrickTextMuted,
                            fontSize = 12.sp
                        )
                    }

                    IconButton(
                        onClick = onToggleHideAmounts,
                        modifier = Modifier
                            .size(42.dp)
                            .testTag("toggle_privacy_btn")
                    ) {
                        Icon(
                            imageVector = if (hideAmounts) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Confidentialité",
                            tint = BrickPrimary
                        )
                    }
                }
            }

            // Primary Card: Caisse & Reste à Vivre du Jour
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF131B2E),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(
                        width = 1.5.dp,
                        brush = Brush.horizontalGradient(listOf(BrickPrimary, CyberGold))
                    ),
                    tonalElevation = 4.dp
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⚡ RESTE À VIVRE DU JOUR",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = BrickPrimary,
                                letterSpacing = 1.sp,
                                fontSize = 11.sp
                            )
                            Surface(
                                color = EmeraldSuccess.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "EN CAISSE",
                                    color = EmeraldSuccess,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        FormattedAmountText(
                            amount = snapshot.resteAVivreDuJour,
                            currency = currency,
                            hideAmounts = hideAmounts,
                            color = BrickTextPrimary,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 30.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = BrickSurfaceBorder)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Argent Total en Caisse",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BrickTextMuted,
                                    fontSize = 11.sp
                                )
                                FormattedAmountText(
                                    amount = totalCaisse,
                                    currency = currency,
                                    hideAmounts = hideAmounts,
                                    color = CyberGold,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Dépensé aujourd'hui",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BrickTextMuted,
                                    fontSize = 11.sp
                                )
                                FormattedAmountText(
                                    amount = todayTotalSpent,
                                    currency = currency,
                                    hideAmounts = hideAmounts,
                                    color = if (todayTotalSpent > snapshot.resteAVivreDuJour) CoralDanger else BrickTextPrimary,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }

            // Fast Action Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionTile(
                        icon = Icons.Default.FlashOn,
                        title = "Ajout 3s",
                        subtitle = "Ultra rapide",
                        tint = BrickPrimary,
                        onClick = onQuickAdd,
                        modifier = Modifier.weight(1f),
                        testTag = "action_quick_add"
                    )

                    ActionTile(
                        icon = Icons.Default.AddCircle,
                        title = "Dépense",
                        subtitle = "50/30/20",
                        tint = CoralDanger,
                        onClick = onAddExpense,
                        modifier = Modifier.weight(1f),
                        testTag = "action_add_expense"
                    )

                    ActionTile(
                        icon = Icons.Default.AccountBalanceWallet,
                        title = "Caisse +",
                        subtitle = "Entrée d'argent",
                        tint = EmeraldSuccess,
                        onClick = onAddIncome,
                        modifier = Modifier.weight(1f),
                        testTag = "action_add_income"
                    )
                }
            }

            // Secondary Quick Actions (Simulator & Recap)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SecondaryActionTile(
                        icon = "🧠",
                        title = "Le Réfléchisseur",
                        subtitle = "Simuler un achat impulsif",
                        onClick = onOpenSimulator,
                        modifier = Modifier.weight(1f),
                        testTag = "action_simulator"
                    )

                    SecondaryActionTile(
                        icon = "🌙",
                        title = "Bilan du Soir",
                        subtitle = "Récapitulatif du jour",
                        onClick = onOpenEveningRecap,
                        modifier = Modifier.weight(1f),
                        testTag = "action_evening_recap"
                    )
                }
            }

            // Health Score & Budget Teaser Card
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToBudget() },
                    color = BrickSurfaceElevated,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, BrickSurfaceBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HealthScoreGauge(
                            score = snapshot.financialHealthScore,
                            size = 84.dp
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Santé Financière",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = BrickTextPrimary
                                )
                                Text(
                                    text = "Voir 50/30/20 →",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BrickPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Valeur nette : ${if (hideAmounts) "••••••" else String.format("%,.0f %s", snapshot.netWorth, currency)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = BrickTextMuted,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (snapshot.financialHealthScore >= 75) "🛡️ Forteresse stable et saine" else "⚠️ Attention aux petites fuites",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (snapshot.financialHealthScore >= 75) EmeraldSuccess else CyberGold,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Recent Transactions Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Opérations Récentes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BrickTextPrimary
                    )
                    Text(
                        text = "${transactions.size} au total",
                        style = MaterialTheme.typography.labelSmall,
                        color = BrickTextMuted
                    )
                }
            }

            if (transactions.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = BrickSurfaceElevated,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, BrickSurfaceBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "🧱", fontSize = 32.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Aucune transaction enregistrée",
                                style = MaterialTheme.typography.titleSmall,
                                color = BrickTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Ajoutez une dépense ou une rentrée d'argent avec les boutons ci-dessus.",
                                style = MaterialTheme.typography.bodySmall,
                                color = BrickTextMuted,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(transactions.take(12), key = { it.id }) { tx ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = BrickSurfaceElevated,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, BrickSurfaceBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(
                                            if (tx.type == "DEPENSE") CoralDanger.copy(alpha = 0.15f) else EmeraldSuccess.copy(alpha = 0.15f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (tx.type == "DEPENSE") Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                        contentDescription = null,
                                        tint = if (tx.type == "DEPENSE") CoralDanger else EmeraldSuccess,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = tx.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = BrickTextPrimary
                                    )
                                    Text(
                                        text = "${tx.category} • ${if (tx.priority == "NEEDS") "Besoins" else if (tx.priority == "WANTS") "Envies" else "Épargne"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = BrickTextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FormattedAmountText(
                                    amount = tx.amount,
                                    currency = currency,
                                    hideAmounts = hideAmounts,
                                    color = if (tx.type == "DEPENSE") CoralDanger else EmeraldSuccess,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    prefix = if (tx.type == "DEPENSE") "- " else "+ "
                                )

                                IconButton(
                                    onClick = { onDeleteTransaction(tx) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.DeleteOutline,
                                        contentDescription = "Supprimer",
                                        tint = BrickTextMuted.copy(alpha = 0.6f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ActionTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Surface(
        modifier = modifier
            .height(82.dp)
            .clickable { onClick() }
            .testTag(testTag),
        color = BrickSurfaceElevated,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, BrickSurfaceBorder)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = BrickTextPrimary, fontSize = 13.sp)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = BrickTextMuted, fontSize = 9.sp)
        }
    }
}

@Composable
private fun SecondaryActionTile(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Surface(
        modifier = modifier
            .height(64.dp)
            .clickable { onClick() }
            .testTag(testTag),
        color = BrickSurface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, BrickSurfaceBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 22.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = BrickTextPrimary, fontSize = 12.sp)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = BrickTextMuted, fontSize = 10.sp)
            }
        }
    }
}
