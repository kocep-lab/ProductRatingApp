package com.example.supermarketratingapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.supermarketratingapp.data.local.CategoryEntity
import com.example.supermarketratingapp.data.local.ProductEntity
import com.example.supermarketratingapp.ui.components.*
import com.example.supermarketratingapp.ui.main.GroupByOption
import com.example.supermarketratingapp.ui.main.SortOption
import com.example.supermarketratingapp.ui.main.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: UiState,
    onSearchQueryChanged: (String) -> Unit,
    onStoreFilterSelected: (String?) -> Unit,
    onCategoryFilterSelected: (Long?) -> Unit,
    onSortOptionSelected: (SortOption) -> Unit,
    onGroupByOptionSelected: (GroupByOption) -> Unit,
    onProductClick: (Long) -> Unit,
    onDeleteProduct: (ProductEntity) -> Unit,
    onAddProductClick: (initialTab: Int) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var productToDelete by remember { mutableStateOf<ProductEntity?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showGroupMenu by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }

    // Filter & Sort products
    val filteredProducts = remember(
        uiState.products,
        uiState.searchQuery,
        uiState.selectedStoreFilter,
        uiState.selectedCategoryFilter,
        uiState.minRatingFilter,
        uiState.sortOption,
        uiState.groupByOption
    ) {
        uiState.products.filter { prod ->
            val matchesQuery = uiState.searchQuery.isBlank() ||
                    prod.name.contains(uiState.searchQuery, ignoreCase = true) ||
                    (prod.brand?.contains(uiState.searchQuery, ignoreCase = true) == true)
            val matchesStore = uiState.selectedStoreFilter == null ||
                    prod.availableStores.contains(uiState.selectedStoreFilter)
            val matchesCategory = uiState.selectedCategoryFilter == null ||
                    prod.categoryId == uiState.selectedCategoryFilter
            val matchesRating = prod.userRating >= uiState.minRatingFilter
            matchesQuery && matchesStore && matchesCategory && matchesRating
        }.let { list ->
            when (uiState.sortOption) {
                SortOption.RECENTLY_UPDATED -> list.sortedByDescending { it.updatedAt }
                SortOption.RECENTLY_ADDED -> list.sortedByDescending { it.createdAt }
                SortOption.ALPHABETICAL_ASC -> list.sortedBy { it.name.lowercase() }
                SortOption.ALPHABETICAL_DESC -> list.sortedByDescending { it.name.lowercase() }
                SortOption.RATING_HIGH_LOW -> list.sortedByDescending { it.userRating }
                SortOption.RATING_LOW_HIGH -> list.sortedBy { it.userRating }
            }
        }
    }

    // Build grouped map for display
    val groupedProducts: Map<String, List<ProductEntity>> = remember(filteredProducts, uiState.groupByOption, uiState.categories) {
        when (uiState.groupByOption) {
            GroupByOption.NONE -> mapOf("" to filteredProducts)
            GroupByOption.CATEGORY -> filteredProducts.groupBy { prod ->
                uiState.categories.find { it.id == prod.categoryId }?.name ?: "Uncategorized"
            }
            GroupByOption.STORE -> {
                val result = mutableMapOf<String, MutableList<ProductEntity>>()
                filteredProducts.forEach { prod ->
                    if (prod.availableStores.isEmpty()) {
                        result.getOrPut("No Store") { mutableListOf() }.add(prod)
                    } else {
                        prod.availableStores.forEach { store ->
                            result.getOrPut(store) { mutableListOf() }.add(prod)
                        }
                    }
                }
                result
            }
            GroupByOption.RATING -> filteredProducts.groupBy { prod ->
                when {
                    prod.userRating >= 4.5f -> "★★★★★ (4.5+)"
                    prod.userRating >= 3.5f -> "★★★★ (3.5+)"
                    prod.userRating >= 2.5f -> "★★★ (2.5+)"
                    prod.userRating >= 1.5f -> "★★ (1.5+)"
                    prod.userRating >= 0.5f -> "★ (0.5+)"
                    else -> "No Rating"
                }
            }
        }
    }

    // Active filter count for badge
    val activeFilterCount = remember(uiState.selectedStoreFilter, uiState.selectedCategoryFilter) {
        listOfNotNull(uiState.selectedStoreFilter, uiState.selectedCategoryFilter).size
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TopAppBar(
                    title = {
                        Text(
                            text = "RateMyGrocery",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        IconButton(onClick = onSettingsClick) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                )

                // Search Bar
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChanged,
                    placeholder = { Text("Search your ratings, products, brands...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChanged("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )

                // Quick Filter & Sort Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    FilterChip(
                        selected = activeFilterCount > 0,
                        onClick = { showFilterSheet = true },
                        label = {
                            Text(if (activeFilterCount > 0) "Filters ($activeFilterCount)" else "Filters")
                        },
                        leadingIcon = { Icon(Icons.Outlined.FilterList, contentDescription = null) }
                    )

                    // Sort Dropdown
                    Box {
                        FilterChip(
                            selected = uiState.sortOption != SortOption.RECENTLY_UPDATED,
                            onClick = { showSortMenu = true },
                            label = { Text(uiState.sortOption.label) },
                            leadingIcon = { Icon(Icons.Outlined.Sort, contentDescription = null) }
                        )
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            SortOption.values().forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = option.label,
                                            fontWeight = if (option == uiState.sortOption) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    leadingIcon = {
                                        if (option == uiState.sortOption) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        }
                                    },
                                    onClick = {
                                        onSortOptionSelected(option)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Group By Dropdown
                    Box {
                        FilterChip(
                            selected = uiState.groupByOption != GroupByOption.NONE,
                            onClick = { showGroupMenu = true },
                            label = { Text(if (uiState.groupByOption == GroupByOption.NONE) "Group" else uiState.groupByOption.label) }
                        )
                        DropdownMenu(
                            expanded = showGroupMenu,
                            onDismissRequest = { showGroupMenu = false }
                        ) {
                            GroupByOption.values().forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = option.label,
                                            fontWeight = if (option == uiState.groupByOption) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    leadingIcon = {
                                        if (option == uiState.groupByOption) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        }
                                    },
                                    onClick = {
                                        onGroupByOptionSelected(option)
                                        showGroupMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddProductClick(0) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        if (uiState.products.isEmpty()) {
            EmptyStateScreen(
                onScanClicked = { onAddProductClick(0) },
                onPasteLinkClicked = { onAddProductClick(1) },
                onSearchClicked = { onAddProductClick(2) },
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                if (filteredProducts.isEmpty()) {
                    item {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.SearchOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No products match your filters",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(onClick = {
                                    onStoreFilterSelected(null)
                                    onCategoryFilterSelected(null)
                                    onSearchQueryChanged("")
                                }) {
                                    Text("Clear All Filters")
                                }
                            }
                        }
                    }
                } else {
                    groupedProducts.forEach { (groupName, products) ->
                        // Show group header only when grouping is active
                        if (uiState.groupByOption != GroupByOption.NONE) {
                            item(key = "header_$groupName") {
                                Text(
                                    text = groupName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                                )
                            }
                        }

                        items(products, key = { it.id }) { product ->
                            val category = uiState.categories.find { it.id == product.categoryId }
                            CompactProductCard(
                                product = product,
                                category = category,
                                onClick = { onProductClick(product.id) },
                                onDeleteClick = { productToDelete = product }
                            )
                        }
                    }
                }
            }
        }
    }

    // Filter Bottom Sheet
    if (showFilterSheet) {
        FilterBottomSheet(
            categories = uiState.categories,
            selectedStoreFilter = uiState.selectedStoreFilter,
            selectedCategoryFilter = uiState.selectedCategoryFilter,
            onStoreFilterSelected = onStoreFilterSelected,
            onCategoryFilterSelected = onCategoryFilterSelected,
            onDismiss = { showFilterSheet = false }
        )
    }

    // Delete Confirmation Dialog
    productToDelete?.let { prod ->
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("Delete Product?") },
            text = { Text("Are you sure you want to remove '${prod.name}' from your ratings?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteProduct(prod)
                        productToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    categories: List<CategoryEntity>,
    selectedStoreFilter: String?,
    selectedCategoryFilter: Long?,
    onStoreFilterSelected: (String?) -> Unit,
    onCategoryFilterSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val stores = listOf("Woolworths", "Coles", "Aldi", "Tong Li", "IGA", "Asian Grocer")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Filter Products",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = {
                    onStoreFilterSelected(null)
                    onCategoryFilterSelected(null)
                }) {
                    Text("Clear All")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Store Filter
            Text(
                text = "By Store",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                stores.forEach { store ->
                    FilterChip(
                        selected = selectedStoreFilter == store,
                        onClick = {
                            onStoreFilterSelected(if (selectedStoreFilter == store) null else store)
                        },
                        label = { Text(store) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Category Filter
            Text(
                text = "By Category",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                categories.forEach { category ->
                    FilterChip(
                        selected = selectedCategoryFilter == category.id,
                        onClick = {
                            onCategoryFilterSelected(if (selectedCategoryFilter == category.id) null else category.id)
                        },
                        label = { Text(category.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Apply Filters")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CompactProductCard(
    product: ProductEntity,
    category: CategoryEntity?,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            // Product Thumbnail
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(56.dp)
            ) {
                if (!product.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!product.brand.isNullOrBlank() || !product.productSize.isNullOrBlank()) {
                    Text(
                        text = listOfNotNull(product.brand, product.productSize).joinToString(" • "),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Stores & Category Badges
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    product.availableStores.take(2).forEach { store ->
                        StoreBadge(storeName = store)
                    }

                    if (category != null) {
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = category.name,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                StarRatingBar(rating = product.userRating, starSize = 16.dp, showValueText = true)

                val badge = ProductBadge.fromKey(product.badge)
                if (badge != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    BadgeChip(badge = badge)
                }
            }
        }
    }
}
