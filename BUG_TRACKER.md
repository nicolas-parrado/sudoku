# Tracker de Bugs - Sudoku Offline

En este archivo registramos, analizamos y solucionamos los bugs identificados en el juego en su dispositivo real.

| ID | Descripción | Estado | Solución |
|---|---|---|---|
| **BUG-001** | El juego no se pone en pantalla completa; sigue mostrando la barra de estado (hora, batería) y de navegación inferior. | 🟢 Solucionado | Habilitado el modo inmersivo total (Immersive Mode) en la `MainActivity` utilizando `WindowInsetsControllerCompat` e insets. |
| **BUG-002** | *Mensaje incompleto en el reporte del usuario ("Al seleccionar una...")* | 🟡 Esperando feedback | Solicitar aclaración al usuario sobre el comportamiento esperado o qué bug observó al realizar esta acción. |
| **BUG-003** | En el Nivel 1 - Piso 1 el Sudoku se generaba ya resuelto, impidiendo jugar. | 🟢 Solucionado | La dificultad de cualquier Sudoku con celdas vacías requiere un mínimo de 1.2 (Naked Singles). Al pedir dificultad 1.0, el generador rechazaba remover números para no subir de 1.0, devolviendo el tablero intacto. Se ajustó el límite mínimo a 1.2 en la curva de dificultad y en el generador de forma segura. |

