package com.example.sudoku.domain.engine

import kotlin.math.roundToInt

object DifficultyCurve {

    data class DifficultyRange(
        val minDifficulty: Double,
        val maxDifficulty: Double
    )

    /**
     * Obtiene el rango de dificultad mínimo y máximo para un Nivel L del modo aventura.
     * Implementa una curva logarítmica suave con definición exacta en los niveles iniciales
     * y estabilizando asintóticamente a partir del nivel 10 entre 8.5 y 10.0.
     */
    fun getDifficultyRangeForLevel(level: Int): DifficultyRange {
        return when (level) {
            1 -> DifficultyRange(1.0, 3.0)
            2 -> DifficultyRange(2.0, 4.5)
            3 -> DifficultyRange(3.0, 5.5)
            4 -> DifficultyRange(4.0, 6.5)
            5 -> DifficultyRange(5.0, 7.5)
            6 -> DifficultyRange(6.0, 8.2)
            7 -> DifficultyRange(6.8, 8.8)
            8 -> DifficultyRange(7.5, 9.2)
            9 -> DifficultyRange(8.0, 9.6)
            else -> DifficultyRange(8.5, 10.0) // Nivel 10+
        }
    }

    /**
     * Calcula la dificultad específica de un Piso F (1 al 10) en un Nivel L.
     * El piso 10 representa el "Boss Floor" con la dificultad máxima del rango del nivel.
     */
    fun getDifficultyForFloor(level: Int, floor: Int): Double {
        val range = getDifficultyRangeForLevel(level)
        val clampedFloor = floor.coerceIn(1, 10)
        
        // Interpolación lineal entre piso 1 y piso 10
        val diff = range.minDifficulty + (clampedFloor - 1) / 9.0 * (range.maxDifficulty - range.minDifficulty)
        
        // Redondear a 1 decimal
        return (diff * 10.0).roundToInt() / 10.0
    }
}
