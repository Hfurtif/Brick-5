package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.data.model.UserProfile
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.BrickViewModel
import com.example.ui.viewmodel.Budget503020ViewModel

data class NavigationItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

class MainActivity : FragmentActivity() {

    private val viewModel: BrickViewModel by viewModels()
    private val budgetViewModel: Budget503020ViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val userProfile by viewModel.userProfile.collectAsState()
            var showSplash by remember { mutableStateOf(true) }

            BrickTheme(themeMode = userProfile?.themeMode ?: "DARK") {
                if (showSplash) {
                    BrickAnimatedSplashScreen(
                        onAnimationFinished = { showSplash = false }
                    )
                } else {
                    val isLocked by viewModel.isPinLocked.collectAsState()
                    if (isLocked && userProfile?.pinCode?.isNotEmpty() == true) {
                        BiometricLockScreen(
                            pinCode = userProfile?.pinCode ?: "",
                            biometricEnabled = userProfile?.biometricEnabled ?: false,
                            onUnlocked = { viewModel.unlockPin() }
                        )
                    } else if (userProfile?.isOnboarded == false) {
                        OnboardingScreen(
                            initialUser = userProfile,
                            onComplete = { updatedProfile, cashLiquide, cashMobile, cashFamily ->
                                viewModel.updateProfile(updatedProfile)

                                // Inject starting caisse into transactions & assets
                                if (cashLiquide > 0) {
                                    viewModel.addTransaction(
                                        title = "Solde initial - Caisse Espèces",
                                        amount = cashLiquide,
                                        category = "Caisse / Liquide",
                                        type = "REVENU",
                                        priority = "NEEDS"
                                    )
                                    viewModel.addAssetLiability(
                                        name = "Caisse Espèces & Poche",
                                        type = "ACTIF",
                                        category = "Caisse",
                                        value = cashLiquide
                                    )
                                }
                                if (cashMobile > 0) {
                                    viewModel.addTransaction(
                                        title = "Solde initial - Mobile Money & Banque",
                                        amount = cashMobile,
                                        category = "Mobile Money",
                                        type = "REVENU",
                                        priority = "NEEDS"
                                    )
                                    viewModel.addAssetLiability(
                                        name = "Mobile Money & Banque",
                                        type = "ACTIF",
                                        category = "Mobile Money",
                                        value = cashMobile
                                    )
                                }
                                if (cashFamily > 0) {
                                    viewModel.addTransaction(
                                        title = "Solde initial - Tontine & Famille",
                                        amount = cashFamily,
                                        category = "Tontine / Famille",
                                        type = "REVENU",
                                        priority = "SAVINGS"
                                    )
                                    viewModel.addAssetLiability(
                                        name = "Tontine & Réserve Famille",
                                        type = "ACTIF",
                                        category = "Tontine",
                                        value = cashFamily
                                    )
                                }
                            }
                        )
                    } else {
                        MainAppContent(
                            viewModel = viewModel,
                            budgetViewModel = budgetViewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainAppContent(
    viewModel: BrickViewModel,
    budgetViewModel: Budget503020ViewModel
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val snapshot by viewModel.financialSnapshot.collectAsState()
    val todaySpent by viewModel.todayTotalSpent.collectAsState()
    val badges by viewModel.badges.collectAsState()
    val assetsLiabilities by viewModel.assetsLiabilities.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()

    val showAddExpense by viewModel.showAddExpenseDialog.collectAsState()
    val addExpenseMode by viewModel.addExpenseInitialMode.collectAsState()
    val showQuickAdd by viewModel.showQuickAddSheet.collectAsState()
    val showEveningRecap by viewModel.showEveningRecap.collectAsState()
    val showSimulator by viewModel.showPurchaseSimulator.collectAsState()
    val simulationResult by viewModel.simulationResult.collectAsState()
    val coachMessages by viewModel.coachMessages.collectAsState()
    val isCoachTyping by viewModel.isCoachTyping.collectAsState()

    val currency = userProfile?.currency ?: "FCFA"

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = BrickBackground,
        bottomBar = {
            BrickBottomNavigationBar(
                selectedScreen = currentScreen,
                onSelectScreen = { viewModel.selectScreen(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                0 -> DashboardScreen(
                    user = userProfile,
                    snapshot = snapshot,
                    transactions = transactions,
                    todayTotalSpent = todaySpent,
                    onToggleHideAmounts = { viewModel.toggleHideAmounts() },
                    onQuickAdd = { viewModel.openQuickAddSheet() },
                    onAddExpense = { viewModel.openAddExpenseDialog("DEPENSE") },
                    onAddIncome = { viewModel.openAddExpenseDialog("REVENU") },
                    onOpenEveningRecap = { viewModel.openEveningRecap() },
                    onOpenSimulator = { viewModel.openPurchaseSimulator() },
                    onNavigateToBudget = { viewModel.selectScreen(2) },
                    onDeleteTransaction = { viewModel.deleteTransaction(it) }
                )

                1 -> BusinessFlowScreen(
                    transactions = transactions,
                    currency = currency,
                    onAddBusinessFlow = { viewModel.openAddExpenseDialog("REVENU") }
                )

                2 -> BudgetAnalyticsScreen(
                    viewModel = budgetViewModel
                )

                3 -> AssetsLiabilitiesScreen(
                    assetsLiabilities = assetsLiabilities,
                    currency = currency,
                    hideAmounts = userProfile?.hideAmounts ?: false,
                    onAddAssetLiability = { name, type, cat, value, cashflow, note ->
                        viewModel.addAssetLiability(name, type, cat, value, cashflow, note)
                    },
                    onDeleteAssetLiability = { viewModel.deleteAssetLiability(it) }
                )

                4 -> CoachAiScreen(
                    messages = coachMessages,
                    isTyping = isCoachTyping,
                    onSendMessage = { viewModel.askCoach(it) }
                )

                5 -> ProfileSecurityScreen(
                    user = userProfile,
                    badges = badges,
                    onUpdateProfile = { viewModel.updateProfile(it) },
                    onResetData = { viewModel.resetData() }
                )
            }
        }
    }

    // Dialogs & Sheets
    if (showAddExpense) {
        AddExpenseDialog(
            initialMode = addExpenseMode,
            currency = currency,
            onDismiss = { viewModel.closeAddExpenseDialog() },
            onSubmit = { title, amount, category, type, priority, note ->
                viewModel.addTransaction(title, amount, category, type, priority, note)
            }
        )
    }

    if (showQuickAdd) {
        UltraFastExpenseSheet(
            currency = currency,
            onDismiss = { viewModel.closeQuickAddSheet() },
            onSubmit = { title, amount, category, type, priority ->
                viewModel.addTransaction(title, amount, category, type, priority)
            }
        )
    }

    if (showSimulator) {
        PurchaseSimulatorDialog(
            currency = currency,
            simulationResult = simulationResult,
            onDismiss = { viewModel.closePurchaseSimulator() },
            onSimulate = { amount, title ->
                viewModel.simulatePurchase(amount, title)
            }
        )
    }

    if (showEveningRecap) {
        val todayTxs by viewModel.todayTransactions.collectAsState()
        EveningRecapDialog(
            todayTransactions = todayTxs,
            todayTotalSpent = todaySpent,
            resteAVivreDuJour = snapshot.resteAVivreDuJour,
            currency = currency,
            streakDays = userProfile?.streakDays ?: 1,
            onDismiss = { viewModel.closeEveningRecap() }
        )
    }
}

@Composable
fun BrickBottomNavigationBar(
    selectedScreen: Int,
    onSelectScreen: (Int) -> Unit
) {
    val items = listOf(
        NavigationItem("Accueil", Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
        NavigationItem("Affaires", Icons.Filled.Business, Icons.Outlined.Business, "nav_business"),
        NavigationItem("Budgets", Icons.Filled.PieChart, Icons.Outlined.PieChart, "nav_budgets"),
        NavigationItem("Actifs", Icons.Filled.AccountBalance, Icons.Outlined.AccountBalance, "nav_assets"),
        NavigationItem("Coach", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, "nav_coach"),
        NavigationItem("Profil", Icons.Filled.Security, Icons.Outlined.Security, "nav_profile")
    )

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .border(
                1.dp,
                Brush.verticalGradient(listOf(BrickSurfaceBorder, Color.Transparent)),
                RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            ),
        containerColor = BrickSurface,
        tonalElevation = 8.dp
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = selectedScreen == index
            NavigationBarItem(
                selected = isSelected,
                onClick = { onSelectScreen(index) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                        tint = if (isSelected) BrickPrimary else BrickTextMuted
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) BrickPrimary else BrickTextMuted,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BrickPrimary,
                    selectedTextColor = BrickPrimary,
                    unselectedIconColor = BrickTextMuted,
                    unselectedTextColor = BrickTextMuted,
                    indicatorColor = BrickPrimary.copy(alpha = 0.15f)
                ),
                modifier = Modifier.testTag(item.testTag)
            )
        }
    }
}
