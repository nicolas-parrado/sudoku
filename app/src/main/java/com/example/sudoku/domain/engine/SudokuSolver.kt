package com.example.sudoku.domain.engine

import com.example.sudoku.domain.model.BoardState
import com.example.sudoku.domain.model.HintDetail
import com.example.sudoku.domain.model.SudokuCell

object SudokuSolver {

    class LogicResult(
        val solvedBoard: String?,
        val difficulty: Double,
        val nextHint: HintDetail?,
        val isSolvableByLogic: Boolean
    )

    /**
     * Intenta resolver el Sudoku utilizando técnicas exclusivamente lógicas y humanas.
     * Retorna el resultado con el tablero resuelto, la dificultad clasificada, el siguiente hint útil,
     * y si se pudo resolver completamente.
     */
    fun analyze(puzzle: String): LogicResult {
        require(puzzle.length == 81) { "El string debe tener exactamente 81 caracteres." }
        
        val grid = IntArray(81) { puzzle[it].digitToInt() }
        val candidates = Array(81) { index ->
            if (grid[index] != 0) mutableSetOf() else (1..9).toMutableSet()
        }

        var changed = true
        var highestTechniqueUsed = 1.0 // Por defecto Naked/Hidden Singles
        var nextHint: HintDetail? = null

        // Inicializar candidatos descartando los valores ya dados en filas, columnas y bloques
        updateAllCandidates(grid, candidates)

        while (changed) {
            changed = false

            // 1. Naked Singles (Dificultad 1.0 - 1.5)
            val nakedSingle = findNakedSingle(grid, candidates)
            if (nakedSingle != null) {
                val (index, value) = nakedSingle
                grid[index] = value
                candidates[index].clear()
                updateCandidatesForCell(index, value, candidates)
                if (nextHint == null) {
                    nextHint = HintDetail(
                        explanation = "Naked Single: La celda en la fila ${index / 9 + 1}, columna ${index % 9 + 1} solo puede contener el número $value.",
                        pivotCells = setOf(index)
                    )
                }
                highestTechniqueUsed = maxOf(highestTechniqueUsed, 1.2)
                changed = true
                continue
            }

            // 2. Hidden Singles (Dificultad 1.5 - 2.5)
            val hiddenSingle = findHiddenSingle(grid, candidates)
            if (hiddenSingle != null) {
                val (index, value, reason) = hiddenSingle
                grid[index] = value
                candidates[index].clear()
                updateCandidatesForCell(index, value, candidates)
                if (nextHint == null) {
                    nextHint = HintDetail(
                        explanation = "Hidden Single: En $reason, el número $value solo puede ir en la fila ${index / 9 + 1}, columna ${index % 9 + 1}.",
                        pivotCells = setOf(index)
                    )
                }
                highestTechniqueUsed = maxOf(highestTechniqueUsed, 1.8)
                changed = true
                continue
            }

            // 3. Pointing Pairs / Claiming Pairs (Dificultad 3.5 - 4.5)
            val pointingEffect = applyPointingPairs(grid, candidates)
            if (pointingEffect != null) {
                if (nextHint == null) {
                    nextHint = pointingEffect.hint
                }
                highestTechniqueUsed = maxOf(highestTechniqueUsed, 3.8)
                changed = true
                continue
            }

            // 4. Naked Pairs (Dificultad 2.5 - 3.0)
            val nakedPairEffect = applyNakedPairs(grid, candidates)
            if (nakedPairEffect != null) {
                if (nextHint == null) {
                    nextHint = nakedPairEffect.hint
                }
                highestTechniqueUsed = maxOf(highestTechniqueUsed, 2.8)
                changed = true
                continue
            }

            // 5. X-Wing (Dificultad 5.0 - 6.0)
            val xWingEffect = applyXWing(grid, candidates)
            if (xWingEffect != null) {
                if (nextHint == null) {
                    nextHint = xWingEffect.hint
                }
                highestTechniqueUsed = maxOf(highestTechniqueUsed, 5.5)
                changed = true
                continue
            }
        }

        val isSolved = grid.none { it == 0 }
        val solvedString = if (isSolved) grid.joinToString("") { it.toString() } else null

        return LogicResult(
            solvedBoard = solvedString,
            difficulty = highestTechniqueUsed,
            nextHint = nextHint,
            isSolvableByLogic = isSolved
        )
    }

    private fun updateAllCandidates(grid: IntArray, candidates: Array<MutableSet<Int>>) {
        for (i in 0 until 81) {
            if (grid[i] != 0) {
                candidates[i].clear()
            } else {
                val row = i / 9
                val col = i % 9
                val block = (row / 3) * 3 + (col / 3)
                for (j in 0 until 81) {
                    if (grid[j] != 0) {
                        val rJ = j / 9
                        val cJ = j % 9
                        val bJ = (rJ / 3) * 3 + (cJ / 3)
                        if (rJ == row || cJ == col || bJ == block) {
                            candidates[i].remove(grid[j])
                        }
                    }
                }
            }
        }
    }

    private fun updateCandidatesForCell(index: Int, value: Int, candidates: Array<MutableSet<Int>>) {
        val row = index / 9
        val col = index % 9
        val block = (row / 3) * 3 + (col / 3)
        for (i in 0 until 81) {
            val r = i / 9
            val c = i % 9
            val b = (r / 3) * 3 + (c / 3)
            if (r == row || c == col || b == block) {
                candidates[i].remove(value)
            }
        }
    }

    private fun findNakedSingle(grid: IntArray, candidates: Array<MutableSet<Int>>): Pair<Int, Int>? {
        for (i in 0 until 81) {
            if (grid[i] == 0 && candidates[i].size == 1) {
                return Pair(i, candidates[i].first())
            }
        }
        return null
    }

    private fun findHiddenSingle(grid: IntArray, candidates: Array<MutableSet<Int>>): Triple<Int, Int, String>? {
        // Buscar en filas
        for (r in 0 until 9) {
            val counts = IntArray(10)
            val lastSeenIndex = IntArray(10)
            for (c in 0 until 9) {
                val idx = r * 9 + c
                if (grid[idx] == 0) {
                    for (cand in candidates[idx]) {
                        counts[cand]++
                        lastSeenIndex[cand] = idx
                    }
                }
            }
            for (num in 1..9) {
                if (counts[num] == 1) {
                    return Triple(lastSeenIndex[num], num, "la fila ${r + 1}")
                }
            }
        }

        // Buscar en columnas
        for (c in 0 until 9) {
            val counts = IntArray(10)
            val lastSeenIndex = IntArray(10)
            for (r in 0 until 9) {
                val idx = r * 9 + c
                if (grid[idx] == 0) {
                    for (cand in candidates[idx]) {
                        counts[cand]++
                        lastSeenIndex[cand] = idx
                    }
                }
            }
            for (num in 1..9) {
                if (counts[num] == 1) {
                    return Triple(lastSeenIndex[num], num, "la columna ${c + 1}")
                }
            }
        }

        // Buscar en bloques 3x3
        for (b in 0 until 9) {
            val counts = IntArray(10)
            val lastSeenIndex = IntArray(10)
            val startRow = (b / 3) * 3
            val startCol = (b % 3) * 3
            for (r in 0 until 3) {
                for (c in 0 until 3) {
                    val idx = (startRow + r) * 9 + (startCol + c)
                    if (grid[idx] == 0) {
                        for (cand in candidates[idx]) {
                            counts[cand]++
                            lastSeenIndex[cand] = idx
                        }
                    }
                }
            }
            for (num in 1..9) {
                if (counts[num] == 1) {
                    return Triple(lastSeenIndex[num], num, "el bloque 3x3 número ${b + 1}")
                }
            }
        }

        return null
    }

    class RuleEffect(val hint: HintDetail)

    private fun applyPointingPairs(grid: IntArray, candidates: Array<MutableSet<Int>>): RuleEffect? {
        // Pointing: si un candidato en un bloque está restringido a una sola fila/columna,
        // lo eliminamos del resto de esa fila/columna.
        for (b in 0 until 9) {
            val startRow = (b / 3) * 3
            val startCol = (b % 3) * 3
            for (num in 1..9) {
                val rows = mutableSetOf<Int>()
                val cols = mutableSetOf<Int>()
                val cells = mutableListOf<Int>()

                for (r in 0 until 3) {
                    for (c in 0 until 3) {
                        val idx = (startRow + r) * 9 + (startCol + c)
                        if (grid[idx] == 0 && candidates[idx].contains(num)) {
                            rows.add(startRow + r)
                            cols.add(startCol + c)
                            cells.add(idx)
                        }
                    }
                }

                // Si está restringido a una sola fila dentro del bloque
                if (rows.size == 1 && cells.size >= 2) {
                    val targetRow = rows.first()
                    var eliminated = false
                    val elimCells = mutableSetOf<Int>()
                    for (c in 0 until 9) {
                        val idx = targetRow * 9 + c
                        if (grid[idx] == 0 && !cells.contains(idx) && candidates[idx].contains(num)) {
                            candidates[idx].remove(num)
                            elimCells.add(idx)
                            eliminated = true
                        }
                    }
                    if (eliminated) {
                        return RuleEffect(
                            HintDetail(
                                explanation = "Pointing Pair: En el bloque ${b + 1}, el número $num solo puede ir en la fila ${targetRow + 1}. Se elimina $num de las demás celdas de la fila.",
                                pivotCells = cells.toSet(),
                                eliminationCells = elimCells
                            )
                        )
                    }
                }

                // Si está restringido a una sola columna dentro del bloque
                if (cols.size == 1 && cells.size >= 2) {
                    val targetCol = cols.first()
                    var eliminated = false
                    val elimCells = mutableSetOf<Int>()
                    for (r in 0 until 9) {
                        val idx = r * 9 + targetCol
                        if (grid[idx] == 0 && !cells.contains(idx) && candidates[idx].contains(num)) {
                            candidates[idx].remove(num)
                            elimCells.add(idx)
                            eliminated = true
                        }
                    }
                    if (eliminated) {
                        return RuleEffect(
                            HintDetail(
                                explanation = "Pointing Pair: En el bloque ${b + 1}, el número $num solo puede ir en la columna ${targetCol + 1}. Se elimina $num de las demás celdas de la columna.",
                                pivotCells = cells.toSet(),
                                eliminationCells = elimCells
                            )
                        )
                    }
                }
            }
        }
        return null
    }

    private fun applyNakedPairs(grid: IntArray, candidates: Array<MutableSet<Int>>): RuleEffect? {
        // Buscar en filas
        for (r in 0 until 9) {
            for (c1 in 0 until 8) {
                val idx1 = r * 9 + c1
                if (grid[idx1] == 0 && candidates[idx1].size == 2) {
                    for (c2 in (c1 + 1) until 9) {
                        val idx2 = r * 9 + c2
                        if (grid[idx2] == 0 && candidates[idx1] == candidates[idx2]) {
                            val pairVals = candidates[idx1]
                            var eliminated = false
                            val elimCells = mutableSetOf<Int>()
                            for (c3 in 0 until 9) {
                                val idx3 = r * 9 + c3
                                if (idx3 != idx1 && idx3 != idx2 && grid[idx3] == 0) {
                                    for (v in pairVals) {
                                        if (candidates[idx3].contains(v)) {
                                            candidates[idx3].remove(v)
                                            elimCells.add(idx3)
                                            eliminated = true
                                        }
                                    }
                                }
                            }
                            if (eliminated) {
                                return RuleEffect(
                                    HintDetail(
                                        explanation = "Naked Pair: Las celdas en fila ${r + 1} columnas ${c1 + 1} y ${c2 + 1} contienen el par exclusivo $pairVals. Se eliminan del resto de la fila.",
                                        pivotCells = setOf(idx1, idx2),
                                        eliminationCells = elimCells
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
        return null
    }

    private fun applyXWing(grid: IntArray, candidates: Array<MutableSet<Int>>): RuleEffect? {
        for (num in 1..9) {
            // Evaluamos por columnas
            val colPositions = Array(9) { mutableListOf<Int>() }
            for (c in 0 until 9) {
                for (r in 0 until 9) {
                    val idx = r * 9 + c
                    if (grid[idx] == 0 && candidates[idx].contains(num)) {
                        colPositions[c].add(r)
                    }
                }
            }

            for (c1 in 0 until 8) {
                if (colPositions[c1].size == 2) {
                    val r1 = colPositions[c1][0]
                    val r2 = colPositions[c1][1]
                    for (c2 in (c1 + 1) until 9) {
                        if (colPositions[c2].size == 2 && colPositions[c2][0] == r1 && colPositions[c2][1] == r2) {
                            // Encontrado X-Wing en columnas c1, c2 en las filas r1, r2
                            var eliminated = false
                            val pivotCells = setOf(r1 * 9 + c1, r2 * 9 + c1, r1 * 9 + c2, r2 * 9 + c2)
                            val elimCells = mutableSetOf<Int>()
                            for (c in 0 until 9) {
                                if (c != c1 && c != c2) {
                                    val idxR1 = r1 * 9 + c
                                    val idxR2 = r2 * 9 + c
                                    if (grid[idxR1] == 0 && candidates[idxR1].contains(num)) {
                                        candidates[idxR1].remove(num)
                                        elimCells.add(idxR1)
                                        eliminated = true
                                    }
                                    if (grid[idxR2] == 0 && candidates[idxR2].contains(num)) {
                                        candidates[idxR2].remove(num)
                                        elimCells.add(idxR2)
                                        eliminated = true
                                    }
                                }
                            }
                            if (eliminated) {
                                return RuleEffect(
                                    HintDetail(
                                        explanation = "X-Wing: El número $num está en patrón X-Wing en columnas ${c1 + 1} y ${c2 + 1}, limitando a las filas ${r1 + 1} y ${r2 + 1}. Se elimina $num de las demás celdas en esas filas.",
                                        pivotCells = pivotCells,
                                        eliminationCells = elimCells
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
        return null
    }

    /**
     * Verifica si el Sudoku actual tiene conflictos visuales directos.
     * Retorna un conjunto de índices de celdas que causan duplicación.
     */
    fun getConflictingCells(board: BoardState): Set<Int> {
        val conflicts = mutableSetOf<Int>()
        val cells = board.cells

        // Validar filas
        for (r in 0 until 9) {
            val values = mutableMapOf<Int, MutableList<Int>>()
            for (c in 0 until 9) {
                val cell = cells[r * 9 + c]
                if (cell.value != 0) {
                    values.getOrPut(cell.value) { mutableListOf() }.add(cell.index)
                }
            }
            for ((_, indices) in values) {
                if (indices.size > 1) {
                    conflicts.addAll(indices)
                }
            }
        }

        // Validar columnas
        for (c in 0 until 9) {
            val values = mutableMapOf<Int, MutableList<Int>>()
            for (r in 0 until 9) {
                val cell = cells[r * 9 + c]
                if (cell.value != 0) {
                    values.getOrPut(cell.value) { mutableListOf() }.add(cell.index)
                }
            }
            for ((_, indices) in values) {
                if (indices.size > 1) {
                    conflicts.addAll(indices)
                }
            }
        }

        // Validar bloques
        for (b in 0 until 9) {
            val values = mutableMapOf<Int, MutableList<Int>>()
            val startRow = (b / 3) * 3
            val startCol = (b % 3) * 3
            for (r in 0 until 3) {
                for (c in 0 until 3) {
                    val cell = cells[(startRow + r) * 9 + (startCol + c)]
                    if (cell.value != 0) {
                        values.getOrPut(cell.value) { mutableListOf() }.add(cell.index)
                    }
                }
            }
            for ((_, indices) in values) {
                if (indices.size > 1) {
                    conflicts.addAll(indices)
                }
            }
        }

        return conflicts
    }
}
