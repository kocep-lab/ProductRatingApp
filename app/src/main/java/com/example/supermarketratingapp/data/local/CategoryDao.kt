package com.example.supermarketratingapp.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY sortOrder ASC, name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    fun getCategoryById(id: Long): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCategory(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertCategories(categories: List<CategoryEntity>): List<Long>

    @Update
    fun updateCategory(category: CategoryEntity): Int

    @Delete
    fun deleteCategory(category: CategoryEntity): Int

    @Query("DELETE FROM categories WHERE id = :id")
    fun deleteCategoryById(id: Long): Int
}
