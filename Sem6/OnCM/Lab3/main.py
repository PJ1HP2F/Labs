import json
from pathlib import Path

import numpy as np


def load_input_from_json(path=None):
    """Read c, A, x from JSON. B = indices where x[i] != 0 (same as interactive flow)."""
    if path is None:
        path = Path(__file__).resolve().parent / "input.json"
    else:
        path = Path(path)
    if not path.is_file():
        raise FileNotFoundError(f"Input file not found: {path}")

    with path.open(encoding="utf-8") as f:
        data = json.load(f)

    for key in ("c", "A", "x"):
        if key not in data:
            raise KeyError(f"{path.name} must contain key {key!r}")

    c = np.asarray(data["c"], dtype=float).ravel()
    A = np.asarray(data["A"], dtype=float)
    x = np.asarray(data["x"], dtype=float).ravel()

    if A.ndim != 2:
        raise ValueError("A must be a 2D array (list of rows)")

    m, n = A.shape
    if m < 1 or n < 1:
        raise ValueError("A must have positive dimensions")

    if c.size != n:
        raise ValueError(f"len(c)={c.size} must equal number of columns in A ({n})")
    if x.size != n:
        raise ValueError(f"len(x)={x.size} must equal number of columns in A ({n})")

    B = [i for i in range(n) if x[i] != 0]
    return c, A, x, B


def multiply_Q_A_optimized(Q, _A, n, i):
    result = _A.copy()
    col_idx = i-1
    for row in range(n):
        result[row, col_idx] = 0
        for k in range(n):
            result[row, col_idx] += Q[row, k] * _A[k, col_idx]
    return result

def calculate_inverse_matrix(A, _A, x, i):
    n = A.shape[0]
    A_asterisk = A.copy()
    A_asterisk[:, i-1] = x
    l = _A @ x
    if l[i-1] == 0:
        return None, None, None
    l_wave = l.copy()
    l_wave[i-1] = -1
    l_hat = (-1 / l[i-1]) * l_wave
    Q = np.identity(n)
    Q[:, i-1] = l_hat
    _A_asterisk = multiply_Q_A_optimized(Q, _A, n, i)
    
    return _A_asterisk, Q, A_asterisk

def print_iteration(iter, AB, AB_inv, x, B, cB, u, delta,
                    j0=None, Aj0=None, z=None, theta=None, theta0=None, k=None, j_asterisk=None):
    print(f"ITERATION #{iter}")
    print(f"x:\n{x}\n")
    print(f"B:\n{B}\n")
    print(f"AB:\n{AB}\n")
    print(f"AB_inv:\n{AB_inv}\n")
    print(f"cB:\n{cB}\n")
    print(f"u:\n{u}\n")
    print(f"delta:\n{delta}\n")
    if (j0 is not None):
        print(f"j0:\n{j0}\n")
        print(f"Aj0:\n{Aj0}\n")
        print(f"z:\n{z}\n")
        print(f"theta:\n{theta}\n")
        print(f"theta0:\n{theta0}\n")
        print(f"k:\n{k}\n")
        print(f"j_asterisk:\n{j_asterisk}\n")
    print(f"{'-'*16}")


def main_simplex_method(c, A, x, B):
    m, n = A.shape
    method_iteration = 1

    AB = A[:,B]
    AB_inv = []
    try:
        AB_inv = np.linalg.inv(AB)
    except np.linalg.LinAlgError:
        print("Basis matrix AB cannot be inverted")
        return None
    
    while True:
        if method_iteration != 1:
            AB_inv, _, AB = calculate_inverse_matrix(AB, AB_inv, AB[:,j0], k)
        
        cB = c[B]
        
        u = cB @ AB_inv

        delta = u @ A - c

        print_iteration(method_iteration, AB, AB_inv, x, B, cB, u, delta)

        if np.all(delta >= 0):
            return x
        
        j0_list = np.where(delta < 0)[0]
        if len(j0_list) == 0:
            return x
        
        j0 = j0_list[0]
        Aj0 = A[:,j0]
        z = AB_inv @ Aj0

        theta = [x[B[i]] / z[i]     if z[i] > 0 else
                 np.inf
                 for i in range(m)]
        
        theta0 = np.min(theta)

        if theta0 is np.inf:
            print("Function is'nt limited")
            return None
        
        k = np.where(np.array(theta) == theta0)[0][0]
        j_asterisk = B[k]

        B_new = B.copy()
        B_new[k] = j0

        x_new = x.copy()
        x_new[j0] = theta0
        for i in range(m):
            if i is not k:
                x_new[B[i]] = x_new[B[i]] - theta0 * z[i]
        x_new[j_asterisk] = 0

        print_iteration(method_iteration, AB, AB_inv, x_new, B_new, cB, u, delta,
                        j0, Aj0, z, theta, theta0, k, j_asterisk)

        x = x_new
        B = B_new
        method_iteration += 1


if __name__ == "__main__":
    c, A, x, B = load_input_from_json()

    print(f"Vector c:\n{c}\n")
    print(f"Matrix A:\n{A}\n")
    print(f"Vector x:\n{x}\n")
    print(f"Set B:\n{B}\n")

    x_result = main_simplex_method(c, A, x , B)
    print(f"Resulting Vector x:\n{x_result}\n")