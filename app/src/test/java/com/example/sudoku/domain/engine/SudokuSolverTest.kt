package com.example.sudoku.domain.engine

import com.example.sudoku.domain.model.BoardState
import com.example.sudoku.domain.model.SudokuCell
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SudokuSolverTest {

    @Test
    fun testLogicalSolverSolvesValidPuzzle() {
        // Un tablero de dificultad 1.2
        val puzzle = "003020600900305001001806400008102900700000008006708200002609500800203009005010300"
        val analysis = SudokuSolver.analyze(puzzle)
        
        assertTrue("El solucionador lógico debería resolver el puzzle sin backtracking", analysis.isSolvableByLogic)
        assertNotNull("La solución devuelta no debe ser nula", analysis.solvedBoard)
        assertEquals(81, analysis.solvedBoard?.length)
        assertTrue("La dificultad estimada debe ser consistente", analysis.difficulty >= 1.0)
    }

    @Test
    fun testVisualConflictDetection() {
        // Crear un tablero con conflicto directo (dos 9s en la misma fila)
        val cells = MutableList(81) { index ->
            SudokuCell(index = index, value = 0, isGiven = false)
        }
        
        // Fila 0, columna 0 y fila 0, columna 1 tienen valor 9
        cells[0] = SudokuCell(index = 0, value = 9, isGiven = true)
        cells[1] = SudokuCell(index = 1, value = 9, isGiven = false)
        
        val board = BoardState(cells)
        val conflicts = SudokuSolver.getConflictingCells(board)
        
        assertTrue("Se debe detectar conflicto en el índice 0", conflicts.contains(0))
        assertTrue("Se debe detectar conflicto en el índice 1", conflicts.contains(1))
        assertEquals(2, conflicts.size)
    }
}
