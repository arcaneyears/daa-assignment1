# DAA Assignment 1 — Fast Sorting & Selection Engine

**Yersaiyn Taubay — group SE2524**
Java 21 · JUnit 5 · Maven · all numbers below produced by `mvn -q exec:java` on this repository.

---

## 1. What was built

| Component | File | Key design decision |
|---|---|---|
| MergeSort | `algorithms/MergeSort.java` | one buffer allocated in the entry point, insertion-sort cutoff at 15, linear merge |
| QuickSort | `algorithms/QuickSort.java` | random pivot, 3-way partition, recursion into the **smaller** side + loop on the larger |
| QuickSelect | `algorithms/QuickSelect.java` | reuses `Partition.threeWay`, keeps one side only, written as a loop → constant stack |
| InsertionSort | `algorithms/InsertionSort.java` | quadratic baseline, also the MergeSort cutoff |
| Median-of-Medians (bonus A) | `algorithms/DeterministicSelect.java` | groups of 5, in-place median gathering, worst-case linear |
| Closest Pair (bonus B) | `geometry/ClosestPair.java` | x-sort once, y-merge on the way up, 7-neighbour strip scan |
| Metrics | `metrics/Metrics.java` | comparisons, swaps, allocations, max depth, logical calls, `System.nanoTime()` — passed as a parameter, never a global |

The partition routine is written **once** (`algorithms/Partition.java`) and shared by QuickSort,
QuickSelect and Median-of-Medians. It returns its two boundaries packed into a `long` rather than
in a fresh `int[]`, because one small allocation per partition call is exactly the memory churn
the assignment asks us to avoid.

---

## 2. Asymptotic bounds

`n` = number of elements. Θ is used where the upper and lower bounds coincide, O/Ω where they do not.

| Algorithm | Best | Average | Worst | Space (aux) |
|---|---|---|---|---|
| **MergeSort** | Θ(n log n) — the recursion tree has the same shape for every input; there is no early-exit test, so even a sorted array performs every merge | Θ(n log n) — same tree, same Θ(n) work per level | Θ(n log n) — a perfectly interleaved input makes every merge run to the end, which is only a constant factor worse | Θ(n) — one buffer, allocated once |
| **QuickSort** (random pivot, 3-way) | Θ(n) — an array of equal keys: a single 3-way partition puts every key in the "= pivot" block and the recursion stops | Θ(n log n) — a random pivot splits in a constant ratio with high probability; expected ≈ 1.39·n·log₂n comparisons | O(n²), Ω(n log n) — quadratic needs every random pivot to be near-extremal, so it is an unlucky *event*, not an input; no fixed input can force it | Θ(log n) stack — guaranteed by recursing into the smaller side |
| **QuickSelect** (random pivot) | Θ(n) — the first partition already lands k inside the "= pivot" block | Θ(n) — expected cost 2n + o(n): the surviving side shrinks geometrically, so Σ n(3/4)ⁱ = Θ(n) | O(n²) — repeated near-extremal pivots peel off one element at a time | Θ(1) — iterative, no stack growth at all |
| **InsertionSort** | Θ(n) — an already sorted array: every key stops after one comparison (n−1 in total) | Θ(n²) — a random array has ≈ n²/4 inversions and each one costs one shift | Θ(n²) — a reversed array has n(n−1)/2 inversions, every key walks the whole prefix | Θ(1) |
| **Median-of-Medians** (bonus A) | Θ(n) | Θ(n) | Θ(n) — the pivot is always ≥ 3n/10 and ≤ 7n/10 in rank, so no input can make it quadratic | Θ(log n) stack |
| **Closest Pair** (bonus B) | Θ(n log n) | Θ(n log n) | Θ(n log n) — the strip scan is Θ(n) per level regardless of the point layout | Θ(n) |

Measured confirmation of the interesting cells: InsertionSort on 10 000 elements needs
**9 999** comparisons when sorted and **49 995 000** when reversed (exactly n−1 and n(n−1)/2);
QuickSort on 1 000 000 equal-ish keys (values 0–9) needs **3 699 921** comparisons at depth **2**,
versus **26 207 771** at depth 13 on random keys.

---

## 3. Recurrences and the Master Theorem

The Master Theorem is applied in the form T(n) = a·T(n/b) + f(n), with n^(log_b a) as the watershed.

### MergeSort
* T(n) = **2**·T(n/2) + **Θ(n)** → a = 2, b = 2, f(n) = Θ(n)
* n^(log₂2) = n¹ = n, and f(n) = Θ(n) = Θ(n^(log_b a) · log⁰n)
* **Case 2** (f matches the watershed) → **T(n) = Θ(n log n)**
* The cutoff does not change this: it replaces the bottom log₂15 ≈ 3.9 levels of the tree with
  Θ(n) insertion-sort work, which is absorbed into the constant. Measured depth at n = 10⁶ is
  **18 ≈ log₂(10⁶/15) + 1**, exactly as predicted.

### QuickSort (balanced split assumed)
* T(n) = **2**·T(n/2) + **Θ(n)** → a = 2, b = 2, f(n) = Θ(n) → **Case 2** → **Θ(n log n)**
* *Why a random pivot gives O(n log n) on average.* A pivot is "good" if its rank lies in the
  middle half of the range, which happens with probability 1/2, and a good pivot leaves at most
  3/4 of the elements. So after an expected two partitions the problem size shrinks by at least a
  factor 4/3, which means the expected recursion depth is O(log n) and each level costs O(n) —
  total O(n log n) in expectation. Because the pivot is drawn from the array *at random* and not
  from a fixed position, this argument holds for **every** input: a sorted array is no longer a
  special case, which is precisely what the measurement shows (1.15–1.26 vs 1.27–1.38
  comparisons per n·log₂n).

### QuickSelect (balanced split assumed)
* T(n) = **1**·T(n/2) + **Θ(n)** → a = 1, b = 2, f(n) = Θ(n)
* n^(log₂1) = n⁰ = 1, so f(n) = Θ(n) grows **polynomially faster** than the watershed
* Regularity holds: a·f(n/b) = n/2 ≤ c·f(n) with c = 1/2 < 1
* **Case 3** (a different case from the two sorts above) → **T(n) = Θ(n)**
* Intuition: only one side survives, so the per-level work is n, n/2, n/4, … and the geometric
  series sums to 2n rather than n log n.

### Median-of-Medians (bonus A)
* T(n) ≤ T(n/5) + T(7n/10) + Θ(n) — two subproblems of **different** sizes, so the Master
  Theorem does not apply. Since 1/5 + 7/10 = 9/10 < 1, the work per level decays geometrically;
  substitution with T(n) ≤ c·n gives c·n/5 + 7c·n/10 + d·n ≤ c·n for c ≥ 10d, so **T(n) = Θ(n)**.

### Closest Pair (bonus B)
* T(n) = **2**·T(n/2) + **Θ(n)** (merge by y + strip scan) → **Case 2** → **Θ(n log n)**.

### InsertionSort
* T(n) = T(n−1) + Θ(n) — subtract-and-conquer, outside the Master Theorem; unrolling gives
  Σ i = **Θ(n²)**.

---

## 4. Experimental setup

* JVM 21.0.10 (OpenJDK, Linux container, 2 cores), `-Xmx4g -Xss16m`.
* n ∈ {1 000, 10 000, 100 000, 1 000 000}; inputs: `random`, `sorted`, `duplicates` (values 0–9)
  and `reversed` as an extra shape.
* A warm-up phase runs every algorithm five times on 50 000-element inputs before anything is
  recorded, so the JIT has compiled the hot loops.
* Every case runs **5 times**; the row written to `results.csv` is the run with the **median
  wall-clock time**, together with the counters of that same run, so time, comparisons and depth
  always describe one consistent execution. Array generation and cloning happen outside the timer.
* InsertionSort is measured only up to n = 10 000 — it is the quadratic baseline.

Reproduce with one command: `mvn -q exec:java` (writes `results.csv` and `results_extended.csv`),
then `python3 scripts/plot_results.py` for the figures.

---

## 5. Plots

**Time vs n** (log–log, one panel per input shape) — `docs/plots/time_vs_n.png`

![Time vs n](docs/plots/time_vs_n.png)

**Max recursion depth vs n**, with log₂n and the 2·log₂n limit drawn in — `docs/plots/depth_vs_n.png`

![Depth vs n](docs/plots/depth_vs_n.png)

**Ratio vs n** — comparisons / (n·log₂n) for the sorts, comparisons / n for selection — `docs/plots/ratio_vs_n.png`

![Ratio vs n](docs/plots/ratio_vs_n.png)

Bonus figures: `docs/plots/select_quick_vs_deterministic.png` and `docs/plots/closest_pair.png`.

---

## 6. Θ check: c₁, c₂ and n₀

Definition: f(n) = Θ(g(n)) iff there are c₁, c₂ > 0 and n₀ such that c₁·g(n) ≤ f(n) ≤ c₂·g(n) for
all n ≥ n₀. Taking f = measured comparisons and reading c₁/c₂ off the ratio plot for n ≥ **n₀ = 10⁴**:

| Algorithm | Input | g(n) | c₁ | c₂ | c₂/c₁ |
|---|---|---|---|---|---|
| MergeSort | random | n·log₂n | 0.96 | 1.00 | 1.04 |
| MergeSort | sorted | n·log₂n | 0.45 | 0.46 | 1.02 |
| MergeSort | duplicates | n·log₂n | 0.92 | 0.95 | 1.04 |
| MergeSort | reversed | n·log₂n | 0.71 | 0.73 | 1.03 |
| QuickSort | random | n·log₂n | 1.27 | 1.38 | 1.08 |
| QuickSort | sorted | n·log₂n | 1.15 | 1.26 | 1.10 |
| QuickSort | reversed | n·log₂n | 1.19 | 1.25 | 1.05 |
| QuickSort | duplicates | n·log₂n | 0.19 | 0.28 | 1.50 |
| QuickSelect | random | n | 2.55 | 5.24 | 2.05 |
| QuickSelect | sorted | n | 2.01 | 2.68 | 1.33 |
| Median-of-Medians | random | n | 8.33 | 8.53 | 1.02 |
| Median-of-Medians | duplicates | n | 3.09 | 3.10 | 1.00 |
| Closest Pair | random points | n·log₂n | 1.00 | 1.01 | 1.01 |
| InsertionSort | random | n² | 0.25 | 0.25 | 1.02 (n₀ = 10³) |

**Reading.** For MergeSort, Median-of-Medians, Closest Pair and InsertionSort the ratio is flat to
within 1–4 %, so the Θ bound is confirmed with very tight constants. QuickSort's ratio drifts by
≤ 10 % and settles around 1.3 — close to the theoretical 2·ln2 ≈ 1.386 for a randomized pivot —
which also confirms Θ(n log n). Two entries deserve a comment:

* **QuickSort on duplicates** falls *below* the envelope (0.19 → and still decreasing). That is
  not noise: with only 10 distinct values the 3-way partition finishes after ~3 levels, so the true
  cost there is Θ(n·d) with d = number of distinct keys, not Θ(n log n). Dividing by n·log₂n
  therefore gives a ratio that decays like 1/log n. The correct g(n) for that input is n.
* **QuickSelect** shows a 2× spread because a single run is recorded and its cost is a *random
  variable* — the pivots differ from run to run. Averaged over many runs it converges to ≈ 3n,
  which is the expected value; the spread is a property of the algorithm, not of the measurement.

---

## 7. Discussion

The measurements agree with the theory wherever the theory is tight, and the places where they
differ are all explainable. MergeSort's comparison count sits at 0.96–1.00·n·log₂n on random input,
which is within 4 % of the information-theoretic n·log₂n − 1.44n ideal, and its depth is exactly
log₂(n/15) + 1 as the cutoff predicts. On sorted input MergeSort still performs every merge
(Θ(n log n) as promised) but only half the comparisons, because one half always exhausts first and
the tail is copied without any comparison at all — the shape of the recursion is input-independent,
the constant is not. QuickSort does ~30 % more comparisons than MergeSort yet is *faster* at
n = 10⁶ (155 ms vs 168 ms), because it works in place: MergeSort moves 19.3 million elements
through the buffer and pays for that memory traffic and the extra cache pressure, while QuickSort's
partition is a single sequential sweep. The opposite happens on duplicate-heavy data, where
QuickSort is 4× faster (19 ms vs 78 ms) — the 3-way partition retires the whole equal block at
once and reaches depth 2. Deviations from the smooth curve come from the usual JVM sources: the
first runs of each case are still being JIT-compiled (which is why a warm-up phase and a median
of five, not a mean, are used), the garbage collector interferes most in Closest Pair where a
million `Point` objects are sorted by an object comparator (the 1 M point run costs 1.6 s, far
more than the comparison count alone would suggest), and CPU cache effects make the same
asymptotic work up to an order of magnitude cheaper at n = 10³ than at n = 10⁶ per element.
Finally, the cutoff of 15 in MergeSort is a pure constant-factor optimisation: the recursion
tree loses its bottom four levels, which removes ~94 % of the recursive calls at n = 10⁶
(164 991 calls instead of ~2 million) while leaving the Θ(n log n) bound untouched.

---

## 8. Bonus results

**Task A — Median-of-Medians vs QuickSelect.** Both return the same element (verified against
`Arrays.sort` on 150 random arrays each, and against each other on every input shape). At
n = 10⁶ random data, QuickSelect needs **3.6 comparisons per element and 17.7 ms**, the
deterministic selector **8.5 per element and 37.5 ms** — a ~2.2× penalty. The reason is
structural: Median-of-Medians pays for the group sorting (n/5 insertion sorts of 5 elements) *and*
a second recursive call just to compute the pivot, so its recurrence has the extra T(n/5) term.
What it buys is the guarantee: its ratio is flat at 8.3–8.5 on every input, including sorted
(6.8–7.2), while QuickSelect's is a random variable with a 2× spread and a Θ(n²) tail that is
only improbable, not impossible. Use QuickSelect by default, Median-of-Medians when a worst-case
bound must be contractual.

**Task B — Closest Pair.** The divide-and-conquer version matches the brute-force O(n²) result to
within 10⁻⁹ on 100 random sets plus 5 sets of exactly 2 000 points, and on degenerate layouts
(duplicate points, collinear points, a 20×20 integer grid). Its distance evaluations divide by
n·log₂n to 0.998–1.011 — a textbook Θ confirmation. At n = 2 000 it is already ~2× faster than
brute force despite the sorting overhead, and the gap grows as n²/(n log n).

---

## 9. Conclusion

All three required components meet their design targets: MergeSort allocates exactly one buffer
(asserted in the test suite for n = 16, 10³ and 10⁵), QuickSort's recursion depth never exceeded
**14** at n = 10⁶ — well inside the 2·log₂n = 39.9 limit and verified on 20 seeded runs over a
sorted array of 100 000 — and QuickSelect runs with a constant stack and linear expected cost.
The empirical ratios confirm the predicted Θ classes with constants stable to a few percent, and
every disagreement with the idealised model traces back to a concrete, identifiable cause.
