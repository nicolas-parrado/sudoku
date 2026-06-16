package com.example.sudoku.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sudoku.domain.model.BoardState
import com.example.sudoku.domain.model.HintDetail
import com.example.sudoku.domain.model.SudokuCell
import com.example.sudoku.presentation.theme.SudokuThemeColors

@Composable
fun GridBoard(
    boardState: BoardState,
    selectedIndex: Int?,
    conflictingCells: Set<Int>,
    activeHint: HintDetail?,
    showVisualHint: Boolean,
    colors: SudokuThemeColors,
    onCellSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedCell = selectedIndex?.let { boardState.getCell(it) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .border(2.dp, colors.gridBorder, RoundedCornerShape(8.dp))
            .padding(1.dp)
    ) {
        val width = constraints.maxWidth.toFloat()
        val cellSize = width / 9f

        // Dibuja las líneas de fondo (grilla de Sudoku)
        Canvas(modifier = Modifier.fillMaxSize()) {
            for (i in 1 until 9) {
                val strokeWidth = if (i % 3 == 0) 3.dp.toPx() else 1.dp.toPx()
                val color = if (i % 3 == 0) colors.gridBorder else colors.gridBorder.copy(alpha = 0.4f)
                
                // Líneas verticales
                drawLine(
                    color = color,
                    start = Offset(x = i * cellSize, y = 0f),
                    end = Offset(x = i * cellSize, y = size.height),
                    strokeWidth = strokeWidth
                )

                // Líneas horizontales
                drawLine(
                    color = color,
                    start = Offset(x = 0f, y = i * cellSize),
                    end = Offset(x = size.width, y = i * cellSize),
                    strokeWidth = strokeWidth
                )
            }
        }

        // Dibuja las celdas
        Column(modifier = Modifier.fillMaxSize()) {
            for (r in 0 until 9) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    for (c in 0 until 9) {
                        val index = r * 9 + c
                        val cell = boardState.getCell(index)

                        // Lógica de colores de fondo de celda
                        val cellBg = when {
                            // 1. Conflicto / Error (Rojo)
                            conflictingCells.contains(index) -> colors.error.copy(alpha = 0.25f)
                            
                            // 2. Visual Hints activos (Pistas de Capa 2)
                            showVisualHint && activeHint?.pivotCells?.contains(index) == true -> colors.pivot.copy(alpha = 0.35f)
                            showVisualHint && activeHint?.eliminationCells?.contains(index) == true -> colors.elimination.copy(alpha = 0.35f)

                            // 3. Celda seleccionada
                            selectedIndex == index -> colors.selectedCell.copy(alpha = 0.7f)

                            // 4. Celdas relacionadas (misma fila, columna o bloque 3x3)
                            selectedCell != null && (cell.row == selectedCell.row || cell.col == selectedCell.col || cell.block == selectedCell.block) -> {
                                colors.relatedCell.copy(alpha = 0.25f)
                            }
                            
                            // 5. Sin resaltar
                            else -> Color.Transparent
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .border(0.25.dp, colors.gridBorder.copy(alpha = 0.15f))
                                .clickable { onCellSelected(index) }
                                .padding(1.dp)
                                .then(
                                    if (cellBg != Color.Transparent) Modifier.border(
                                        if (selectedIndex == index) 2.dp else 0.dp,
                                        if (selectedIndex == index) colors.primary else Color.Transparent
                                    ) else Modifier
                                )
                                .then(Modifier.border(0.dp, Color.Transparent)) // Evita problemas
                                .then(Modifier.border(0.dp, Color.Transparent))
                        ) {
                            // Dibujar fondo de celda
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawRect(color = cellBg)
                            }

                            // Contenido de la celda (Número o Notas)
                            if (cell.value != 0) {
                                val fontColor = when {
                                    cell.isGiven -> colors.text.copy(alpha = 0.95f) // Dado
                                    conflictingCells.contains(index) -> colors.error // Error
                                    else -> colors.primary // Usuario
                                }
                                Text(
                                    text = cell.value.toString(),
                                    color = fontColor,
                                    fontSize = 24.sp,
                                    fontWeight = if (cell.isGiven) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            } else if (cell.notes.isNotEmpty()) {
                                // Grilla de notas (3x3 interna)
                                NoteGrid(notes = cell.notes, colors = colors)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NoteGrid(notes: Set<Int>, colors: SudokuThemeColors) {
    Column(modifier = Modifier.fillMaxSize().padding(2.dp)) {
        for (row in 0 until 3) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                for (col in 0 until 3) {
                    val noteNum = row * 3 + col + 1
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (notes.contains(noteNum)) {
                            Text(
                                text = noteNum.toString(),
                                color = colors.noteText.copy(alpha = 0.75f),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Light,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
