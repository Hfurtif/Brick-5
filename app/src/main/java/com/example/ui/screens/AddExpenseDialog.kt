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
import com.example.ui.theme.CyberGold
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NeedColor
import com.example.ui.theme.NeonViolet
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
    initialFlowType: String = "DEPENSE",
    onDismiss: () -> Unit,
    onConfirm: (title: String, amount: Double, category: String, type: String, priority: String, note: String) -> Unit
) {
    var flowType by remember { mutableStateOf(initialFlowType) } // DEPENSE, ENTREE, INVESTISSEMENT, DON
    var amountStr by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember {
        mutableStateOf(
            when (initialFlowType) {
                "ENTREE" -> "Vente & Commerce"
                "INVESTISSEMENT" -> "Achat Stock & Marchandise"
                "DON" -> "Dîme (10%)"
                else -> "Alimentation"
            }
        )
    }
    var selectedType by remember { mutableStateOf("BESOIN") }
    var selectedPriority by remember { mutableStateOf("UTILE") }
    var note by remember { mutableStateOf("") }

    val expenseCategories = listOf(
        "Alimentation" to "🛒",
        "Logement" to "🏠",
        "Transports" to "🚗",
        "Sorties & Loisirs" to "🍸",
        "Shopping" to "🛍️",
        "Abonnements" to "📱",
        "Santé" to "💊",
        "Autre" to "💡"
    )

    val inflowCategories = listOf(
        "Vente & Commerce" to "📦",
        "Prestation & Contrat" to "💼",
        "Bénéfice d'Affaires" to "📈",
        "Commission & Bonus" to "💰",
        "Salaire / Paie" to "💵",
        "Entrée Diverse" to "⚡"
    )

    val investCategories = listOf(
        "Achat Stock & Marchandise" to "📦",
        "Outils & Matériel Pro" to "⚙️",
        "Marketing & Publicité" to "📢",
        "Capital & Associés" to "🏢",
        "Formation & Savoir" to "📚"
    )

    val donCategories = listOf(
        "Dîme (10%)" to "🕊️",
        "Aumône & Charité" to "🤲",
        "Soutien Famille & Proches" to "👨‍👩‍👧",
        "Don Communautaire" to "🤝"
    )

    val activeCategories = when (flowType) {
        "ENTREE" -> inflowCategories
        "INVESTISSEMENT" -> investCategories
        "DON" -> donCategories
        else -> expenseCategories
    }

    // Voice recognition launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                val numberRegex = "\\b(\\d+[.,]?\\d*)\\b".toRegex()
                val match = numberRegex.find(spokenText)
                if (match != null) {
                    amountStr = match.value.replace(',', '.')
                }
                title = spokenText.replace(numberRegex, "").replace("euros", "").replace("euro", "").trim().replaceFirstChar { it.uppercase() }

                // Heuristic detection for business flow keywords
                val lower = spokenText.lowercase()
                if (lower.contains("vente") || lower.contains("client") || lower.contains("contrat") || lower.contains("chiffre")) {
                    flowType = "ENTREE"
                    selectedCategory = "Vente & Commerce"
                } else if (lower.contains("stock") || lower.contains("marchandise") || lower.contains("matériel") || lower.contains("outil")) {
                    flowType = "INVESTISSEMENT"
                    selectedCategory = "Achat Stock & Marchandise"
                } else if (lower.contains("don") || lower.contains("dime") || lower.contains("dîme") || lower.contains("aumône")) {
                    flowType = "DON"
                    selectedCategory = "Dîme (10%)"
                } else if (lower.contains("resto") || lower.contains("bar") || lower.contains("sortir")) {
                    flowType = "DEPENSE"
                    selectedCategory = "Sorties & Loisirs"
                    selectedType = "ENVIE"
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
                    text = when (flowType) {
                        "ENTREE" -> "💰 Entrée / Ventes"
                        "INVESTISSEMENT" -> "📦 Investissement Business"
                        "DON" -> "🕊️ Don & Dîme"
                        else -> "💸 Nouvelle Dépense"
                    },
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )

                IconButton(
                    onClick = {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.FRENCH.toString())
                            putExtra(RecognizerIntent.EXTRA_PROMPT, "Dis par exemple : 'vente 45' ou 'don 20' ou 'courses 60'")
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
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Flow Type Switcher Tabs (Dépense, Entrée, Invest, Don)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        "DEPENSE" to "Dépense",
                        "ENTREE" to "Entrée (+)",
                        "INVESTISSEMENT" to "Invest. (📦)",
                        "DON" to "Don (🕊️)"
                    ).forEach { (fType, label) ->
                        val isSelected = flowType == fType
                        val color = when (fType) {
                            "ENTREE" -> EmeraldSuccess
                            "INVESTISSEMENT" -> NeonVioletGlow
                            "DON" -> CyberGold
                            else -> ElectricCyan
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) color else BrickSurface)
                                .border(1.dp, if (isSelected) color else BrickSurfaceBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    flowType = fType
                                    selectedCategory = when (fType) {
                                        "ENTREE" -> "Vente & Commerce"
                                        "INVESTISSEMENT" -> "Achat Stock & Marchandise"
                                        "DON" -> "Dîme (10%)"
                                        else -> "Alimentation"
                                    }
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (isSelected) BrickBackground else TextMuted
                            )
                        }
                    }
                }

                // Amount Field
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Montant ($currency)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = when (flowType) {
                            "ENTREE" -> EmeraldSuccess
                            "INVESTISSEMENT" -> NeonVioletGlow
                            "DON" -> CyberGold
                            else -> ElectricCyan
                        },
                        unfocusedBorderColor = BrickSurfaceBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_amount_input")
                )

                // Description Field
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = {
                        Text(
                            when (flowType) {
                                "ENTREE" -> "Description (ex: Vente boutique, Client X)"
                                "INVESTISSEMENT" -> "Description (ex: Achat cartons stock)"
                                "DON" -> "Description (ex: Dîme dimanche, Aumône)"
                                else -> "Description (ex: Supermarché, Resto)"
                            }
                        )
                    },
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

                // Sub-type selector (Only for Dépenses standard 50/30/20)
                if (flowType == "DEPENSE") {
                    Text(
                        text = "Classification Dépense :",
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
                }

                // Category Chips
                Text(
                    text = when (flowType) {
                        "ENTREE" -> "Source de revenu / commerce :"
                        "INVESTISSEMENT" -> "Type d'investissement d'affaires :"
                        "DON" -> "Bénéficiaire du don :"
                        else -> "Catégorie :"
                    },
                    fontSize = 11.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.SemiBold
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    activeCategories.forEach { (cat, emoji) ->
                        val isSelected = selectedCategory == cat
                        val activeHighlight = when (flowType) {
                            "ENTREE" -> EmeraldSuccess
                            "INVESTISSEMENT" -> NeonVioletGlow
                            "DON" -> CyberGold
                            else -> ElectricCyan
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) activeHighlight else BrickSurface)
                                .border(
                                    1.dp,
                                    if (isSelected) activeHighlight else BrickSurfaceBorder,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    selectedCategory = cat
                                    if (flowType == "DEPENSE") {
                                        if (cat in listOf("Sorties & Loisirs", "Shopping")) {
                                            selectedType = "ENVIE"
                                        } else if (cat in listOf("Logement", "Alimentation", "Transports", "Santé")) {
                                            selectedType = "BESOIN"
                                        }
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

                // Priority Selection (Optional note)
                if (flowType == "DEPENSE") {
                    Text(
                        text = "Priorité :",
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
                                    .padding(vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = prio.lowercase().replaceFirstChar { it.uppercase() },
                                    fontSize = 10.sp,
                                    color = if (isSelected) TextWhite else TextMuted,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
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
                        val computedType = when (flowType) {
                            "ENTREE" -> "ENTREE"
                            "INVESTISSEMENT" -> "INVESTISSEMENT_AFFAIRES"
                            "DON" -> "DON"
                            else -> selectedType
                        }
                        val computedPriority = when (flowType) {
                            "ENTREE", "INVESTISSEMENT" -> "ESSENTIEL"
                            "DON" -> "UTILE"
                            else -> selectedPriority
                        }

                        onConfirm(
                            title.ifBlank { selectedCategory },
                            amount,
                            selectedCategory,
                            computedType,
                            computedPriority,
                            note
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = when (flowType) {
                        "ENTREE" -> EmeraldSuccess
                        "INVESTISSEMENT" -> NeonViolet
                        "DON" -> CyberGold
                        else -> ElectricCyan
                    }
                ),
                modifier = Modifier.testTag("confirm_add_expense_btn")
            ) {
                Text(
                    text = when (flowType) {
                        "ENTREE" -> "Enregistrer l'Entrée (+)"
                        "INVESTISSEMENT" -> "Enregistrer l'Investissement"
                        "DON" -> "Enregistrer le Don"
                        else -> "Ajouter la Dépense"
                    },
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
