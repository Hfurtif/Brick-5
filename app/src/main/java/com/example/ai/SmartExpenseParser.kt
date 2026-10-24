package com.example.ai

object SmartExpenseParser {

    fun parse(input: String): ParsedExpense? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return null

        val numberRegex = Regex("(\\d+[\\d\\s.,]*)")
        val match = numberRegex.find(trimmed) ?: return null

        val rawNumber = match.value.replace(" ", "").replace(",", ".")
        val amount = rawNumber.toDoubleOrNull() ?: return null

        val textPart = trimmed.replace(match.value, "").trim()
        val lowerText = textPart.lowercase()

        val isIncome = lowerText.contains("salaire") || lowerText.contains("revenu") ||
                lowerText.contains("vente") || lowerText.contains("paiement reçu") ||
                lowerText.contains("gain") || lowerText.contains("tontine reçu")

        val (category, priority) = when {
            lowerText.contains("loyer") || lowerText.contains("maison") -> "Logement" to "NEEDS"
            lowerText.contains("facture") || lowerText.contains("courant") || lowerText.contains("cie") || lowerText.contains("sodeci") || lowerText.contains("eau") || lowerText.contains("electricite") -> "Factures" to "NEEDS"
            lowerText.contains("taxi") || lowerText.contains("gbaka") || lowerText.contains("yango") || lowerText.contains("essence") || lowerText.contains("transport") -> "Transport" to "NEEDS"
            lowerText.contains("manger") || lowerText.contains("nourriture") || lowerText.contains("marche") || lowerText.contains("courses") || lowerText.contains("pain") -> "Alimentation" to "NEEDS"
            lowerText.contains("sante") || lowerText.contains("pharma") || lowerText.contains("docteur") || lowerText.contains("medicament") -> "Santé" to "NEEDS"
            lowerText.contains("resto") || lowerText.contains("biere") || lowerText.contains("bar") || lowerText.contains("maquis") || lowerText.contains("boite") || lowerText.contains("fete") -> "Sorties" to "WANTS"
            lowerText.contains("shopping") || lowerText.contains("habit") || lowerText.contains("vetement") || lowerText.contains("chaussure") -> "Shopping" to "WANTS"
            lowerText.contains("tontine") || lowerText.contains("epargne") || lowerText.contains("banque") -> "Épargne" to "SAVINGS"
            lowerText.contains("dette") || lowerText.contains("remboursement") || lowerText.contains("credit") -> "Dettes" to "SAVINGS"
            else -> "Autre" to "NEEDS"
        }

        val title = if (textPart.isNotBlank()) {
            textPart.replaceFirstChar { it.uppercase() }
        } else {
            if (isIncome) "Entrée d'argent" else "Dépense"
        }

        return ParsedExpense(
            title = title,
            amount = amount,
            category = category,
            type = if (isIncome) "REVENU" else "DEPENSE",
            priority = priority
        )
    }
}
