package com.example.supermarketratingapp

import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.supermarketratingapp.ui.main.MainViewModel
import com.example.supermarketratingapp.ui.screens.*

@Composable
fun MainNavigation(viewModel: MainViewModel = viewModel()) {
    val backStack = rememberNavBackStack(HomeKey)
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var addSheetInitialTab by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearToastMessage()
        }
    }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<HomeKey> {
                HomeScreen(
                    uiState = uiState,
                    onSearchQueryChanged = { query ->
                        viewModel.updateSearchQuery(query)
                        if (query.isNotBlank()) {
                            viewModel.searchDiscoverProducts(query)
                        }
                    },
                    onStoreFilterSelected = { viewModel.setStoreFilter(it) },
                    onCategoryFilterSelected = { viewModel.setCategoryFilter(it) },
                    onSortOptionSelected = { viewModel.setSortOption(it) },
                    onGroupByOptionSelected = { viewModel.setGroupByOption(it) },
                    onProductClick = { productId ->
                        backStack.add(ProductDetailKey(productId))
                    },
                    onDeleteProduct = { product ->
                        viewModel.deleteProduct(product)
                    },
                    onAddProductClick = { initialTab -> addSheetInitialTab = initialTab },
                    onSettingsClick = { backStack.add(SettingsKey) },
                    modifier = Modifier.fillMaxSize()
                )
            }

            entry<ProductDetailKey> { key ->
                val product = uiState.products.find { it.id == key.productId }
                if (product != null) {
                    ProductDetailScreen(
                        product = product,
                        categories = uiState.categories,
                        onSaveProduct = { viewModel.saveProduct(it) },
                        onDeleteProduct = { viewModel.deleteProduct(it) },
                        onAddCategoryClick = { name -> viewModel.addCategory(name) },
                        onBackClick = { backStack.removeLastOrNull() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            entry<SearchResultsKey> { key ->
                SearchResultsScreen(
                    query = key.query,
                    savedResults = uiState.products.filter {
                        it.name.contains(key.query, ignoreCase = true) ||
                        (it.brand?.contains(key.query, ignoreCase = true) == true)
                    },
                    discoverResults = uiState.discoverSearchResults,
                    categories = uiState.categories,
                    isDiscoverSearching = uiState.isDiscoverSearching,
                    onQueryChanged = { newQuery ->
                        viewModel.updateSearchQuery(newQuery)
                        viewModel.searchDiscoverProducts(newQuery)
                    },
                    onSavedProductClick = { productId ->
                        backStack.add(ProductDetailKey(productId))
                    },
                    onImportDiscoverProduct = { remote ->
                        viewModel.importDiscoverProduct(remote) { newId ->
                            backStack.add(ProductDetailKey(newId))
                        }
                    },
                    onBackClick = { backStack.removeLastOrNull() },
                    modifier = Modifier.fillMaxSize()
                )
            }

            entry<SettingsKey> {
                SettingsScreen(
                    categories = uiState.categories,
                    onAddCategory = { name -> viewModel.addCategory(name) },
                    onUpdateCategory = { category, newName -> viewModel.updateCategory(category, newName) },
                    onDeleteCategory = { category -> viewModel.deleteCategory(category) },
                    onExportBackup = { callback -> viewModel.exportBackup(callback) },
                    onImportBackup = { json -> viewModel.importBackup(json) },
                    onBackClick = { backStack.removeLastOrNull() },
                    modifier = Modifier.fillMaxSize()
                )
            }

        }
    )

    addSheetInitialTab?.let { tab ->
        AddProductSheet(
            initialTab = tab,
            onScanBarcode = { barcode ->
                viewModel.processScannedBarcode(barcode) { newId ->
                    backStack.add(ProductDetailKey(newId))
                }
            },
            onPasteUrl = { url ->
                viewModel.processPastedUrl(url) { newId ->
                    backStack.add(ProductDetailKey(newId))
                }
            },
            onSearchDiscover = { query ->
                viewModel.searchDiscoverProducts(query)
            },
            onImportDiscoverProduct = { remote ->
                viewModel.importDiscoverProduct(remote) { newId ->
                    backStack.add(ProductDetailKey(newId))
                }
            },
            isDiscoverSearching = uiState.isDiscoverSearching,
            discoverResults = uiState.discoverSearchResults,
            onDismiss = { addSheetInitialTab = null }
        )
    }
}

