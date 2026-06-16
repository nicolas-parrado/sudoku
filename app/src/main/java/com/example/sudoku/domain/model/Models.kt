package com.example.sudoku.domain.model

data class SudokuCell(
    val index: Int, // 0 to 80
    val value: Int, // 1 to 9, or 0 if empty
    val isGiven: Boolean = false, // If true, it was set from the start and cannot be modified
    val notes: Set<Int> = emptySet() // Small pencil marks (1 to 9)
) {
    val row: Int get() = index / 9
    val col: Int get() = index % 9
    val block: Int get() = (row / 3) * 3 + (col / 3)
}

data class BoardState(
    val cells: List<SudokuCell> = List(81) { SudokuCell(it, 0) }
) {
    fun getCell(index: Int): SudokuCell = cells[index]
    fun getCell(row: Int, col: Int): SudokuCell = cells[row * 9 + col]

    fun getRowCells(row: Int): List<SudokuCell> = cells.filter { it.row == row }
    fun getColCells(col: Int): List<SudokuCell> = cells.filter { it.col == col }
    fun getBlockCells(block: Int): List<SudokuCell> = cells.filter { it.block == block }

    fun to81CharString(): String {
        return cells.joinToString("") { it.value.toString() }
    }

    companion object {
        fun from81CharString(puzzle: String, solution: String? = null): BoardState {
            require(puzzle.length == 81) { "Puzzle string must be exactly 81 characters" }
            val cells = List(81) { index ->
                val charVal = puzzle[index].digitToInt()
                SudokuCell(
                    index = index,
                    value = charVal,
                    isGiven = charVal != 0
                )
            }
            return BoardState(cells)
        }
    }
}

enum class GameSlot {
    ADVENTURE,
    PRACTICE
}

data class BoardHistory(
    val cells: List<SudokuCell>
)

data class HintDetail(
    val explanation: String,
    val pivotCells: Set<Int> = emptySet(), // Blue cells in Capa 2
    val eliminationCells: Set<Int> = emptySet(), // Red/orange cells in Capa 2
    val highlightRows: Set<Int> = emptySet(),
    val highlightCols: Set<Int> = emptySet()
)
