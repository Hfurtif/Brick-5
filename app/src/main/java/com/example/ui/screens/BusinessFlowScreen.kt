package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.ui.components.NeonGlassCard
import com.example.ui.components.PrivacyAmountText
import com.example.ui.theme.BrickBackground
import com.example.ui.theme.BrickSurface
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BusinessFlowScreen(
    user: UserProfile,
    snapshot: FinancialSnapshot,
    transactions: List<TransactionEntity>,
    onOpenAddTransactionWithMode: (String) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, ENTREE, INVESTISSEMENT, DON

    // Filter transactions belonging to Business, Investments, and Donations
    val businessFlowTransactions = transactions.filter { t ->
        t.type in listOf("ENTREE", "REVENU", "AFFAIRES_IN", "DON", "DIME", "AUMONE", "INVESTISSEMENT_AFFAIRES", "STOCK", "MATERIEL_AFFAIRES") ||
        t.category in listOf(
            "Vente", "Commerce", "Vente & Commerce", "Prestation", "Prestation & Contrat",
            "Chiffre d'Affaires", "Bénéfice d'Affaires", "Commission & Bonus", "Entrée Diverse",
            "Don", "Dîme", "Dîme (10%)", "Aumône & Charité", "Soutien Famille & Proches", "Don Communautaire", "Charité", "Solidarité",
            "Stock", "Achat Stock & Marchandise", "Outils & Matériel Pro", "Matériel", "Investissement Business", "Marketing & Publicité", "Capital & Associés", "Formation & Savoir"
        )
    }

    val displayList = businessFlowTransactions.filter { t ->
        when (selectedFilter) {
            "ENTREE" -> t.type in listOf("ENTREE", "REVENU", "AFFAIRES_IN") ||
                t.category in listOf("Vente", "Commerce", "Vente & Commerce", "Prestation", "Prestation & Contrat", "Chiffre d'Affaires", "Bénéfice d'Affaires", "Commission & Bonus", "Entrée Diverse")
            "INVESTISSEMENT" -> t.type in listOf("INVESTISSEMENT_AFFAIRES", "STOCK", "MATERIEL_AFFAIRES") ||
                t.category in listOf("Stock", "Achat Stock & Marchandise", "Outils & Matériel Pro", "Matériel", "Investissement Business", "Marketing & Publicité", "Capital & Associés", "Formation & Savoir")
            "DON" -> t.type in listOf("DON", "DIME", "AUMONE") ||
                t.category in listOf("Don", "Dîme", "Dîme (10%)", "Aumône & Charité", "Soutien Famille & Proches", "Don Communautaire", "Charité", "Solidarité")
            else -> true
        }
    }

    val totalInflow = snapshot.totalBusinessInflow
    val totalInvest = snapshot.totalBusinessInvestments
    val totalDons = snapshot.totalDonations
    val netProfit = snapshot.netBusinessProfit
    val tithePercentage = if (totalInflow > 0) (totalDons / totalInflow) * 100 else 0.0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BrickBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Header
        item {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "AFFAIRES, DONS & INVESTISSEMENTS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Pôle Entrepreneurs & Commerce",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextWhite
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(CyberGold.copy(alpha = 0.15f))
                            .border(1.dp, CyberGold.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Pro & Perso",
                            color = CyberGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Summary Card: Business Profit & Cash Flow
        item {
            NeonGlassCard(borderColor = ElectricCyan.copy(alpha = 0.4f)) {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "RÉSULTAT NET DES AFFAIRES CE MOIS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PrivacyAmountText(
                            amount = netProfit,
                            currency = user.currency,
                            hideAmounts = user.hideAmounts,
                            fontSize = 30,
                            color = if (netProfit >= 0) EmeraldSuccess else CoralDanger
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (netProfit >= 0) EmeraldSuccess.copy(alpha = 0.2f) else CoralDanger.copy(alpha = 0.2f)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (netProfit >= 0) "Bénéfice Positif" else "Déficit Temporaire",
                                color = if (netProfit >= 0) EmeraldSuccess else CoralDanger,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // 3 Metric Pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Entrées / CA
                        MetricSubCard(
                            label = "Entrées / Ventes",
                            amount = totalInflow,
                            currency = user.currency,
                            hideAmounts = user.hideAmounts,
                            color = EmeraldSuccess,
                            icon = "💰",
                            modifier = Modifier.weight(1f)
                        )

                        // Investissements
                        MetricSubCard(
                            label = "Invest. Business",
                            amount = totalInvest,
                            currency = user.currency,
                            hideAmounts = user.hideAmounts,
                            color = NeonVioletGlow,
                            icon = "📦",
                            modifier = Modifier.weight(1f)
                        )

                        // Dons & Dîmes
                        MetricSubCard(
                            label = "Dons & Dîmes",
                            amount = totalDons,
                            currency = user.currency,
                            hideAmounts = user.hideAmounts,
                            color = CyberGold,
                            icon = "🕊️",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Tithing progress bar (Dîme 10% indicator)
                    if (totalInflow > 0) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Objectif Dîme / Solidarité (10% du CA)",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                                Text(
                                    text = "${String.format("%.1f", tithePercentage)}% atteint",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (tithePercentage >= 10.0) CyberGold else TextWhite
                                )
                            }
                            LinearProgressIndicator(
                                progress = { (tithePercentage / 10.0).toFloat().coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = CyberGold,
                                trackColor = BrickSurface
                            )
                        }
                    }
                }
            }
        }

        // Action Buttons Row (Add Entrée, Add Investissement, Add Don)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "ACTIONS RAPIDES AFFAIRES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Button Entrée
                    Button(
                        onClick = { onOpenAddTransactionWithMode("ENTREE") },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("add_business_inflow_btn")
                    ) {
                        Text("💰 + Entrée", color = BrickBackground, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                    }

                    // Button Investissement
                    Button(
                        onClick = { onOpenAddTransactionWithMode("INVESTISSEMENT") },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("add_business_invest_btn")
                    ) {
                        Text("📦 + Investir", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    // Button Don / Dîme
                    Button(
                        onClick = { onOpenAddTransactionWithMode("DON") },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberGold),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("add_donation_btn")
                    ) {
                        Text("🕊️ + Don", color = BrickBackground, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Golden Rules for Business & Giving
        item {
            NeonGlassCard(borderColor = CyberGold.copy(alpha = 0.3f)) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Lightbulb, contentDescription = null, tint = CyberGold, modifier = Modifier.size(18.dp))
                        Text(
                            text = "RÈGLES D'OR DU COMMERCE & DE LA PROSPÉRITÉ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberGold
                        )
                    }

                    Text(
                        text = "1. Sépare ta caisse d'affaires de tes dépenses personnelles : verse-toi un salaire régulier plutôt que de puiser dans le tiroir-caisse.",
                        fontSize = 12.sp,
                        color = TextWhite,
                        lineHeight = 16.sp
                    )
                    Text(
                        text = "2. Réinvestis au minimum 20% des bénéfices dans tes stocks et outils de travail pour démultiplier tes ventes futures.",
                        fontSize = 12.sp,
                        color = TextWhite,
                        lineHeight = 16.sp
                    )
                    Text(
                        text = "3. La Dîme (10%) et le Don : donner protège contre la cupidité et maintient une mentalité d'abondance indispensable pour réussir en affaires.",
                        fontSize = 12.sp,
                        color = TextWhite,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Filter Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "ALL" to "Tous les flux",
                    "ENTREE" to "Entrées (+)",
                    "INVESTISSEMENT" to "Invest. (📦)",
                    "DON" to "Dons (🕊️)"
                ).forEach { (key, label) ->
                    val isSelected = selectedFilter == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = key },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricCyan,
                            selectedLabelColor = BrickBackground,
                            containerColor = BrickSurfaceElevated,
                            labelColor = TextWhite
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) ElectricCyan else BrickSurfaceBorder
                        )
                    )
                }
            }
        }

        // List of Business Transactions
        if (displayList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(BrickSurfaceElevated)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📊", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Aucun flux d'affaires enregistré ce mois",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = "Utilise les boutons ci-dessus pour noter tes ventes, achats de stock ou dons.",
                            fontSize = 12.sp,
                            color = TextMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(displayList, key = { it.id }) { item ->
                BusinessFlowRow(
                    item = item,
                    currency = user.currency,
                    hideAmounts = user.hideAmounts,
                    onDelete = { onDeleteTransaction(item) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
private fun MetricSubCard(
    label: String,
    amount: Double,
    currency: String,
    hideAmounts: Boolean,
    color: Color,
    icon: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(BrickSurfaceElevated)
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = icon, fontSize = 12.sp)
                Text(text = label, fontSize = 10.sp, color = TextMuted, maxLines = 1)
            }
            Spacer(modifier = Modifier.height(4.dp))
            PrivacyAmountText(
                amount = amount,
                currency = currency,
                hideAmounts = hideAmounts,
                fontSize = 13,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun BusinessFlowRow(
    item: TransactionEntity,
    currency: String,
    hideAmounts: Boolean,
    onDelete: () -> Unit
) {
    val isEntree = item.type in listOf("ENTREE", "REVENU", "AFFAIRES_IN") || item.category in listOf("Vente", "Commerce", "Vente & Commerce", "Prestation", "Prestation & Contrat", "Chiffre d'Affaires", "Bénéfice d'Affaires", "Commission & Bonus", "Entrée Diverse")
    val isDon = item.type in listOf("DON", "DIME", "AUMONE") || item.category in listOf("Don", "Dîme", "Dîme (10%)", "Aumône & Charité", "Soutien Famille & Proches", "Don Communautaire", "Charité", "Solidarité")
    val isInvest = item.type in listOf("INVESTISSEMENT_AFFAIRES", "STOCK", "MATERIEL_AFFAIRES") || item.category in listOf("Stock", "Achat Stock & Marchandise", "Outils & Matériel Pro", "Matériel", "Investissement Business", "Marketing & Publicité", "Capital & Associés", "Formation & Savoir")

    val (badgeColor, sign, typeLabel) = when {
        isEntree -> Triple(EmeraldSuccess, "+", "Entrée / Vente")
        isDon -> Triple(CyberGold, "-", "Don / Dîme")
        isInvest -> Triple(NeonVioletGlow, "-", "Invest. Business")
        else -> Triple(ElectricCyan, "-", "Flux")
    }

    val dateStr = SimpleDateFormat("dd MMM • HH:mm", Locale.FRENCH).format(Date(item.timestamp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(BrickSurfaceElevated)
            .border(1.dp, BrickSurfaceBorder, RoundedCornerShape(14.dp))
            .padding(12.dp),
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
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(badgeColor.copy(alpha = 0.15f))
                    .border(1.dp, badgeColor.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when {
                        isEntree -> "💰"
                        isDon -> "🕊️"
                        isInvest -> "📦"
                        else -> "⚡"
                    },
                    fontSize = 16.sp
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = typeLabel,
                        fontSize = 11.sp,
                        color = badgeColor,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(text = "•", fontSize = 11.sp, color = TextDark)
                    Text(text = dateStr, fontSize = 10.sp, color = TextMuted)
                }
                if (item.note.isNotBlank()) {
                    Text(
                        text = item.note,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val isCfa = currency.uppercase().contains("CFA") || currency.uppercase().contains("F")
            val numFormatted = if (isCfa || item.amount % 1.0 == 0.0) {
                String.format(Locale.FRENCH, "%,d", item.amount.toLong()).replace('\u00A0', ' ')
            } else {
                String.format(Locale.FRENCH, "%,.2f", item.amount).replace('\u00A0', ' ')
            }
            val amountFormatted = if (hideAmounts) "•••• $currency" else "$sign $numFormatted $currency"
            Text(
                text = amountFormatted,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = badgeColor
            )

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Supprimer",
                    tint = TextMuted.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
