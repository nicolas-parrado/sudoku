# Sudoku Premium - 100% Offline Android App

Una aplicación de Sudoku móvil nativa para Android, desarrollada con altos estándares de ingeniería de software, arquitectura limpia, persistencia transaccional SQLite con Room y un motor de validación deductiva matemática. La aplicación es 100% offline, libre de publicidad y compilable en cualquier entorno mediante contenedores Docker.

---

## 🚀 Arquitectura y Decisiones de Diseño

Para asegurar la robustez, mantenibilidad y velocidad de desarrollo, se implementaron las siguientes decisiones arquitectónicas:

1.  **Arquitectura Monomódulo Limpia**:
    El código se estructura en un único módulo `:app` dividido en capas claras basadas en Clean Architecture:
    *   `domain/`: Modelos inmutables de negocio, lógica pura de Sudoku y los motores matemáticos.
    *   `data/`: Acceso a base de datos local (Room SQLite), repositorios y deserialización/serialización de pilas con Gson.
    *   `presentation/`: Vistas reactivas mediante Jetpack Compose, ViewModels (que exponen estados asíncronos via StateFlow) y el sistema dinámico de personalización visual.

2.  **Persistencia Transaccional Sin Pérdida de Datos**:
    *   Se utiliza **Room Database** para guardar de manera asíncrona y transparente el estado del tablero en **cada acción del usuario** (añadir número, borrar, toggle nota).
    *   Las pilas de **Deshacer (Undo)** y **Rehacer (Redo)** se serializan como objetos JSON dentro de la entidad `GameSlotEntity`, evitando las escrituras relacionales masivas en base de datos.
    *   Se implementan dos slots independientes: `adventure` y `practice`, permitiendo saltar de un modo a otro sin perder el progreso de ninguna partida.

3.  **Compilación en Contenedor Docker**:
    *   El entorno encapsula **JDK 17**, **Android SDK Command-Line Tools** y **Gradle 8.7**.
    *   Cualquier máquina con Docker instalado puede compilar la APK de depuración o de producción, y ejecutar los tests automatizados sin necesidad de instalar localmente el entorno de desarrollo Android.

---

## 🧠 Motor Matemático y Generador

### 1. Algoritmo de Normalización Canónica (Identidad Única)
Para evitar rompecabezas lógicamente repetidos que comparten la misma estructura base por simetría geométrica o reetiquetado numérico, implementamos el **Canonical Normalization Algorithm** antes de almacenar cualquier puzzle en Room:
1.  **Symmetry Simulation**: Generación de las 8 variaciones geométricas del tablero (Original, Rotaciones de 90°, 180°, 270° y sus respectivos espejos/reflexiones).
2.  **Digit Normalization**: Relocalización secuencial de los dígitos (1 al 9) de izquierda a derecha y arriba hacia abajo. El primer número que aparezca se convierte en 1, el siguiente único en 2, y así sucesivamente.
3.  **Lexicographical Minima**: Se comparan las 8 variantes de 81 caracteres resultantes y se selecciona el string de menor valor lexicográfico como el **Canonical Seed Unique ID**.
4.  Este ID tiene una restricción `UNIQUE` en la base de datos de semillas.

### 2. Solvedor Lógico Virtual (Human Deductive Solver)
El motor de Sudoku **no utiliza fuerza bruta (backtracking / adivinanzas)** para resolver o proponer pistas. Se implementó un solucionador lógico virtual en Kotlin que emula el razonamiento humano aplicando secuencialmente técnicas lógicas por capas de dificultad:
*   **Dificultad 1.0 - 3.0**: Naked Singles, Hidden Singles, Naked Pairs.
*   **Dificultad 3.5 - 6.0**: Pointing Pairs, X-Wing.
*   **Dificultad Superior (7.0 - 10.0)**: Se recupera de un pool de semillas avanzadas pre-generadas (Swordfish, Jellyfish, AIC, Forcing Chains) cargadas localmente en `assets/seeds_hard.txt` durante el primer inicio de la app para proteger el hardware del dispositivo.

---

## 🎨 Modos de Juego e Interfaz de Usuario

### Modo Aventura (La Torre Infinita)
*   Niveles infinitos. Cada nivel tiene 10 pisos. El piso 10 actúa como el **Boss Floor**.
*   **Curva de dificultad logarítmica progresiva**: escala rápido en los niveles iniciales (L1: 1.0 a 3.0; L2: 2.0 a 4.5) y satura asintóticamente entre 8.5 y 10.0 a partir del nivel 10.
*   Al completarse el tablero, se purga la pila de Undo/Redo del nivel para liberar recursos.

### Modo Práctica
*   Permite seleccionar una dificultad entera del 1 al 10.
*   Ofrece un **Layered Hint System** en dos niveles:
    *   *Capa 1 (Texto)*: Explicación teórica de la jugada óptima recomendada.
    *   *Capa 2 (Visual)*: Resalta en color **azul** las celdas pivote y en **naranja** las celdas donde se pueden eliminar candidatos.

### Personalización Visual (Temas Premium)
Dispone de 3 paletas de colores HSL premium seleccionables desde el menú de configuraciones:
1.  **Slate / Midnight (Por defecto)**: Fondo pizarra oscura con acentos cian eléctrico y azul cobalto.
2.  **Nordic Frost**: Minimalista con grises nórdicos y acentos menta suave.
3.  **Cyberpunk / Obsidian**: Negro puro AMOLED con acentos violeta y cian neón.

### ⚡ Sistema de Poderes y Tienda Premium (Poderes de la Tropa)
Se ha implementado una tienda flotante moderna de poderes lógicos que permite usar monedas ganadas durante el juego (o gratis en el modo Práctica) para realizar acciones tácticas:
1.  **Lápiz Mágico (Auto Notas)**: Coloca instantáneamente todas las notas lógicas válidas (candidatos) en las casillas vacías de acuerdo al estado del tablero. (Costo: 30 🪙)
2.  **Ojo de Halcón (Revelar Celda)**: Revela y bloquea el número correcto de la casilla seleccionada. (Costo: 100 🪙)
3.  **Escoba Lógica (Limpiar Notas)**: Elimina todas las notas del usuario que entren en conflicto directo con los números colocados. (Costo: 30 🪙)
4.  **Bomba de Números**: Revela una casilla vacía aleatoria en el tablero que contenga en su solución el número seleccionado actualmente en el teclado. (Costo: 50 🪙)
5.  **Ráfaga de Singles (Autocompletar Singles)**: Resuelve de forma automática todas las celdas vacías que tengan un único candidato lógico posible (Naked Singles) en ese instante. (Costo: 120 🪙)
6.  **Congelar Tiempo**: Pausa el cronómetro del nivel por una cantidad de segundos equivalente a la mitad del tiempo de bono de velocidad. Durante la congelación, el reloj se ilumina en azul hielo con un copo de nieve `❄️`. (Costo: 40 🪙)

### 🎨 Iconografía Vectorial y Ergonomía
Se rediseñó la UI reemplazando emojis de texto por recursos de iconos vectoriales XML (`ImageVector` de recursos) en el Panel de Control y en el diálogo de poderes:
*   **Controles**: Deshacer (`ic_undo`), Rehacer (`ic_redo`), Borrar (`ic_delete`), Notas (`ic_notes`), y la Tienda de Poderes (`ic_powerups`).
*   **Poderes**: Lápiz Mágico (`ic_auto_notes`), Ojo de Halcón (`ic_hawkeye`), Escoba Lógica (`ic_broom`), Bomba (`ic_bomb`), Ráfaga (`ic_singles`), y Congelado (`ic_freeze`).
*   **Ergonomía optimizada**: El teclado numérico (`Numpad`) se ubicó directamente debajo del tablero de Sudoku para acercar los números a la zona de interacción principal del pulgar, dejando los botones de control y el disparador de poderes abajo.

---

## 🛠️ Cómo Compilar y Ejecutar con Docker

### Prerrequisitos
*   Tener instalado **Docker** y **Docker Compose**.

### 1. Ejecutar Tests Unitarios
Para verificar la lógica de normalización y el resolvedor virtual en Kotlin:
```bash
docker-compose run --rm builder gradle test
```

### 2. Compilar APK de Depuración (Debug)
Compila y genera la APK en `app/build/outputs/apk/debug/app-debug.apk`:
```bash
docker-compose run --rm builder gradle assembleDebug
```

### 3. Compilar APK de Producción (Release)
Compila y optimiza la APK para producción:
```bash
docker-compose run --rm builder gradle assembleRelease
```

---

## 📁 Estructura de Directorios del Proyecto

```text
Sudoku/
├── Dockerfile
├── docker-compose.yml
├── settings.gradle.kts
├── build.gradle.kts
├── git_init_flow.sh
├── README.md
├── gradle/
│   └── libs.versions.toml
└── app/
    ├── build.gradle.kts
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   ├── assets/
        │   │   └── seeds_hard.txt
        │   ├── java/com/example/sudoku/
        │   │   ├── MainActivity.kt
        │   │   ├── domain/
        │   │   │   ├── model/Models.kt
        │   │   │   └── engine/
        │   │   │       ├── CanonicalNormalization.kt
        │   │   │       ├── SudokuSolver.kt
        │   │   │       ├── SudokuGenerator.kt
        │   │   │       └── DifficultyCurve.kt
        │   │   ├── data/
        │   │   │   ├── local/
        │   │   │   │   ├── Entities.kt
        │   │   │   │   ├── AppDatabase.kt
        │   │   │   │   ├── GameSlotDao.kt
        │   │   │   │   └── SeedPuzzleDao.kt
        │   │   │   └── repository/SudokuRepository.kt
        │   │   └── presentation/
        │   │       ├── theme/
        │   │       │   ├── Color.kt
        │   │       │   └── Theme.kt
        │   │       ├── viewmodel/GameViewModel.kt
        │   │       └── ui/
        │   │           ├── MainScreen.kt
        │   │           ├── GameScreen.kt
        │   │           ├── GridBoard.kt
        │   │           └── SettingsDialog.kt
        │   └── res/
        │       ├── drawable/
        │       │   ├── ic_undo.xml
        │       │   ├── ic_redo.xml
        │       │   ├── ic_delete.xml
        │       │   ├── ic_notes.xml
        │       │   ├── ic_powerups.xml
        │       │   ├── ic_auto_notes.xml
        │       │   ├── ic_hawkeye.xml
        │       │   ├── ic_broom.xml
        │       │   ├── ic_bomb.xml
        │       │   ├── ic_singles.xml
        │       │   └── ic_freeze.xml
        │       ├── values/strings.xml
        │       └── xml/
        │           ├── backup_rules.xml
        │           └── data_extraction_rules.xml
        └── test/
            └── java/com/example/sudoku/domain/engine/
                ├── CanonicalNormalizationTest.kt
                └── SudokuSolverTest.kt
```
