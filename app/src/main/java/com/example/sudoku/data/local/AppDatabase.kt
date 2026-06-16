package com.example.sudoku.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [GameSlotEntity::class, SeedPuzzleEntity::class, PracticeStatsEntity::class, AdventureRecordEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameSlotDao(): GameSlotDao
    abstract fun seedPuzzleDao(): SeedPuzzleDao
    abstract fun practiceStatsDao(): PracticeStatsDao
    abstract fun adventureRecordDao(): AdventureRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sudoku_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
