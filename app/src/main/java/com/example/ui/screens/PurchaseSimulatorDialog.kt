package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.PurchaseSimulationResult
import com.example.ui.theme.BrickBackground
import com.example.ui.theme.BrickSurfaceBorder
import com.example.ui.theme.BrickSurfaceElevated
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.CyberGold
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.NeonVioletGlow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun PurchaseSimulatorDialog(
    currency: String,
    simulationResult: PurchaseSimulationResult?,
    onSimulate: (amount: Double, title: String) -> Unit,
    onDismiss: () -> Unit
) {
    var amountStr by remember { mutableStateOf("") }
    var itemTitle by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BrickSurfaceElevated,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    tint = NeonVioletGlow
                )
                Text(
                    text = "Besoin ou Envie ?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Avant de dégainer ta carte bleue pour un coup de tête, évalue l'impact réel sur ton mois et ton temps de vie.",
                    fontSize = 12.sp,
                    color = TextMuted,
                    lineHeight = 16.sp
                )

                OutlinedTextField(
                    value = itemTitle,
                    onValueChange = { itemTitle = it },
                    label = { Text("Qu'est-ce que tu veux acheter ?") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = NeonVioletGlow,
                        unfocusedBorderColor = BrickSurfaceBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("sim_title_input")
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Prix ($currency)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = NeonVioletGlow,
                        unfocusedBorderColor = BrickSurfaceBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("sim_amount_input")
                )

                Button(
                    onClick = {
                        val amount = amountStr.replace(',', '.').toDoubleOrNull() ?: 0.0
                        if (amount > 0.0) {
                            onSimulate(amount, itemTitle.ifBlank { "Achat" })
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                    modifier = Modifier.fillMaxWidth().testTag("run_simulation_btn")
                ) {
                    Text(
                        text = "Calculer l'impact",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (simulationResult != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (simulationResult.canAfford) EmeraldSuccess.copy(alpha = 0.12f)
                                else CoralDanger.copy(alpha = 0.12f)
                            )
                            .border(
                                1.dp,
                                if (simulationResult.canAfford) EmeraldSuccess.copy(alpha = 0.4f)
                                else CoralDanger.copy(alpha = 0.4f),
                                RoundedCornerShape(14.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = simulationResult.recommendation,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (simulationResult.canAfford) EmeraldSuccess else CoralDanger
                            )

                            Text(
                                text = "⏱️ Coût en travail : ${String.format("%.1f", simulationResult.hoursOfWork)} heures de ton labeur pour cet objet.",
                                fontSize = 11.sp,
                                color = CyberGold,
                                fontWeight = FontWeight.Medium
                            )

                            Text(
                                text = "📉 Reste à vivre après achat : ~${simulationResult.impactOnDailyAllowance.toInt()} $currency / jour.",
                                fontSize = 11.sp,
                                color = TextWhite
                            )

                            Text(
                                text = "🧱 ${simulationResult.alternativeRichDadAdvice}",
                                fontSize = 11.sp,
                                color = TextMuted,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Fermer", color = ElectricCyan)
            }
        }
    )
}
