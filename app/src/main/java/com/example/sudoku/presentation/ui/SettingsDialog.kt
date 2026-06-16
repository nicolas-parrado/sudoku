package com.example.sudoku.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sudoku.presentation.theme.SudokuTheme
import com.example.sudoku.presentation.theme.SudokuThemeColors

@Composable
fun SettingsDialog(
    colors: SudokuThemeColors,
    activeTheme: SudokuTheme.Type,
    onThemeChanged: (SudokuTheme.Type) -> Unit,
    onRestart: () -> Unit,
    onGoToMenu: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                text = "Configuración",
                color = colors.text,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Divider(color = colors.gridBorder.copy(alpha = 0.3f))
                
                // Opción Cambiar Tema
                Text(
                    text = "Cambiar Estilo Visual:",
                    color = colors.text.copy(alpha = 0.8f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val themes = listOf(
                        Triple(SudokuTheme.Type.SLATE, "Slate / Midnight", colors.primary),
                        Triple(SudokuTheme.Type.NORDIC, "Nordic Frost", colors.primary),
                        Triple(SudokuTheme.Type.CYBERPUNK, "Cyberpunk / Obsidian", colors.primary)
                    )

                    themes.forEach { (type, name, _) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onThemeChanged(type) }
                                .padding(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            RadioButton(
                                selected = activeTheme == type,
                                onClick = { onThemeChanged(type) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = colors.primary,
                                    unselectedColor = colors.text.copy(alpha = 0.4f)
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = name, color = colors.text, fontSize = 14.sp)
                        }
                    }
                }

                Divider(color = colors.gridBorder.copy(alpha = 0.3f))

                // Acciones de reinicio y volver al menú
                Button(
                    onClick = {
                        onRestart()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary.copy(alpha = 0.15f),
                        contentColor = colors.primary
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = "Reiniciar Tablero Actual", fontWeight = FontWeight.Medium)
                }

                Button(
                    onClick = {
                        onGoToMenu()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.text.copy(alpha = 0.08f),
                        contentColor = colors.text
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = "Volver al Menú Principal", fontWeight = FontWeight.Medium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.background
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "Cerrar", fontWeight = FontWeight.Bold)
            }
        }
    )
}
