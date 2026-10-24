package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    val businessIncomes = remember(transactions) {
        transactions.filter { it.type == "REVENU" && (it.category.contains("Vente", ignoreCase = true) || it.category.contains("Business", ignoreCase = true) || it.category.contains("Prestation", ignoreCase = true)) }
    }
    val businessExpenses = remember(transactions) {
        transactions.filter { it.type == "DEPENSE" && (it.category.contains("Business", ignoreCase = true) || it.category.contains("Marchandise", ignoreCase = true) || it.category.contains("Stock", ignoreCase = true)) }
    }

    val totalSales = businessIncomes.sumOf { it.amount }
    val totalCosts = businessExpenses.sumOf { it.amount }
    val grossProfit = totalSales - totalCosts

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
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Flux Affaires & Commerce",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = BrickTextPrimary
                        )
                        Text(
                            text = "Séparez votre argent personnel de vos affaires",
                            style = MaterialTheme.typography.bodySmall,
                            color = BrickTextMuted
                        )
                    }

                    Button(
                        onClick = onAddBusinessFlow,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberGold)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF0A0E1A), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Encaissement", color = Color(0xFF0A0E1A), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            // Hero KPI Card
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF131B2E),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.5.dp, CyberGold)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "BÉNÉFICE BRUT DES AFFAIRES",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = CyberGold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${String.format("%,.0f", grossProfit)} $currency",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = if (grossProfit >= 0) EmeraldSuccess else CoralDanger,
                            fontSize = 28.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = BrickSurfaceBorder)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Chiffre d'affaires encaissé", style = MaterialTheme.typography.labelSmall, color = BrickTextMuted, fontSize = 10.sp)
                                Text(
                                    text = "+${String.format("%,.0f", totalSales)} $currency",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldSuccess
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Coûts & Achats de stock", style = MaterialTheme.typography.labelSmall, color = BrickTextMuted, fontSize = 10.sp)
                                Text(
                                    text = "-${String.format("%,.0f", totalCosts)} $currency",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = CoralDanger
                                )
                            }
                        }
                    }
                }
            }

            // Flow items
            item {
                Text(
                    text = "Opérations d'Affaires (${businessIncomes.size + businessExpenses.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrickTextPrimary
                )
            }

            val allBusiness = (businessIncomes + businessExpenses).sortedByDescending { it.timestamp }
            if (allBusiness.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = BrickSurface,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, BrickSurfaceBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "💼", fontSize = 32.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Aucun flux commercial pour le moment",
                                style = MaterialTheme.typography.titleSmall,
                                color = BrickTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Enregistrez vos ventes, achats de marchandises ou prestations pour suivre votre rentabilité.",
                                style = MaterialTheme.typography.bodySmall,
                                color = BrickTextMuted,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(allBusiness, key = { it.id }) { tx ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = BrickSurfaceElevated,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BrickSurfaceBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(tx.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = BrickTextPrimary)
                                Text(tx.category, style = MaterialTheme.typography.labelSmall, color = BrickTextMuted, fontSize = 11.sp)
                            }
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
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
