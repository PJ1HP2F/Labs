import numpy as np
import json
from pathlib import Path


def _as_float_matrix(value, name: str) -> np.ndarray:
    try:
        arr = np.array(value, dtype=float)
    except (TypeError, ValueError) as e:
        raise ValueError(f"{name} must be a numeric 2D array") from e

    if arr.ndim != 2:
        raise ValueError(f"{name} must be a 2D array")

    return arr


def _as_float_vector(value, name: str) -> np.ndarray:
    try:
        arr = np.array(value, dtype=float)
    except (TypeError, ValueError) as e:
        raise ValueError(f"{name} must be a numeric vector") from e

    if arr.ndim != 1:
        raise ValueError(f"{name} must be a 1D array")

    return arr


def multiply_Q_A_optimized(Q, _A, n, i):
    result = _A.copy()
    
    col_idx = i-1

    for row in range(n):
        result[row, col_idx] = 0
        for k in range(n):
            result[row, col_idx] += Q[row, k] * _A[k, col_idx]
    
    return result


def read_input_json(path: str | Path = "input.json"):
    p = Path(path)
    if not p.exists():
        raise FileNotFoundError(f"Input file not found: {p}")

    try:
        data = json.loads(p.read_text(encoding="utf-8"))
    except json.JSONDecodeError as e:
        raise ValueError(f"Invalid JSON in {p}: {e}") from e

    if not isinstance(data, dict):
        raise ValueError("input.json root must be an object")

    if "A" not in data or "A_inv" not in data or "x" not in data or "i" not in data:
        raise ValueError("input.json must contain keys: A, A_inv, x, i")

    A = _as_float_matrix(data["A"], "A")
    _A = _as_float_matrix(data["A_inv"], "A_inv")
    x = _as_float_vector(data["x"], "x")

    if A.shape[0] != A.shape[1]:
        raise ValueError("A must be square (NxN)")

    n = A.shape[0]

    if _A.shape != (n, n):
        raise ValueError(f"A_inv must have shape ({n}, {n})")

    if x.shape != (n,):
        raise ValueError(f"x must have size {n}")

    try:
        i = int(data["i"])
    except (TypeError, ValueError) as e:
        raise ValueError("i must be an integer") from e

    if not (1 <= i <= n):
        raise ValueError(f"Invalid i: must be in range [1; {n}]")

    return A, _A, x, i


def calculate_inverse_matrix(A, _A, x, i):
    n = A.shape[0]

    print("Calculating maxtrix A' by replacing i-th column with vector x")
    A_asterisk = A.copy()
    A_asterisk[:, i-1] = x
    print(f"Matrix A':\n{A_asterisk}\n")

    print("Then we'll calculate vector l = A^-1 * x")
    l = _A @ x
    print(f"Vector l:\n{l.reshape(-1,1)}\n")

    if l[i-1] == 0:
        print(f"l[{i}] = 0, matrix A' is uninversable")
        return None, None, None
    print(f"l[{i}] != 0, matrix A' is inversable")
    
    print("Calculating vector l~, by replacing i-th element with -1")
    l_wave = l.copy()
    l_wave[i-1] = -1
    print(f"Vector l~:\n{l_wave.reshape(-1,1)}\n")

    print("Calculating 'l = (-1 / (l[i])) * l~")
    l_hat = (-1 / l[i-1]) * l_wave
    print(f"Vector 'l:\n{l_hat.reshape(-1,1)}\n")

    print("Calculating matrix Q, i-th colums is 'l")
    Q = np.identity(n)
    Q[:, i-1] = l_hat
    print(f"Matrix Q:\n{Q}\n")

    print("Calculating (A')^-1 = Q * A^-1")
    _A_asterisk = multiply_Q_A_optimized(Q, _A, n, i)
    print(f"Matrix A'^-1:\n{_A_asterisk}\n")
    
    return _A_asterisk, Q, A_asterisk


def main():
    try:
        A, _A, x, i = read_input_json("input.json")

        print(f"Matrix A:\n{A}\n")
        print(f"Inverse matrix A^-1:\n{_A}\n")
        print(f"Vector x:\n{x.reshape(-1,1)}\n")
        print(f"Index i: {i}\n")

        _A_asterisk, Q, A_asterisk = calculate_inverse_matrix(A, _A, x, i)
        if _A_asterisk is not None:
            print(f"Resulting inversed matrix A'^-1 is:\n{_A_asterisk}")
            
    except Exception as e:
        print(f"Error occured: {e}")


if __name__ == "__main__":
    main()