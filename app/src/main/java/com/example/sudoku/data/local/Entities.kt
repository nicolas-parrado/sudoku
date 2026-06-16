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
    val activeThemeName: String = "slate" // Persistimos también el tema preferido
)

@Entity(tableName = "seed_puzzles")
data class SeedPuzzleEntity(
    @PrimaryKey
    val id: String, // Canonical seed (81 chars string)
    val puzzleString: String,
    val solutionString: String,
    val difficulty: Double
)
