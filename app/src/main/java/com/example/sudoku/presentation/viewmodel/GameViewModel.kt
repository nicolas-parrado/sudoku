package com.example.sudoku.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sudoku.data.repository.SudokuRepository
import com.example.sudoku.domain.engine.SudokuSolver
import com.example.sudoku.domain.model.BoardHistory
import com.example.sudoku.domain.model.BoardState
import com.example.sudoku.domain.model.GameSlot
import com.example.sudoku.domain.model.HintDetail
import com.example.sudoku.domain.model.SudokuCell
import com.example.sudoku.presentation.theme.SudokuTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SudokuRepository(application)

    // Estado general de la UI del juego
    data class GameUiState(
        val isMenuOpen: Boolean = true,
        val activeSlot: GameSlot = GameSlot.ADVENTURE,
        val boardState: BoardState = BoardState(),
        val selectedIndex: Int? = null,
        val isNoteModeActive: Boolean = false,
        val level: Int = 1,
        val floor: Int = 1,
        val chosenDifficulty: Int = 3, // Práctica 1..10
        val elapsedSeconds: Long = 0,
        val currentDifficulty: Double = 1.0,
        val isCompleted: Boolean = false,
        val activeTheme: SudokuTheme.Type = SudokuTheme.Type.SLATE,
        val conflictingCells: Set<Int> = emptySet(),
        
        // Pistas
        val activeHint: HintDetail? = null,
        val showVisualHint: Boolean = false,

        // Botones deshabilitados
        val disabledNumbers: Set<Int> = emptySet(),
        
        // Undo / Redo
        val canUndo: Boolean = false,
        val canRedo: Boolean = false,

        // Carga y estadísticas de práctica/aventura
        val isLoading: Boolean = false,
        val practiceStats: Map<Int, com.example.sudoku.data.local.PracticeStatsEntity> = emptyMap(),
        val hintsRequestedInCurrentGame: Int = 0,
        val accumulatedTimeSeconds: Long = 0,
        val accumulatedHintsUsed: Int = 0,
        val adventureRecord: com.example.sudoku.data.local.AdventureRecordEntity? = null,
        val isAdventureCompleted: Boolean = false,
        val coins: Int = 100,
        val lastCoinsEarned: Int = 0,
        val lastTimeBonusEarned: Boolean = false,
        val lastBonusEarned: Int = 0,
        val nextHintCost: Int = 0,
        
        // Nuevos poderes
        val selectedNumpadNumber: Int = 1,
        val isTimerFrozen: Boolean = false,
        val frozenTimeRemaining: Long = 0
    ) {
        val baseReward: Int
            get() = when (level) {
                in 1..2 -> 50
                in 3..4 -> 100
                in 5..6 -> 150
                in 7..8 -> 200
                else -> 250
            }

        val baseBonusReward: Int
            get() = (baseReward * 0.25).toInt()

        val timeLimitForBonus: Long
            get() = when (level) {
                in 1..2 -> 180L // 3 min
                in 3..4 -> 300L // 5 min
                in 5..6 -> 480L // 8 min
                in 7..8 -> 720L // 12 min
                else -> 900L // 15 min
            }

        val currentBonusReward: Int
            get() {
                if (elapsedSeconds < timeLimitForBonus) {
                    return baseBonusReward
                } else {
                    val minutesOver = ((elapsedSeconds - timeLimitForBonus) / 60).toInt() + 1
                    var currentBonus = baseBonusReward.toDouble()
                    repeat(minutesOver) {
                        currentBonus *= 0.60
                    }
                    return kotlin.math.round(currentBonus).toInt()
                }
            }
    }

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    // Historial en memoria (Undo / Redo)
    private val undoStack = mutableListOf<BoardHistory>()
    private val redoStack = mutableListOf<BoardHistory>()

    // Solución correcta para verificar finalización
    private var currentSolution: String = ""

    // Timer Job
    private var timerJob: Job? = null

    init {
        // Inicializar semillas asíncronamente en el primer inicio
        viewModelScope.launch {
            repository.initializeSeedsIfNeeded()
            loadPracticeStats()
            loadAdventureRecord()
            // Intentar cargar partidas previas
            loadSavedGame(GameSlot.ADVENTURE)
        }
    }

    fun loadPracticeStats() {
        viewModelScope.launch {
            val statsList = repository.getAllPracticeStats()
            val statsMap = statsList.associateBy { it.difficulty }
            _uiState.update { it.copy(practiceStats = statsMap) }
        }
    }

    fun loadAdventureRecord() {
        viewModelScope.launch {
            val record = repository.getAdventureRecord()
            _uiState.update { it.copy(adventureRecord = record) }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                if (!_uiState.value.isMenuOpen && !_uiState.value.isCompleted) {
                    _uiState.update { state ->
                        if (state.isTimerFrozen) {
                            val nextRemaining = state.frozenTimeRemaining - 1
                            if (nextRemaining <= 0) {
                                state.copy(
                                    isTimerFrozen = false,
                                    frozenTimeRemaining = 0
                                )
                            } else {
                                state.copy(
                                    frozenTimeRemaining = nextRemaining
                                )
                            }
                        } else {
                            state.copy(
                                elapsedSeconds = state.elapsedSeconds + 1
                            )
                        }
                    }
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
    }

    /**
     * Carga el estado de juego desde Room para el slot activo o por defecto.
     */
    fun loadSavedGame(slot: GameSlot) {
        viewModelScope.launch {
            val data = repository.loadGameSlot(slot)
            if (data != null) {
                undoStack.clear()
                undoStack.addAll(data.undoStack)
                redoStack.clear()
                redoStack.addAll(data.redoStack)
                
                // Intentar recuperar la solución ejecutando el solver
                val initialPuzzleString = getGivenPuzzleString(data.boardState)
                val solvedResult = SudokuSolver.analyze(initialPuzzleString)
                currentSolution = solvedResult.solvedBoard ?: ""

                _uiState.update {
                    it.copy(
                        isMenuOpen = false,
                        activeSlot = slot,
                        boardState = data.boardState,
                        selectedIndex = null,
                        level = data.level,
                        floor = data.floor,
                        chosenDifficulty = data.chosenDifficulty,
                        elapsedSeconds = data.elapsedSeconds,
                        currentDifficulty = data.difficulty,
                        activeTheme = SudokuTheme.Type.fromName(data.themeName),
                        isCompleted = checkIsCompleted(data.boardState),
                        canUndo = undoStack.isNotEmpty(),
                        canRedo = redoStack.isNotEmpty(),
                        activeHint = null,
                        showVisualHint = false,
                        accumulatedTimeSeconds = data.accumulatedTimeSeconds,
                        accumulatedHintsUsed = data.accumulatedHintsUsed,
                        isAdventureCompleted = false,
                        coins = data.coins
                    )
                }
                updateDisabledNumbers()
                updateConflicts()
                startTimer()
            } else {
                // Si no hay guardado, abrimos el menú inicial
                _uiState.update { it.copy(isMenuOpen = true, activeSlot = slot) }
            }
        }
    }

    /**
     * Inicia una nueva partida según el modo y parámetros actuales.
     */
    fun startNewGame(slot: GameSlot) {
        stopTimer()
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    isLoading = true, 
                    isMenuOpen = false, 
                    activeSlot = slot,
                    hintsRequestedInCurrentGame = 0 
                ) 
            }
            
            val difficultyDecimal = if (slot == GameSlot.ADVENTURE) {
                val lvl = _uiState.value.level
                when (lvl) {
                    in 1..2 -> 0.0
                    in 3..4 -> 1.0
                    in 5..6 -> 2.0
                    in 7..8 -> 3.0
                    else -> 4.0
                }
            } else {
                _uiState.value.chosenDifficulty.toDouble()
            }

            // Generación o recuperación asíncrona en Dispatchers.Default
            val (puzzle, solution) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                var puzzleStr = ""
                var solutionStr = ""
                val seed = repository.getRandomHardSeed(difficultyDecimal, difficultyDecimal)
                if (seed != null) {
                    puzzleStr = seed.puzzleString
                    solutionStr = seed.solutionString
                } else {
                    // Fallback rápido si la BD no está cargada todavía
                    puzzleStr =   "003020600900305001001806400008102900700000008006708200002609500800203009005010300"
                    solutionStr = "483921657967345821251876493548132976729564138136798245372689514814253769695417382"
                }
                Pair(puzzleStr, solutionStr)
            }

            currentSolution = solution
            val newBoard = BoardState.from81CharString(puzzle, solution)
            undoStack.clear()
            redoStack.clear()

            _uiState.update {
                it.copy(
                    boardState = newBoard,
                    selectedIndex = null,
                    elapsedSeconds = 0,
                    currentDifficulty = difficultyDecimal,
                    isCompleted = false,
                    canUndo = false,
                    canRedo = false,
                    activeHint = null,
                    showVisualHint = false,
                    isLoading = false,
                    isAdventureCompleted = false
                )
            }
            
            updateDisabledNumbers()
            updateConflicts()
            saveCurrentStateToRoom()
            startTimer()
        }
    }

    /**
     * Guarda el estado actual en la base de datos asíncronamente.
     */
    private fun saveCurrentStateToRoom() {
        val state = _uiState.value
        viewModelScope.launch {
            repository.saveGameSlot(
                slot = state.activeSlot,
                boardState = state.boardState,
                undoStack = undoStack,
                redoStack = redoStack,
                level = state.level,
                floor = state.floor,
                chosenDifficulty = state.chosenDifficulty,
                elapsedSeconds = state.elapsedSeconds,
                difficulty = state.currentDifficulty,
                themeName = state.activeTheme.name.lowercase(),
                accumulatedTimeSeconds = state.accumulatedTimeSeconds,
                accumulatedHintsUsed = state.accumulatedHintsUsed,
                coins = state.coins
            )
        }
    }

    // --- ACCIONES DEL JUGADOR ---

    fun selectCell(index: Int) {
        if (_uiState.value.isCompleted) return
        _uiState.update { it.copy(selectedIndex = index) }
    }

    fun inputNumber(number: Int) {
        _uiState.update { it.copy(selectedNumpadNumber = number) }
        val index = _uiState.value.selectedIndex ?: return
        val cell = _uiState.value.boardState.getCell(index)
        if (cell.isGiven || _uiState.value.isCompleted) return

        pushToUndoStack()

        val updatedCells = _uiState.value.boardState.cells.toMutableList()
        if (_uiState.value.isNoteModeActive) {
            // Modo Nota: Alterna el número dentro del set de notas de la celda
            val currentNotes = cell.notes
            val newNotes = if (currentNotes.contains(number)) {
                currentNotes - number
            } else {
                currentNotes + number
            }
            // Al agregar notas, limpiamos el valor de la celda
            updatedCells[index] = cell.copy(value = 0, notes = newNotes)
        } else {
            // Modo Número: asigna el valor directo y limpia notas
            updatedCells[index] = cell.copy(value = number, notes = emptySet())
            
            // FEAT-002: Limpieza automática de notas en la misma fila, columna o bloque 3x3
            val targetRow = cell.row
            val targetCol = cell.col
            val targetBlock = cell.block
            for (i in 0 until 81) {
                if (i != index) {
                    val cellI = updatedCells[i]
                    if (cellI.value == 0 && cellI.notes.contains(number)) {
                        val r = i / 9
                        val c = i % 9
                        val b = (r / 3) * 3 + (c / 3)
                        if (r == targetRow || c == targetCol || b == targetBlock) {
                            updatedCells[i] = cellI.copy(notes = cellI.notes - number)
                        }
                    }
                }
            }
        }

        val nextBoardState = BoardState(updatedCells)
        val isDone = checkIsCompleted(nextBoardState)

        _uiState.update {
            it.copy(
                boardState = nextBoardState,
                isCompleted = isDone,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }

        if (isDone) {
            handleGameCompletion()
        }

        updateDisabledNumbers()
        updateConflicts()
        saveCurrentStateToRoom()
    }

    private fun savePracticeCompletionStats() {
        val state = _uiState.value
        val diffInt = state.chosenDifficulty
        val currentElapsed = state.elapsedSeconds
        val currentHints = state.hintsRequestedInCurrentGame

        viewModelScope.launch {
            val currentStats = repository.getStatsForDifficulty(diffInt)
            val newStats = if (currentStats == null) {
                com.example.sudoku.data.local.PracticeStatsEntity(
                    difficulty = diffInt,
                    timesPlayed = 1,
                    bestTimeSeconds = currentElapsed,
                    recordHintsUsed = currentHints
                )
            } else {
                val bestTime = minOf(currentStats.bestTimeSeconds, currentElapsed)
                val hintsUsed = if (currentElapsed < currentStats.bestTimeSeconds) {
                    currentHints
                } else if (currentElapsed == currentStats.bestTimeSeconds) {
                    minOf(currentStats.recordHintsUsed, currentHints)
                } else {
                    currentStats.recordHintsUsed
                }
                currentStats.copy(
                    timesPlayed = currentStats.timesPlayed + 1,
                    bestTimeSeconds = bestTime,
                    recordHintsUsed = hintsUsed
                )
            }
            repository.savePracticeStats(newStats)
            loadPracticeStats()
        }
    }

    private fun saveAdventureCompletionStats(totalTime: Long, totalHints: Int) {
        viewModelScope.launch {
            val record = repository.getAdventureRecord()
            val newRecord = if (record == null) {
                com.example.sudoku.data.local.AdventureRecordEntity(
                    bestTimeSeconds = totalTime,
                    hintsUsed = totalHints,
                    completedCount = 1
                )
            } else {
                val isNewBest = totalTime < record.bestTimeSeconds
                val bestTime = if (isNewBest) totalTime else record.bestTimeSeconds
                val bestHints = if (isNewBest) totalHints else record.hintsUsed
                record.copy(
                    bestTimeSeconds = bestTime,
                    hintsUsed = bestHints,
                    completedCount = record.completedCount + 1
                )
            }
            repository.saveAdventureRecord(newRecord)
            loadAdventureRecord()
        }
    }

    fun resetAdventure() {
        viewModelScope.launch {
            repository.saveGameSlot(
                slot = GameSlot.ADVENTURE,
                boardState = BoardState(),
                undoStack = emptyList(),
                redoStack = emptyList(),
                level = 1,
                floor = 1,
                chosenDifficulty = 0,
                elapsedSeconds = 0,
                difficulty = 0.0,
                themeName = _uiState.value.activeTheme.name.lowercase(),
                accumulatedTimeSeconds = 0,
                accumulatedHintsUsed = 0,
                coins = 100
            )
            _uiState.update {
                it.copy(
                    level = 1,
                    floor = 1,
                    accumulatedTimeSeconds = 0,
                    accumulatedHintsUsed = 0,
                    isAdventureCompleted = false,
                    coins = 100,
                    lastCoinsEarned = 0,
                    lastTimeBonusEarned = false
                )
            }
            startNewGame(GameSlot.ADVENTURE)
        }
    }

    fun clearCell() {
        val index = _uiState.value.selectedIndex ?: return
        val cell = _uiState.value.boardState.getCell(index)
        if (cell.isGiven || _uiState.value.isCompleted) return

        pushToUndoStack()

        val updatedCells = _uiState.value.boardState.cells.toMutableList()
        updatedCells[index] = cell.copy(value = 0, notes = emptySet())

        _uiState.update {
            it.copy(
                boardState = BoardState(updatedCells),
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }

        updateDisabledNumbers()
        updateConflicts()
        saveCurrentStateToRoom()
    }

    fun toggleNoteMode() {
        _uiState.update { it.copy(isNoteModeActive = !it.isNoteModeActive) }
    }

    fun undo() {
        if (undoStack.isEmpty() || _uiState.value.isCompleted) return
        
        // El estado actual va al redo stack
        redoStack.add(BoardHistory(_uiState.value.boardState.cells))

        val previousState = undoStack.removeAt(undoStack.size - 1)
        _uiState.update {
            it.copy(
                boardState = BoardState(previousState.cells),
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
        updateDisabledNumbers()
        updateConflicts()
        saveCurrentStateToRoom()
    }

    fun redo() {
        if (redoStack.isEmpty() || _uiState.value.isCompleted) return

        // El estado actual va al undo stack
        undoStack.add(BoardHistory(_uiState.value.boardState.cells))

        val nextState = redoStack.removeAt(redoStack.size - 1)
        _uiState.update {
            it.copy(
                boardState = BoardState(nextState.cells),
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
        updateDisabledNumbers()
        updateConflicts()
        saveCurrentStateToRoom()
    }

    // --- CONFIGURACIÓN Y MENÚS ---

    fun setLevelAndFloor(level: Int, floor: Int) {
        _uiState.update { it.copy(level = level, floor = floor) }
    }

    fun setChosenDifficulty(diff: Int) {
        _uiState.update { it.copy(chosenDifficulty = diff) }
    }

    fun changeTheme(theme: SudokuTheme.Type) {
        _uiState.update { it.copy(activeTheme = theme) }
        saveCurrentStateToRoom()
    }

    fun restartCurrentSudoku() {
        val originalCells = _uiState.value.boardState.cells.map {
            if (it.isGiven) it else it.copy(value = 0, notes = emptySet())
        }
        undoStack.clear()
        redoStack.clear()
        _uiState.update {
            it.copy(
                boardState = BoardState(originalCells),
                isCompleted = false,
                canUndo = false,
                canRedo = false,
                selectedIndex = null,
                activeHint = null,
                showVisualHint = false
            )
        }
        updateDisabledNumbers()
        updateConflicts()
        saveCurrentStateToRoom()
        startTimer()
    }

    fun openMenu() {
        stopTimer()
        _uiState.update { it.copy(isMenuOpen = true) }
    }

    // --- SISTEMA DE PISTAS (HINTS) ---

    fun requestHint() {
        val state = _uiState.value
        if (state.isCompleted) return

        val currentPuzzleString = getGivenPlusUserPuzzleString(state.boardState)
        val analysis = SudokuSolver.analyze(currentPuzzleString)

        if (analysis.nextHint != null) {
            val cost = (analysis.nextHint.difficultyRating * 10).toInt()
            
            // Verificar economía en modo Aventura
            if (state.activeSlot == GameSlot.ADVENTURE) {
                if (state.coins < cost) {
                    _uiState.update {
                        it.copy(
                            activeHint = HintDetail("Monedas insuficientes. Esta pista cuesta $cost 🪙 (Tienes: ${state.coins} 🪙)"),
                            showVisualHint = false
                        )
                    }
                    return
                }
                // Si hay fondos suficientes, cobrar
                _uiState.update {
                    it.copy(
                        coins = it.coins - cost,
                        activeHint = analysis.nextHint,
                        showVisualHint = false,
                        hintsRequestedInCurrentGame = it.hintsRequestedInCurrentGame + 1
                    )
                }
            } else {
                // Modo Práctica: pista gratis
                _uiState.update {
                    it.copy(
                        activeHint = analysis.nextHint,
                        showVisualHint = false,
                        hintsRequestedInCurrentGame = it.hintsRequestedInCurrentGame + 1
                    )
                }
            }
            saveCurrentStateToRoom()
        } else {
            // El resolvedor no encuentra más pistas con las técnicas implementadas
            if (state.conflictingCells.isNotEmpty()) {
                _uiState.update {
                    it.copy(
                        activeHint = HintDetail("Resuelve primero los conflictos en rojo antes de pedir una pista lógica."),
                        showVisualHint = false
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        activeHint = HintDetail("No se encontraron pistas sencillas. Intenta avanzar rellenando celdas obvias."),
                        showVisualHint = false
                    )
                }
            }
        }
    }

    fun toggleVisualHint() {
        _uiState.update { it.copy(showVisualHint = !it.showVisualHint) }
    }

    fun clearActiveHint() {
        _uiState.update {
            it.copy(
                activeHint = null,
                showVisualHint = false
            )
        }
    }

    fun advanceAdventureFloor() {
        val currentFloor = _uiState.value.floor
        val currentLevel = _uiState.value.level
        
        val nextAccumTime = _uiState.value.accumulatedTimeSeconds + _uiState.value.elapsedSeconds
        val nextAccumHints = _uiState.value.accumulatedHintsUsed + _uiState.value.hintsRequestedInCurrentGame

        if (currentFloor < 10) {
            _uiState.update {
                it.copy(
                    floor = currentFloor + 1,
                    accumulatedTimeSeconds = nextAccumTime,
                    accumulatedHintsUsed = nextAccumHints
                )
            }
        } else {
            // Avanza de nivel
            _uiState.update {
                it.copy(
                    level = currentLevel + 1,
                    floor = 1,
                    accumulatedTimeSeconds = nextAccumTime,
                    accumulatedHintsUsed = nextAccumHints
                )
            }
        }
        startNewGame(GameSlot.ADVENTURE)
    }

    // --- MÉTODOS DE AYUDA INTERNA ---

    private fun pushToUndoStack() {
        undoStack.add(BoardHistory(_uiState.value.boardState.cells.map { it.copy() }))
        redoStack.clear() // Vaciar rehacer en cada acción nueva
    }

    private fun checkIsCompleted(board: BoardState): Boolean {
        // Un tablero está completo si no tiene celdas vacías,
        // no tiene conflictos visuales, y coincide con la solución del puzzle
        val currentStr = board.to81CharString()
        if (currentStr.contains('0')) return false
        
        // Si no tenemos la solución calculada por seguridad
        if (currentSolution.isEmpty()) return false
        
        return currentStr == currentSolution
    }

    private fun updateDisabledNumbers() {
        // Un número se deshabilita/oculta si está colocado 9 veces correctamente
        val counts = IntArray(10)
        val cells = _uiState.value.boardState.cells
        for (i in 0 until 81) {
            val v = cells[i].value
            // Comprobamos contra la solución para asegurar que está "correctamente" colocado 9 veces
            if (v != 0 && currentSolution.isNotEmpty() && currentSolution[i].digitToInt() == v) {
                counts[v]++
            }
        }
        val disabled = mutableSetOf<Int>()
        for (num in 1..9) {
            if (counts[num] >= 9) {
                disabled.add(num)
            }
        }
        _uiState.update { it.copy(disabledNumbers = disabled) }
    }

    private fun updateConflicts() {
        val conflicts = SudokuSolver.getConflictingCells(_uiState.value.boardState)
        _uiState.update { it.copy(conflictingCells = conflicts) }
        updateNextHintCost()
    }

    private fun updateNextHintCost() {
        val state = _uiState.value
        if (state.isCompleted) return
        val currentPuzzleString = getGivenPlusUserPuzzleString(state.boardState)
        val analysis = SudokuSolver.analyze(currentPuzzleString)
        val cost = if (analysis.nextHint != null) {
            (analysis.nextHint.difficultyRating * 10).toInt()
        } else {
            0
        }
        _uiState.update { it.copy(nextHintCost = cost) }
    }

    private fun getGivenPuzzleString(board: BoardState): String {
        return board.cells.joinToString("") { if (it.isGiven) it.value.toString() else "0" }
    }

    private fun getGivenPlusUserPuzzleString(board: BoardState): String {
        return board.cells.joinToString("") { if (it.value != 0) it.value.toString() else "0" }
    }

    // --- LÓGICA DE PODERES ---

    private fun handleGameCompletion() {
        stopTimer()
        undoStack.clear()
        redoStack.clear()
        _uiState.update { it.copy(canUndo = false, canRedo = false) }

        if (_uiState.value.activeSlot == GameSlot.PRACTICE) {
            savePracticeCompletionStats()
        } else {
            // Modo Aventura: calcular monedas y verificar final de la torre
            val currentFloor = _uiState.value.floor
            val currentLevel = _uiState.value.level
            val elapsed = _uiState.value.elapsedSeconds

            val baseReward = _uiState.value.baseReward
            val bonusReward = _uiState.value.currentBonusReward
            val totalReward = baseReward + bonusReward

            val totalTime = _uiState.value.accumulatedTimeSeconds + elapsed
            val totalHints = _uiState.value.accumulatedHintsUsed + _uiState.value.hintsRequestedInCurrentGame

            _uiState.update {
                it.copy(
                    coins = it.coins + totalReward,
                    lastCoinsEarned = totalReward,
                    lastBonusEarned = bonusReward,
                    lastTimeBonusEarned = elapsed < _uiState.value.timeLimitForBonus
                )
            }

            if (currentLevel == 10 && currentFloor == 10) {
                _uiState.update {
                    it.copy(
                        accumulatedTimeSeconds = totalTime,
                        accumulatedHintsUsed = totalHints,
                        isAdventureCompleted = true
                    )
                }
                saveAdventureCompletionStats(totalTime, totalHints)
            }
        }
    }

    fun fillAllNotes() {
        if (_uiState.value.isCompleted) return
        val state = _uiState.value

        if (state.activeSlot == GameSlot.ADVENTURE) {
            if (state.coins < 30) {
                _uiState.update {
                    it.copy(
                        activeHint = HintDetail("Monedas insuficientes. Llenar notas cuesta 30 🪙 (Tienes: ${state.coins} 🪙)"),
                        showVisualHint = false
                    )
                }
                return
            }
            _uiState.update { it.copy(coins = it.coins - 30) }
        }

        pushToUndoStack()

        val cells = state.boardState.cells
        val updatedCells = cells.map { cell ->
            if (cell.value != 0 || cell.isGiven) {
                cell.copy(notes = emptySet())
            } else {
                val index = cell.index
                val row = cell.row
                val col = cell.col
                val block = cell.block

                val possible = (1..9).toMutableSet()
                for (i in 0 until 81) {
                    if (i != index) {
                        val other = cells[i]
                        if (other.value != 0) {
                            if (other.row == row || other.col == col || other.block == block) {
                                possible.remove(other.value)
                            }
                        }
                    }
                }
                cell.copy(notes = possible)
            }
        }

        _uiState.update {
            it.copy(
                boardState = BoardState(updatedCells),
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
        saveCurrentStateToRoom()
    }

    fun revealSelectedCell() {
        if (_uiState.value.isCompleted) return
        val state = _uiState.value
        val index = state.selectedIndex
        if (index == null) {
            _uiState.update {
                it.copy(
                    activeHint = HintDetail("Selecciona primero una celda vacía para revelar su número."),
                    showVisualHint = false
                )
            }
            return
        }
        val cell = state.boardState.getCell(index)
        if (cell.isGiven || cell.value != 0) {
            _uiState.update {
                it.copy(
                    activeHint = HintDetail("Solo puedes revelar celdas vacías."),
                    showVisualHint = false
                )
            }
            return
        }

        if (state.activeSlot == GameSlot.ADVENTURE) {
            if (state.coins < 100) {
                _uiState.update {
                    it.copy(
                        activeHint = HintDetail("Monedas insuficientes. Ojo de Halcón cuesta 100 🪙 (Tienes: ${state.coins} 🪙)"),
                        showVisualHint = false
                    )
                }
                return
            }
            _uiState.update { it.copy(coins = it.coins - 100) }
        }

        pushToUndoStack()

        val correctValue = currentSolution[index].digitToInt()
        val updatedCells = state.boardState.cells.toMutableList()
        updatedCells[index] = cell.copy(value = correctValue, notes = emptySet())

        // Limpieza automática de notas en fila, columna o bloque (FEAT-002)
        val targetRow = cell.row
        val targetCol = cell.col
        val targetBlock = cell.block
        for (i in 0 until 81) {
            if (i != index) {
                val cellI = updatedCells[i]
                if (cellI.value == 0 && cellI.notes.contains(correctValue)) {
                    val r = i / 9
                    val c = i % 9
                    val b = (r / 3) * 3 + (c / 3)
                    if (r == targetRow || c == targetCol || b == targetBlock) {
                        updatedCells[i] = cellI.copy(notes = cellI.notes - correctValue)
                    }
                }
            }
        }

        val nextBoardState = BoardState(updatedCells)
        val isDone = checkIsCompleted(nextBoardState)

        _uiState.update {
            it.copy(
                boardState = nextBoardState,
                isCompleted = isDone,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }

        if (isDone) {
            handleGameCompletion()
        }

        updateDisabledNumbers()
        updateConflicts()
        saveCurrentStateToRoom()
    }

    fun cleanRedundantNotes() {
        if (_uiState.value.isCompleted) return
        val state = _uiState.value

        if (state.activeSlot == GameSlot.ADVENTURE) {
            if (state.coins < 30) {
                _uiState.update {
                    it.copy(
                        activeHint = HintDetail("Monedas insuficientes. Escoba Lógica cuesta 30 🪙 (Tienes: ${state.coins} 🪙)"),
                        showVisualHint = false
                    )
                }
                return
            }
            _uiState.update { it.copy(coins = it.coins - 30) }
        }

        pushToUndoStack()

        val cells = state.boardState.cells
        val updatedCells = cells.map { cell ->
            if (cell.value != 0 || cell.isGiven) {
                cell
            } else {
                val index = cell.index
                val row = cell.row
                val col = cell.col
                val block = cell.block

                val possible = (1..9).toMutableSet()
                for (i in 0 until 81) {
                    if (i != index) {
                        val other = cells[i]
                        if (other.value != 0) {
                            if (other.row == row || other.col == col || other.block == block) {
                                possible.remove(other.value)
                            }
                        }
                    }
                }
                val newNotes = cell.notes.intersect(possible)
                cell.copy(notes = newNotes)
            }
        }

        _uiState.update {
            it.copy(
                boardState = BoardState(updatedCells),
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
        saveCurrentStateToRoom()
    }

    fun triggerNumberBomb() {
        if (_uiState.value.isCompleted) return
        val state = _uiState.value
        val targetNumber = state.selectedNumpadNumber

        val emptyCellsWithTarget = state.boardState.cells.filter { cell ->
            cell.value == 0 && !cell.isGiven && currentSolution[cell.index].digitToInt() == targetNumber
        }

        if (emptyCellsWithTarget.isEmpty()) {
            _uiState.update {
                it.copy(
                    activeHint = HintDetail("No quedan celdas vacías para el número $targetNumber."),
                    showVisualHint = false
                )
            }
            return
        }

        if (state.activeSlot == GameSlot.ADVENTURE) {
            if (state.coins < 50) {
                _uiState.update {
                    it.copy(
                        activeHint = HintDetail("Monedas insuficientes. Bomba de Números cuesta 50 🪙 (Tienes: ${state.coins} 🪙)"),
                        showVisualHint = false
                    )
                }
                return
            }
            _uiState.update { it.copy(coins = it.coins - 50) }
        }

        pushToUndoStack()

        val randomCell = emptyCellsWithTarget.random()
        val index = randomCell.index
        val updatedCells = state.boardState.cells.toMutableList()
        updatedCells[index] = randomCell.copy(value = targetNumber, notes = emptySet())

        // Limpieza automática de notas en fila, columna o bloque (FEAT-002)
        val targetRow = randomCell.row
        val targetCol = randomCell.col
        val targetBlock = randomCell.block
        for (i in 0 until 81) {
            if (i != index) {
                val cellI = updatedCells[i]
                if (cellI.value == 0 && cellI.notes.contains(targetNumber)) {
                    val r = i / 9
                    val c = i % 9
                    val b = (r / 3) * 3 + (c / 3)
                    if (r == targetRow || c == targetCol || b == targetBlock) {
                        updatedCells[i] = cellI.copy(notes = cellI.notes - targetNumber)
                    }
                }
            }
        }

        val nextBoardState = BoardState(updatedCells)
        val isDone = checkIsCompleted(nextBoardState)

        _uiState.update {
            it.copy(
                boardState = nextBoardState,
                isCompleted = isDone,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }

        if (isDone) {
            handleGameCompletion()
        }

        updateDisabledNumbers()
        updateConflicts()
        saveCurrentStateToRoom()
    }

    fun autoCompleteSingles() {
        if (_uiState.value.isCompleted) return
        val state = _uiState.value

        val cells = state.boardState.cells
        val singlesToFill = mutableMapOf<Int, Int>()

        for (cell in cells) {
            if (cell.value == 0 && !cell.isGiven) {
                val index = cell.index
                val row = cell.row
                val col = cell.col
                val block = cell.block

                val possible = (1..9).toMutableSet()
                for (i in 0 until 81) {
                    if (i != index) {
                        val other = cells[i]
                        if (other.value != 0) {
                            if (other.row == row || other.col == col || other.block == block) {
                                possible.remove(other.value)
                            }
                        }
                    }
                }
                if (possible.size == 1) {
                    singlesToFill[index] = possible.first()
                }
            }
        }

        if (singlesToFill.isEmpty()) {
            _uiState.update {
                it.copy(
                    activeHint = HintDetail("No se encontraron celdas con un único candidato lógico (Naked Singles) en este momento."),
                    showVisualHint = false
                )
            }
            return
        }

        if (state.activeSlot == GameSlot.ADVENTURE) {
            if (state.coins < 120) {
                _uiState.update {
                    it.copy(
                        activeHint = HintDetail("Monedas insuficientes. Ráfaga de Singles cuesta 120 🪙 (Tienes: ${state.coins} 🪙)"),
                        showVisualHint = false
                    )
                }
                return
            }
            _uiState.update { it.copy(coins = it.coins - 120) }
        }

        pushToUndoStack()

        val updatedCells = cells.toMutableList()
        for ((index, value) in singlesToFill) {
            updatedCells[index] = updatedCells[index].copy(value = value, notes = emptySet())
        }

        for ((index, value) in singlesToFill) {
            val cell = updatedCells[index]
            val targetRow = cell.row
            val targetCol = cell.col
            val targetBlock = cell.block
            for (i in 0 until 81) {
                if (i != index && !singlesToFill.containsKey(i)) {
                    val cellI = updatedCells[i]
                    if (cellI.value == 0 && cellI.notes.contains(value)) {
                        val r = i / 9
                        val c = i % 9
                        val b = (r / 3) * 3 + (c / 3)
                        if (r == targetRow || c == targetCol || b == targetBlock) {
                            updatedCells[i] = cellI.copy(notes = cellI.notes - value)
                        }
                    }
                }
            }
        }

        val nextBoardState = BoardState(updatedCells)
        val isDone = checkIsCompleted(nextBoardState)

        _uiState.update {
            it.copy(
                boardState = nextBoardState,
                isCompleted = isDone,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }

        if (isDone) {
            handleGameCompletion()
        }

        updateDisabledNumbers()
        updateConflicts()
        saveCurrentStateToRoom()
    }

    fun freezeTime() {
        if (_uiState.value.isCompleted) return
        val state = _uiState.value

        if (state.activeSlot == GameSlot.ADVENTURE) {
            if (state.coins < 40) {
                _uiState.update {
                    it.copy(
                        activeHint = HintDetail("Monedas insuficientes. Congelar Tiempo cuesta 40 🪙 (Tienes: ${state.coins} 🪙)"),
                        showVisualHint = false
                    )
                }
                return
            }
            _uiState.update { it.copy(coins = it.coins - 40) }
        }

        val limit = when (state.level) {
            in 1..2 -> 180L
            in 3..4 -> 300L
            in 5..6 -> 480L
            in 7..8 -> 720L
            else -> 900L
        }
        val duration = limit / 2

        _uiState.update {
            it.copy(
                isTimerFrozen = true,
                frozenTimeRemaining = duration
            )
        }
    }
}
