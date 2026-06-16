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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import com.example.sudoku.presentation.theme.SudokuTheme
import com.example.sudoku.presentation.theme.SudokuThemeColors
import com.example.sudoku.presentation.viewmodel.GameViewModel
import kotlin.math.roundToInt

@Composable
fun MainScreen(
    viewModel: GameViewModel,
    colors: SudokuThemeColors,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(GameSlot.ADVENTURE) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. TÍTULO Y LOGO
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 40.dp)
            ) {
                Text(
                    text = "SUDOKU",
                    color = colors.primary,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 4.sp
                )
                Text(
                    text = "100% OFFLINE & DEDUCTIVO",
                    color = colors.text.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            // 2. TABS DE SELECCIÓN DE MODO
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surface)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedTab == GameSlot.ADVENTURE) colors.primary else colors.surface)
                            .clickable { selectedTab = GameSlot.ADVENTURE }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Modo Aventura",
                            color = if (selectedTab == GameSlot.ADVENTURE) colors.background else colors.text,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedTab == GameSlot.PRACTICE) colors.primary else colors.surface)
                            .clickable { selectedTab = GameSlot.PRACTICE }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Entrenamiento",
                            color = if (selectedTab == GameSlot.PRACTICE) colors.background else colors.text,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // CONTENIDO DE TAB SELECCIONADA
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.gridBorder.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        .padding(20.dp)
                ) {
                    if (selectedTab == GameSlot.ADVENTURE) {
                        AdventureSettings(state = state, colors = colors, viewModel = viewModel)
                    } else {
                        PracticeSettings(state = state, colors = colors, viewModel = viewModel)
                    }
                }
            }

            // 3. BOTÓN DE INICIO DE JUEGO Y SELECCIÓN DE TEMA
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Selector rápido de Temas
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tema:",
                        color = colors.text.copy(alpha = 0.6f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    ThemeDot(type = SudokuTheme.Type.SLATE, current = state.activeTheme, color = colors.primary, onClick = { viewModel.changeTheme(it) })
                    ThemeDot(type = SudokuTheme.Type.NORDIC, current = state.activeTheme, color = colors.primary, onClick = { viewModel.changeTheme(it) })
                    ThemeDot(type = SudokuTheme.Type.CYBERPUNK, current = state.activeTheme, color = colors.primary, onClick = { viewModel.changeTheme(it) })
                }

                Button(
                    onClick = {
                        // Cargar guardado si existe para el slot seleccionado
                        viewModel.loadSavedGame(selectedTab)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.background
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "CONTINUAR PARTIDA",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Button(
                    onClick = {
                        viewModel.startNewGame(selectedTab)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .border(1.5.dp, colors.primary, RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.background,
                        contentColor = colors.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "NUEVA PARTIDA",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ThemeDot(
    type: SudokuTheme.Type,
    current: SudokuTheme.Type,
    color: androidx.compose.ui.graphics.Color,
    onClick: (SudokuTheme.Type) -> Unit
) {
    val isSelected = current == type
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(
                when (type) {
                    SudokuTheme.Type.SLATE -> androidx.compose.ui.graphics.Color(0xFF06B6D4)
                    SudokuTheme.Type.NORDIC -> androidx.compose.ui.graphics.Color(0xFF8FBCBB)
                    SudokuTheme.Type.CYBERPUNK -> androidx.compose.ui.graphics.Color(0xFFD946EF)
                }
            )
            .border(
                if (isSelected) 3.dp else 0.dp,
                if (isSelected) androidx.compose.ui.graphics.Color.White else androidx.compose.ui.graphics.Color.Transparent,
                CircleShape
            )
            .clickable { onClick(type) }
    )
}

@Composable
fun AdventureSettings(
    state: GameViewModel.GameUiState,
    colors: SudokuThemeColors,
    viewModel: GameViewModel
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "La Torre Infinita",
            color = colors.primary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Supera pisos lógicos con dificultad progresiva y enfréntate al Boss en el Piso 10 de cada nivel.",
            color = colors.text.copy(alpha = 0.7f),
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Nivel Actual", color = colors.text.copy(alpha = 0.5f), fontSize = 11.sp)
                Text(text = "${state.level}", color = colors.text, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Piso Actual", color = colors.text.copy(alpha = 0.5f), fontSize = 11.sp)
                Text(text = "${state.floor}", color = colors.text, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PracticeSettings(
    state: GameViewModel.GameUiState,
    colors: SudokuThemeColors,
    viewModel: GameViewModel
) {
    var sliderValue by remember(state.chosenDifficulty) { mutableStateOf(state.chosenDifficulty.toFloat()) }
    
    val diffDescription = when (sliderValue.roundToInt()) {
        in 1..3 -> "Visual / Escaneo Básico"
        in 4..6 -> "Visual Avanzado & X-Wing"
        in 7..8 -> "Avanzado / Simple Coloring"
        else -> "Experto / Bowman's Bingo"
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Modo Entrenamiento",
            color = colors.primary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Selecciona libremente cualquier dificultad. Obtén pistas lógicas ilimitadas en tiempo real.",
            color = colors.text.copy(alpha = 0.7f),
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Dificultad:", color = colors.text.copy(alpha = 0.6f), fontSize = 13.sp)
            Text(
                text = "${sliderValue.roundToInt()}.0",
                color = colors.primary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Slider(
            value = sliderValue,
            onValueChange = {
                sliderValue = it
                viewModel.setChosenDifficulty(it.roundToInt())
            },
            valueRange = 1f..10f,
            steps = 8,
            colors = SliderDefaults.colors(
                thumbColor = colors.primary,
                activeTrackColor = colors.primary,
                inactiveTrackColor = colors.gridBorder.copy(alpha = 0.3f)
            )
        )
        
        Text(
            text = "Técnicas: $diffDescription",
            color = colors.text.copy(alpha = 0.5f),
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        val stats = state.practiceStats[sliderValue.roundToInt()]
        Spacer(modifier = Modifier.height(16.dp))
        
        if (stats != null && stats.timesPlayed > 0) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.background.copy(alpha = 0.4f))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "🏆 RÉCORD PERSONAL",
                    color = colors.primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Mejor Tiempo:", color = colors.text.copy(alpha = 0.6f), fontSize = 12.sp)
                    Text(text = formatTime(stats.bestTimeSeconds), color = colors.text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Ayudas Usadas:", color = colors.text.copy(alpha = 0.6f), fontSize = 12.sp)
                    Text(text = "${stats.recordHintsUsed}", color = colors.text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Veces Completado:", color = colors.text.copy(alpha = 0.6f), fontSize = 12.sp)
                    Text(text = "${stats.timesPlayed}", color = colors.text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.background.copy(alpha = 0.2f))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "¡Aún no has superado este nivel! Inténtalo para establecer tu récord personal. 💪",
                    color = colors.text.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

private fun formatTime(seconds: Long): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%02d:%02d", m, s)
}
