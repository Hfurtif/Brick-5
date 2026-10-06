package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssetLiability
import com.example.data.model.FinancialGoal
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
import kotlin.math.max

// Standardized Asset & Liability Categories
val ASSET_CATEGORIES = listOf(
    "Liquidités & Livrets",
    "Investissements & Bourse",
    "Immobilier & Foncier",
    "Crypto & Web3",
    "Entreprise & Autre"
)

val LIABILITY_CATEGORIES = listOf(
    "Crédit Immobilier",
    "Crédit Auto & Véhicule",
    "Crédit Conso",
    "Prêt Étudiant",
    "Dette Personnelle"
)

fun getCategoryIcon(category: String, isActif: Boolean): String {
    return when {
        category.contains("Liqui", ignoreCase = true) -> "🏦"
        category.contains("Bourse", ignoreCase = true) || category.contains("Invest", ignoreCase = true) -> "📈"
        category.contains("Immo", ignoreCase = true) -> "🏠"
        category.contains("Crypto", ignoreCase = true) -> "🪙"
        category.contains("Auto", ignoreCase = true) || category.contains("Véhicule", ignoreCase = true) -> "🚗"
        category.contains("Étudiant", ignoreCase = true) -> "🎓"
        category.contains("Conso", ignoreCase = true) -> "💳"
        category.contains("Dette", ignoreCase = true) -> "🤝"
        isActif -> "💎"
        else -> "⚠️"
    }
}

@Composable
fun AssetsLiabilitiesScreen(
    user: UserProfile,
    assetsLiabilities: List<AssetLiability>,
    goals: List<FinancialGoal>,
    onAddAssetLiability: (name: String, type: String, category: String, value: Double, cashflow: Double, note: String) -> Unit,
    onDeleteAssetLiability: (AssetLiability) -> Unit,
    onAddGoal: (title: String, target: Double, current: Double, category: String) -> Unit,
    onUpdateGoalAmount: (FinancialGoal, Double) -> Unit,
    onDeleteGoal: (FinancialGoal) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showAddGoalDialog by remember { mutableStateOf(false) }

    // Active filters
    var typeFilter by remember { mutableStateOf("ALL") } // ALL, ACTIF, PASSIF
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }

    val assets = assetsLiabilities.filter { it.type == "ACTIF" }
    val liabilities = assetsLiabilities.filter { it.type == "PASSIF" }

    // Automatic Net Worth Calculation: Total Actifs - Total Passifs
    val totalAssetsValue = assets.sumOf { it.value }
    val totalLiabilitiesValue = liabilities.sumOf { it.value }
    val netWorth = totalAssetsValue - totalLiabilitiesValue
    val netCashflowMonthly = assets.sumOf { it.monthlyCashflow } + liabilities.sumOf { it.monthlyCashflow }

    // Solvency Ratio: Assets / Liabilities
    val solvencyRatio = if (totalLiabilitiesValue > 0) totalAssetsValue / totalLiabilitiesValue else if (totalAssetsValue > 0) 99.0 else 1.0

    // Filter items according to active type and category
    val filteredItems = assetsLiabilities.filter { item ->
        val matchesType = when (typeFilter) {
            "ACTIF" -> item.type == "ACTIF"
            "PASSIF" -> item.type == "PASSIF"
            else -> true
        }
        val matchesCategory = selectedCategoryFilter == null || item.category.equals(selectedCategoryFilter, ignoreCase = true)
        matchesType && matchesCategory
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BrickBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Screen Header
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Actifs, Passifs & Valeur Nette",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextWhite
            )
            Text(
                text = "Bilan patrimonial en temps réel • Philosophie Père Riche",
                fontSize = 12.sp,
                color = TextMuted
            )
        }

        // ==========================================
        // HERO CARD: CALCUL AUTOMATIQUE DE LA VALEUR NETTE
        // ==========================================
        item {
            val netWorthColor = when {
                netWorth > 0 -> ElectricCyan
                netWorth == 0.0 -> TextWhite
                else -> CoralDanger
            }

            NeonGlassCard(
                borderColor = if (netWorth >= 0) ElectricCyan.copy(alpha = 0.6f) else CoralDanger.copy(alpha = 0.6f)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "💎 VALEUR NETTE DU PATRIMOINE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ElectricCyan,
                                    letterSpacing = 1.sp
                                )
                            }
                            Text(
                                text = "Actifs totaux - Passifs totaux",
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }

                        // Solvency status pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (solvencyRatio >= 2.0) EmeraldSuccess.copy(alpha = 0.18f)
                                    else if (solvencyRatio >= 1.0) CyberGold.copy(alpha = 0.18f)
                                    else CoralDanger.copy(alpha = 0.18f)
                                )
                                .border(
                                    1.dp,
                                    if (solvencyRatio >= 2.0) EmeraldSuccess.copy(alpha = 0.4f)
                                    else if (solvencyRatio >= 1.0) CyberGold.copy(alpha = 0.4f)
                                    else CoralDanger.copy(alpha = 0.4f),
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = when {
                                    solvencyRatio >= 2.0 -> "🛡️ Solvable (${String.format("%.1fx", solvencyRatio)})"
                                    solvencyRatio >= 1.0 -> "⚖️ Équilibré (${String.format("%.1fx", solvencyRatio)})"
                                    else -> "⚠️ Endetté"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (solvencyRatio >= 2.0) EmeraldSuccess else if (solvencyRatio >= 1.0) CyberGold else CoralDanger
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Large Net Worth Number
                    PrivacyAmountText(
                        amount = netWorth,
                        currency = user.currency,
                        hideAmounts = user.hideAmounts,
                        fontSize = 32,
                        fontWeight = FontWeight.Black,
                        color = netWorthColor,
                        modifier = Modifier.testTag("net_worth_amount")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3 KPI pillars: Total Actifs, Total Passifs, Cashflow Net
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(BrickSurfaceElevated)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Total Actifs
                        Column(horizontalAlignment = Alignment.Start) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(13.dp))
                                Text("Actifs (${assets.size})", fontSize = 10.sp, color = TextMuted)
                            }
                            Text(
                                text = if (user.hideAmounts) "••••" else "+${totalAssetsValue.toInt()} ${user.currency}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldSuccess
                            )
                        }

                        // Total Passifs
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = CoralDanger, modifier = Modifier.size(13.dp))
                                Text("Passifs (${liabilities.size})", fontSize = 10.sp, color = TextMuted)
                            }
                            Text(
                                text = if (user.hideAmounts) "••••" else "-${totalLiabilitiesValue.toInt()} ${user.currency}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = CoralDanger
                            )
                        }

                        // Cashflow mensuel net
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Cashflow net", fontSize = 10.sp, color = TextMuted)
                            val isCashflowPos = netCashflowMonthly >= 0
                            Text(
                                text = "${if (isCashflowPos) "+" else ""}${netCashflowMonthly.toInt()} ${user.currency}/m",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCashflowPos) CyberGold else CoralDanger
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // CATEGORY & TYPE FILTER SECTION
        // ==========================================
        item {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Header with Add Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Inventaire Patrimonial",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )

                    Button(
                        onClick = { showAddDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrickSurfaceElevated),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(ElectricCyan)),
                        modifier = Modifier.testTag("add_asset_liability_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Ajouter", color = ElectricCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Type Segmented Filter (Tous, Actifs, Passifs)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val typeTabs = listOf(
                        Triple("ALL", "Tous (${assetsLiabilities.size})", TextWhite),
                        Triple("ACTIF", "Actifs (${assets.size})", EmeraldSuccess),
                        Triple("PASSIF", "Passifs (${liabilities.size})", CoralDanger)
                    )

                    typeTabs.forEach { (typeKey, label, activeColor) ->
                        val isSelected = typeFilter == typeKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) activeColor.copy(alpha = 0.2f) else BrickSurfaceElevated)
                                .border(
                                    1.dp,
                                    if (isSelected) activeColor else BrickSurfaceBorder,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    typeFilter = typeKey
                                    selectedCategoryFilter = null // reset subcategory on tab change
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) activeColor else TextMuted
                            )
                        }
                    }
                }

                // Sub-category Horizontal Filter Chips
                val availableCategories = when (typeFilter) {
                    "ACTIF" -> ASSET_CATEGORIES
                    "PASSIF" -> LIABILITY_CATEGORIES
                    else -> (ASSET_CATEGORIES + LIABILITY_CATEGORIES).distinct()
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategoryFilter == null,
                            onClick = { selectedCategoryFilter = null },
                            label = { Text("Toutes catégories", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricCyan.copy(alpha = 0.2f),
                                selectedLabelColor = ElectricCyan,
                                containerColor = BrickSurfaceElevated,
                                labelColor = TextMuted
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedCategoryFilter == null,
                                borderColor = BrickSurfaceBorder,
                                selectedBorderColor = ElectricCyan
                            )
                        )
                    }

                    items(availableCategories) { cat ->
                        val isSelected = selectedCategoryFilter.equals(cat, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedCategoryFilter = if (isSelected) null else cat
                            },
                            label = {
                                Text(
                                    text = "${getCategoryIcon(cat, typeFilter != "PASSIF")} $cat",
                                    fontSize = 11.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonViolet.copy(alpha = 0.25f),
                                selectedLabelColor = NeonVioletGlow,
                                containerColor = BrickSurfaceElevated,
                                labelColor = TextMuted
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = BrickSurfaceBorder,
                                selectedBorderColor = NeonVioletGlow
                            )
                        )
                    }
                }
            }
        }

        // ==========================================
        // LIST OF ASSETS & LIABILITIES
        // ==========================================
        if (filteredItems.isEmpty()) {
            item {
                NeonGlassCard(borderColor = BrickSurfaceBorder) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "🔍", fontSize = 28.sp)
                        Text(
                            text = "Aucun élément dans cette catégorie",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = "Clique sur 'Ajouter' pour classifier un nouvel actif ou passif",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        } else {
            items(filteredItems, key = { it.id }) { item ->
                val isActif = item.type == "ACTIF"
                val accentColor = if (isActif) EmeraldSuccess else CoralDanger

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
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(accentColor.copy(alpha = 0.15f))
                                .border(1.dp, accentColor.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = getCategoryIcon(item.category, isActif),
                                fontSize = 18.sp
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = item.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextWhite
                                )
                                // Category badge tag
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(BrickSurface)
                                        .border(0.8.dp, BrickSurfaceBorder, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = item.category,
                                        fontSize = 9.sp,
                                        color = TextMuted,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))
                            val isPosCashflow = item.monthlyCashflow >= 0
                            Text(
                                text = if (isActif) {
                                    if (item.monthlyCashflow > 0) "+${item.monthlyCashflow.toInt()} ${user.currency}/mois (revenu généré)"
                                    else "0 ${user.currency}/mois (plus-value potentielle)"
                                } else {
                                    "${item.monthlyCashflow.toInt()} ${user.currency}/mois (coût/mensualité)"
                                },
                                fontSize = 11.sp,
                                color = if (isPosCashflow) EmeraldSuccess else CoralDanger,
                                fontWeight = FontWeight.Medium
                            )

                            if (item.note.isNotBlank()) {
                                Text(text = item.note, fontSize = 10.sp, color = TextDark)
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PrivacyAmountText(
                            amount = item.value,
                            currency = user.currency,
                            hideAmounts = user.hideAmounts,
                            fontSize = 14,
                            fontWeight = FontWeight.Bold,
                            color = if (isActif) TextWhite else CoralDanger
                        )

                        IconButton(
                            onClick = { onDeleteAssetLiability(item) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = TextDark, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // ==========================================
        // SECTION OBJECTIFS FINANCIERS ("SE PAYER EN PREMIER")
        // ==========================================
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Objectifs d'Épargne & Actifs",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Text(
                        text = "Construis ton matelas de sécurité et tes futurs actifs",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                Button(
                    onClick = { showAddGoalDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrickSurfaceElevated),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(CyberGold)),
                    modifier = Modifier.testTag("add_goal_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = CyberGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Objectif", color = CyberGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(goals, key = { it.id }) { goal ->
            val progress = (goal.currentAmount / max(1.0, goal.targetAmount)).toFloat().coerceIn(0f, 1f)
            NeonGlassCard(borderColor = NeonViolet.copy(alpha = 0.4f)) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = if (goal.category == "URGENCE") "🛡️" else "🎯", fontSize = 18.sp)
                            Column {
                                Text(text = goal.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                Text(text = "Cible : ${goal.targetAmount.toInt()} ${user.currency}", fontSize = 11.sp, color = TextMuted)
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(
                                onClick = { onUpdateGoalAmount(goal, 100.0) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NeonViolet.copy(alpha = 0.25f)),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(text = "+100 ${user.currency}", fontSize = 11.sp, color = NeonVioletGlow, fontWeight = FontWeight.Bold)
                            }
                            IconButton(onClick = { onDeleteGoal(goal) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = TextDark, modifier = Modifier.size(14.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = NeonVioletGlow,
                        trackColor = BrickSurfaceBorder
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (user.hideAmounts) "••••" else "${goal.currentAmount.toInt()} ${user.currency} épargnés",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NeonVioletGlow
                        )
                        Text(
                            text = "${(progress * 100).toInt()}% complété",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }

    // ==========================================
    // DIALOG: AJOUTER UN ACTIF OU UN PASSIF AVEC CATÉGORIES DÉDIÉES
    // ==========================================
    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var type by remember { mutableStateOf("ACTIF") }
        var selectedCategory by remember { mutableStateOf(ASSET_CATEGORIES.first()) }
        var valueStr by remember { mutableStateOf("") }
        var cashflowStr by remember { mutableStateOf("") }
        var note by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = BrickSurfaceElevated,
            title = {
                Text(text = "Ajouter au Patrimoine", color = TextWhite, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Type Selector (Actif vs Passif)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                type = "ACTIF"
                                selectedCategory = ASSET_CATEGORIES.first()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (type == "ACTIF") EmeraldSuccess else BrickSurface
                            )
                        ) {
                            Text(text = "📈 Actif (+)", color = if (type == "ACTIF") BrickBackground else TextWhite, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                type = "PASSIF"
                                selectedCategory = LIABILITY_CATEGORIES.first()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (type == "PASSIF") CoralDanger else BrickSurface
                            )
                        ) {
                            Text(text = "💳 Passif (-)", color = if (type == "PASSIF") TextWhite else TextMuted, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Category Selector
                    Text("Catégorie :", fontSize = 12.sp, color = TextMuted)
                    val categoriesForType = if (type == "ACTIF") ASSET_CATEGORIES else LIABILITY_CATEGORIES

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(categoriesForType) { cat ->
                            val isSel = selectedCategory == cat
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = if (type == "ACTIF") EmeraldSuccess.copy(alpha = 0.25f) else CoralDanger.copy(alpha = 0.25f),
                                    selectedLabelColor = if (type == "ACTIF") EmeraldSuccess else CoralDanger,
                                    containerColor = BrickSurface,
                                    labelColor = TextMuted
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSel,
                                    borderColor = BrickSurfaceBorder,
                                    selectedBorderColor = if (type == "ACTIF") EmeraldSuccess else CoralDanger
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nom (ex: Livret A, ETF World, Prêt Auto)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = BrickSurfaceBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = valueStr,
                        onValueChange = { valueStr = it },
                        label = { Text(if (type == "ACTIF") "Valeur estimée (${user.currency})" else "Capital restant dû (${user.currency})") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = BrickSurfaceBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = cashflowStr,
                        onValueChange = { cashflowStr = it },
                        label = { Text(if (type == "ACTIF") "Revenu passif généré / mois (+)" else "Mensualité / coût par mois (-)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = BrickSurfaceBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Note / conseil ou échéance") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = BrickSurfaceBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val v = valueStr.toDoubleOrNull() ?: 0.0
                        var cf = cashflowStr.toDoubleOrNull() ?: 0.0
                        // Auto sign adjustment: negative for liabilities if user typed positive
                        if (type == "PASSIF" && cf > 0) {
                            cf = -cf
                        }
                        if (name.isNotBlank()) {
                            onAddAssetLiability(name, type, selectedCategory, v, cf, note)
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
                ) {
                    Text("Enregistrer", color = BrickBackground, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Annuler", color = TextMuted)
                }
            }
        )
    }

    // Dialog Add Goal
    if (showAddGoalDialog) {
        var title by remember { mutableStateOf("") }
        var targetStr by remember { mutableStateOf("") }
        var currentStr by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("EPARGNE") }

        AlertDialog(
            onDismissRequest = { showAddGoalDialog = false },
            containerColor = BrickSurfaceElevated,
            title = {
                Text(text = "Créer un Objectif Financier", color = TextWhite, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Nom (ex: Fonds d'urgence, Voyage)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = CyberGold,
                            unfocusedBorderColor = BrickSurfaceBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = targetStr,
                        onValueChange = { targetStr = it },
                        label = { Text("Montant cible (${user.currency})") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = CyberGold,
                            unfocusedBorderColor = BrickSurfaceBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = currentStr,
                        onValueChange = { currentStr = it },
                        label = { Text("Montant déjà disponible (${user.currency})") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = CyberGold,
                            unfocusedBorderColor = BrickSurfaceBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val t = targetStr.toDoubleOrNull() ?: 1000.0
                        val c = currentStr.toDoubleOrNull() ?: 0.0
                        if (title.isNotBlank()) {
                            onAddGoal(title, t, c, category)
                            showAddGoalDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberGold)
                ) {
                    Text("Créer l'objectif", color = BrickBackground, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddGoalDialog = false }) {
                    Text("Annuler", color = TextMuted)
                }
            }
        )
    }
}
