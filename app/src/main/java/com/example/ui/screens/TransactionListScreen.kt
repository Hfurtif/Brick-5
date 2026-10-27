package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Transaction
import com.example.ui.components.FormattedAmountText
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Jetpack Compose screen that displays the full list of financial transactions
 * retrieved from the local Room database, complete with filters, statistics summary,
 * search, and a Floating Action Button to trigger adding a new transaction.
 */
@Composable
fun TransactionListScreen(
    transactions: List<Transaction>,
    currency: String = "FCFA",
    hideAmounts: Boolean = false,
    onAddTransactionClick: () -> Unit,
    onDeleteTransaction: (Transaction) -> Unit,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (onBackClick != null) {
        BackHandler { onBackClick() }
    }

    var selectedFilter by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }
    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.getDefault()) }

    // Filter transactions
    val filteredTransactions = remember(transactions, selectedFilter, searchQuery) {
        transactions.filter { tx ->
            val matchesFilter = when (selectedFilter) {
                "INCOME" -> tx.type == "REVENU" || tx.type == "INCOME"
                "EXPENSE" -> tx.type == "DEPENSE" || tx.type == "EXPENSE"
                "NEEDS" -> tx.priority == "NEEDS"
                "WANTS" -> tx.priority == "WANTS"
                "SAVINGS" -> tx.priority == "SAVINGS"
                else -> true
            }

            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                tx.title.contains(searchQuery, ignoreCase = true) ||
                    tx.category.contains(searchQuery, ignoreCase = true) ||
                    tx.note.contains(searchQuery, ignoreCase = true)
            }

            matchesFilter && matchesSearch
        }
    }

    // Totals
    val totalIncome = remember(transactions) {
        transactions.filter { it.type == "REVENU" || it.type == "INCOME" }.sumOf { it.amount }
    }
    val totalExpense = remember(transactions) {
        transactions.filter { it.type == "DEPENSE" || it.type == "EXPENSE" }.sumOf { it.amount }
    }
    val netBalance = totalIncome - totalExpense

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = BrickBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTransactionClick,
                containerColor = BrickPrimary,
                contentColor = Color(0xFF0A0E1A),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .padding(bottom = 8.dp, end = 4.dp)
                    .testTag("add_transaction_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Ajouter une transaction",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Nouvelle Transaction",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .fillMaxHeight(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (onBackClick != null) {
                            IconButton(
                                onClick = onBackClick,
                                modifier = Modifier
                                    .size(40.dp)
                                    .testTag("back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Retour",
                                    tint = BrickTextPrimary
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Historique des Transactions",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = BrickTextPrimary,
                                fontSize = 20.sp,
                                softWrap = true,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${transactions.size} opérations enregistrées en base locale",
                                style = MaterialTheme.typography.bodySmall,
                                color = BrickTextMuted,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Balance and Statistics Summary Card
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = BrickSurfaceElevated,
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, BrickSurfaceBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "SOLDE NET DES FLUX",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = BrickTextMuted,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = if (hideAmounts) "••••••" else String.format("%,.0f %s", netBalance, currency),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (netBalance >= 0) EmeraldSuccess else CoralDanger
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = BrickSurfaceBorder)
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Entrées Totales", style = MaterialTheme.typography.labelSmall, color = BrickTextMuted, fontSize = 11.sp)
                                    Text(
                                        text = if (hideAmounts) "••••••" else "+${String.format("%,.0f %s", totalIncome, currency)}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldSuccess
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Dépenses Totales", style = MaterialTheme.typography.labelSmall, color = BrickTextMuted, fontSize = 11.sp)
                                    Text(
                                        text = if (hideAmounts) "••••••" else "-${String.format("%,.0f %s", totalExpense, currency)}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = CoralDanger
                                    )
                                }
                            }
                        }
                    }
                }

                // Search Bar
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("transaction_search_input"),
                        placeholder = { Text("Rechercher (titre, catégorie, note)...", color = BrickTextMuted, fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Rechercher", tint = BrickTextMuted)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Effacer", tint = BrickTextMuted)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrickPrimary,
                            unfocusedBorderColor = BrickSurfaceBorder,
                            focusedContainerColor = BrickSurface,
                            unfocusedContainerColor = BrickSurface,
                            focusedTextColor = BrickTextPrimary,
                            unfocusedTextColor = BrickTextPrimary
                        )
                    )
                }

                // Horizontal Scrollable Filter Chips (no text compression)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val filters = listOf(
                            "ALL" to "Toutes (${transactions.size})",
                            "EXPENSE" to "Dépenses 🔴",
                            "INCOME" to "Revenus 🟢",
                            "NEEDS" to "Besoins (50%)",
                            "WANTS" to "Envies (30%)",
                            "SAVINGS" to "Épargne (20%)"
                        )

                        filters.forEach { (key, label) ->
                            val isSelected = selectedFilter == key
                            Surface(
                                modifier = Modifier
                                    .clickable { selectedFilter = key }
                                    .height(36.dp)
                                    .testTag("filter_$key"),
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) BrickPrimary else BrickSurfaceElevated,
                                border = BorderStroke(1.dp, if (isSelected) BrickPrimary else BrickSurfaceBorder)
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 14.dp),
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

                // Transactions Count Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Opérations (${filteredTransactions.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = BrickTextPrimary
                        )
                    }
                }

                // Empty State
                if (filteredTransactions.isEmpty()) {
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("empty_transactions_card"),
                            color = BrickSurfaceElevated,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, BrickSurfaceBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "💳", fontSize = 38.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = if (searchQuery.isNotBlank() || selectedFilter != "ALL") {
                                        "Aucune transaction ne correspond à ce filtre"
                                    } else {
                                        "Aucune transaction enregistrée"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    color = BrickTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Touchez le bouton '+' ci-dessous pour ajouter une dépense ou une entrée de caisse.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = BrickTextMuted,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = onAddTransactionClick,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BrickPrimary)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF0A0E1A), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Ajouter une transaction", color = Color(0xFF0A0E1A), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    // Transaction Items
                    items(filteredTransactions, key = { it.id }) { tx ->
                        val isExpense = tx.type == "DEPENSE" || tx.type == "EXPENSE"
                        val formattedDate = remember(tx.date, tx.timestamp) {
                            val time = if (tx.date > 0) tx.date else tx.timestamp
                            dateFormat.format(Date(time))
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("transaction_item_${tx.id}"),
                            color = BrickSurfaceElevated,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, BrickSurfaceBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Income / Expense Indicator Icon
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .background(
                                                if (isExpense) CoralDanger.copy(alpha = 0.15f) else EmeraldSuccess.copy(alpha = 0.15f),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isExpense) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                            contentDescription = if (isExpense) "Dépense" else "Revenu",
                                            tint = if (isExpense) CoralDanger else EmeraldSuccess,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = tx.title.ifBlank { tx.category },
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = BrickTextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = tx.category,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = BrickPrimary,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(text = "•", color = BrickTextMuted, fontSize = 10.sp)
                                            Text(
                                                text = when (tx.priority) {
                                                    "NEEDS" -> "Besoins (50%)"
                                                    "WANTS" -> "Envies (30%)"
                                                    "SAVINGS" -> "Épargne (20%)"
                                                    else -> tx.priority
                                                },
                                                style = MaterialTheme.typography.labelSmall,
                                                color = BrickTextMuted,
                                                fontSize = 11.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = formattedDate,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = BrickTextMuted.copy(alpha = 0.7f),
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        FormattedAmountText(
                                            amount = tx.amount,
                                            currency = currency,
                                            hideAmounts = hideAmounts,
                                            color = if (isExpense) CoralDanger else EmeraldSuccess,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            prefix = if (isExpense) "- " else "+ "
                                        )
                                        if (tx.isAsset) {
                                            Text(
                                                text = "Actif",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = CyberGold,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(4.dp))

                                    IconButton(
                                        onClick = { transactionToDelete = tx },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .testTag("delete_tx_${tx.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Supprimer la transaction",
                                            tint = BrickTextMuted.copy(alpha = 0.6f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Space for FAB clearance
                item {
                    Spacer(modifier = Modifier.height(72.dp))
                }
            }
        }
    }

    // Confirmation dialog before deleting
    if (transactionToDelete != null) {
        val tx = transactionToDelete!!
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = {
                Text(
                    text = "Supprimer cette transaction ?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrickTextPrimary
                )
            },
            text = {
                Text(
                    text = "Voulez-vous vraiment supprimer '${tx.title.ifBlank { tx.category }}' de ${String.format("%,.0f %s", tx.amount, currency)} ? Cette action mettra à jour vos soldes et alertes de budget.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BrickTextMuted
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTransaction(tx)
                        transactionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralDanger)
                ) {
                    Text("Supprimer", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text("Annuler", color = BrickTextMuted)
                }
            },
            containerColor = BrickSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
