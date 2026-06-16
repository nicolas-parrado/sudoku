package com.example.sudoku.domain.engine

import java.util.Random

object SudokuGenerator {

    private val random = Random()

    data class GeneratedPuzzle(
        val puzzle: String,
        val solution: String,
        val difficulty: Double
    )

    /**
     * Genera un tablero de Sudoku con una única solución lógica en el rango de dificultad especificado (de 1.0 a 6.0).
     */
    fun generate(minDiff: Double, maxDiff: Double): GeneratedPuzzle {
        var attempts = 0
        while (attempts < 100) {
            attempts++
            // 1. Generar una solución completa válida
            val solutionGrid = IntArray(81)
            if (!fillBoard(solutionGrid)) continue
            
            val solutionStr = solutionGrid.joinToString("") { it.toString() }
            
            // 2. Intentar remover números para alcanzar la dificultad
            val puzzleGrid = solutionGrid.clone()
            val cellIndices = (0 until 81).shuffled(random)
            
            var currentDifficulty = 1.0
            var cellsRemoved = 0

            // Queremos remover celdas hasta que la dificultad esté dentro de los límites o el solver lógico falle.
            // Para dificultades bajas (1.0-3.0) removemos menos celdas. Para intermedias (3.5-6.0) removemos más.
            val targetRemovals = when {
                maxDiff <= 3.0 -> 30 + random.nextInt(10) // 30-40 celdas vacías
                maxDiff <= 4.5 -> 40 + random.nextInt(8)  // 40-48 celdas vacías
                else -> 46 + random.nextInt(8)            // 46-54 celdas vacías
            }

            for (idx in cellIndices) {
                if (cellsRemoved >= targetRemovals) break

                val originalValue = puzzleGrid[idx]
                puzzleGrid[idx] = 0

                val puzzleStr = puzzleGrid.joinToString("") { it.toString() }
                val analysis = SudokuSolver.analyze(puzzleStr)

                // Si se puede resolver lógicamente y no supera la dificultad máxima
                if (analysis.isSolvableByLogic && analysis.difficulty <= maxDiff) {
                    currentDifficulty = analysis.difficulty
                    cellsRemoved++
                } else {
                    // Revertimos la remoción si rompe la lógica o excede la dificultad
                    puzzleGrid[idx] = originalValue
                }
            }

            val finalPuzzleStr = puzzleGrid.joinToString("") { it.toString() }
            val finalAnalysis = SudokuSolver.analyze(finalPuzzleStr)

            if (finalAnalysis.isSolvableByLogic && finalAnalysis.difficulty >= minDiff && finalAnalysis.difficulty <= maxDiff) {
                return GeneratedPuzzle(
                    puzzle = finalPuzzleStr,
                    solution = solutionStr,
                    difficulty = finalAnalysis.difficulty
                )
            }
        }
        
        // Fallback rápido si se excede el límite de intentos (retorna un tablero básico válido de dificultad 1.0)
        return getFallbackPuzzle(minDiff, maxDiff)
    }

    private fun fillBoard(grid: IntArray): Boolean {
        return solve(grid, 0)
    }

    private fun solve(grid: IntArray, index: Int): Boolean {
        if (index == 81) return true
        val row = index / 9
        val col = index % 9
        val block = (row / 3) * 3 + (col / 3)

        val numbers = (1..9).shuffled(random)
        for (num in numbers) {
            if (isValidPlacement(grid, row, col, block, num)) {
                grid[index] = num
                if (solve(grid, index + 1)) return true
                grid[index] = 0
            }
        }
        return false
    }

    private fun isValidPlacement(grid: IntArray, row: Int, col: Int, block: Int, num: Int): Boolean {
        for (i in 0 until 81) {
            val r = i / 9
            val c = i % 9
            val b = (r / 3) * 3 + (c / 3)
            if ((r == row || c == col || b == block) && grid[i] == num) {
                return false
            }
        }
        return true
    }

    private fun getFallbackPuzzle(minDiff: Double, maxDiff: Double): GeneratedPuzzle {
        // Tablero semilla precalculado simple de dificultad 1.2
        val puzzle =   "003020600900305001001806400008102900700000008006708200002609500800203009005010300"
        val solution = "483921657967345821251876493548132976729564138136798245372689514814253769695417382"
        return GeneratedPuzzle(puzzle, solution, 1.2)
    }
}
