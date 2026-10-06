package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.ui.theme.BrickBackground
import com.example.ui.theme.BrickSurface
import com.example.ui.theme.BrickSurfaceBorder
import com.example.ui.theme.BrickSurfaceElevated
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeedColor
import com.example.ui.theme.NeonVioletGlow
import com.example.ui.theme.SaveColor
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.WantColor
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddExpenseDialog(
    currency: String,
    onDismiss: () -> Unit,
    onConfirm: (title: String, amount: Double, category: String, type: String, priority: String, note: String) -> Unit
) {
    var amountStr by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Alimentation") }
    var selectedType by remember { mutableStateOf("BESOIN") }
    var selectedPriority by remember { mutableStateOf("UTILE") }
    var note by remember { mutableStateOf("") }

    val categories = listOf(
        "Alimentation" to "🛒",
        "Logement" to "🏠",
        "Transports" to "🚗",
        "Sorties & Loisirs" to "🍸",
        "Shopping" to "🛍️",
        "Abonnements" to "📱",
        "Santé" to "💊",
        "Investissement" to "📈",
        "Autre" to "💡"
    )

    // Voice recognition launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                // Parse spoken input, e.g. "45 euros restaurant" or "courses 60"
                val numberRegex = "\\b(\\d+[.,]?\\d*)\\b".toRegex()
                val match = numberRegex.find(spokenText)
                if (match != null) {
                    amountStr = match.value.replace(',', '.')
                }
                title = spokenText.replace(numberRegex, "").replace("euros", "").replace("euro", "").trim().replaceFirstChar { it.uppercase() }
                if (spokenText.contains("resto", ignoreCase = true) || spokenText.contains("bar", ignoreCase = true)) {
                    selectedCategory = "Sorties & Loisirs"
                    selectedType = "ENVIE"
                } else if (spokenText.contains("course", ignoreCase = true) || spokenText.contains("manger", ignoreCase = true)) {
                    selectedCategory = "Alimentation"
                    selectedType = "BESOIN"
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BrickSurfaceElevated,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Nouvelle Dépense",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )

                IconButton(
                    onClick = {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.FRENCH.toString())
                            putExtra(RecognizerIntent.EXTRA_PROMPT, "Dis par exemple : '45 euros restaurant'")
                        }
                        try {
                            speechLauncher.launch(intent)
                        } catch (e: Exception) {
                            // Ignored if no recognizer available
                        }
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(ElectricCyan.copy(alpha = 0.2f))
                        .testTag("voice_expense_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Saisie vocale",
                        tint = ElectricCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Amount Field
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Montant ($currency)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = BrickSurfaceBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_amount_input")
                )

                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Description (ex: Supermarché, Resto)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = BrickSurfaceBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_title_input")
                )

                // Type Toggle (50/30/20)
                Text(
                    text = "Type (Règle 50 / 30 / 20) :",
                    fontSize = 11.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TypePill(
                        label = "Besoin (50%)",
                        color = NeedColor,
                        isSelected = selectedType == "BESOIN",
                        modifier = Modifier.weight(1f)
                    ) {
                        selectedType = "BESOIN"
                    }
                    TypePill(
                        label = "Envie (30%)",
                        color = WantColor,
                        isSelected = selectedType == "ENVIE",
                        modifier = Modifier.weight(1f)
                    ) {
                        selectedType = "ENVIE"
                    }
                    TypePill(
                        label = "Épargne (20%)",
                        color = SaveColor,
                        isSelected = selectedType == "EPARGNE",
                        modifier = Modifier.weight(1f)
                    ) {
                        selectedType = "EPARGNE"
                    }
                }

                // Category Chips
                Text(
                    text = "Catégorie :",
                    fontSize = 11.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.SemiBold
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { (cat, emoji) ->
                        val isSelected = selectedCategory == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) ElectricCyan else BrickSurface)
                                .border(
                                    1.dp,
                                    if (isSelected) ElectricCyan else BrickSurfaceBorder,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    selectedCategory = cat
                                    if (cat in listOf("Sorties & Loisirs", "Shopping")) {
                                        selectedType = "ENVIE"
                                    } else if (cat in listOf("Logement", "Alimentation", "Transports", "Santé")) {
                                        selectedType = "BESOIN"
                                    } else if (cat == "Investissement") {
                                        selectedType = "EPARGNE"
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "$emoji $cat",
                                fontSize = 11.sp,
                                color = if (isSelected) BrickBackground else TextWhite,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                // Priority Selection
                Text(
                    text = "Niveau de priorité :",
                    fontSize = 11.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("ESSENTIEL", "UTILE", "SUPERFLU").forEach { prio ->
                        val isSelected = selectedPriority == prio
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) NeonVioletGlow.copy(alpha = 0.3f) else BrickSurface)
                                .border(
                                    1.dp,
                                    if (isSelected) NeonVioletGlow else BrickSurfaceBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedPriority = prio }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = prio.lowercase().replaceFirstChar { it.uppercase() },
                                fontSize = 11.sp,
                                color = if (isSelected) TextWhite else TextMuted,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.replace(',', '.').toDoubleOrNull() ?: 0.0
                    if (amount > 0.0) {
                        onConfirm(
                            title.ifBlank { selectedCategory },
                            amount,
                            selectedCategory,
                            selectedType,
                            selectedPriority,
                            note
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                modifier = Modifier.testTag("confirm_add_expense_btn")
            ) {
                Text(
                    text = "Ajouter la dépense",
                    color = BrickBackground,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Annuler", color = TextMuted)
            }
        }
    )
}

@Composable
private fun TypePill(
    label: String,
    color: Color,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) color.copy(alpha = 0.25f) else BrickSurface)
            .border(1.dp, if (isSelected) color else BrickSurfaceBorder, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (isSelected) color else TextMuted,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
