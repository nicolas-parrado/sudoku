package com.example.sudoku.presentation.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Test

class GameUiStateTest {

    @Test
    fun testBonusCalculationUnderLimit() {
        // Nivel 1: recompensa base = 50, bonus base = 12, límite = 180s (3 min)
        val stateUnderLimit = GameViewModel.GameUiState(
            level = 1,
            elapsedSeconds = 150 // Menor que 180s
        )
        assertEquals(12, stateUnderLimit.currentBonusReward)
        assertEquals(180L, stateUnderLimit.timeLimitForBonus)
        assertEquals(12, stateUnderLimit.baseBonusReward)
    }

    @Test
    fun testBonusCalculationOverLimitLevel1() {
        // Nivel 1: base = 50, bonus base = 12, límite = 180s (3 min)
        
        // Minuto 1 extra: de 180s a 239s (180s + 60s)
        // Bonus esperado: 12 * 0.6 = 7.2 -> 7
        val stateMin1 = GameViewModel.GameUiState(
            level = 1,
            elapsedSeconds = 200
        )
        assertEquals(7, stateMin1.currentBonusReward)

        // Minuto 2 extra: de 240s a 299s
        // Bonus esperado: 12 * 0.6 * 0.6 = 4.32 -> 4
        val stateMin2 = GameViewModel.GameUiState(
            level = 1,
            elapsedSeconds = 260
        )
        assertEquals(4, stateMin2.currentBonusReward)
    }

    @Test
    fun testBonusCalculationOverLimitLevel3() {
        // Nivel 3: base = 100, bonus base = 25, límite = 300s (5 min)
        
        // Minuto 1 extra: de 300s a 359s
        // Bonus esperado: 25 * 0.6 = 15
        val stateMin1 = GameViewModel.GameUiState(
            level = 3,
            elapsedSeconds = 320
        )
        assertEquals(15, stateMin1.currentBonusReward)

        // Minuto 2 extra: de 360s a 419s
        // Bonus esperado: 25 * 0.6 * 0.6 = 9
        val stateMin2 = GameViewModel.GameUiState(
            level = 3,
            elapsedSeconds = 380
        )
        assertEquals(9, stateMin2.currentBonusReward)

        // Minuto 3 extra: de 420s a 479s
        // Bonus esperado: 25 * 0.6^3 = 5.4 -> 5
        val stateMin3 = GameViewModel.GameUiState(
            level = 3,
            elapsedSeconds = 440
        )
        assertEquals(5, stateMin3.currentBonusReward)
    }
}
