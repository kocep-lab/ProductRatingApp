package com.example.supermarketratingapp.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.supermarketratingapp.data.local.AppDatabase
import com.example.supermarketratingapp.data.local.CategoryEntity
import com.example.supermarketratingapp.data.local.ProductEntity
import com.example.supermarketratingapp.data.local.createProduct
import com.example.supermarketratingapp.data.remote.RemoteProductResult

import com.example.supermarketratingapp.data.remote.ScrapedUrlResult
import com.example.supermarketratingapp.data.repository.ProductRepository
import com.example.supermarketratingapp.ui.components.ProductBadge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class SortOption(val label: String) {
    RECENTLY_UPDATED("Recently Updated"),
    RECENTLY_ADDED("Recently Added"),
    ALPHABETICAL_ASC("Name (A-Z)"),
    ALPHABETICAL_DESC("Name (Z-A)"),
    RATING_HIGH_LOW("Rating (High-Low)"),
    RATING_LOW_HIGH("Rating (Low-High)")
}

enum class GroupByOption(val label: String) {
    NONE("None (Flat List)"),
    CATEGORY("Category"),
    STORE("Store"),
    RATING("Rating")
}

data class UiState(
    val products: List<ProductEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedStoreFilter: String? = null,
    val selectedCategoryFilter: Long? = null,
    val selectedBadgeFilter: String? = null,
    val minRatingFilter: Float = 0.0f,
    val sortOption: SortOption = SortOption.RECENTLY_UPDATED,
    val groupByOption: GroupByOption = GroupByOption.NONE,
    val isLoading: Boolean = false,
    val toastMessage: String? = null,
    
    // Add product bottom sheet state
    val scannedBarcodeResult: RemoteProductResult? = null,
    val scrapedUrlResult: ScrapedUrlResult? = null,
    val discoverSearchResults: List<RemoteProductResult> = emptyList(),
    val isDiscoverSearching: Boolean = false,
    val activeTab: Int = 0 // 0: Barcode, 1: Paste Link, 2: Search Name
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = ProductRepository(database.productDao(), database.categoryDao())

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.allProducts.collect { productList ->
                _uiState.update { it.copy(products = productList) }
            }
        }
        viewModelScope.launch {
            repository.allCategories.collect { categoryList ->
                _uiState.update { it.copy(categories = categoryList) }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setStoreFilter(store: String?) {
        _uiState.update { it.copy(selectedStoreFilter = store) }
    }

    fun setCategoryFilter(categoryId: Long?) {
        _uiState.update { it.copy(selectedCategoryFilter = categoryId) }
    }

    fun setBadgeFilter(badgeKey: String?) {
        _uiState.update { it.copy(selectedBadgeFilter = badgeKey) }
    }

    fun setMinRatingFilter(rating: Float) {
        _uiState.update { it.copy(minRatingFilter = rating) }
    }

    fun setSortOption(option: SortOption) {
        _uiState.update { it.copy(sortOption = option) }
    }

    fun setGroupByOption(option: GroupByOption) {
        _uiState.update { it.copy(groupByOption = option) }
    }

    fun clearToastMessage() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    // Barcode Scan Processing
    fun processScannedBarcode(barcode: String, onNavigateToProduct: (Long) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // Check duplicate in local DB
            val existing = repository.getProductByBarcode(barcode)
            if (existing != null) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        toastMessage = "You've already reviewed this product!" 
                    ) 
                }
                onNavigateToProduct(existing.id)
                return@launch
            }

            // Query Open Food Facts
            val remote = repository.fetchOpenFoodFactsByBarcode(barcode)
            _uiState.update { it.copy(isLoading = false, scannedBarcodeResult = remote) }

            if (remote != null) {
                // Save new product entry pre-filled from remote
                val newId = repository.saveProduct(
                    createProduct(
                        barcode = remote.barcode,
                        name = remote.name,
                        brand = remote.brand,
                        productSize = remote.productSize,
                        availableStores = remote.stores.ifEmpty { listOf("Woolworths", "Coles") },
                        imageUrl = remote.imageUrl,
                        healthStarRating = remote.healthStarRating,
                        nutriScore = remote.nutriScore,
                        energyKj = remote.energyKj,
                        proteinG = remote.proteinG,
                        fatG = remote.fatG,
                        saturatedFatG = remote.saturatedFatG,
                        carbsG = remote.carbsG,
                        sugarsG = remote.sugarsG,
                        sodiumMg = remote.sodiumMg
                    )
                )
                onNavigateToProduct(newId)
            } else {
                // Product not found in database -> prompt manual entry pre-filled with barcode
                val newId = repository.saveProduct(
                    createProduct(
                        barcode = barcode,
                        name = "New Product ($barcode)",
                        availableStores = listOf("Supermarket")
                    )
                )
                _uiState.update { it.copy(toastMessage = "Product not found in database. Please enter details.") }
                onNavigateToProduct(newId)
            }
        }
    }

    // URL Scraper Processing
    fun processPastedUrl(url: String, onNavigateToProduct: (Long) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.scrapeProductUrl(url)
            _uiState.update { it.copy(isLoading = false, scrapedUrlResult = result) }

            val newId = repository.saveProduct(
                createProduct(
                    name = result.title,
                    brand = result.brand,
                    availableStores = listOf(result.storeName),
                    imageUrl = result.imageUrl,
                    sourceUrl = result.sourceUrl
                )
            )
            onNavigateToProduct(newId)
        }
    }


    // Discover / Text Search Processing
    fun searchDiscoverProducts(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isDiscoverSearching = true) }
            val results = repository.searchOpenFoodFacts(query)
            _uiState.update { it.copy(isDiscoverSearching = false, discoverSearchResults = results) }
        }
    }

    fun importDiscoverProduct(remote: RemoteProductResult, onNavigateToProduct: (Long) -> Unit) {
        viewModelScope.launch {
            val newId = repository.saveProduct(
                createProduct(
                    barcode = remote.barcode,
                    name = remote.name,
                    brand = remote.brand,
                    productSize = remote.productSize,
                    availableStores = remote.stores.ifEmpty { listOf("Woolworths", "Coles") },
                    imageUrl = remote.imageUrl,
                    healthStarRating = remote.healthStarRating,
                    nutriScore = remote.nutriScore,
                    energyKj = remote.energyKj,
                    proteinG = remote.proteinG,
                    fatG = remote.fatG,
                    saturatedFatG = remote.saturatedFatG,
                    carbsG = remote.carbsG,
                    sugarsG = remote.sugarsG,
                    sodiumMg = remote.sodiumMg
                )
            )
            onNavigateToProduct(newId)
        }
    }


    fun saveProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.saveProduct(product)
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            _uiState.update { it.copy(toastMessage = "Product deleted") }
        }
    }

    // Category Operations
    fun addCategory(name: String, colorHex: String? = "#2196F3") {
        viewModelScope.launch {
            val category = CategoryEntity(id = 0, name = name, iconName = null, colorHex = colorHex, sortOrder = 0)
            repository.saveCategory(category)
            _uiState.update { it.copy(toastMessage = "Category '$name' added") }
        }
    }


    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(category)
            _uiState.update { it.copy(toastMessage = "Category '${category.name}' deleted") }
        }
    }

    // Backup & Export
    fun exportBackup(onFileReady: (java.io.File) -> Unit) {
        viewModelScope.launch {
            val file = repository.exportDataToJson(getApplication())
            onFileReady(file)
        }
    }

    fun importBackup(jsonContent: String) {
        viewModelScope.launch {
            val count = repository.importDataFromJson(jsonContent)
            _uiState.update { it.copy(toastMessage = "Successfully imported $count products") }
        }
    }
}
