package com.example.sudoku.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class CanonicalNormalizationTest {

    @Test
    fun testCanonicalNormalizationIdenticalForSymmetries() {
        // Tablero Sudoku de prueba
        val original = "003020600900305001001806400008102900700000008006708200002609500800203009005010300"
        
        // Generamos la versión rotada 90 grados
        val rotated = rotate90String(original)
        
        val canonicalOriginal = CanonicalNormalization.getCanonicalSeed(original)
        val canonicalRotated = CanonicalNormalization.getCanonicalSeed(rotated)
        
        // Deben ser equivalentes (producir la misma semilla canónica lexicográficamente mínima)
        assertEquals("Las semillas canónicas de tableros simétricos deben ser idénticas", canonicalOriginal, canonicalRotated)
    }

    @Test
    fun testCanonicalNormalizationDifferentForDistinctPuzzles() {
        val puzzle1 = "003020600900305001001806400008102900700000008006708200002609500800203009005010300"
        val puzzle2 = "000000000008000000000203004000450060001000700030082000200507000000000300000000000"
        
        val canonical1 = CanonicalNormalization.getCanonicalSeed(puzzle1)
        val canonical2 = CanonicalNormalization.getCanonicalSeed(puzzle2)
        
        assertNotEquals("Puzzles con diferentes lógicas no deben compartir ID canónico", canonical1, canonical2)
    }

    private fun rotate90String(puzzle: String): String {
        val result = CharArray(81)
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                result[c * 9 + (8 - r)] = puzzle[r * 9 + c]
            }
        }
        return String(result)
    }
}
