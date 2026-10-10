package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiCoachService
import com.example.ai.BrickBadge
import com.example.ai.CoachMessage
import com.example.ai.FinancialSnapshot
import com.example.ai.LocalFinancialEngine
import com.example.ai.PurchaseSimulationResult
import com.example.data.db.BrickDatabase
import com.example.data.model.AssetLiability
import com.example.data.model.FinancialGoal
import com.example.data.model.TransactionEntity
import com.example.data.model.UserProfile
import com.example.data.repository.BrickRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.notification.DailyEveningRecapWorker
import java.util.Calendar

class BrickViewModel(application: Application) : AndroidViewModel(application) {

    private val db = BrickDatabase.getInstance(application)
    private val repository = BrickRepository(
        userDao = db.userDao(),
        transactionDao = db.transactionDao(),
        goalDao = db.goalDao(),
        assetDao = db.assetDao()
    )
    private val aiCoachService = AiCoachService(application)

    // Active bottom navigation screen
    private val _currentScreen = MutableStateFlow(0)
    val currentScreen: StateFlow<Int> = _currentScreen.asStateFlow()

    // Dialogs & Modals
    private val _addExpenseInitialMode = MutableStateFlow("DEPENSE")
    val addExpenseInitialMode: StateFlow<String> = _addExpenseInitialMode.asStateFlow()

    private val _showAddExpenseDialog = MutableStateFlow(false)
    val showAddExpenseDialog: StateFlow<Boolean> = _showAddExpenseDialog.asStateFlow()

    private val _showQuickAddSheet = MutableStateFlow(false)
    val showQuickAddSheet: StateFlow<Boolean> = _showQuickAddSheet.asStateFlow()

    private val _showEveningRecap = MutableStateFlow(false)
    val showEveningRecap: StateFlow<Boolean> = _showEveningRecap.asStateFlow()

    private val _showPurchaseSimulator = MutableStateFlow(false)
    val showPurchaseSimulator: StateFlow<Boolean> = _showPurchaseSimulator.asStateFlow()

    private val _isPinLocked = MutableStateFlow(false)
    val isPinLocked: StateFlow<Boolean> = _isPinLocked.asStateFlow()

    // Simulation state
    private val _simulationResult = MutableStateFlow<PurchaseSimulationResult?>(null)
    val simulationResult: StateFlow<PurchaseSimulationResult?> = _simulationResult.asStateFlow()

    // Chat messages
    private val _coachMessages = MutableStateFlow<List<CoachMessage>>(
        listOf(
            CoachMessage(
                sender = "COACH",
                text = "Bienvenue sur BRICK ! Je suis ton coach financier personnel.\nMon objectif : t'aider à stopper les dépenses compulsives, séparer impitoyablement tes Besoins de tes Envies, et bâtir de solides fondations (Actifs vs Passifs). Comment puis-je t'aider aujourd'hui ?",
                source = "LOCAL"
            )
        )
    )
    val coachMessages: StateFlow<List<CoachMessage>> = _coachMessages.asStateFlow()

    private val _isCoachTyping = MutableStateFlow(false)
    val isCoachTyping: StateFlow<Boolean> = _isCoachTyping.asStateFlow()

    // Database Flows
    val userProfile: StateFlow<UserProfile> = repository.userProfile
        .map { it ?: UserProfile() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserProfile()
        )

    val transactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val goals: StateFlow<List<FinancialGoal>> = repository.allGoals
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val assetsLiabilities: StateFlow<List<AssetLiability>> = repository.allAssetsLiabilities
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Dynamic Financial Snapshot computed reactively
    val financialSnapshot: StateFlow<FinancialSnapshot> = combine(
        userProfile,
        transactions,
        assetsLiabilities,
        goals
    ) { user, txs, assets, gls ->
        LocalFinancialEngine.calculateSnapshot(user, txs, assets, gls)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LocalFinancialEngine.calculateSnapshot(UserProfile(), emptyList())
    )

    val badges: StateFlow<List<BrickBadge>> = combine(
        financialSnapshot,
        userProfile
    ) { snap, user ->
        LocalFinancialEngine.getBadges(snap, user.streakDays)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Today & Yesterday Flows for Quick Add and Evening Recap
    val todayTransactions: StateFlow<List<TransactionEntity>> = transactions.map { list ->
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfToday = cal.timeInMillis
        list.filter { it.timestamp >= startOfToday }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val todayTotalSpent: StateFlow<Double> = todayTransactions.map { list ->
        list.sumOf { it.amount }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    val yesterdayTotalSpent: StateFlow<Double> = transactions.map { list ->
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfToday = cal.timeInMillis
        val startOfYesterday = startOfToday - (24L * 3600 * 1000)
        list.filter { it.timestamp in startOfYesterday until startOfToday }.sumOf { it.amount }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    init {
        viewModelScope.launch {
            try {
                repository.initializeDefaultDataIfEmpty()
                val user = repository.userProfile.firstOrNull() ?: UserProfile()
                DailyEveningRecapWorker.schedule(
                    application,
                    user.dailyReminderHour,
                    user.dailyReminderMinute
                )
            } catch (e: Exception) {
                android.util.Log.e("BrickViewModel", "Error in init: ${e.message}", e)
            }
        }
    }

    fun isOnline(): Boolean = aiCoachService.isOnline()

    fun selectScreen(index: Int) {
        _currentScreen.value = index
    }

    fun openAddExpenseDialog(flowMode: String = "DEPENSE") {
        _addExpenseInitialMode.value = flowMode
        _showAddExpenseDialog.value = true
    }

    fun closeAddExpenseDialog() {
        _showAddExpenseDialog.value = false
    }

    fun openQuickAddSheet() {
        _showQuickAddSheet.value = true
    }

    fun closeQuickAddSheet() {
        _showQuickAddSheet.value = false
    }

    fun openEveningRecap() {
        _showEveningRecap.value = true
    }

    fun closeEveningRecap() {
        _showEveningRecap.value = false
    }

    fun openPurchaseSimulator() {
        _simulationResult.value = null
        _showPurchaseSimulator.value = true
    }

    fun closePurchaseSimulator() {
        _showPurchaseSimulator.value = false
    }

    fun toggleHideAmounts() {
        viewModelScope.launch {
            val current = userProfile.value
            repository.saveUserProfile(current.copy(hideAmounts = !current.hideAmounts))
        }
    }

    fun updateProfile(updated: UserProfile) {
        viewModelScope.launch {
            repository.saveUserProfile(updated)
            DailyEveningRecapWorker.schedule(
                getApplication(),
                updated.dailyReminderHour,
                updated.dailyReminderMinute
            )
            com.example.widget.BrickResteAVivreWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    fun addTransaction(
        title: String,
        amount: Double,
        category: String,
        type: String,
        priority: String = "UTILE",
        note: String = "",
        isRecurring: Boolean = false
    ) {
        viewModelScope.launch {
            val t = TransactionEntity(
                title = title.ifBlank { "Dépense $category" },
                amount = amount,
                category = category,
                type = type,
                priority = priority,
                note = note,
                isRecurring = isRecurring
            )
            repository.addTransaction(t)

            // Update streak if needed
            val current = userProfile.value
            repository.saveUserProfile(current.copy(streakDays = current.streakDays + 1))
            com.example.widget.BrickResteAVivreWidgetProvider.updateAllWidgets(getApplication())
            closeAddExpenseDialog()
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
            com.example.widget.BrickResteAVivreWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    fun addGoal(title: String, target: Double, current: Double, category: String) {
        viewModelScope.launch {
            repository.addGoal(
                FinancialGoal(
                    title = title,
                    targetAmount = target,
                    currentAmount = current,
                    category = category
                )
            )
        }
    }

    fun updateGoalAmount(goal: FinancialGoal, addedAmount: Double) {
        viewModelScope.launch {
            repository.updateGoal(goal.copy(currentAmount = goal.currentAmount + addedAmount))
        }
    }

    fun deleteGoal(goal: FinancialGoal) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }

    fun addAssetLiability(name: String, type: String, category: String, value: Double, cashflow: Double, note: String) {
        viewModelScope.launch {
            repository.addAssetLiability(
                AssetLiability(
                    name = name,
                    type = type,
                    category = category,
                    value = value,
                    monthlyCashflow = cashflow,
                    note = note
                )
            )
        }
    }

    fun deleteAssetLiability(item: AssetLiability) {
        viewModelScope.launch {
            repository.deleteAssetLiability(item)
        }
    }

    fun runSimulation(amount: Double, title: String) {
        val res = LocalFinancialEngine.simulatePurchase(
            amount = amount,
            title = title,
            user = userProfile.value,
            snapshot = financialSnapshot.value
        )
        _simulationResult.value = res
    }

    fun askCoach(query: String) {
        if (query.isBlank()) return
        val userMsg = CoachMessage(sender = "USER", text = query)
        _coachMessages.value = _coachMessages.value + userMsg
        _isCoachTyping.value = true

        viewModelScope.launch {
            val reply = aiCoachService.askCoach(
                userMessage = query,
                user = userProfile.value,
                snapshot = financialSnapshot.value,
                transactions = transactions.value
            )
            _isCoachTyping.value = false
            _coachMessages.value = _coachMessages.value + reply
        }
    }

    fun resetData() {
        viewModelScope.launch {
            repository.resetAllData()
            _coachMessages.value = listOf(
                CoachMessage(
                    sender = "COACH",
                    text = "Données réinitialisées. Prêt à repartir sur des bases financières solides !"
                )
            )
        }
    }
}
