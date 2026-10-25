package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AssetLiability
import com.example.ui.theme.*

@Composable
fun AssetsLiabilitiesScreen(
    assetsLiabilities: List<AssetLiability>,
    currency: String,
    hideAmounts: Boolean,
    onAddAssetLiability: (name: String, type: String, category: String, value: Double, cashflow: Double, note: String) -> Unit,
    onDeleteAssetLiability: (AssetLiability) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedCategoryFilter by remember { mutableStateOf<String?>("ALL") }

    val assets = assetsLiabilities.filter { it.type == "ACTIF" }
    val liabilities = assetsLiabilities.filter { it.type == "PASSIF" }

    val displayAssets = remember(assets, selectedCategoryFilter) {
        when (selectedCategoryFilter) {
            "PASSIF" -> emptyList()
            "TONTINE" -> assets.filter { it.category.contains("Tontine", ignoreCase = true) || it.name.contains("Tontine", ignoreCase = true) }
            "EPARGNE" -> assets.filter { it.category.contains("Épargne", ignoreCase = true) || it.category.contains("Mobile", ignoreCase = true) }
            "BUSINESS" -> assets.filter { it.category.contains("Business", ignoreCase = true) || it.name.contains("Business", ignoreCase = true) }
            else -> assets
        }
    }

    val displayLiabilities = remember(liabilities, selectedCategoryFilter) {
        when (selectedCategoryFilter) {
            "ACTIF", "TONTINE", "EPARGNE", "BUSINESS" -> emptyList()
            else -> liabilities
        }
    }

    val totalAssets = assets.sumOf { it.value }
    val totalLiabilities = liabilities.sumOf { it.value }
    val netWorth = totalAssets - totalLiabilities

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
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Objectifs d'Épargne & Actifs",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = BrickTextPrimary,
                            fontSize = 20.sp,
                            softWrap = true,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Bâtissez un patrimoine qui travaille pour vous",
                            style = MaterialTheme.typography.bodySmall,
                            color = BrickTextMuted,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { showAddDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrickPrimary),
                        modifier = Modifier.testTag("add_asset_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF0A0E1A), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ajouter", color = Color(0xFF0A0E1A), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            // Horizontal Scrollable Filter Chips (prevents horizontal crushing)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(
                        "ALL" to "Tous (${assets.size + liabilities.size})",
                        "ACTIF" to "Actifs 📈 (${assets.size})",
                        "PASSIF" to "Passifs 💳 (${liabilities.size})",
                        "TONTINE" to "Tontines 🏺",
                        "EPARGNE" to "Épargne 🏦",
                        "BUSINESS" to "Business 📦"
                    ).forEach { (code, label) ->
                        val isSelected = selectedCategoryFilter == code
                        Surface(
                            modifier = Modifier
                                .clickable { selectedCategoryFilter = code }
                                .widthIn(min = 84.dp)
                                .height(36.dp),
                            color = if (isSelected) BrickPrimary else BrickSurfaceElevated,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (isSelected) BrickPrimary else BrickSurfaceBorder)
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color(0xFF0A0E1A) else BrickTextPrimary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }

            // Net Worth Hero Card
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
                        Text(
                            text = "VALEUR NETTE DU PATRIMOINE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = CyberGold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (hideAmounts) "•••••• $currency" else "${String.format("%,.0f", netWorth)} $currency",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = if (netWorth >= 0) BrickTextPrimary else CoralDanger,
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
                                Text("Total Actifs (Ce qui vous enrichit)", style = MaterialTheme.typography.labelSmall, color = BrickTextMuted, fontSize = 10.sp)
                                Text(
                                    text = if (hideAmounts) "••••••" else "+${String.format("%,.0f", totalAssets)} $currency",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldSuccess
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Total Passifs (Dettes & Emprunts)", style = MaterialTheme.typography.labelSmall, color = BrickTextMuted, fontSize = 10.sp)
                                Text(
                                    text = if (hideAmounts) "••••••" else "-${String.format("%,.0f", totalLiabilities)} $currency",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = CoralDanger
                                )
                            }
                        }
                    }
                }
            }

            // Actifs Section
            if (displayAssets.isNotEmpty() || selectedCategoryFilter in listOf("ALL", "ACTIF", "TONTINE", "EPARGNE", "BUSINESS")) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Actifs & Réserves (${displayAssets.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldSuccess
                        )
                    }
                }

                if (displayAssets.isEmpty()) {
                    item {
                        EmptySectionCard(
                            icon = "📈",
                            message = "Aucun actif trouvé pour ce filtre. Ajoutez votre réserve tontine, Mobile Money ou business."
                        )
                    }
                } else {
                    items(displayAssets, key = { it.id }) { item ->
                        AssetLiabilityRow(
                            item = item,
                            currency = currency,
                            hideAmounts = hideAmounts,
                            isAsset = true,
                            onDelete = { onDeleteAssetLiability(item) }
                        )
                    }
                }
            }

            // Passifs Section
            if (displayLiabilities.isNotEmpty() || selectedCategoryFilter in listOf("ALL", "PASSIF")) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Passifs & Dettes (${displayLiabilities.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CoralDanger
                        )
                    }
                }

                if (displayLiabilities.isEmpty()) {
                    item {
                        EmptySectionCard(
                            icon = "🛡️",
                            message = "Aucune dette active ! Votre forteresse est libre de tout fardeau financier."
                        )
                    }
                } else {
                    items(displayLiabilities, key = { it.id }) { item ->
                        AssetLiabilityRow(
                            item = item,
                            currency = currency,
                            hideAmounts = hideAmounts,
                            isAsset = false,
                            onDelete = { onDeleteAssetLiability(item) }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    if (showAddDialog) {
        AddAssetLiabilityDialog(
            currency = currency,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, type, cat, value, cashflow, note ->
                onAddAssetLiability(name, type, cat, value, cashflow, note)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AssetLiabilityRow(
    item: AssetLiability,
    currency: String,
    hideAmounts: Boolean,
    isAsset: Boolean,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = BrickSurfaceElevated,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, BrickSurfaceBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrickTextPrimary
                )
                Text(
                    text = "${item.category}${if (item.monthlyCashflow != 0.0) " • Flux: ${String.format("%,.0f", item.monthlyCashflow)}/mois" else ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = BrickTextMuted,
                    fontSize = 11.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (hideAmounts) "••••••" else "${if (isAsset) "+" else "-"}${String.format("%,.0f", item.value)} $currency",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isAsset) EmeraldSuccess else CoralDanger
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "Supprimer",
                        tint = BrickTextMuted.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptySectionCard(icon: String, message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = BrickSurface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, BrickSurfaceBorder)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = BrickTextMuted,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun AddAssetLiabilityDialog(
    currency: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: String, cat: String, value: Double, cashflow: Double, note: String) -> Unit
) {
    var type by remember { mutableStateOf("ACTIF") } // "ACTIF" or "PASSIF"
    var name by remember { mutableStateOf("") }
    var valueStr by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Tontine") }

    val assetCategories = listOf("Tontine", "Mobile Money", "Compte Épargne", "Business", "Terrain / Immo", "Autre Actif")
    val liabilityCategories = listOf("Dette Personnelle", "Crédit Bancaire", "Avance sur Salaire", "Dette Familiale", "Autre Passif")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .imePadding(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(24.dp),
                color = BrickSurface,
                border = BorderStroke(1.dp, BrickSurfaceBorder),
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (type == "ACTIF") "Ajouter un Actif 📈" else "Ajouter un Passif 💳",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BrickTextPrimary
                        )
                        IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Fermer", tint = BrickTextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clickable {
                                    type = "ACTIF"
                                    category = "Tontine"
                                },
                            color = if (type == "ACTIF") EmeraldSuccess.copy(alpha = 0.2f) else BrickSurfaceElevated,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (type == "ACTIF") EmeraldSuccess else BrickSurfaceBorder)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("Actif (Enrichit)", color = if (type == "ACTIF") EmeraldSuccess else BrickTextMuted, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clickable {
                                    type = "PASSIF"
                                    category = "Dette Personnelle"
                                },
                            color = if (type == "PASSIF") CoralDanger.copy(alpha = 0.2f) else BrickSurfaceElevated,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (type == "PASSIF") CoralDanger else BrickSurfaceBorder)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("Passif (Dette)", color = if (type == "PASSIF") CoralDanger else BrickTextMuted, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Nom de l'élément", color = BrickTextMuted) },
                        placeholder = { Text(if (type == "ACTIF") "Ex: Tontine des commerçants, Stock..." else "Ex: Emprunt ami, crédit microfinance...", color = BrickTextMuted) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrickPrimary,
                            unfocusedBorderColor = BrickSurfaceBorder,
                            focusedTextColor = BrickTextPrimary,
                            unfocusedTextColor = BrickTextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Catégorie : $category",
                        style = MaterialTheme.typography.labelSmall,
                        color = BrickTextMuted,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val activeCategories = if (type == "ACTIF") assetCategories else liabilityCategories
                        activeCategories.forEach { cat ->
                            val isSel = category == cat
                            Surface(
                                modifier = Modifier
                                    .clickable { category = cat }
                                    .height(34.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) BrickPrimary else BrickSurfaceElevated,
                                border = BorderStroke(1.dp, if (isSel) BrickPrimary else BrickSurfaceBorder)
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cat,
                                        color = if (isSel) Color(0xFF0A0E1A) else BrickTextPrimary,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = valueStr,
                        onValueChange = { newVal ->
                            if (newVal.all { it.isDigit() }) valueStr = newVal
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Valeur ou Montant dû ($currency)", color = BrickTextMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrickPrimary,
                            unfocusedBorderColor = BrickSurfaceBorder,
                            focusedTextColor = BrickTextPrimary,
                            unfocusedTextColor = BrickTextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    val value = valueStr.toDoubleOrNull() ?: 0.0

                    Button(
                        onClick = {
                            if (name.isNotBlank() && value > 0) {
                                onConfirm(name.trim(), type, category, value, 0.0, "")
                            }
                        },
                        enabled = name.isNotBlank() && value > 0,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == "ACTIF") EmeraldSuccess else CoralDanger
                        )
                    ) {
                        Text("Enregistrer dans le patrimoine", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
