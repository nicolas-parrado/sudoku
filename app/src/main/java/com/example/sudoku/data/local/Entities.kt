package com.example.sudoku.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_slots")
data class GameSlotEntity(
    @PrimaryKey
    val slotId: String, // "adventure" o "practice"
    val level: Int = 1,
    val floor: Int = 1,
    val chosenDifficulty: Int = 1,
    val boardStateJson: String,
    val undoStackJson: String,
    val redoStackJson: String,
    val elapsedSeconds: Long = 0,
    val difficulty: Double = 1.0,
    val activeThemeName: String = "slate",
    val accumulatedTimeSeconds: Long = 0,
    val accumulatedHintsUsed: Int = 0
)

@Entity(tableName = "seed_puzzles")
data class SeedPuzzleEntity(
    @PrimaryKey
    val id: String, // Canonical seed (81 chars string)
    val puzzleString: String,
    val solutionString: String,
    val difficulty: Double
)

@Entity(tableName = "practice_stats")
data class PracticeStatsEntity(
    @PrimaryKey
    val difficulty: Int, // 0 to 4 (Fácil, Medio, Difícil, Injusto, Extremo)
    val timesPlayed: Int = 0,
    val bestTimeSeconds: Long = Long.MAX_VALUE,
    val recordHintsUsed: Int = 0
)

@Entity(tableName = "adventure_records")
data class AdventureRecordEntity(
    @PrimaryKey
    val id: Int = 1, // Siempre 1 para registro único
    val bestTimeSeconds: Long = Long.MAX_VALUE,
    val hintsUsed: Int = 0,
    val completedCount: Int = 0
)
