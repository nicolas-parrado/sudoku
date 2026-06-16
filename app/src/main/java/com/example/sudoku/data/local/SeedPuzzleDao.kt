package com.example.sudoku.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SeedPuzzleDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSeeds(seeds: List<SeedPuzzleEntity>)

    @Query("SELECT * FROM seed_puzzles WHERE difficulty >= :minDiff AND difficulty <= :maxDiff ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomSeedInDifficultyRange(minDiff: Double, maxDiff: Double): SeedPuzzleEntity?

    @Query("SELECT COUNT(*) FROM seed_puzzles")
    suspend fun countSeeds(): Int
}
