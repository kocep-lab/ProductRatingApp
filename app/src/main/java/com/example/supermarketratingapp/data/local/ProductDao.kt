package com.example.supermarketratingapp.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY updatedAt DESC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    fun getProductById(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    fun getProductByBarcode(barcode: String): ProductEntity?

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%' OR userComment LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun searchProducts(query: String): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProduct(product: ProductEntity): Long

    @Update
    fun updateProduct(product: ProductEntity): Int

    @Delete
    fun deleteProduct(product: ProductEntity): Int

    @Query("DELETE FROM products WHERE id = :id")
    fun deleteProductById(id: Long): Int

    @Query("DELETE FROM products")
    fun deleteAllProducts(): Int

    @Query("UPDATE products SET categoryId = NULL WHERE categoryId = :categoryId")
    fun clearCategoryFromProducts(categoryId: Long): Int
}
