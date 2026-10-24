package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddExpenseDialog(
    initialMode: String = "DEPENSE",
    currency: String,
    onDismiss: () -> Unit,
    onSubmit: (title: String, amount: Double, category: String, type: String, priority: String, note: String) -> Unit
) {
    var mode by remember { mutableStateOf(initialMode) } // "DEPENSE" or "REVENU"
    var title by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(if (initialMode == "REVENU") "Vente / Salaire" else "Alimentation") }
    var priority by remember { mutableStateOf("NEEDS") }
    var note by remember { mutableStateOf("") }

    val expenseCategories = listOf("Alimentation", "Transport", "Logement", "Factures", "Santé", "Sorties", "Shopping", "Tontine", "Autre")
    val incomeCategories = listOf("Vente / Commerce", "Salaire / Paiement", "Tontine reçue", "Prestation", "Transfert reçu", "Autre")

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
                    // Title Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (mode == "REVENU") "💰 Entrée en Caisse" else "➕ Nouvelle Dépense",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = BrickTextPrimary,
                            fontSize = 18.sp
                        )

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("close_expense_dialog")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Fermer", tint = BrickTextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Mode Switcher (Dépense vs Entrée d'argent)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clickable {
                                    mode = "DEPENSE"
                                    category = "Alimentation"
                                },
                            color = if (mode == "DEPENSE") CoralDanger.copy(alpha = 0.2f) else BrickSurfaceElevated,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (mode == "DEPENSE") CoralDanger else BrickSurfaceBorder)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "💸 Dépense",
                                    color = if (mode == "DEPENSE") CoralDanger else BrickTextMuted,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clickable {
                                    mode = "REVENU"
                                    category = "Vente / Commerce"
                                },
                            color = if (mode == "REVENU") EmeraldSuccess.copy(alpha = 0.2f) else BrickSurfaceElevated,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (mode == "REVENU") EmeraldSuccess else BrickSurfaceBorder)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "💵 Entrée de Caisse",
                                    color = if (mode == "REVENU") EmeraldSuccess else BrickTextMuted,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Amount Field
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { newVal ->
                            if (newVal.all { it.isDigit() || it == '.' || it == ',' }) {
                                amountStr = newVal
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("expense_amount_input"),
                        label = { Text("Montant ($currency)", color = BrickTextMuted) },
                        placeholder = { Text("Ex: 5000", color = BrickTextMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (mode == "REVENU") EmeraldSuccess else BrickPrimary,
                            unfocusedBorderColor = BrickSurfaceBorder,
                            focusedTextColor = BrickTextPrimary,
                            unfocusedTextColor = BrickTextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Title Field
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("expense_title_input"),
                        label = { Text("Motif ou description", color = BrickTextMuted) },
                        placeholder = { Text(if (mode == "REVENU") "Ex: Paiement client, salaire" else "Ex: Courses marché, taxi", color = BrickTextMuted) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (mode == "REVENU") EmeraldSuccess else BrickPrimary,
                            unfocusedBorderColor = BrickSurfaceBorder,
                            focusedTextColor = BrickTextPrimary,
                            unfocusedTextColor = BrickTextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Category Selection (Adaptive FlowRow)
                    Text(
                        text = "Catégorie",
                        style = MaterialTheme.typography.labelMedium,
                        color = BrickTextMuted,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val activeCategories = if (mode == "REVENU") incomeCategories else expenseCategories
                        activeCategories.forEach { cat ->
                            val isSelected = category == cat
                            Surface(
                                modifier = Modifier
                                    .clickable { category = cat }
                                    .padding(vertical = 2.dp),
                                color = if (isSelected) BrickPrimary.copy(alpha = 0.2f) else BrickSurfaceElevated,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, if (isSelected) BrickPrimary else BrickSurfaceBorder)
                            ) {
                                Text(
                                    text = cat,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) BrickPrimary else BrickTextPrimary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    if (mode == "DEPENSE") {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Règle 50/30/20 (Plafond budgétaire)",
                            style = MaterialTheme.typography.labelMedium,
                            color = BrickTextMuted,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("NEEDS" to "Besoins (50%)", "WANTS" to "Envies (30%)", "SAVINGS" to "Épargne (20%)").forEach { (key, label) ->
                                val isSelected = priority == key
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .clickable { priority = key },
                                    color = if (isSelected) BrickPrimary.copy(alpha = 0.2f) else BrickSurfaceElevated,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, if (isSelected) BrickPrimary else BrickSurfaceBorder)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSelected) BrickPrimary else BrickTextMuted,
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    val amount = amountStr.replace(",", ".").toDoubleOrNull() ?: 0.0
                    Button(
                        onClick = {
                            val finalTitle = title.trim().ifEmpty { if (mode == "REVENU") "Entrée de caisse" else category }
                            onSubmit(finalTitle, amount, category, mode, priority, note)
                            onDismiss()
                        },
                        enabled = amount > 0,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_expense_btn"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (mode == "REVENU") EmeraldSuccess else BrickPrimary
                        )
                    ) {
                        Text(
                            text = if (mode == "REVENU") "Enregistrer l'entrée de caisse" else "Enregistrer la dépense",
                            color = if (mode == "REVENU") Color.White else Color(0xFF0A0E1A),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
