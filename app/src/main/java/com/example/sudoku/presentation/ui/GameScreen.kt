package com.example.sudoku.presentation.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.sudoku.R
import com.example.sudoku.domain.model.GameSlot
import com.example.sudoku.domain.model.HintDetail
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
    var showPowerups by remember { mutableStateOf(false) }
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
                        val diffName = when (state.chosenDifficulty) {
                            0 -> "Fácil"
                            1 -> "Medio"
                            2 -> "Difícil"
                            3 -> "Injusto"
                            else -> "Extremo"
                        }
                        "Práctica ($diffName)"
                    }
                    Text(
                        text = modeTitle,
                        color = colors.text,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Tiempo: ${formatTime(state.elapsedSeconds)}",
                            color = if (state.isTimerFrozen) Color(0xFF29B6F6) else colors.text.copy(alpha = 0.6f),
                            fontSize = 14.sp,
                            fontWeight = if (state.isTimerFrozen) FontWeight.Bold else FontWeight.Normal
                        )
                        if (state.isTimerFrozen) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "❄️ ${state.frozenTimeRemaining}s",
                                color = Color(0xFF29B6F6),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (state.activeSlot == GameSlot.PRACTICE) {
                        val stats = state.practiceStats[state.chosenDifficulty]
                        if (stats != null && stats.timesPlayed > 0) {
                            Text(
                                text = "Récord: ${formatTime(stats.bestTimeSeconds)} (Ayudas: ${stats.recordHintsUsed} vs ${state.hintsRequestedInCurrentGame})",
                                color = colors.text.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (state.activeSlot == GameSlot.ADVENTURE) {
                        Text(
                            text = "🪙 ${state.coins}",
                            color = colors.primary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 16.dp)
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

            // 3. NUMPAD (Teclado) - Colocado inmediatamente debajo del tablero
            Numpad(
                disabledNumbers = state.disabledNumbers,
                activeNotesForSelected = getActiveNotesForSelected(state),
                colors = colors,
                onNumberInput = { viewModel.inputNumber(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4. CONTROL PANEL (Panel de Control) - Colocado abajo del Numpad
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Deshacer (Undo)
                ControlButton(
                    iconResId = R.drawable.ic_undo,
                    label = "Deshacer",
                    enabled = state.canUndo && !state.isCompleted,
                    colors = colors,
                    onClick = { viewModel.undo() }
                )

                // Rehacer (Redo)
                ControlButton(
                    iconResId = R.drawable.ic_redo,
                    label = "Rehacer",
                    enabled = state.canRedo && !state.isCompleted,
                    colors = colors,
                    onClick = { viewModel.redo() }
                )

                // Borrar
                ControlButton(
                    iconResId = R.drawable.ic_delete,
                    label = "Borrar",
                    enabled = state.selectedIndex != null && !state.isCompleted,
                    colors = colors,
                    onClick = { viewModel.clearCell() }
                )

                // Notas Toggle
                ControlButton(
                    iconResId = R.drawable.ic_notes,
                    label = "Notas",
                    enabled = !state.isCompleted,
                    isActive = state.isNoteModeActive,
                    colors = colors,
                    onClick = { viewModel.toggleNoteMode() }
                )

                // Poderes
                ControlButton(
                    iconResId = R.drawable.ic_powerups,
                    label = "Poderes",
                    enabled = !state.isCompleted,
                    colors = colors,
                    onClick = { showPowerups = true }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. HINT & COMPLETION CARD
            if (state.isAdventureCompleted) {
                AdventureVictoryCard(
                    accumulatedTime = state.accumulatedTimeSeconds,
                    accumulatedHints = state.accumulatedHintsUsed,
                    colors = colors,
                    onReset = { viewModel.resetAdventure() },
                    onGoToMenu = { viewModel.openMenu() }
                )
            } else if (state.isCompleted) {
                CompletionCard(
                    slot = state.activeSlot,
                    level = state.level,
                    floor = state.floor,
                    colors = colors,
                    lastCoinsEarned = state.lastCoinsEarned,
                    lastTimeBonusEarned = state.lastTimeBonusEarned,
                    onNext = {
                        if (state.activeSlot == GameSlot.ADVENTURE) {
                            viewModel.advanceAdventureFloor()
                        } else {
                            viewModel.startNewGame(GameSlot.PRACTICE)
                        }
                    }
                )
            } else {
                HintSection(
                    activeHint = state.activeHint,
                    showVisualHint = state.showVisualHint,
                    colors = colors,
                    activeSlot = state.activeSlot,
                    nextHintCost = state.nextHintCost,
                    onRequestHint = { viewModel.requestHint() },
                    onToggleVisualHint = { viewModel.toggleVisualHint() },
                    onClearHint = { viewModel.clearActiveHint() }
                )
            }
        }

        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.background.copy(alpha = 0.85f))
                    .clickable(enabled = true, onClickLabel = null, onClick = {}),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = colors.primary,
                        strokeWidth = 4.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Calculando patrones lógicos...",
                        color = colors.text,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
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

    // Poderes modal
    if (showPowerups) {
        PowerUpsDialog(
            state = state,
            colors = colors,
            onUsePowerUp = { powerId ->
                when (powerId) {
                    "auto_notes" -> viewModel.fillAllNotes()
                    "hawkeye" -> viewModel.revealSelectedCell()
                    "broom" -> viewModel.cleanRedundantNotes()
                    "bomb" -> viewModel.triggerNumberBomb()
                    "singles" -> viewModel.autoCompleteSingles()
                    "freeze" -> viewModel.freezeTime()
                }
            },
            onDismiss = { showPowerups = false }
        )
    }
}

@Composable
fun ControlButton(
    iconResId: Int,
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
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = label,
                tint = if (isActive) colors.background else if (enabled) colors.text else colors.text.copy(alpha = 0.25f),
                modifier = Modifier.size(20.dp)
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
    activeSlot: GameSlot,
    nextHintCost: Int,
    onRequestHint: () -> Unit,
    onToggleVisualHint: () -> Unit,
    onClearHint: () -> Unit
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
                val buttonText = if (activeSlot == GameSlot.ADVENTURE) {
                    "Pedir Pista ($nextHintCost 🪙)"
                } else {
                    "Pedir Pista (Gratis)"
                }
                Button(
                    onClick = onRequestHint,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.background),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = buttonText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pista Encontrada:",
                    color = colors.primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "✕",
                    color = colors.text.copy(alpha = 0.5f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { onClearHint() }
                        .padding(4.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = activeHint.explanation,
                color = colors.text,
                fontSize = 14.sp,
                lineHeight = 18.sp
            )
            
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (activeHint.pivotCells.isNotEmpty() || activeHint.eliminationCells.isNotEmpty()) {
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
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }
                
                Button(
                    onClick = onRequestHint,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.text.copy(alpha = 0.08f), contentColor = colors.text),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = "Siguiente Pista", fontSize = 12.sp)
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
    lastCoinsEarned: Int = 0,
    lastTimeBonusEarned: Boolean = false,
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
        if (slot == GameSlot.ADVENTURE && lastCoinsEarned > 0) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "🪙 +$lastCoinsEarned monedas ganadas",
                    color = colors.primary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                if (lastTimeBonusEarned) {
                    Text(
                        text = "⚡ ¡Bono de velocidad +25% conseguido!",
                        color = colors.primary.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
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

@Composable
fun AdventureVictoryCard(
    accumulatedTime: Long,
    accumulatedHints: Int,
    colors: SudokuThemeColors,
    onReset: () -> Unit,
    onGoToMenu: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.primary.copy(alpha = 0.15f))
            .border(2.dp, colors.primary, RoundedCornerShape(16.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "🎉 ¡TORRE CONQUISTADA! 🎉",
            color = colors.primary,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "¡Felicidades, Comandante! Has superado los 100 pisos de la torre lógica junto a tu tropa. Tu paciencia, intelecto y constancia han dado frutos.",
            color = colors.text,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(colors.surface.copy(alpha = 0.5f))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Tiempo Total:", color = colors.text.copy(alpha = 0.6f), fontSize = 13.sp)
                Text(text = formatAdventureTimeHelper(accumulatedTime), color = colors.text, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Ayudas Utilizadas:", color = colors.text.copy(alpha = 0.6f), fontSize = 13.sp)
                Text(text = "$accumulatedHints", color = colors.text, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onReset,
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.background),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Nueva Aventura", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Button(
                onClick = onGoToMenu,
                colors = ButtonDefaults.buttonColors(containerColor = colors.surface, contentColor = colors.text),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Volver al Menú", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

private fun formatAdventureTimeHelper(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hours > 0) {
        String.format("%dh %02dm %02ds", hours, minutes, secs)
    } else {
        String.format("%02d:%02d", minutes, secs)
    }
}

private fun getActiveNotesForSelected(state: GameViewModel.GameUiState): Set<Int> {
    val index = state.selectedIndex ?: return emptySet()
    return state.boardState.getCell(index).notes
}

@Composable
fun PowerUpsDialog(
    state: GameViewModel.GameUiState,
    colors: SudokuThemeColors,
    onUsePowerUp: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = colors.background,
            border = BorderStroke(1.5.dp, colors.primary.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header del Diálogo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚡ Poderes de la Tropa",
                        color = colors.primary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "✕",
                        color = colors.text.copy(alpha = 0.5f),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { onDismiss() }
                            .padding(4.dp)
                    )
                }

                if (state.activeSlot == GameSlot.ADVENTURE) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.surface)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tus Monedas: 🪙 ${state.coins}",
                            color = colors.primary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.surface)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Modo Práctica - ¡Poderes Gratuitos!",
                            color = colors.text.copy(alpha = 0.8f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Lista de Poderes
                val limit = when (state.level) {
                    in 1..2 -> 180L
                    in 3..4 -> 300L
                    in 5..6 -> 480L
                    in 7..8 -> 720L
                    else -> 900L
                }
                val freezeSecs = limit / 2

                val powers = listOf(
                    PowerUpItem(
                        id = "auto_notes",
                        name = "Lápiz Mágico",
                        description = "Rellena todas las notas lógicas válidas en las casillas vacías.",
                        cost = 30,
                        iconRes = R.drawable.ic_auto_notes,
                        color = Color(0xFFFFB74D) // Naranja
                    ),
                    PowerUpItem(
                        id = "hawkeye",
                        name = "Ojo de Halcón",
                        description = "Revela el número correcto de la casilla seleccionada.",
                        cost = 100,
                        iconRes = R.drawable.ic_hawkeye,
                        color = Color(0xFF4FC3F7) // Celeste
                    ),
                    PowerUpItem(
                        id = "broom",
                        name = "Escoba Lógica",
                        description = "Limpia las notas manuales redundantes o que generen conflicto.",
                        cost = 30,
                        iconRes = R.drawable.ic_broom,
                        color = Color(0xFF81C784) // Verde
                    ),
                    PowerUpItem(
                        id = "bomb",
                        name = "Bomba de Números",
                        description = "Revela una casilla vacía aleatoria que lleve el número seleccionado (${state.selectedNumpadNumber}).",
                        cost = 50,
                        iconRes = R.drawable.ic_bomb,
                        color = Color(0xFFE57373) // Rojo/Rosa
                    ),
                    PowerUpItem(
                        id = "singles",
                        name = "Ráfaga de Singles",
                        description = "Resuelve todas las celdas que tengan un único candidato lógico posible.",
                        cost = 120,
                        iconRes = R.drawable.ic_singles,
                        color = Color(0xFFBA68C8) // Púrpura
                    ),
                    PowerUpItem(
                        id = "freeze",
                        name = "Congelar Tiempo",
                        description = "Congela el reloj por $freezeSecs segundos de cara al bono de velocidad.",
                        cost = 40,
                        iconRes = R.drawable.ic_freeze,
                        color = Color(0xFF4DD0E1), // Cyan/Hielo
                        enabledInPractice = false
                    )
                )

                powers.forEach { power ->
                    val isAvailable = state.activeSlot == GameSlot.PRACTICE || state.coins >= power.cost
                    val isPowerEnabled = power.enabledInPractice || state.activeSlot == GameSlot.ADVENTURE
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.gridBorder.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Icono del Poder
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(power.color.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = power.iconRes),
                                contentDescription = power.name,
                                tint = power.color,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Textos descriptivos
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = power.name,
                                color = colors.text,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = power.description,
                                color = colors.text.copy(alpha = 0.6f),
                                fontSize = 11.sp,
                                lineHeight = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Botón de compra / uso
                        Button(
                            onClick = {
                                onUsePowerUp(power.id)
                                onDismiss()
                            },
                            enabled = isAvailable && isPowerEnabled,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = power.color,
                                contentColor = colors.background,
                                disabledContainerColor = colors.gridBorder.copy(alpha = 0.1f),
                                disabledContentColor = colors.text.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            val btnText = if (state.activeSlot == GameSlot.PRACTICE) {
                                if (!power.enabledInPractice) "No útil" else "Usar"
                            } else {
                                "${power.cost} 🪙"
                            }
                            Text(
                                text = btnText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

data class PowerUpItem(
    val id: String,
    val name: String,
    val description: String,
    val cost: Int,
    val iconRes: Int,
    val color: Color,
    val enabledInPractice: Boolean = true
)
