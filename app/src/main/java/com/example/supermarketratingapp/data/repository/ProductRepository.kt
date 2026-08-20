package com.example.supermarketratingapp.data.repository

import android.content.Context
import com.example.supermarketratingapp.data.local.*
import com.example.supermarketratingapp.data.remote.OpenFoodFactsService
import com.example.supermarketratingapp.data.remote.RemoteProductResult
import com.example.supermarketratingapp.data.remote.SupermarketUrlScraper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class ProductRepository(
    private val productDao: ProductDao,
    private val categoryDao: CategoryDao,
    private val openFoodFactsService: OpenFoodFactsService = OpenFoodFactsService(),
    private val urlScraper: SupermarketUrlScraper = SupermarketUrlScraper()
) {
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    suspend fun getProductById(id: Long): ProductEntity? = withContext(Dispatchers.IO) {
        productDao.getProductById(id)
    }

    suspend fun getProductByBarcode(barcode: String): ProductEntity? = withContext(Dispatchers.IO) {
        productDao.getProductByBarcode(barcode)
    }

    fun searchSavedProducts(query: String): Flow<List<ProductEntity>> = productDao.searchProducts(query)

    suspend fun searchOpenFoodFacts(query: String): List<RemoteProductResult> {
        return openFoodFactsService.searchProductsByName(query)
    }

    suspend fun fetchOpenFoodFactsByBarcode(barcode: String): RemoteProductResult? {
        return openFoodFactsService.fetchProductByBarcode(barcode)
    }

    suspend fun scrapeProductUrl(url: String) = urlScraper.scrapeUrl(url)

    suspend fun saveProduct(product: ProductEntity): Long = withContext(Dispatchers.IO) {
        val existing = if (!product.barcode.isNullOrEmpty()) {
            productDao.getProductByBarcode(product.barcode!!)
        } else null

        val toSave = if (existing != null) {
            product.copy(id = existing.id, updatedAt = System.currentTimeMillis())
        } else {
            product.copy(updatedAt = System.currentTimeMillis())
        }

        if (toSave.id > 0) {
            productDao.updateProduct(toSave)
            toSave.id
        } else {
            productDao.insertProduct(toSave)
        }
    }

    suspend fun deleteProduct(product: ProductEntity) = withContext(Dispatchers.IO) {
        productDao.deleteProduct(product)
    }

    suspend fun deleteProductById(id: Long) = withContext(Dispatchers.IO) {
        productDao.deleteProductById(id)
    }

    // Category CRUD
    suspend fun saveCategory(category: CategoryEntity): Long = withContext(Dispatchers.IO) {
        if (category.id > 0) {
            categoryDao.updateCategory(category)
            category.id
        } else {
            categoryDao.insertCategory(category)
        }
    }

    suspend fun deleteCategory(category: CategoryEntity) = withContext(Dispatchers.IO) {
        productDao.clearCategoryFromProducts(category.id)
        categoryDao.deleteCategory(category)
    }

    // JSON Export / Import
    suspend fun exportDataToJson(context: Context): File = withContext(Dispatchers.IO) {
        val products = mutableListOf<ProductEntity>()
        productDao.getAllProducts().collect { list ->
            products.addAll(list)
            return@collect
        }

        val jsonString = Json.encodeToString(products)
        val exportFile = File(context.cacheDir, "supermarket_reviews_backup.json")
        exportFile.writeText(jsonString)
        exportFile
    }

    suspend fun importDataFromJson(jsonContent: String): Int = withContext(Dispatchers.IO) {
        val imported = Json.decodeFromString<List<ProductEntity>>(jsonContent)
        var count = 0
        imported.forEach { prod ->
            saveProduct(prod.copy(id = 0))
            count++
        }
        count
    }
}
