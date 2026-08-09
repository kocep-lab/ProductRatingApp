package com.example.supermarketratingapp.data.remote

object AuQueryNormalizer {

    private val aliasMap = mapOf(
        "smith" to listOf("smiths", "smith's", "smiths chips"),
        "smiths" to listOf("smith's", "smiths chips"),
        "coke" to listOf("coca-cola", "coca cola"),
        "red rock" to listOf("red rock deli"),
        "arnott" to listOf("arnott's", "arnotts"),
        "arnotts" to listOf("arnott's", "arnotts biscuits"),
        "tim tam" to listOf("timtam", "arnott's tim tam"),
        "cadbury" to listOf("cadbury chocolate"),
        "woolie" to listOf("woolworths"),
        "woolies" to listOf("woolworths"),
        "dorito" to listOf("doritos"),
        "bega" to listOf("bega cheese"),
        "twistie" to listOf("twisties"),
        "cheezels" to listOf("cheezels")
    )

    fun normalizeQuery(query: String): String {
        val trimmed = query.trim().lowercase()
        return aliasMap[trimmed]?.firstOrNull() ?: trimmed
    }

    fun getSearchKeywords(query: String): List<String> {
        val trimmed = query.trim().lowercase()
        val expanded = aliasMap[trimmed] ?: emptyList()
        return (listOf(trimmed) + expanded).distinct()
    }
}
