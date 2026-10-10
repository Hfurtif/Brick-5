package com.example.ai

data class ParsedExpense(
    val title: String,
    val amount: Double,
    val category: String,
    val type: String, // BESOIN, ENVIE, EPARGNE
    val priority: String, // ESSENTIEL, UTILE, SUPERFLU
    val confidenceText: String
)

object SmartExpenseParser {

    private val CURRENCY_WORDS = listOf(
        "francs", "franc", "fcfa", "cfa", "f", "euro", "euros", "eur", "€", "$", "dollars", "dollar", "chf"
    )

    fun parse(input: String): ParsedExpense {
        val trimmed = input.trim()
        if (trimmed.isBlank()) {
            return ParsedExpense(
                title = "Dépense rapide",
                amount = 0.0,
                category = "Alimentation",
                type = "BESOIN",
                priority = "UTILE",
                confidenceText = "Saisie vide"
            )
        }

        // Extract amount using regex
        val numberRegex = "\\b(\\d+(?:[.,]\\d+)?)\\b".toRegex()
        val match = numberRegex.find(trimmed)
        val amount = match?.value?.replace(',', '.')?.toDoubleOrNull() ?: 0.0

        // Extract title: remove numbers and currency words
        var cleanedTitle = trimmed
        if (match != null) {
            cleanedTitle = cleanedTitle.removeRange(match.range)
        }
        for (w in CURRENCY_WORDS) {
            cleanedTitle = cleanedTitle.replace("(?i)\\b$w\\b".toRegex(), "")
        }
        cleanedTitle = cleanedTitle.trim().replace("\\s+".toRegex(), " ")

        val finalTitle = if (cleanedTitle.isNotBlank()) {
            cleanedTitle.replaceFirstChar { it.uppercase() }
        } else {
            "Dépense"
        }

        // Categorize & determine Needs vs Wants according to Rich Dad principles
        val (category, type, priority) = inferCategoryAndType(finalTitle, amount)

        return ParsedExpense(
            title = finalTitle,
            amount = amount,
            category = category,
            type = type,
            priority = priority,
            confidenceText = "Détecté : $category ($type)"
        )
    }

    private fun inferCategoryAndType(title: String, amount: Double): Triple<String, String, String> {
        val lower = title.lowercase()

        // 0. Business, Commerce & Inflow (Pour ceux qui font des affaires)
        if (lower.containsAny(
                "vente", "client", "prestation", "chiffre", "recette", "commerce", "boutique",
                "service", "affaires", "facturation", "contrat", "honoraire", "versement", "bénéfice"
            )
        ) {
            return Triple("Vente & Commerce", "ENTREE", "ESSENTIEL")
        }

        // 0.1 Stock & Business Investments (Investissements d'affaires)
        if (lower.containsAny(
                "stock", "marchandise", "fournisseur", "matériel", "materiel", "outils",
                "outil", "colis pro", "achat gros", "réassort", "reassort"
            )
        ) {
            return Triple("Achat Stock & Marchandise", "INVESTISSEMENT_AFFAIRES", "ESSENTIEL")
        }

        // 0.2 Dons, Dîmes & Solidarité
        if (lower.containsAny(
                "don", "dime", "dîme", "aumone", "aumône", "charite", "charité",
                "solidarite", "solidarité", "eglise", "église", "mosquee", "mosquée", "orphelinat"
            )
        ) {
            return Triple("Dîme (10%)", "DON", "UTILE")
        }

        // 1. Food / Alimentation (Essential need)
        if (lower.containsAny(
                "piment", "pain", "riz", "viande", "tomate", "oignon", "poisson", "fruit",
                "legume", "légume", "courses", "lait", "manger", "supermarché", "marché",
                "banane", "attieke", "attiéké", "manioc", "igname", "poulet", "eau"
            )
        ) {
            return Triple("Alimentation", "BESOIN", "ESSENTIEL")
        }

        // 2. Transport (Essential need)
        if (lower.containsAny("taxi", "bus", "essence", "carburant", "moto", "zem", "transport", "péage", "navigo", "train", "uber")) {
            return Triple("Transports", "BESOIN", "ESSENTIEL")
        }

        // 3. Housing / Factures (Essential need)
        if (lower.containsAny("loyer", "courant", "sbee", "cie", "sodeci", "charges", "gaz", "électricité", "eau", "ampoule")) {
            return Triple("Logement", "BESOIN", "ESSENTIEL")
        }

        // 4. Sorties, Bar, Restaurant, Loisirs (Wants)
        if (lower.containsAny("resto", "restaurant", "bar", "biere", "bière", "cine", "ciné", "café", "cafe", "sortie", "glace", "chawarma", "chicha", "boîte", "soiree", "soirée")) {
            return Triple("Sorties & Loisirs", "ENVIE", "UTILE")
        }

        // 5. Shopping, Vêtements (Wants / Superfluous)
        if (lower.containsAny("sneakers", "basket", "habit", "chaussure", "chemise", "montre", "pantalon", "robe", "bijou", "parfum", "shopping")) {
            return Triple("Shopping", "ENVIE", "SUPERFLU")
        }

        // 6. Subscriptions / Telecom (Needs or Wants)
        if (lower.containsAny("recharge", "forfait", "internet", "netflix", "spotify", "canal+", "wifi", "abonnement")) {
            val isStreaming = lower.containsAny("netflix", "spotify", "canal+")
            return Triple("Abonnements", if (isStreaming) "ENVIE" else "BESOIN", "UTILE")
        }

        // 7. Health (Essential need)
        if (lower.containsAny("pharmacie", "médicament", "medicament", "docteur", "santé", "sante", "clinique", "dentiste", "soin")) {
            return Triple("Santé", "BESOIN", "ESSENTIEL")
        }

        // 8. Investments / Savings (Asset)
        if (lower.containsAny("action", "crypto", "tontine", "epargne", "épargne", "placement", "livret", "bourse", "pea")) {
            return Triple("Investissement", "EPARGNE", "ESSENTIEL")
        }

        // Default heuristic based on size
        return if (amount > 50.0) {
            Triple("Autre", "ENVIE", "UTILE")
        } else {
            Triple("Autre", "BESOIN", "UTILE")
        }
    }

    private fun String.containsAny(vararg words: String): Boolean {
        return words.any { this.contains(it) }
    }
}
