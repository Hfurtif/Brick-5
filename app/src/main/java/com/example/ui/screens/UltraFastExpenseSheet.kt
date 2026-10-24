package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.SmartExpenseParser
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun UltraFastExpenseSheet(
    currency: String,
    onDismiss: () -> Unit,
    onSubmit: (title: String, amount: Double, category: String, type: String, priority: String) -> Unit
) {
    var rawInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Alimentation") }
    val quickAmounts = listOf(500, 1000, 2000, 5000, 10000, 25000)
    val quickCategories = listOf("Alimentation", "Transport", "Factures", "Sorties", "Santé", "Vente")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = BrickSurface,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = BrickPrimary.copy(alpha = 0.5f))
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp)
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FlashOn, contentDescription = null, tint = BrickPrimary, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Ajout Ultra-Rapide (3s)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BrickTextPrimary
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer", tint = BrickTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Smart Input
                OutlinedTextField(
                    value = rawInput,
                    onValueChange = { rawInput = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ultra_fast_input"),
                    placeholder = { Text("Tapez ex: 1500 taxi, 5000 resto...", color = BrickTextMuted, fontSize = 14.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            val parsed = SmartExpenseParser.parse(rawInput)
                            if (parsed != null) {
                                onSubmit(parsed.title, parsed.amount, parsed.category, parsed.type, parsed.priority)
                                onDismiss()
                            }
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrickPrimary,
                        unfocusedBorderColor = BrickSurfaceBorder,
                        focusedTextColor = BrickTextPrimary,
                        unfocusedTextColor = BrickTextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Amount Chips
                Text(
                    text = "Montants rapides ($currency)",
                    style = MaterialTheme.typography.labelSmall,
                    color = BrickTextMuted,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickAmounts.forEach { amt ->
                        Surface(
                            modifier = Modifier.clickable {
                                rawInput = if (rawInput.isBlank()) "$amt" else "$amt $rawInput"
                            },
                            color = BrickSurfaceElevated,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, BrickSurfaceBorder)
                        ) {
                            Text(
                                text = "+${String.format("%,d", amt)}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = BrickPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Categories
                Text(
                    text = "Catégories",
                    style = MaterialTheme.typography.labelSmall,
                    color = BrickTextMuted,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickCategories.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            modifier = Modifier.clickable {
                                selectedCategory = cat
                                if (!rawInput.contains(cat, ignoreCase = true)) {
                                    rawInput = "$rawInput $cat".trim()
                                }
                            },
                            color = if (isSelected) BrickPrimary.copy(alpha = 0.2f) else BrickSurfaceElevated,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (isSelected) BrickPrimary else BrickSurfaceBorder)
                        ) {
                            Text(
                                text = cat,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) BrickPrimary else BrickTextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val parsed = SmartExpenseParser.parse(rawInput)
                        if (parsed != null) {
                            onSubmit(parsed.title, parsed.amount, parsed.category, parsed.type, parsed.priority)
                        } else {
                            val amount = rawInput.filter { it.isDigit() }.toDoubleOrNull() ?: 1000.0
                            onSubmit(selectedCategory, amount, selectedCategory, "DEPENSE", "NEEDS")
                        }
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("ultra_fast_submit_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrickPrimary)
                ) {
                    Text("Valider immédiatement ⚡", color = Color(0xFF0A0E1A), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
