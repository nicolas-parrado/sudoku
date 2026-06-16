package com.example.sudoku.data.repository

import android.content.Context
import com.example.sudoku.data.local.AppDatabase
import com.example.sudoku.data.local.GameSlotEntity
import com.example.sudoku.data.local.SeedPuzzleEntity
import com.example.sudoku.domain.engine.CanonicalNormalization
import com.example.sudoku.domain.model.BoardHistory
import com.example.sudoku.domain.model.BoardState
import com.example.sudoku.domain.model.GameSlot
import com.example.sudoku.domain.model.SudokuCell
import com.example.sudoku.data.local.PracticeStatsEntity
import com.example.sudoku.data.local.AdventureRecordEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class SudokuRepository(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val gameSlotDao = db.gameSlotDao()
    private val seedPuzzleDao = db.seedPuzzleDao()
    private val practiceStatsDao = db.practiceStatsDao()
    private val adventureRecordDao = db.adventureRecordDao()
    private val gson = Gson()

    /**
     * Inicializa la base de datos de semillas si está vacía o tiene menos de 50000. Lee de seeds_hard.txt en assets.
     */
    suspend fun initializeSeedsIfNeeded() = withContext(Dispatchers.IO) {
        if (seedPuzzleDao.countSeeds() < 50000) {
            seedPuzzleDao.clearAllSeeds()
            practiceStatsDao.clearAllStats() // Limpia estadísticas previas para nueva escala de 5 niveles
            val seeds = mutableListOf<SeedPuzzleEntity>()
            try {
                context.assets.open("seeds_hard.txt").use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            val parts = line!!.split(",")
                            if (parts.size == 3) {
                                val puzzle = parts[0].trim()
                                val solution = parts[1].trim()
                                val difficulty = parts[2].trim().toDoubleOrNull() ?: 0.0
                                val canonicalId = CanonicalNormalization.getCanonicalSeed(puzzle)
                                seeds.add(
                                    SeedPuzzleEntity(
                                        id = canonicalId,
                                        puzzleString = puzzle,
                                        solutionString = solution,
                                        difficulty = difficulty
                                    )
                                )
                            }
                        }
                    }
                }
                if (seeds.isNotEmpty()) {
                    val batchSize = 5000
                    for (i in 0 until seeds.size step batchSize) {
                        val batch = seeds.subList(i, minOf(i + batchSize, seeds.size))
                        seedPuzzleDao.insertSeeds(batch)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Carga el estado de juego guardado para un slot específico.
     */
    suspend fun loadGameSlot(slot: GameSlot): LoadedSlotData? = withContext(Dispatchers.IO) {
        val entity = gameSlotDao.getSlot(slot.name.lowercase()) ?: return@withContext null
        
        val cellType = object : TypeToken<List<SudokuCell>>() {}.type
        val historyType = object : TypeToken<List<BoardHistory>>() {}.type

        val cells: List<SudokuCell> = gson.fromJson(entity.boardStateJson, cellType)
        val undoStack: List<BoardHistory> = gson.fromJson(entity.undoStackJson, historyType)
        val redoStack: List<BoardHistory> = gson.fromJson(entity.redoStackJson, historyType)

        LoadedSlotData(
            boardState = BoardState(cells),
            undoStack = undoStack,
            redoStack = redoStack,
            level = entity.level,
            floor = entity.floor,
            chosenDifficulty = entity.chosenDifficulty,
            elapsedSeconds = entity.elapsedSeconds,
            difficulty = entity.difficulty,
            themeName = entity.activeThemeName,
            accumulatedTimeSeconds = entity.accumulatedTimeSeconds,
            accumulatedHintsUsed = entity.accumulatedHintsUsed
        )
    }

    /**
     * Guarda de forma asíncrona el estado actual de juego en Room.
     */
    suspend fun saveGameSlot(
        slot: GameSlot,
        boardState: BoardState,
        undoStack: List<BoardHistory>,
        redoStack: List<BoardHistory>,
        level: Int,
        floor: Int,
        chosenDifficulty: Int,
        elapsedSeconds: Long,
        difficulty: Double,
        themeName: String,
        accumulatedTimeSeconds: Long = 0,
        accumulatedHintsUsed: Int = 0
    ) = withContext(Dispatchers.IO) {
        val boardStateJson = gson.toJson(boardState.cells)
        val undoStackJson = gson.toJson(undoStack)
        val redoStackJson = gson.toJson(redoStack)

        val entity = GameSlotEntity(
            slotId = slot.name.lowercase(),
            level = level,
            floor = floor,
            chosenDifficulty = chosenDifficulty,
            boardStateJson = boardStateJson,
            undoStackJson = undoStackJson,
            redoStackJson = redoStackJson,
            elapsedSeconds = elapsedSeconds,
            difficulty = difficulty,
            activeThemeName = themeName,
            accumulatedTimeSeconds = accumulatedTimeSeconds,
            accumulatedHintsUsed = accumulatedHintsUsed
        )
        gameSlotDao.insertSlot(entity)
    }

    /**
     * Obtiene un puzzle pre-generado complejo de la base de datos Room.
     */
    suspend fun getRandomHardSeed(minDiff: Double, maxDiff: Double): SeedPuzzleEntity? = withContext(Dispatchers.IO) {
        initializeSeedsIfNeeded() // Nos aseguramos de tener semillas
        seedPuzzleDao.getRandomSeedInDifficultyRange(minDiff, maxDiff)
    }

    suspend fun getStatsForDifficulty(difficulty: Int): PracticeStatsEntity? = withContext(Dispatchers.IO) {
        practiceStatsDao.getStatsForDifficulty(difficulty)
    }

    suspend fun savePracticeStats(stats: PracticeStatsEntity) = withContext(Dispatchers.IO) {
        practiceStatsDao.saveStats(stats)
    }

    suspend fun getAllPracticeStats(): List<PracticeStatsEntity> = withContext(Dispatchers.IO) {
        practiceStatsDao.getAllStats()
    }

    suspend fun getAdventureRecord(): AdventureRecordEntity? = withContext(Dispatchers.IO) {
        adventureRecordDao.getRecord()
    }

    suspend fun saveAdventureRecord(record: AdventureRecordEntity) = withContext(Dispatchers.IO) {
        adventureRecordDao.saveRecord(record)
    }

    suspend fun clearAdventureRecord() = withContext(Dispatchers.IO) {
        adventureRecordDao.clearRecord()
    }

    data class LoadedSlotData(
        val boardState: BoardState,
        val undoStack: List<BoardHistory>,
        val redoStack: List<BoardHistory>,
        val level: Int,
        val floor: Int,
        val chosenDifficulty: Int,
        val elapsedSeconds: Long,
        val difficulty: Double,
        val themeName: String,
        val accumulatedTimeSeconds: Long = 0,
        val accumulatedHintsUsed: Int = 0
    )
}
