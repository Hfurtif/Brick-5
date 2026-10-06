package com.example

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.SmartExpenseParser
import com.example.data.db.BrickDatabase
import com.example.data.model.TransactionEntity
import com.example.ui.theme.BrickBackground
import com.example.ui.theme.BrickSurface
import com.example.ui.theme.BrickSurfaceBorder
import com.example.ui.theme.BrickSurfaceElevated
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.BrickTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class QuickAddActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BrickTheme {
                QuickAddFloatingContent(
                    onDismiss = { finish() },
                    onSaved = {
                        triggerHaptic(this)
                        finish()
                    }
                )
            }
        }
    }

    private fun triggerHaptic(context: Context) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(50)
            }
        }
    }
}

@Composable
fun QuickAddFloatingContent(
    onDismiss: () -> Unit,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val nameFocusRequester = remember { FocusRequester() }

    var expenseName by remember { mutableStateOf("") }
    var expenseAmountStr by remember { mutableStateOf("") }

    // Smart computation to deduce category and amount
    val resolvedExpense = remember(expenseName, expenseAmountStr) {
        val explicitAmount = expenseAmountStr.replace(',', '.').toDoubleOrNull()
        if (explicitAmount != null && explicitAmount > 0.0) {
            val parsed = SmartExpenseParser.parse(expenseName)
            parsed.copy(
                title = expenseName.ifBlank { "Dépense" }.replaceFirstChar { it.uppercase() },
                amount = explicitAmount
            )
        } else {
            // Check if user typed "piment 50" directly in the name field
            SmartExpenseParser.parse(expenseName)
        }
    }

    // Auto focus name on open
    LaunchedEffect(Unit) {
        delay(120)
        nameFocusRequester.requestFocus()
    }

    fun submitExpense() {
        if (resolvedExpense.amount > 0 && resolvedExpense.title.isNotBlank()) {
            scope.launch {
                val db = BrickDatabase.getInstance(context)
                db.transactionDao().insertTransaction(
                    TransactionEntity(
                        title = resolvedExpense.title,
                        amount = resolvedExpense.amount,
                        category = resolvedExpense.category,
                        type = resolvedExpense.type,
                        priority = resolvedExpense.priority
                    )
                )
                com.example.widget.BrickResteAVivreWidgetProvider.updateAllWidgets(context)
                onSaved()
            }
        }
    }

    // Outer background: translucent touch outside dismisses
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Floating minimal dialogue box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(BrickSurfaceElevated, BrickSurface)
                    )
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        listOf(ElectricCyan, NeonViolet, BrickSurfaceBorder)
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { /* stop propagation */ }
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
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
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ElectricCyan.copy(alpha = 0.15f))
                                .border(1.dp, ElectricCyan, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Ajout Dépense Rapide",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextWhite
                            )
                            Text(
                                text = "Saisie directe sans menu",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fermer",
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Field 1: Nom
                OutlinedTextField(
                    value = expenseName,
                    onValueChange = { expenseName = it },
                    placeholder = { Text("Ex: Piment, Taxi, Courses...", color = TextMuted) },
                    label = { Text("Nom de la dépense") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
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
                        .fillMaxWidth()
                        .focusRequester(nameFocusRequester)
                        .testTag("dialog_expense_name_input")
                )

                // Field 2: Montant
                OutlinedTextField(
                    value = expenseAmountStr,
                    onValueChange = { expenseAmountStr = it },
                    placeholder = { Text("Ex: 50, 500, 2500...", color = TextMuted) },
                    label = { Text("Montant") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { submitExpense() }
                    ),
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
                        .fillMaxWidth()
                        .testTag("dialog_expense_amount_input")
                )

                // Live category & validation info
                if (resolvedExpense.amount > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(BrickSurface)
                            .border(1.dp, ElectricCyan.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "⚡ ${resolvedExpense.title} • ${String.format("%,.0f", resolvedExpense.amount).replace(',', ' ')} • ${resolvedExpense.category} (${resolvedExpense.type})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan
                        )
                    }
                }

                // Large Add Button
                Button(
                    onClick = { submitExpense() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("dialog_add_expense_btn"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = BrickBackground
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (resolvedExpense.amount > 0) "AJOUTER (${String.format("%,.0f", resolvedExpense.amount).replace(',', ' ')})" else "AJOUTER LA DÉPENSE",
                        color = BrickBackground,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
