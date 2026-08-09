package com.example.supermarketratingapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [ProductEntity::class, CategoryEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "supermarket_rating_db"
                )
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.categoryDao()?.insertCategories(getDefaultCategories())
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

fun getDefaultCategories(): List<CategoryEntity> {
    return listOf(
        CategoryEntity(id = 0, name = "Snacks", iconName = "Cookie", colorHex = "#FF9800", sortOrder = 1),
        CategoryEntity(id = 0, name = "Pantry", iconName = "Kitchen", colorHex = "#795548", sortOrder = 2),
        CategoryEntity(id = 0, name = "Asian Grocery", iconName = "RamenDining", colorHex = "#E91E63", sortOrder = 3),
        CategoryEntity(id = 0, name = "Dairy & Fridge", iconName = "WaterDrop", colorHex = "#2196F3", sortOrder = 4),
        CategoryEntity(id = 0, name = "Frozen", iconName = "AcUnit", colorHex = "#00BCD4", sortOrder = 5),
        CategoryEntity(id = 0, name = "Beverages", iconName = "LocalCafe", colorHex = "#9C27B0", sortOrder = 6),
        CategoryEntity(id = 0, name = "Fresh Produce", iconName = "Nutrition", colorHex = "#4CAF50", sortOrder = 7),
        CategoryEntity(id = 0, name = "Bakery", iconName = "BakeryDining", colorHex = "#FFC107", sortOrder = 8),
        CategoryEntity(id = 0, name = "Meat & Seafood", iconName = "SetMeal", colorHex = "#F44336", sortOrder = 9),
        CategoryEntity(id = 0, name = "Household", iconName = "CleaningServices", colorHex = "#607D8B", sortOrder = 10),
        CategoryEntity(id = 0, name = "Personal Care", iconName = "SelfImprovement", colorHex = "#9E9E9E", sortOrder = 11)
    )
}

