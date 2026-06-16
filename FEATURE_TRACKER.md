# Tracker de Features Adicionales - Sudoku Offline

En este archivo registramos, detallamos e implementamos las nuevas características del juego.

| ID | Descripción | Estado | Plan de Desarrollo |
|---|---|---|---|
| **FEAT-001** | **Resaltar números iguales**: Al seleccionar una celda con un valor ingresado (ej: 5), resaltar de color diferente todos los 5 en el tablero y las notas con el valor 5. | 🟢 Implementado | Modificado `GridBoard.kt` para evaluar y pintar el fondo de celdas con el mismo valor con opacidad de color primario, y destacar en negrita las notas idénticas en la vista `NoteGrid`. |
| **FEAT-002** | **Auto-eliminación de notas**: Al ingresar un número real en una celda, borrar automáticamente dicho número de las notas de las celdas en la misma fila, columna o bloque. | 🟢 Implementado | Implementado en `GameViewModel.kt#inputNumber`, donde tras ingresar un número definitivo, se itera por la misma fila, columna y bloque 3x3 removiendo el número de los candidatos (`notes`). |
| **FEAT-003** | **Cerrar ayuda visual**: Poder descartar y cerrar la sugerencia o pista activa en pantalla. | 🟢 Implementado | Añadido un botón de descarte `✕` en `HintSection` de `GameScreen.kt` y una nueva función `clearActiveHint()` en `GameViewModel.kt` para limpiar la sugerencia en la UI. |
