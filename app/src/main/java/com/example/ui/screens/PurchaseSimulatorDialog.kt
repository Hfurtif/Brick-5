package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Psychology
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
import com.example.ai.PurchaseSimulationResult
import com.example.ui.theme.*

@Composable
fun PurchaseSimulatorDialog(
    currency: String,
    simulationResult: PurchaseSimulationResult?,
    onDismiss: () -> Unit,
    onSimulate: (amount: Double, title: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }

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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = CyberGold, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Le Réfléchisseur BRICK",
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

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Simulez un achat impulsif avant de dépenser votre argent.",
                        style = MaterialTheme.typography.bodySmall,
                        color = BrickTextMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sim_title_input"),
                        label = { Text("Qu'avez-vous envie d'acheter ?", color = BrickTextMuted) },
                        placeholder = { Text("Ex: Nouvelle paire de baskets, smartphone...", color = BrickTextMuted) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberGold,
                            unfocusedBorderColor = BrickSurfaceBorder,
                            focusedTextColor = BrickTextPrimary,
                            unfocusedTextColor = BrickTextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { newVal ->
                            if (newVal.all { it.isDigit() || it == '.' || it == ',' }) {
                                amountStr = newVal
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sim_amount_input"),
                        label = { Text("Prix de l'article ($currency)", color = BrickTextMuted) },
                        placeholder = { Text("Ex: 45000", color = BrickTextMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberGold,
                            unfocusedBorderColor = BrickSurfaceBorder,
                            focusedTextColor = BrickTextPrimary,
                            unfocusedTextColor = BrickTextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val amount = amountStr.replace(",", ".").toDoubleOrNull() ?: 0.0

                    Button(
                        onClick = {
                            if (amount > 0) {
                                onSimulate(amount, title.ifEmpty { "Achat plaisir" })
                            }
                        },
                        enabled = amount > 0,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("sim_submit_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberGold)
                    ) {
                        Text("Analyser l'impact financier 🧠", color = Color(0xFF0A0E1A), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    if (simulationResult != null) {
                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider(color = BrickSurfaceBorder)
                        Spacer(modifier = Modifier.height(14.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = BrickSurfaceElevated,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, if (simulationResult.canAfford) EmeraldSuccess else CoralDanger)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = simulationResult.verdict,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (simulationResult.score >= 60) EmeraldSuccess else CoralDanger
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = simulationResult.advice,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = BrickTextPrimary,
                                    lineHeight = 18.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "⏳ " + simulationResult.impactOnGoals,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyberGold,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
