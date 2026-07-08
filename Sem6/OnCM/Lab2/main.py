import json
import sys
from pathlib import Path

import numpy as np


def load_input_json(path="input.json"):
    """Load problem data from JSON. Path can be overridden via argv[1]."""
    path = Path(path)
    with path.open(encoding="utf-8") as f:
        data = json.load(f)

    n = int(data["n"])
    m = int(data["m"])

    c = np.array(data["c"], dtype=float)
    if c.shape != (n,):
        raise ValueError(f"c must have length {n}, got {len(c)}")

    d = bool(data.get("maximize", False))

    A = np.array(data["A"], dtype=float)
    if A.shape != (m, n):
        raise ValueError(f"A must be shape ({m}, {n}), got {A.shape}")

    b = np.array(data["b"], dtype=float)
    if b.shape != (m,):
        raise ValueError(f"b must have length {m}, got {len(b)}")

    r = np.array(data["r"], dtype=float)
    if r.shape != (m,):
        raise ValueError(f"r must have length {m}, got {len(r)}")

    s = np.array(data["s"], dtype=float)
    if s.shape != (n,):
        raise ValueError(f"s must have length {n}, got {len(s)}")

    t = bool(data.get("canonical", False))

    return n, m, c, d, A, b, r, s, t

def numberInput(type=float):
    while (True):
        try:
            element = type(input())
            return element
        except ValueError:
            print(f"Invalid input. Enter valid {type} number")

def vectorInput(n, signs=False):
    vec = []
    while True:
        user_input = input(f"Enter row: ").strip().split()
        if len(user_input) != n:
            print(f"{n} elements must be present. Try again...\n")
        else:
            try:
                if signs:
                    for num in user_input:
                        val = float(num)
                        if val > 0:
                            vec.append(1)
                        elif val < 0:
                            vec.append(-1)
                        else:
                            vec.append(0)
                    break
                else:
                    for num in user_input:
                        vec.append(float(num))
                    break
            except ValueError:
                    print("Error: enter valid number values")
                    vec = []

    return np.array(vec, float)

def matrixInput(n, m):
    matrix = []
    for i in range(1, m+1):
        print(f"Enter matrix's #{i} row")
        row = vectorInput(n)
        matrix.append(row)
    return np.array(matrix, float)


def linear_to_normal(c, d, A, b, r, s):
    print("1. Checking min or max, if min then c *= -1")
    if not d:
        c = -c

    A_new = A.copy()
    b_new = b.copy()
    c_new = c.copy()
    r_new = r.copy()
    s_new = s.copy()

    print("2. For each r[i] = \"=\" splitting in two")
    new_rows = []
    new_b = []
    new_r = []
    
    for i in range(len(r_new)):
        if r_new[i] == 0:
            new_rows.append(A_new[i])
            new_b.append(b_new[i])
            new_r.append(-1)

            new_rows.append(A_new[i])
            new_b.append(b_new[i])
            new_r.append(1)
        else:
            new_rows.append(A_new[i])
            new_b.append(b_new[i])
            new_r.append(r_new[i])
    
    A_new = np.array(new_rows)
    b_new = np.array(new_b)
    r_new = np.array(new_r)

    print("3. For each r[i] = \">=\" inverting to \"<=\"")
    for i in range(len(r_new)):
        if r_new[i] == 1:
            A_new[i] *= -1
            b_new[i] *= -1
            r_new[i] = -1

    print("4. For each s[i] = \"<=\" inverting")
    for i in range(len(s_new)):
        if s_new[i] == -1:
            A_new[:, i] *= -1
            c_new[i] *= -1
            s_new[i] = 1

    print("5. For each s[i] = \"><\" splitting in two")
    final_cols = []
    final_c = []
    
    for i in range(len(s_new)):
        if s_new[i] == 1:
            final_cols.append(A_new[:, i:i+1])
            final_c.append(c_new[i])
        else:
            final_cols.append(A_new[:, i:i+1])
            final_c.append(c_new[i])
            final_cols.append(-A_new[:, i:i+1])
            final_c.append(-c_new[i])

    if final_cols:
        A_new = np.hstack(final_cols)
        c_new = np.array(final_c)

    return c_new, A_new, b_new


def linear_to_canonical(c, d, A, b, r, s):
    print("1. Checking min or max, if min then c *= -1")
    if not d:
        c = -c

    print(f"Vector c:\n{c}\n")

    A_new = A.copy()
    b_new = b.copy()
    c_new = c.copy()
    r_new = r.copy()
    s_new = s.copy()

    print("2-3. For each not \"=\" add columns and append to s")
    extra_cols = []
    extra_c = []
    extra_s = []

    for i in range(len(r_new)):
        if r_new[i] == -1:
            col = np.zeros((A.shape[0], 1))
            col[i, 0] = 1
            extra_cols.append(col)
            extra_c.append(0)
            extra_s.append(1)
            r_new[i] = 0
        elif r_new[i] == 1:
            col = np.zeros((A.shape[0], 1))
            col[i, 0] = -1
            extra_cols.append(col)
            extra_c.append(0)
            extra_s.append(1)
            r_new[i] = 0

    if extra_cols:
        A_new = np.hstack((A_new, np.hstack(extra_cols)))
        c_new = np.append(c_new, extra_c)
        s_new = np.append(s_new, extra_s)

    print("4. For each s[i] = \"<=\" inversing")
    for i in range(len(s_new)):
        if s_new[i] == -1:
            A_new[:, i] *= -1
            c_new[i] *= -1
            s_new[i] = 1

    print("5. For each s[i] = \"><\" splitting in two")
    final_cols = []
    final_c = []
    
    for i in range(len(s_new)):
        if s_new[i] == 1:
            final_cols.append(A_new[:, i:i+1])
            final_c.append(c_new[i])
        else:
            final_cols.append(A_new[:, i:i+1])
            final_c.append(c_new[i])
            final_cols.append(-A_new[:, i:i+1])
            final_c.append(-c_new[i])

    if final_cols:
        A_new = np.hstack(final_cols)
        c_new = np.array(final_c)

    return c_new, A_new, b_new


if __name__ == "__main__":
    json_path = Path(sys.argv[1]) if len(sys.argv) > 1 else Path("input.json")
    n, m, c, d, A, b, r, s, t = load_input_json(json_path)
    
    if not t:
        c, A, b = linear_to_normal(c, d, A, b, r, s)
    else:
        c, A, b = linear_to_canonical(c, d, A, b, r, s)

    print(f"Vector c:\n{c}\n")
    print(f"Matrix A:\n{A}\n")
    print(f"Vector b:\n{b}\n")