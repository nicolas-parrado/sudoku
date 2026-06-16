package com.example.sudoku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.sudoku.presentation.theme.SudokuTheme
import com.example.sudoku.presentation.ui.GameScreen
import com.example.sudoku.presentation.ui.MainScreen
import com.example.sudoku.presentation.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val state by viewModel.uiState.collectAsState()

            SudokuTheme(themeType = state.activeTheme) {
                val colors = SudokuTheme.colors

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = colors.background
                ) {
                    if (state.isMenuOpen) {
                        MainScreen(
                            viewModel = viewModel,
                            colors = colors,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        GameScreen(
                            viewModel = viewModel,
                            colors = colors,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
