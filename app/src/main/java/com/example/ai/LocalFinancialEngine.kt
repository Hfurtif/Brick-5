package com.example.ai

import com.example.data.model.AssetLiability
import com.example.data.model.FinancialGoal
import com.example.data.model.TransactionEntity
import com.example.data.model.UserProfile
import java.util.Calendar

object LocalFinancialEngine {

    fun calculateFinancialSnapshot(
        user: UserProfile?,
        transactions: List<TransactionEntity>,
        assetsLiabilities: List<AssetLiability>
    ): FinancialSnapshot {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayStart = cal.timeInMillis

        val todayExpenses = transactions
            .filter { it.type == "DEPENSE" && it.timestamp >= todayStart }
            .sumOf { it.amount }

        val monthlySalary = user?.monthlySalary ?: 0.0
        val fixedCharges = user?.fixedCharges ?: 0.0

        val totalAssets = assetsLiabilities.filter { it.type == "ACTIF" }.sumOf { it.value }
        val totalLiabilities = assetsLiabilities.filter { it.type == "PASSIF" }.sumOf { it.value }
        val netWorth = totalAssets - totalLiabilities

        // Daily disposable income (Reste à vivre du jour)
        val dailyBudget = if (monthlySalary > 0) {
            maxOf(0.0, (monthlySalary - fixedCharges) / 30.0)
        } else {
            // If no fixed salary, derive daily allowance from total net cash in hand / 30
            val totalCash = transactions.filter { it.type == "REVENU" }.sumOf { it.amount } -
                    transactions.filter { it.type == "DEPENSE" }.sumOf { it.amount }
            maxOf(0.0, totalCash / 30.0)
        }
        val resteAVivreDuJour = maxOf(0.0, dailyBudget - todayExpenses)

        // Monthly net cashflow
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        val calMonth = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        val monthStart = calMonth.timeInMillis

        val monthRevenues = transactions
            .filter { it.type == "REVENU" && it.timestamp >= monthStart }
            .sumOf { it.amount }
        val effectiveRevenue = if (monthlySalary > 0) monthlySalary else monthRevenues

        val monthExpenses = transactions
            .filter { it.type == "DEPENSE" && it.timestamp >= monthStart }
            .sumOf { it.amount }
        val monthlyNetCashflow = effectiveRevenue - fixedCharges - monthExpenses

        // Identify Financial Leaks
        val leaks = mutableListOf<FinancialLeak>()
        val wantsExpenses = transactions
            .filter { it.type == "DEPENSE" && (it.priority == "WANTS" || it.category in listOf("Sorties", "Restaurant", "Shopping")) }

        val smallImpulses = wantsExpenses.filter { it.amount in 1000.0..15000.0 }
        if (smallImpulses.size >= 3) {
            val totalSmall = smallImpulses.sumOf { it.amount }
            leaks.add(
                FinancialLeak(
                    category = "Micro-dépenses & Plaisirs impulsifs",
                    monthlyEstimate = totalSmall,
                    annualCost = totalSmall * 12,
                    advice = "Ces petites sorties répétées grignotent votre sécurité. Fixez-vous une enveloppe espèces hebdomadaire stricte."
                )
            )
        }

        // Financial Health Score (0 - 100)
        var score = 50
        if (netWorth > 0) score += 15
        if (monthlyNetCashflow > 0) score += 15
        if (resteAVivreDuJour > 0) score += 10
        if (user?.streakDays ?: 0 >= 3) score += 10
        score = score.coerceIn(10, 100)

        return FinancialSnapshot(
            resteAVivreDuJour = resteAVivreDuJour,
            monthlyNetCashflow = monthlyNetCashflow,
            financialHealthScore = score,
            netWorth = netWorth,
            activeLeaks = leaks
        )
    }

    fun simulatePurchase(
        user: UserProfile?,
        snapshot: FinancialSnapshot,
        amount: Double,
        title: String
    ): PurchaseSimulationResult {
        val dailyBudget = if ((user?.monthlySalary ?: 0.0) > 0) {
            (user!!.monthlySalary - user.fixedCharges) / 30.0
        } else {
            maxOf(snapshot.resteAVivreDuJour, 2000.0)
        }

        val daysOfWorkRequired = if (dailyBudget > 0) amount / dailyBudget else 1.0

        val canAfford = snapshot.resteAVivreDuJour >= amount || (snapshot.monthlyNetCashflow > amount * 1.5)

        val score = when {
            amount <= snapshot.resteAVivreDuJour -> 90
            amount <= snapshot.monthlyNetCashflow -> 65
            amount <= snapshot.monthlyNetCashflow * 2 -> 40
            else -> 15
        }

        val verdict = when {
            score >= 80 -> "Achat validé sans danger 🟢"
            score >= 60 -> "Achat faisable avec modération 🟡"
            score >= 40 -> "Risque de tension financière 🟠"
            else -> "Alerte : Dépense fortement déconseillée 🔴"
        }

        val advice = when {
            score >= 80 -> "Votre caisse et votre reste à vivre couvrent cette dépense sans impacter vos objectifs."
            score >= 60 -> "Vous pouvez l'acheter, mais réduisez vos dépenses de confort sur les 3 prochains jours."
            score >= 40 -> "Cet achat vous coûte environ ${String.format("%.1f", daysOfWorkRequired)} jours d'efforts. Attendez 48h de réflexion."
            else -> "Ce montant dépasse votre marge de sécurité. Vous risquez d'entamer vos charges fixes ou de vous endetter."
        }

        val impactOnGoals = "Équivaut à environ ${String.format("%.1f", daysOfWorkRequired)} jours de travail."

        return PurchaseSimulationResult(
            canAfford = canAfford,
            score = score,
            verdict = verdict,
            advice = advice,
            daysOfWorkRequired = daysOfWorkRequired,
            impactOnGoals = impactOnGoals
        )
    }

    fun evaluateBadges(
        user: UserProfile?,
        transactions: List<TransactionEntity>,
        goals: List<FinancialGoal>
    ): List<BrickBadge> {
        val badges = mutableListOf<BrickBadge>()

        badges.add(
            BrickBadge(
                id = "first_brick",
                title = "Première Brique",
                description = "Vous avez configuré votre profil et démarré votre forteresse.",
                icon = "🧱",
                isUnlocked = user?.isOnboarded == true
            )
        )

        badges.add(
            BrickBadge(
                id = "discipline_streak",
                title = "Discipline de Fer",
                description = "Gardez 3 jours de streak de suivi financier.",
                icon = "🔥",
                isUnlocked = (user?.streakDays ?: 0) >= 3
            )
        )

        val hasGoal = goals.isNotEmpty()
        badges.add(
            BrickBadge(
                id = "visionary",
                title = "Visionnaire",
                description = "Avoir défini au moins un objectif d'épargne ou d'investissement.",
                icon = "🎯",
                isUnlocked = hasGoal
            )
        )

        val transactionCount = transactions.size
        badges.add(
            BrickBadge(
                id = "sentinel",
                title = "Sentinelle",
                description = "Enregistrer au moins 5 opérations financières.",
                icon = "🛡️",
                isUnlocked = transactionCount >= 5
            )
        )

        return badges
    }
}
