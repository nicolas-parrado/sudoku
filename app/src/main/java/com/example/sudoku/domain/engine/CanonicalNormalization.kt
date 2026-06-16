package com.example.sudoku.domain.engine

object CanonicalNormalization {

    /**
     * Devuelve el ID de Semilla Canónica Única para un tablero de Sudoku (string de 81 caracteres).
     * Sigue las 3 fases: Simulación de transformaciones geométricas (simetrías), Normalización de dígitos y selección del mínimo lexicográfico.
     */
    fun getCanonicalSeed(puzzle: String): String {
        require(puzzle.length == 81) { "El tablero debe tener exactamente 81 caracteres." }
        
        // Convertimos el string en un arreglo de enteros 9x9 para facilitar manipulaciones
        val grid = IntArray(81) { puzzle[it].digitToInt() }
        
        val variations = mutableSetOf<String>()

        // Generamos las variaciones simétricas básicas (8 variantes geométricas)
        val symmetries = getAllSymmetries(grid)

        for (symGrid in symmetries) {
            // Para cada variante geométrica, aplicamos la normalización de dígitos
            val normalized = normalizeDigits(symGrid)
            variations.add(normalized)
        }

        // De todas las cadenas normalizadas resultantes, seleccionamos la menor lexicográficamente
        return variations.minOrNull() ?: puzzle
    }

    private fun getAllSymmetries(grid: IntArray): List<IntArray> {
        val list = mutableListOf<IntArray>()
        var current = grid
        
        // Rotaciones (0, 90, 180, 270)
        repeat(4) {
            list.add(current)
            list.add(reflectHorizontal(current))
            list.add(reflectVertical(current))
            current = rotate90(current)
        }
        
        return list
    }

    private fun rotate90(grid: IntArray): IntArray {
        val result = IntArray(81)
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                result[c * 9 + (8 - r)] = grid[r * 9 + c]
            }
        }
        return result
    }

    private fun reflectHorizontal(grid: IntArray): IntArray {
        val result = IntArray(81)
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                result[(8 - r) * 9 + c] = grid[r * 9 + c]
            }
        }
        return result
    }

    private fun reflectVertical(grid: IntArray): IntArray {
        val result = IntArray(81)
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                result[r * 9 + (8 - c)] = grid[r * 9 + c]
            }
        }
        return result
    }

    /**
     * Renombra los dígitos de forma secuencial de arriba a izquierda a abajo a derecha.
     * Ejemplo: Si el primer número no cero que encuentra es un 5, lo renombrará a 1.
     * Si el siguiente único es un 3, lo renombrará a 2, etc.
     */
    private fun normalizeDigits(grid: IntArray): String {
        val mapping = IntArray(10) { 0 } // Mapea del 1-9 original al 1-9 nuevo. 0 se queda como 0.
        var nextDigit = 1
        val result = StringBuilder(81)

        for (valOriginal in grid) {
            if (valOriginal == 0) {
                result.append('0')
            } else {
                if (mapping[valOriginal] == 0) {
                    mapping[valOriginal] = nextDigit
                    nextDigit++
                }
                result.append(mapping[valOriginal].toString())
            }
        }
        return result.toString()
    }
}
