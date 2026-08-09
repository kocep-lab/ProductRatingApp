package com.example.supermarketratingapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val barcode: String?,
    val name: String,
    val brand: String?,
    val productSize: String?,
    val availableStores: List<String>,
    val categoryId: Long?,
    val customTags: List<String>,
    val userRating: Float,
    val userComment: String?,
    val badge: String?,
    val imageUrl: String?,
    val localImagePath: String?,
    val sourceUrl: String?,
    
    val healthStarRating: Float?,
    val nutriScore: String?,
    val energyKj: Float?,
    val proteinG: Float?,
    val fatG: Float?,
    val saturatedFatG: Float?,
    val carbsG: Float?,
    val sugarsG: Float?,
    val sodiumMg: Float?,
    val fibreG: Float?,
    
    val createdAt: Long,
    val updatedAt: Long
)

fun createProduct(
    id: Long = 0,
    barcode: String? = null,
    name: String,
    brand: String? = null,
    productSize: String? = null,
    availableStores: List<String> = emptyList(),
    categoryId: Long? = null,
    customTags: List<String> = emptyList(),
    userRating: Float = 0.0f,
    userComment: String? = null,
    badge: String? = null,
    imageUrl: String? = null,
    localImagePath: String? = null,
    sourceUrl: String? = null,
    healthStarRating: Float? = null,
    nutriScore: String? = null,
    energyKj: Float? = null,
    proteinG: Float? = null,
    fatG: Float? = null,
    saturatedFatG: Float? = null,
    carbsG: Float? = null,
    sugarsG: Float? = null,
    sodiumMg: Float? = null,
    fibreG: Float? = null,
    createdAt: Long = System.currentTimeMillis(),
    updatedAt: Long = System.currentTimeMillis()
): ProductEntity {
    return ProductEntity(
        id = id,
        barcode = barcode,
        name = name,
        brand = brand,
        productSize = productSize,
        availableStores = availableStores,
        categoryId = categoryId,
        customTags = customTags,
        userRating = userRating,
        userComment = userComment,
        badge = badge,
        imageUrl = imageUrl,
        localImagePath = localImagePath,
        sourceUrl = sourceUrl,
        healthStarRating = healthStarRating,
        nutriScore = nutriScore,
        energyKj = energyKj,
        proteinG = proteinG,
        fatG = fatG,
        saturatedFatG = saturatedFatG,
        carbsG = carbsG,
        sugarsG = sugarsG,
        sodiumMg = sodiumMg,
        fibreG = fibreG,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
