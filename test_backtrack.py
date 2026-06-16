import time

def solve_sudoku(grid):
    def get_candidates(g, idx):
        if g[idx] != 0:
            return set()
        r, c = idx // 9, idx % 9
        used = set()
        for i in range(9):
            used.add(g[r * 9 + i])
            used.add(g[i * 9 + c])
        br, bc = (r // 3) * 3, (c // 3) * 3
        for i in range(3):
            for j in range(3):
                used.add(g[(br + i) * 9 + (bc + j)])
        return set(range(1, 10)) - used

    def backtrack(g):
        min_candidates = 10
        best_idx = -1
        best_cands = []
        for i in range(81):
            if g[i] == 0:
                cands = get_candidates(g, i)
                if not cands:
                    return False
                if len(cands) < min_candidates:
                    min_candidates = len(cands)
                    best_idx = i
                    best_cands = cands
                    if min_candidates == 1:
                        break
        if best_idx == -1:
            return True
        
        for val in best_cands:
            g[best_idx] = val
            if backtrack(g):
                return True
            g[best_idx] = 0
        return False

    g_copy = list(grid)
    if backtrack(g_copy):
        return g_copy
    return None

# String de Hodoku original
hodoku_str = "6....57.32.7.....8....4................1..62....4..3.5.1...8.9.4.2.9351..6......."
puzzle_str = hodoku_str.replace('.', '0')
puzzle = [int(c) for c in puzzle_str]
t0 = time.time()
sol = solve_sudoku(puzzle)
t1 = time.time()
print(f"Solucionado en {t1 - t0:.6f} segundos.")
print("".join(map(str, sol)))
