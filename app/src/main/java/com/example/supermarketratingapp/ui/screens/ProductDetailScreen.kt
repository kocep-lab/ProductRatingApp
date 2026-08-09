package com.example.supermarketratingapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.supermarketratingapp.data.local.CategoryEntity
import com.example.supermarketratingapp.data.local.ProductEntity
import com.example.supermarketratingapp.ui.components.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProductDetailScreen(
    product: ProductEntity,
    categories: List<CategoryEntity>,
    onSaveProduct: (ProductEntity) -> Unit,
    onDeleteProduct: (ProductEntity) -> Unit,
    onAddCategoryClick: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember(product) { mutableStateOf(product.name) }
    var brand by remember(product) { mutableStateOf(product.brand ?: "") }
    var productSize by remember(product) { mutableStateOf(product.productSize ?: "") }
    var rating by remember(product) { mutableStateOf(product.userRating) }
    var comment by remember(product) { mutableStateOf(product.userComment ?: "") }
    var selectedBadge by remember(product) { mutableStateOf(ProductBadge.fromKey(product.badge)) }
    var selectedCategoryId by remember(product) { mutableStateOf(product.categoryId) }
    var stores by remember(product) { mutableStateOf(product.availableStores.toMutableStateList()) }
    var tags by remember(product) { mutableStateOf(product.customTags.toMutableStateList()) }
    var newTagInput by remember { mutableStateOf("") }
    var newStoreInput by remember { mutableStateOf("") }
    var showAddStoreDialog by remember { mutableStateOf(false) }
    var showAddCategoryPromptDialog by remember { mutableStateOf(false) }
    var newCategoryInput by remember { mutableStateOf("") }

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }


    val allSupermarkets = remember { listOf("Woolworths", "Coles", "Aldi", "Tong Li", "IGA", "Asian Grocer") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Product Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteConfirmDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Product", tint = MaterialTheme.colorScheme.error)
                    }
                    IconButton(
                        onClick = {
                            val updated = product.copy(
                                name = name,
                                brand = brand.ifBlank { null },
                                productSize = productSize.ifBlank { null },
                                userRating = rating,
                                userComment = comment.ifBlank { null },
                                badge = selectedBadge?.key,
                                categoryId = selectedCategoryId,
                                availableStores = stores.toList(),
                                customTags = tags.toList(),
                                updatedAt = System.currentTimeMillis()
                            )
                            onSaveProduct(updated)
                            onBackClick()
                        }
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save Changes", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Hero Photo
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                if (!product.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Product Name & Brand
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Product Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text("Brand") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = productSize,
                    onValueChange = { productSize = it },
                    label = { Text("Size (e.g. 175g)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Available Stores
            Text(text = "Available Stores", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val availableList = (allSupermarkets + stores).distinct()
                availableList.forEach { store ->
                    val isChecked = stores.contains(store)
                    FilterChip(
                        selected = isChecked,
                        onClick = {
                            if (isChecked) stores.remove(store) else stores.add(store)
                        },
                        label = { Text(store) }
                    )
                }
                AssistChip(
                    onClick = { showAddStoreDialog = true },
                    label = { Text("+ Add Store") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            // Personal Rating
            Text(text = "Your Rating", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            StarRatingBar(
                rating = rating,
                onRatingChanged = { rating = it },
                starSize = 32.dp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Decision Badge
            Text(text = "Quick Badge", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ProductBadge.values().forEach { badge ->
                    BadgeChip(
                        badge = badge,
                        isSelected = selectedBadge == badge,
                        onClick = {
                            selectedBadge = if (selectedBadge == badge) null else badge
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Personal Comment Box
            Text(text = "Your Notes & Review", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                placeholder = { Text("What did you think of the flavor, value, texture...") },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Category Selection
            Text(text = "Category", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                var expandedCatMenu by remember { mutableStateOf(false) }
                val currentCategory = categories.find { it.id == selectedCategoryId }

                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { expandedCatMenu = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = currentCategory?.name ?: "Select Category")
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }

                    DropdownMenu(
                        expanded = expandedCatMenu,
                        onDismissRequest = { expandedCatMenu = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    selectedCategoryId = cat.id
                                    expandedCatMenu = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { showAddCategoryPromptDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Category")
                }
            }

            if (showAddCategoryPromptDialog) {
                AlertDialog(
                    onDismissRequest = { showAddCategoryPromptDialog = false },
                    title = { Text("Add New Category") },
                    text = {
                        OutlinedTextField(
                            value = newCategoryInput,
                            onValueChange = { newCategoryInput = it },
                            label = { Text("Category Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (newCategoryInput.isNotBlank()) {
                                    onAddCategoryClick(newCategoryInput.trim())
                                    newCategoryInput = ""
                                    showAddCategoryPromptDialog = false
                                }
                            }
                        ) {
                            Text("Add")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddCategoryPromptDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }


            Spacer(modifier = Modifier.height(16.dp))

            // Custom Tags
            Text(text = "Custom Tags", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                tags.forEach { tag ->
                    InputChip(
                        selected = true,
                        onClick = { tags.remove(tag) },
                        label = { Text(tag) },
                        trailingIcon = { Icon(Icons.Default.Close, contentDescription = "Remove tag") }
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                OutlinedTextField(
                    value = newTagInput,
                    onValueChange = { newTagInput = it },
                    placeholder = { Text("e.g. #Crispy") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (newTagInput.isNotBlank()) {
                            val formatted = if (newTagInput.startsWith("#")) newTagInput else "#$newTagInput"
                            if (!tags.contains(formatted)) tags.add(formatted)
                            newTagInput = ""
                        }
                    }
                ) {
                    Text("Add Tag")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Nutrition Facts Panel
            NutritionSection(product = product)
        }
    }

    // Add Custom Store Dialog
    if (showAddStoreDialog) {
        AlertDialog(
            onDismissRequest = { showAddStoreDialog = false },
            title = { Text("Add Custom Store") },
            text = {
                OutlinedTextField(
                    value = newStoreInput,
                    onValueChange = { newStoreInput = it },
                    label = { Text("Store Name (e.g. Tong Li Market)") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newStoreInput.isNotBlank()) {
                            stores.add(newStoreInput.trim())
                            newStoreInput = ""
                        }
                        showAddStoreDialog = false
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddStoreDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Product?") },
            text = { Text("Are you sure you want to delete '${product.name}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteProduct(product)
                        showDeleteConfirmDialog = false
                        onBackClick()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) { Text("Cancel") }
            }
        )
    }
}
