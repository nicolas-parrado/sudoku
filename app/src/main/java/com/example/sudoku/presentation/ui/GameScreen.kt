package com.example.sudoku.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sudoku.domain.model.GameSlot
import com.example.sudoku.presentation.theme.SudokuThemeColors
import com.example.sudoku.presentation.viewmodel.GameViewModel

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    colors: SudokuThemeColors,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var showSettings by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. HEADER (Encabezado)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    val modeTitle = if (state.activeSlot == GameSlot.ADVENTURE) {
                        "Nivel ${state.level} - Piso ${state.floor}"
                    } else {
                        "Práctica (Dificultad ${state.chosenDifficulty})"
                    }
                    Text(
                        text = modeTitle,
                        color = colors.text,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tiempo: ${formatTime(state.elapsedSeconds)}",
                        color = colors.text.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                }

                // Botón Configuración
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colors.surface)
                        .clickable { showSettings = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "⚙", color = colors.text, fontSize = 20.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. GRID BOARD (Tablero)
            GridBoard(
                boardState = state.boardState,
                selectedIndex = state.selectedIndex,
                conflictingCells = state.conflictingCells,
                activeHint = state.activeHint,
                showVisualHint = state.showVisualHint,
                colors = colors,
                onCellSelected = { viewModel.selectCell(it) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. CONTROL PANEL (Panel de Control)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Deshacer (Undo)
                ControlButton(
                    text = "↩",
                    label = "Deshacer",
                    enabled = state.canUndo && !state.isCompleted,
                    colors = colors,
                    onClick = { viewModel.undo() }
                )

                // Rehacer (Redo)
                ControlButton(
                    text = "↪",
                    label = "Rehacer",
                    enabled = state.canRedo && !state.isCompleted,
                    colors = colors,
                    onClick = { viewModel.redo() }
                )

                // Borrar
                ControlButton(
                    text = "⌫",
                    label = "Borrar",
                    enabled = state.selectedIndex != null && !state.isCompleted,
                    colors = colors,
                    onClick = { viewModel.clearCell() }
                )

                // Notas Toggle
                ControlButton(
                    text = "✎",
                    label = "Notas",
                    enabled = !state.isCompleted,
                    isActive = state.isNoteModeActive,
                    colors = colors,
                    onClick = { viewModel.toggleNoteMode() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. NUMPAD (Teclado)
            Numpad(
                disabledNumbers = state.disabledNumbers,
                activeNotesForSelected = getActiveNotesForSelected(state),
                colors = colors,
                onNumberInput = { viewModel.inputNumber(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 5. HINT & COMPLETION CARD
            if (state.isCompleted) {
                CompletionCard(
                    slot = state.activeSlot,
                    level = state.level,
                    floor = state.floor,
                    colors = colors,
                    onNext = {
                        if (state.activeSlot == GameSlot.ADVENTURE) {
                            viewModel.advanceAdventureFloor()
                        } else {
                            viewModel.startNewGame(GameSlot.PRACTICE)
                        }
                    }
                )
            } else if (state.activeSlot == GameSlot.PRACTICE) {
                HintSection(
                    activeHint = state.activeHint,
                    showVisualHint = state.showVisualHint,
                    colors = colors,
                    onRequestHint = { viewModel.requestHint() },
                    onToggleVisualHint = { viewModel.toggleVisualHint() }
                )
            }
        }
    }

    // Configuración modal
    if (showSettings) {
        SettingsDialog(
            colors = colors,
            activeTheme = state.activeTheme,
            onThemeChanged = { viewModel.changeTheme(it) },
            onRestart = { viewModel.restartCurrentSudoku() },
            onGoToMenu = { viewModel.openMenu() },
            onDismiss = { showSettings = false }
        )
    }
}

@Composable
fun ControlButton(
    text: String,
    label: String,
    enabled: Boolean,
    colors: SudokuThemeColors,
    onClick: () -> Unit,
    isActive: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.clickable(enabled = enabled, onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isActive -> colors.primary
                        enabled -> colors.surface
                        else -> colors.surface.copy(alpha = 0.3f)
                    }
                )
                .border(
                    1.dp,
                    if (isActive) colors.primary else colors.gridBorder.copy(alpha = 0.2f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = if (isActive) colors.background else if (enabled) colors.text else colors.text.copy(alpha = 0.25f),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = if (enabled) colors.text.copy(alpha = 0.7f) else colors.text.copy(alpha = 0.3f),
            fontSize = 11.sp
        )
    }
}

@Composable
fun Numpad(
    disabledNumbers: Set<Int>,
    activeNotesForSelected: Set<Int>,
    colors: SudokuThemeColors,
    onNumberInput: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (num in 1..9) {
            val isDisabled = disabledNumbers.contains(num)
            val isNoteActive = activeNotesForSelected.contains(num)
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        when {
                            isDisabled -> colors.surface.copy(alpha = 0.1f) // Ocultar o atenuar
                            isNoteActive -> colors.primary.copy(alpha = 0.15f) // Resaltar nota
                            else -> colors.surface
                        }
                    )
                    .border(
                        1.dp,
                        when {
                            isDisabled -> Color.Transparent
                            isNoteActive -> colors.primary
                            else -> colors.gridBorder.copy(alpha = 0.3f)
                        },
                        RoundedCornerShape(8.dp)
                    )
                    .clickable(enabled = !isDisabled) { onNumberInput(num) },
                contentAlignment = Alignment.Center
            ) {
                if (!isDisabled) {
                    Text(
                        text = num.toString(),
                        color = if (isNoteActive) colors.primary else colors.text,
                        fontSize = 22.sp,
                        fontWeight = if (isNoteActive) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun HintSection(
    activeHint: HintDetail?,
    showVisualHint: Boolean,
    colors: SudokuThemeColors,
    onRequestHint: () -> Unit,
    onToggleVisualHint: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surface)
            .border(1.dp, colors.gridBorder.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        if (activeHint == null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pistas Lógicas",
                    color = colors.text.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Button(
                    onClick = onRequestHint,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.background),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = "Pedir Pista", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Text(
                text = "Pista Encontrada:",
                color = colors.primary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = activeHint.explanation,
                color = colors.text,
                fontSize = 14.sp,
                lineHeight = 18.sp
            )
            
            if (activeHint.pivotCells.isNotEmpty() || activeHint.eliminationCells.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onToggleVisualHint,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (showVisualHint) colors.primary else colors.primary.copy(alpha = 0.15f),
                            contentColor = if (showVisualHint) colors.background else colors.primary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (showVisualHint) "Ocultar Ayuda Visual" else "Mostrar Ayuda Visual",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Button(
                        onClick = onRequestHint,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.text.copy(alpha = 0.08f), contentColor = colors.text),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "Siguiente", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun CompletionCard(
    slot: GameSlot,
    level: Int,
    floor: Int,
    colors: SudokuThemeColors,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.primary.copy(alpha = 0.1f))
            .border(1.5.dp, colors.primary, RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "¡FELICITACIONES!",
            color = colors.primary,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        val details = if (slot == GameSlot.ADVENTURE) {
            "Has completado el Piso $floor del Nivel $level."
        } else {
            "Has completado la partida de Práctica con éxito."
        }
        Text(
            text = details,
            color = colors.text,
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )
        Button(
            onClick = onNext,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = colors.background
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val btnText = if (slot == GameSlot.ADVENTURE) "Avanzar al siguiente piso" else "Generar nuevo Sudoku"
            Text(text = btnText, fontWeight = FontWeight.Bold)
        }
    }
}

private fun formatTime(seconds: Long): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%02d:%02d", m, s)
}

private fun getActiveNotesForSelected(state: GameViewModel.GameUiState): Set<Int> {
    val index = state.selectedIndex ?: return emptySet()
    return state.boardState.getCell(index).notes
}
