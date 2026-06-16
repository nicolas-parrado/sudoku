package com.example.sudoku.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sudoku.data.repository.SudokuRepository
import com.example.sudoku.domain.engine.DifficultyCurve
import com.example.sudoku.domain.engine.SudokuGenerator
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
        val canRedo: Boolean = false
    )

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
            // Intentar cargar partidas previas
            loadSavedGame(GameSlot.ADVENTURE)
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                if (!_uiState.value.isMenuOpen && !_uiState.value.isCompleted) {
                    _uiState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
                    // Guardar de forma periódica o asincrónica
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
                        showVisualHint = false
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
            _uiState.update { it.copy(isMenuOpen = false, activeSlot = slot) }
            
            val difficultyDecimal: Double
            val minDiff: Double
            val maxDiff: Double

            if (slot == GameSlot.ADVENTURE) {
                val lvl = _uiState.value.level
                val flr = _uiState.value.floor
                difficultyDecimal = DifficultyCurve.getDifficultyForFloor(lvl, flr)
                minDiff = maxOf(1.0, difficultyDecimal - 0.5)
                maxDiff = difficultyDecimal
            } else {
                // Práctica: dificultad entera (1 a 10)
                val diffInt = _uiState.value.chosenDifficulty
                minDiff = if (diffInt == 1) 1.0 else (diffInt - 1).toDouble()
                maxDiff = diffInt.toDouble()
                difficultyDecimal = diffInt.toDouble()
            }

            // Generación o recuperación
            val puzzle: String
            val solution: String

            if (maxDiff >= 7.0) {
                // Dificultades altas: Pool de Room
                val seed = repository.getRandomHardSeed(minDiff, maxDiff)
                if (seed != null) {
                    puzzle = seed.puzzleString
                    solution = seed.solutionString
                } else {
                    // Fallback rápido
                    puzzle =   "003020600900305001001806400008102900700000008006708200002609500800203009005010300"
                    solution = "483921657967345821251876493548132976729564138136798245372689514814253769695417382"
                }
            } else {
                // Dificultades bajas/medias: Generador en tiempo real
                val generated = SudokuGenerator.generate(minDiff, maxDiff)
                puzzle = generated.puzzle
                solution = generated.solution
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
                    showVisualHint = false
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
                themeName = state.activeTheme.name.lowercase()
            )
        }
    }

    // --- ACCIONES DEL JUGADOR ---

    fun selectCell(index: Int) {
        if (_uiState.value.isCompleted) return
        _uiState.update { it.copy(selectedIndex = index) }
    }

    fun inputNumber(number: Int) {
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
            stopTimer()
            // Si es modo aventura y completó el piso, podemos preparar la UI para avanzar
            // Borramos historial de undo/redo para el slot al completarse según los requerimientos
            undoStack.clear()
            redoStack.clear()
            _uiState.update { it.copy(canUndo = false, canRedo = false) }
        }

        updateDisabledNumbers()
        updateConflicts()
        saveCurrentStateToRoom()
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
        if (_uiState.value.isCompleted) return
        
        // Pistas exclusivas para Modo Práctica (o libres en modo práctica)
        // Ejecutamos el solver analítico con el tablero parcial actual
        val currentPuzzleString = getGivenPlusUserPuzzleString(_uiState.value.boardState)
        val analysis = SudokuSolver.analyze(currentPuzzleString)

        if (analysis.nextHint != null) {
            _uiState.update {
                it.copy(
                    activeHint = analysis.nextHint,
                    showVisualHint = false
                )
            }
        } else {
            // El resolvedor no encuentra más pistas con las técnicas implementadas
            // O el tablero ya tiene errores que impiden la lógica estándar
            if (_uiState.value.conflictingCells.isNotEmpty()) {
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

        if (currentFloor < 10) {
            _uiState.update { it.copy(floor = currentFloor + 1) }
        } else {
            // Avanza de nivel
            _uiState.update { it.copy(level = currentLevel + 1, floor = 1) }
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
    }

    private fun getGivenPuzzleString(board: BoardState): String {
        return board.cells.joinToString("") { if (it.isGiven) it.value.toString() else "0" }
    }

    private fun getGivenPlusUserPuzzleString(board: BoardState): String {
        return board.cells.joinToString("") { if (it.value != 0) it.value.toString() else "0" }
    }
}
