package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionEntity
import com.example.ui.theme.*

@Composable
fun BusinessFlowScreen(
    transactions: List<TransactionEntity>,
    currency: String,
    onAddBusinessFlow: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("ALL") } // "ALL", "INCOME", "INVEST", "DONATION"

    val businessIncomes = remember(transactions) {
        transactions.filter {
            it.type == "REVENU" && (
                it.category.contains("Vente", ignoreCase = true) ||
                it.category.contains("Business", ignoreCase = true) ||
                it.category.contains("Prestation", ignoreCase = true) ||
                it.category.contains("Commerce", ignoreCase = true)
            )
        }
    }

    val businessExpenses = remember(transactions) {
        transactions.filter {
            it.type == "DEPENSE" && (
                it.category.contains("Business", ignoreCase = true) ||
                it.category.contains("Stock", ignoreCase = true) ||
                it.category.contains("Marchandise", ignoreCase = true) ||
                it.category.contains("Invest", ignoreCase = true)
            )
        }
    }

    val donationExpenses = remember(transactions) {
        transactions.filter {
            it.type == "DEPENSE" && (
                it.category.contains("Don", ignoreCase = true) ||
                it.category.contains("Dîme", ignoreCase = true) ||
                it.category.contains("Aumône", ignoreCase = true) ||
                it.category.contains("Solidarité", ignoreCase = true)
            )
        }
    }

    val totalSales = businessIncomes.sumOf { it.amount }
    val totalInvest = businessExpenses.sumOf { it.amount }
    val totalDonations = donationExpenses.sumOf { it.amount }
    val grossProfit = totalSales - totalInvest - totalDonations

    val filteredList = remember(transactions, selectedFilter) {
        when (selectedFilter) {
            "INCOME" -> businessIncomes
            "INVEST" -> businessExpenses
            "DONATION" -> donationExpenses
            else -> (businessIncomes + businessExpenses + donationExpenses).sortedByDescending { it.timestamp }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BrickBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing),
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
            // Header: Category Super-Title + Wrapped Title + Safe Area
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "AFFAIRES, DONS & INVESTISSEMENTS",
                        style = MaterialTheme.typography.labelSmall,
                        color = BrickPrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pôle Entrepreneurs & Commerce",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = BrickTextPrimary,
                            fontSize = 20.sp,
                            softWrap = true,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            color = CyberGold.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, CyberGold.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "Pôle Pro",
                                color = CyberGold,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Hero KPI Card: Résultat Net des Affaires
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF131B2E),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(
                        width = 1.5.dp,
                        brush = Brush.horizontalGradient(listOf(EmeraldSuccess, CyberGold))
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "RÉSULTAT NET DES AFFAIRES CE MOIS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CyberGold,
                                letterSpacing = 1.sp,
                                fontSize = 10.sp,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Surface(
                                color = if (grossProfit >= 0) EmeraldSuccess.copy(alpha = 0.2f) else CoralDanger.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (grossProfit >= 0) "Bénéfice Positif" else "Déficit",
                                    color = if (grossProfit >= 0) EmeraldSuccess else CoralDanger,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "${String.format("%,.0f", grossProfit)} $currency",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = if (grossProfit >= 0) BrickTextPrimary else CoralDanger,
                            fontSize = 28.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = BrickSurfaceBorder)
                        Spacer(modifier = Modifier.height(12.dp))

                        // 3 Sub-stats with proper spacing
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("💰 Ventes / CA", style = MaterialTheme.typography.labelSmall, color = BrickTextMuted, fontSize = 10.sp, maxLines = 1)
                                Text(
                                    text = "${String.format("%,.0f", totalSales)} $currency",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldSuccess,
                                    fontSize = 12.sp
                                )
                            }
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("📦 Invest. Stock", style = MaterialTheme.typography.labelSmall, color = BrickTextMuted, fontSize = 10.sp, maxLines = 1)
                                Text(
                                    text = "${String.format("%,.0f", totalInvest)} $currency",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = WantColor,
                                    fontSize = 12.sp
                                )
                            }
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                Text("🕊️ Dons & Dîme", style = MaterialTheme.typography.labelSmall, color = BrickTextMuted, fontSize = 10.sp, maxLines = 1)
                                Text(
                                    text = "${String.format("%,.0f", totalDonations)} $currency",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberGold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // Quick Business Actions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onAddBusinessFlow,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                    ) {
                        Text("+ Vente / CA", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
                    }
                    Button(
                        onClick = onAddBusinessFlow,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrickSecondary)
                    ) {
                        Text("+ Stock / Pro", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
                    }
                    Button(
                        onClick = onAddBusinessFlow,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberGold)
                    ) {
                        Text("+ Don / Dîme", color = Color(0xFF0A0E1A), fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
                    }
                }
            }

            // Golden Rules of Business Card
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = BrickSurfaceElevated,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, BrickSurfaceBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "💡", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "RÈGLES D'OR DU COMMERCE & DE LA PROSPÉRITÉ",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CyberGold,
                                fontSize = 11.sp,
                                softWrap = true,
                                maxLines = 2
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "1. Sépare ta caisse d'affaires de tes dépenses personnelles : verse-toi un salaire régulier plutôt que de puiser dans le tiroir-caisse.",
                            style = MaterialTheme.typography.bodySmall,
                            color = BrickTextPrimary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "2. Réinvestis au minimum 20% des bénéfices dans tes stocks et outils de travail pour démultiplier tes ventes futures.",
                            style = MaterialTheme.typography.bodySmall,
                            color = BrickTextPrimary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "3. La Dîme (10%) et le Don : donner protège contre la cupidité et maintient une mentalité d'abondance indispensable pour réussir en affaires.",
                            style = MaterialTheme.typography.bodySmall,
                            color = BrickTextPrimary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Tab Filters with HORIZONTAL SCROLL (BUG FIX 1: "Dons" never squeezed vertically!)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val tabs = listOf(
                        "ALL" to "Tous les flux",
                        "INCOME" to "Entrées (+)",
                        "INVEST" to "Invest. (📦)",
                        "DONATION" to "Dons (🕊️)"
                    )

                    tabs.forEach { (key, label) ->
                        val isSelected = selectedFilter == key
                        Surface(
                            modifier = Modifier
                                .defaultMinSize(minWidth = 92.dp)
                                .height(40.dp)
                                .clickable { selectedFilter = key },
                            color = if (isSelected) BrickPrimary else BrickSurfaceElevated,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (isSelected) BrickPrimary else BrickSurfaceBorder)
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color(0xFF0A0E1A) else BrickTextPrimary,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }

            // Transactions list
            if (filteredList.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = BrickSurface,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, BrickSurfaceBorder)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "💼", fontSize = 32.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Aucun flux dans cette catégorie",
                                style = MaterialTheme.typography.titleSmall,
                                color = BrickTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Enregistrez vos ventes, achats de marchandises ou dons pour alimenter ce pôle.",
                                style = MaterialTheme.typography.bodySmall,
                                color = BrickTextMuted,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(filteredList, key = { it.id }) { tx ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = BrickSurfaceElevated,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BrickSurfaceBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (tx.type == "REVENU") EmeraldSuccess.copy(alpha = 0.2f) else CoralDanger.copy(alpha = 0.2f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (tx.type == "REVENU") "💰" else if (tx.category.contains("Don", ignoreCase = true)) "🕊️" else "📦",
                                        fontSize = 16.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = tx.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = BrickTextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${tx.category} • ${if (tx.type == "REVENU") "Entrée / Vente" else "Dépense Pro"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = BrickTextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "${if (tx.type == "DEPENSE") "-" else "+"}${String.format("%,.0f", tx.amount)} $currency",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (tx.type == "DEPENSE") CoralDanger else EmeraldSuccess
                            )
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
