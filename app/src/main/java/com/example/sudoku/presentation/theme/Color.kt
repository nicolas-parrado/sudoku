package com.example.sudoku.presentation.theme

import androidx.compose.ui.graphics.Color

// Paletas HSL / Hexadecimales personalizadas
object SudokuColors {
    // Slate / Midnight Theme
    val SlateBackground = Color(0xFF0B0F19)
    val SlateSurface = Color(0xFF1E293B)
    val SlatePrimary = Color(0xFF06B6D4) // Cian eléctrico
    val SlateSelected = Color(0xFF1E3A8A)
    val SlateRelated = Color(0xFF1E293B) // Más sutil
    val SlateText = Color(0xFFF3F4F6)
    val SlateNote = Color(0xFF9CA3AF)
    val SlateError = Color(0xFFEF4444)
    val SlateGridBorder = Color(0xFF475569)

    // Nordic Frost Theme
    val NordicBackground = Color(0xFF1F232A)
    val NordicSurface = Color(0xFF2E3440)
    val NordicPrimary = Color(0xFF8FBCBB) // Menta polar
    val NordicSelected = Color(0xFF434C5E)
    val NordicRelated = Color(0xFF3B4252)
    val NordicText = Color(0xFFECEFF4)
    val NordicNote = Color(0xFFD8DEE9)
    val NordicError = Color(0xFFBF616A)
    val NordicGridBorder = Color(0xFF4C566A)

    // Cyberpunk / Obsidian Theme
    val CyberBackground = Color(0xFF000000) // Negro AMOLED
    val CyberSurface = Color(0xFF121212)
    val CyberPrimary = Color(0xFFD946EF) // Fucsia neón
    val CyberSelected = Color(0xFF3B0764)
    val CyberRelated = Color(0xFF1E1B4B)
    val CyberText = Color(0xFF00F5FF) // Cian neón
    val CyberNote = Color(0xFF8B5CF6)
    val CyberError = Color(0xFFFF0055)
    val CyberGridBorder = Color(0xFF374151)
    
    // Colores de pistas (Capa 2)
    val HintPivot = Color(0xFF3B82F6) // Azul para pivotes
    val HintElimination = Color(0xFFF97316) // Naranja/Rojo para eliminaciones
}
