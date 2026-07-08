import json
from pathlib import Path

import numpy as np


def load_problem_from_json(path):
    path = Path(path)
    with path.open(encoding="utf-8") as f:
        data = json.load(f)

    m = int(data["m"])
    n = int(data["n"])
    if m <= 0 or n <= 0:
        raise ValueError("m and n must be positive integers")

    c = np.array(data["c"], dtype=float)
    A = np.array(data["A"], dtype=float)
    b = np.array(data["b"], dtype=float)
    B = [int(x) for x in data["B"]]

    if c.shape != (n,):
        raise ValueError(f"c must have length n={n}, got {c.shape}")
    if A.shape != (m, n):
        raise ValueError(f"A must be shape ({m}, {n}), got {A.shape}")
    if b.shape != (m,):
        raise ValueError(f"b must have length m={m}, got {b.shape}")
    if len(B) != m:
        raise ValueError(f"B must have length m={m}, got {len(B)}")
    for idx in B:
        if idx < 1 or idx > n:
            raise ValueError(f"Basis index {idx} out of range [1, {n}]")

    return c, A, b, B


def multiply_Q_A_optimized(Q, _A, n, i):
    result = _A.copy()
    col_idx = i - 1
    l_hat = Q[:, col_idx]
    row_i = _A[col_idx, :]
    for r in range(n):
        factor = l_hat[r]
        if r == col_idx:
            factor -= 1
        if factor != 0:
            result[r, :] += factor * row_i
    return result

def calculate_inverse_matrix(A, _A, x, i):
    n = A.shape[0]
    A_asterisk = A.copy()
    A_asterisk[:, i-1] = x
    l = _A @ x
    if l[i-1] == 0:
        if np.linalg.matrix_rank(A_asterisk) < n:
            return A_asterisk, None, A_asterisk
        else:
            return None, None, None
    l_wave = l.copy()
    l_wave[i-1] = -1
    l_hat = (-1 / l[i-1]) * l_wave
    Q = np.identity(n)
    Q[:, i-1] = l_hat
    _A_asterisk = multiply_Q_A_optimized(Q, _A, n, i)
    return _A_asterisk, Q, A_asterisk

def dual_simplex_method(c, A, b, B_initial):
    m, n = A.shape
    B = [index - 1 for index in B_initial]
    method_iteration = 1

    #1 (First iter)
    AB = A[:,B]
    AB_inv = []
    try:
        AB_inv = np.linalg.inv(AB)
    except np.linalg.LinAlgError:
        print("Basis matrix AB cannot be inverted")
        return None
    
    while True:
        print(f"#ITERATION {method_iteration}")
        #1 (Non-first iter)
        if method_iteration != 1:
            AB_inv, _, AB = calculate_inverse_matrix(AB, AB_inv, A[:,j_0], k_index + 1)

        ###
        print(f"AB {AB}")
        print(f"AB Inverted {AB_inv}")
        ###

        #2
        cB = c[B]

        #3
        y = AB_inv.T @ cB

        ###
        print("cB =", cB)
        print("y^T = cB^T * AB_inv =", y)
        ###

        #4
        kappa_B = AB_inv @ b
        kappa = np.zeros(n)
        for i, bi in enumerate(B):
            kappa[bi] = kappa_B[i]

        ###
        print("kappa_B = AB_inv * b =", kappa_B)
        print("Pseudo-plan:\n", kappa)
        ###

        #5
        if np.all(kappa >= 0):
            ###
            print("Kappas are positive numbers, optimal plan has been found")
            ###

            return kappa
        else:
            #6
            negative_kappas = [index for index in range(m) if kappa_B[index] < 0]
            if not negative_kappas:
                ###
                print("Error: negative component wasn't found")
                ###

                return None
            
            k_index = negative_kappas[0]
            j_k = B[k_index]

            #7
            delta_y = AB_inv[k_index,:]

            ###
            print("delta_y =", delta_y)
            ###

            j_indicies = [j for j in range(n) if j not in B]
            nyu = {}
            for j in j_indicies:
                nyu[j] = delta_y @ A[:,j]

                ###
                print(f"nyu[{j + 1}] = delta_y^T * A[:, {j + 1}] = {nyu[j]}")
                ###

            #8
            if all(nyu[j] >= 0 for j in nyu):
                ###
                print("Task doesnt have a valid plan")
                ###

                return None
            
            #9
            sigma = {}
            for j in j_indicies:
                if nyu[j] < 0:
                    sigma[j] = (c[j] - (A[:,j] @ y)) / nyu[j]

                    ###
                    print(f"sigma[{j + 1}] = (c[{j + 1}] - A[:, {j + 1}]^T * y) / nyu[{j + 1}] = {sigma[j]}")
                    ###

            #10
            j_0, sigma_0 = min(sigma.items(), key=lambda item: item[1])

            ###
            print(f"j_0 with minimal sigma: sigma_0 = {sigma_0} when j_0 = {j_0 + 1}")
            ###

            #11
            B[k_index] = j_0

            ###
            print(f"Modifying basis: changing index {j_k + 1} to {j_0 + 1}.")
            print(f"New basis B:\n{[i + 1 for i in B]}")
            ###

            method_iteration += 1


if __name__ == "__main__":
    json_path = Path(__file__).resolve().parent / "input.json"
    c, A, b, B = load_problem_from_json(json_path)

    print(f"Vector c:\n{c}\n")
    print(f"Matrix A:\n{A}\n")
    print(f"Vector b:\n{b}\n")
    print(f"Plan B:\n{B}\n")

    results = dual_simplex_method(c, A, b , B)
    if results is not None:
        print(f"Solution plan:\n{results}\n")
    else:
        print("No solution")
