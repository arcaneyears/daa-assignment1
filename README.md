# DAA Assignment 1 — Fast Sorting & Selection Engine

**Yersaiyn Taubay — group SE2524**

Divide-and-conquer MergeSort, QuickSort and QuickSelect for `int[]`, with a `Metrics` class that
counts comparisons, maximum recursion depth and time, and a benchmark that writes `results.csv`.

Analysis, recurrences, plots and the Θ check: **[REPORT.md](REPORT.md)**.

Repository: https://github.com/arcaneyears/daa-assignment1 (branch `main`, tag `v1.0`)

## Requirements

* JDK 21 or newer
* Maven 3.8+
* Python 3 with matplotlib (only to redraw the plots)

## Build

```bash
mvn clean package
```

## Run the tests

```bash
mvn test
```

27 JUnit 5 tests: MergeSort and QuickSort are compared with `Arrays.sort` on 200 random arrays and
100 duplicate-heavy arrays each; QuickSelect is compared with `sorted[k]` on 150 random arrays;
edge cases (empty / one element / all equal / sorted / reversed) are covered; QuickSort's recursion
depth on a sorted array of 100 000 is checked to be ≤ 2·log₂n; MergeSort is checked to allocate only
one buffer.

## Run the benchmark (one command, writes results.csv)

```bash
mvn -q compile exec:java
```

or, after `mvn package`:

```bash
java -jar target/daa-assignment1-1.0.jar results.csv
```

It runs every algorithm on n = 1 000 / 10 000 / 100 000 / 1 000 000 and the input types
`random`, `sorted` and `duplicates` (InsertionSort only up to 10 000), 5 runs per case, and writes
the median to `results.csv` with the columns `algorithm,input,n,time_ms,comparisons,max_depth`.

## Redraw the plots

```bash
python3 scripts/plot_results.py results.csv docs/plots
```

Writes `docs/plots/time_vs_n.png`, `depth_vs_n.png` and `ratio_vs_n.png`, and prints the
c₁/c₂ values used in the Θ check of the report.

## Project layout

```
src/main/java/com/taubay/daa/
├── algorithms/
│   ├── InsertionSort.java   quadratic baseline + MergeSort cutoff
│   ├── MergeSort.java       one reusable buffer, cutoff 15, linear merge
│   ├── Partition.java       3-way partition shared by QuickSort and QuickSelect
│   ├── QuickSort.java       random pivot, recursion into the smaller side, loop on the larger
│   └── QuickSelect.java     iterative one-side search, constant stack
├── metrics/Metrics.java     comparisons, max depth, nanoTime timer
├── util/ArrayUtils.java     input generators
└── bench/
    ├── BenchmarkRunner.java warm-up, 5 runs per case, median, CSV export
    └── CsvWriter.java
```

## Git workflow

`main` holds only working code and carries the **v1.0** tag. Work was done on
`feature/metrics`, `feature/mergesort`, `feature/quicksort` and `feature/select`,
merged with `--no-ff`.
