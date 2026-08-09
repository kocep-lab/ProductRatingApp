package com.example.supermarketratingapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
    val iconName: String?,
    val colorHex: String?,
    val sortOrder: Int
)
