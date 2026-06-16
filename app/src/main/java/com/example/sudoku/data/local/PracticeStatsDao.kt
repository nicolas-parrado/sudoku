package com.example.sudoku.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PracticeStatsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveStats(stats: PracticeStatsEntity)

    @Query("SELECT * FROM practice_stats WHERE difficulty = :difficulty")
    suspend fun getStatsForDifficulty(difficulty: Int): PracticeStatsEntity?

    @Query("SELECT * FROM practice_stats")
    suspend fun getAllStats(): List<PracticeStatsEntity>
}
