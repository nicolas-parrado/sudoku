package com.example.sudoku.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class SudokuThemeColors(
    val background: Color,
    val surface: Color,
    val primary: Color,
    val selectedCell: Color,
    val relatedCell: Color,
    val text: Color,
    val noteText: Color,
    val error: Color,
    val gridBorder: Color,
    val pivot: Color = SudokuColors.HintPivot,
    val elimination: Color = SudokuColors.HintElimination
)

private val LocalSudokuColors = staticCompositionLocalOf<SudokuThemeColors> {
    error("No SudokuThemeColors provided")
}

object SudokuTheme {
    val colors: SudokuThemeColors
        @Composable
        @ReadOnlyComposable
        get() = LocalSudokuColors.current

    enum class Type {
        SLATE,
        NORDIC,
        CYBERPUNK;

        companion object {
            fun fromName(name: String): Type {
                return entries.find { it.name.lowercase() == name.lowercase() } ?: SLATE
            }
        }
    }
}

private val SlatePalette = SudokuThemeColors(
    background = SudokuColors.SlateBackground,
    surface = SudokuColors.SlateSurface,
    primary = SudokuColors.SlatePrimary,
    selectedCell = SudokuColors.SlateSelected,
    relatedCell = SudokuColors.SlateRelated,
    text = SudokuColors.SlateText,
    noteText = SudokuColors.SlateNote,
    error = SudokuColors.SlateError,
    gridBorder = SudokuColors.SlateGridBorder
)

private val NordicPalette = SudokuThemeColors(
    background = SudokuColors.NordicBackground,
    surface = SudokuColors.NordicSurface,
    primary = SudokuColors.NordicPrimary,
    selectedCell = SudokuColors.NordicSelected,
    relatedCell = SudokuColors.NordicRelated,
    text = SudokuColors.NordicText,
    noteText = SudokuColors.NordicNote,
    error = SudokuColors.NordicError,
    gridBorder = SudokuColors.NordicGridBorder
)

private val CyberpunkPalette = SudokuThemeColors(
    background = SudokuColors.CyberBackground,
    surface = SudokuColors.CyberSurface,
    primary = SudokuColors.CyberPrimary,
    selectedCell = SudokuColors.CyberSelected,
    relatedCell = SudokuColors.CyberRelated,
    text = SudokuColors.CyberText,
    noteText = SudokuColors.CyberNote,
    error = SudokuColors.CyberError,
    gridBorder = SudokuColors.CyberGridBorder
)

@Composable
fun SudokuTheme(
    themeType: SudokuTheme.Type = SudokuTheme.Type.SLATE,
    content: @Composable () -> Unit
) {
    val colors = when (themeType) {
        SudokuTheme.Type.SLATE -> SlatePalette
        SudokuTheme.Type.NORDIC -> NordicPalette
        SudokuTheme.Type.CYBERPUNK -> CyberpunkPalette
    }

    CompositionLocalProvider(
        LocalSudokuColors provides colors,
        content = content
    )
}
