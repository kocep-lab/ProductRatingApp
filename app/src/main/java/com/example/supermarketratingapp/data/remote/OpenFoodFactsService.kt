package com.example.supermarketratingapp.data.remote

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*

data class RemoteProductResult(
    val barcode: String?,
    val name: String,
    val brand: String? = null,
    val productSize: String? = null,
    val imageUrl: String? = null,
    val stores: List<String> = emptyList(),
    val healthStarRating: Float? = null,
    val nutriScore: String? = null,
    val energyKj: Float? = null,
    val proteinG: Float? = null,
    val fatG: Float? = null,
    val saturatedFatG: Float? = null,
    val carbsG: Float? = null,
    val sugarsG: Float? = null,
    val sodiumMg: Float? = null
)

class OpenFoodFactsService {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(json)
        }
    }

    suspend fun fetchProductByBarcode(barcode: String): RemoteProductResult? = withContext(Dispatchers.IO) {
        try {
            val response: HttpResponse = client.get("https://world.openfoodfacts.net/api/v2/product/$barcode.json") {
                header("User-Agent", "SupermarketRatingApp-Android/1.0 (Australia; kocep@example.com)")
            }
            if (response.status.value == 200) {
                val responseText = response.bodyAsText()
                val jsonObject = json.parseToJsonElement(responseText).jsonObject
                val status = jsonObject["status"]?.jsonPrimitive?.intOrNull ?: 0
                if (status == 1) {
                    val product = jsonObject["product"]?.jsonObject
                    if (product != null) {
                        return@withContext parseProductJsonObject(product, barcode)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }

    suspend fun searchProductsByName(query: String): List<RemoteProductResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<RemoteProductResult>()
        try {
            val url = "https://world.openfoodfacts.org/cgi/search.pl?search_terms=${query.trim()}&search_simple=1&action=process&json=1&page_size=20&country=australia"
            val response: HttpResponse = client.get(url) {
                header("User-Agent", "SupermarketRatingApp-Android/1.0 (Australia; kocep@example.com)")
            }
            if (response.status.value == 200) {
                val responseText = response.bodyAsText()
                val jsonObject = json.parseToJsonElement(responseText).jsonObject
                val products = jsonObject["products"]?.jsonArray
                products?.forEach { item ->
                    val prodObj = item.jsonObject
                    val parsed = parseProductJsonObject(prodObj, prodObj["code"]?.jsonPrimitive?.contentOrNull)
                    if (parsed != null && parsed.name.isNotBlank()) {
                        results.add(parsed)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext results
    }

    private fun parseProductJsonObject(product: JsonObject, code: String?): RemoteProductResult? {
        val productName = product["product_name"]?.jsonPrimitive?.contentOrNull
            ?: product["product_name_en"]?.jsonPrimitive?.contentOrNull
            ?: return null

        val brand = product["brands"]?.jsonPrimitive?.contentOrNull
        val quantity = product["quantity"]?.jsonPrimitive?.contentOrNull
        val imageUrl = product["image_front_url"]?.jsonPrimitive?.contentOrNull
            ?: product["image_url"]?.jsonPrimitive?.contentOrNull

        val storesStr = product["stores"]?.jsonPrimitive?.contentOrNull ?: ""
        val stores = storesStr.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        // Nutriments
        val nutriments = product["nutriments"]?.jsonObject
        val energyKj = nutriments?.get("energy-kj_100g")?.jsonPrimitive?.floatOrNull
            ?: nutriments?.get("energy_100g")?.jsonPrimitive?.floatOrNull
        val proteinG = nutriments?.get("proteins_100g")?.jsonPrimitive?.floatOrNull
        val fatG = nutriments?.get("fat_100g")?.jsonPrimitive?.floatOrNull
        val satFatG = nutriments?.get("saturated-fat_100g")?.jsonPrimitive?.floatOrNull
        val carbsG = nutriments?.get("carbohydrates_100g")?.jsonPrimitive?.floatOrNull
        val sugarsG = nutriments?.get("sugars_100g")?.jsonPrimitive?.floatOrNull
        val sodiumMg = nutriments?.get("sodium_100g")?.jsonPrimitive?.floatOrNull?.let { it * 1000f }
            ?: nutriments?.get("salt_100g")?.jsonPrimitive?.floatOrNull?.let { (it / 2.5f) * 1000f }

        val nutriScore = product["nutriscore_grade"]?.jsonPrimitive?.contentOrNull?.uppercase()
        val healthStar = product["health_score"]?.jsonPrimitive?.floatOrNull?.let { (it / 20f).coerceIn(0.5f, 5.0f) }

        return RemoteProductResult(
            barcode = code,
            name = productName,
            brand = brand,
            productSize = quantity,
            imageUrl = imageUrl,
            stores = stores,
            healthStarRating = healthStar,
            nutriScore = nutriScore,
            energyKj = energyKj,
            proteinG = proteinG,
            fatG = fatG,
            saturatedFatG = satFatG,
            carbsG = carbsG,
            sugarsG = sugarsG,
            sodiumMg = sodiumMg
        )
    }
}
