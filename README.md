# DAA Assignment 1 — Fast Sorting & Selection Engine

**Yersaiyn Taubay — group SE2524**

Divide-and-conquer core for a data-analytics platform: MergeSort, QuickSort and QuickSelect that
do not waste memory, cannot overflow the stack and are instrumented end to end, plus both bonus
tasks (deterministic Median-of-Medians select and Closest Pair of Points).

Full analysis, recurrences, plots and the Θ check: **[REPORT.md](REPORT.md)**.

## Requirements

* JDK 21 or newer
* Maven 3.8+
* Python 3 with matplotlib (only to regenerate the plots)

## Build

```bash
mvn clean package
```

## Run the tests

```bash
mvn test
```

41 JUnit 5 tests: every sort is compared with `Arrays.sort` on 200+ random arrays, selection is
compared with `sorted[k]` on 150+ random arrays, edge cases (empty / one element / all equal /
sorted / reversed) are covered, QuickSort's recursion depth on a sorted array of 100 000 is
asserted to stay under 2·log₂n on 20 seeded runs, MergeSort is asserted to allocate exactly one
buffer, and Closest Pair is validated against the brute-force O(n²) solution for n ≤ 2 000.

## Run the benchmark (one command, writes results.csv)

```bash
mvn -q exec:java
```

or, after `mvn package`:

```bash
java -Xmx4g -Xss16m -jar target/daa-assignment1-1.0.jar results.csv
```

It runs every algorithm on n = 1 000 / 10 000 / 100 000 / 1 000 000 over four input shapes
(`random`, `sorted`, `duplicates`, `reversed`), five repetitions each, and writes the median run to

* `results.csv` — `algorithm,input,n,time_ms,comparisons,max_depth`
* `results_extended.csv` — the same plus `swaps,allocations,recursive_calls`

## Regenerate the plots

```bash
python3 scripts/plot_results.py results.csv docs/plots
```

Produces `docs/plots/time_vs_n.png`, `depth_vs_n.png`, `ratio_vs_n.png`, the two bonus figures,
and `docs/theta_check.md` with the empirical c₁/c₂ constants.

## Project layout

```
src/main/java/com/taubay/daa/
├── algorithms/
│   ├── InsertionSort.java        quadratic baseline + MergeSort cutoff
│   ├── MergeSort.java            one reusable buffer, cutoff 15, linear merge
│   ├── Partition.java            3-way Dutch-flag partition (shared, allocation-free)
│   ├── QuickSort.java            random pivot, smaller-side recursion, loop on the larger side
│   ├── QuickSelect.java          iterative one-side descent, constant stack
│   └── DeterministicSelect.java  bonus A — median of medians, worst-case O(n)
├── geometry/
│   ├── Point.java
│   └── ClosestPair.java          bonus B — O(n log n) + brute-force reference
├── metrics/Metrics.java          comparisons, swaps, allocations, depth, nanoTime
├── util/ArrayUtils.java          input generators
└── bench/
    ├── BenchmarkRunner.java      warm-up, median of 5, CSV export
    └── CsvWriter.java
```

## Design guarantees

| Guarantee | How it is enforced | Where it is checked |
|---|---|---|
| MergeSort allocates one buffer | allocated in the public entry point and passed down | `MergeSortTest#allocatesSingleBuffer` |
| No `StackOverflowError` in QuickSort | recurse into the smaller side, loop on the larger → depth ≤ ⌊log₂n⌋+1 | `QuickSortTest#depthStaysBoundedOnSortedInput`, `#noStackOverflowOnLargeSortedInput` |
| Duplicates stay linear | 3-way partition retires the equal block | `QuickSortTest#duplicatesCostLinearWork` |
| QuickSelect never nests frames | tail-call eliminated into a `while` loop | `QuickSelectTest#linearExpectedWorkWithConstantStack` |
| Invalid `k` is rejected | `IllegalArgumentException` with the valid range in the message | `QuickSelectTest#invalidInputRejected` |
| No global counters | a `Metrics` instance is a parameter of every algorithm | `MetricsTest` |

## Git workflow

`main` holds only working code and carries the **v1.0** tag. Work was done on
`feature/metrics`, `feature/mergesort`, `feature/quicksort`, `feature/select` and
`feature/closest-pair`, merged with `--no-ff` so the branch structure stays visible in
`git log --graph`.
