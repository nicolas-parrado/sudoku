import subprocess
import time
import os
from multiprocessing import Process

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

def worker(level, count, temp_output_path, jar_path):
    print(f"Iniciando worker para nivel {level}.0...")
    t0 = time.time()
    cmd = ["java", "-Xmx1024m", "-jar", jar_path, "/s", "/sl", str(level), "/o", "stdout"]
    proc = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
    
    collected = 0
    with open(temp_output_path, "w") as f:
        try:
            for line in proc.stdout:
                line = line.strip()
                if not line or " #" not in line:
                    continue
                puzzle_raw = line.split(" #")[0].strip()
                puzzle_str = puzzle_raw.replace('.', '0')
                if len(puzzle_str) == 81:
                    grid = [int(c) for c in puzzle_str]
                    solution_grid = solve_sudoku(grid)
                    if solution_grid:
                        solution_str = "".join(map(str, solution_grid))
                        f.write(f"{puzzle_str},{solution_str},{float(level)}\n")
                        collected += 1
                        if collected % 1000 == 0:
                            print(f"  [Nivel {level}.0] -> {collected}/{count} listos. Tiempo: {time.time() - t0:.2f}s")
                        if collected >= count:
                            break
        finally:
            proc.terminate()
            proc.wait()
            
    print(f"Worker Nivel {level}.0 finalizado en {time.time() - t0:.2f}s.")

def main():
    jar_path = "/Users/nparrado/Downloads/hodoku.jar"
    output_path = "app/src/main/assets/seeds_hard.txt"
    puzzles_per_level = 10000
    
    print("Iniciando generación masiva en paralelo con 5 procesos workers...")
    t_start = time.time()
    
    processes = []
    temp_files = []
    
    for level in range(5):
        temp_file = f"seeds_temp_{level}.txt"
        temp_files.append(temp_file)
        p = Process(target=worker, args=(level, puzzles_per_level, temp_file, jar_path))
        processes.append(p)
        p.start()
        
    for p in processes:
        p.join()
        
    # Combinar todos los archivos temporales en seeds_hard.txt
    print("\nCombinando resultados en seeds_hard.txt...")
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    with open(output_path, "w") as outfile:
        for temp_file in temp_files:
            if os.path.exists(temp_file):
                with open(temp_file, "r") as infile:
                    outfile.write(infile.read())
                os.remove(temp_file)
                
    print(f"¡Proceso completo! 50,000 semillas guardadas en {output_path}. Tiempo total: {time.time() - t_start:.2f}s.")

if __name__ == "__main__":
    main()
