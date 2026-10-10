package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.SmartExpenseParser
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
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.WantColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun UltraFastExpenseSheet(
    currency: String,
    todayTotal: Double,
    onDismiss: () -> Unit,
    onSaveExpense: (title: String, amount: Double, category: String, type: String, priority: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }

    // Quick single-phrase bar OR 2 fields
    var isSentenceMode by remember { mutableStateOf(true) }
    var sentenceInput by remember { mutableStateOf("") }
    var itemName by remember { mutableStateOf("") }
    var itemAmountStr by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Alimentation") }
    var selectedType by remember { mutableStateOf("BESOIN") }

    var isConfirmed by remember { mutableStateOf(false) }
    var confirmedAmount by remember { mutableStateOf(0.0) }

    // Realtime intelligent parsing
    val parsedResult = remember(sentenceInput, isSentenceMode, itemName, itemAmountStr) {
        if (isSentenceMode) {
            SmartExpenseParser.parse(sentenceInput)
        } else {
            val amt = itemAmountStr.replace(',', '.').toDoubleOrNull() ?: 0.0
            val p = SmartExpenseParser.parse(itemName)
            p.copy(title = itemName.ifBlank { p.title }, amount = amt)
        }
    }

    LaunchedEffect(Unit) {
        delay(150)
        focusRequester.requestFocus()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BrickSurfaceElevated,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .size(width = 44.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(ElectricCyan.copy(alpha = 0.5f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(ElectricCyan.copy(alpha = 0.15f))
                            .border(1.dp, ElectricCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Saisie Éclair (5 sec)",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = "Aujourd'hui : ${todayTotal.toInt()} $currency dépensés",
                            fontSize = 11.sp,
                            color = ElectricCyan
                        )
                    }
                }

                // Mode switch
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(BrickSurface)
                        .border(1.dp, BrickSurfaceBorder, RoundedCornerShape(10.dp))
                        .clickable { isSentenceMode = !isSentenceMode }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isSentenceMode) "2 champs" else "1 phrase",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonVioletGlow
                    )
                }
            }

            if (!isConfirmed) {
                if (isSentenceMode) {
                    // Smart Phrase input
                    OutlinedTextField(
                        value = sentenceInput,
                        onValueChange = { sentenceInput = it },
                        placeholder = { Text("Ex: piment 50 ou taxi 500", color = TextMuted) },
                        label = { Text("Saisie rapide (nom + montant)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (parsedResult.amount > 0) {
                                    confirmedAmount = parsedResult.amount
                                    isConfirmed = true
                                    onSaveExpense(
                                        parsedResult.title,
                                        parsedResult.amount,
                                        parsedResult.category,
                                        parsedResult.type,
                                        parsedResult.priority
                                    )
                                    scope.launch {
                                        delay(800)
                                        sheetState.hide()
                                        onDismiss()
                                    }
                                }
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = BrickSurfaceBorder,
                            focusedContainerColor = BrickSurface,
                            unfocusedContainerColor = BrickSurface
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .testTag("quick_phrase_input")
                    )
                } else {
                    // Two field input: Nom + Montant
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = itemName,
                            onValueChange = { itemName = it },
                            placeholder = { Text("Ex: Piment", color = TextMuted) },
                            label = { Text("Article") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = BrickSurfaceBorder,
                                focusedContainerColor = BrickSurface,
                                unfocusedContainerColor = BrickSurface
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1.5f)
                                .focusRequester(focusRequester)
                                .testTag("quick_item_name_input")
                        )

                        OutlinedTextField(
                            value = itemAmountStr,
                            onValueChange = { itemAmountStr = it },
                            placeholder = { Text("50", color = TextMuted) },
                            label = { Text("Prix ($currency)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = BrickSurfaceBorder,
                                focusedContainerColor = BrickSurface,
                                unfocusedContainerColor = BrickSurface
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quick_item_amount_input")
                        )
                    }
                }

                // AI Live Prediction Tag
                if (parsedResult.amount > 0 || parsedResult.title.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(BrickSurface)
                            .border(1.dp, ElectricCyan.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "IA BRICK : ${parsedResult.title} • ${parsedResult.amount} $currency • ${parsedResult.category} (${parsedResult.type})",
                            fontSize = 11.sp,
                            color = TextWhite,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Big Add Button
                Button(
                    onClick = {
                        val amount = parsedResult.amount
                        if (amount > 0.0) {
                            confirmedAmount = amount
                            isConfirmed = true
                            onSaveExpense(
                                parsedResult.title,
                                amount,
                                parsedResult.category,
                                parsedResult.type,
                                parsedResult.priority
                            )
                            scope.launch {
                                delay(800)
                                sheetState.hide()
                                onDismiss()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("submit_quick_expense_btn"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when (parsedResult.type) {
                            "ENTREE" -> EmeraldSuccess
                            "INVESTISSEMENT_AFFAIRES" -> NeonViolet
                            "DON" -> CyberGold
                            else -> ElectricCyan
                        }
                    )
                ) {
                    val isSpecial = parsedResult.type in listOf("ENTREE", "INVESTISSEMENT_AFFAIRES", "DON")
                    val textColor = if (parsedResult.type == "INVESTISSEMENT_AFFAIRES") TextWhite else BrickBackground
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = textColor
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (parsedResult.type) {
                            "ENTREE" -> "💰 NOTER L'ENTRÉE (+${parsedResult.amount.toInt()} $currency)"
                            "INVESTISSEMENT_AFFAIRES" -> "📦 NOTER L'ACHAT STOCK (${parsedResult.amount.toInt()} $currency)"
                            "DON" -> "🕊️ NOTER LE DON (${parsedResult.amount.toInt()} $currency)"
                            else -> "ENREGISTRER EN 5s (${parsedResult.amount.toInt()} $currency)"
                        },
                        color = textColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp
                    )
                }
            } else {
                // Confirmation State with Animation
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Validé",
                        tint = EmeraldSuccess,
                        modifier = Modifier.size(54.dp)
                    )

                    Text(
                        text = "Dépense enregistrée !",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )

                    Text(
                        text = "Nouveau total aujourd'hui : ${(todayTotal + confirmedAmount).toInt()} $currency",
                        fontSize = 13.sp,
                        color = ElectricCyan,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
