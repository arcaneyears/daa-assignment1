package com.taubay.daa.bench;

import com.taubay.daa.algorithms.DeterministicSelect;
import com.taubay.daa.algorithms.InsertionSort;
import com.taubay.daa.algorithms.MergeSort;
import com.taubay.daa.algorithms.QuickSelect;
import com.taubay.daa.algorithms.QuickSort;
import com.taubay.daa.geometry.ClosestPair;
import com.taubay.daa.geometry.Point;
import com.taubay.daa.metrics.Metrics;
import com.taubay.daa.util.ArrayUtils;
import com.taubay.daa.util.ArrayUtils.InputType;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Runs every algorithm on every input shape and size, five times each, and writes the median
 * run to {@code results.csv}.
 *
 * <p>Measurement rules:</p>
 * <ul>
 *   <li>A warm-up phase runs every algorithm on medium-sized inputs before anything is
 *       recorded, so the JIT has compiled the hot loops by the time the timer starts.</li>
 *   <li>Input generation and array cloning happen <b>outside</b> the timed region.</li>
 *   <li>Each case is repeated {@value #REPEATS} times; the row written to the CSV is the run
 *       with the <b>median wall-clock time</b>, together with the counters of that same run,
 *       so time, comparisons and depth always describe one single consistent execution.</li>
 *   <li>Insertion sort is only measured up to {@value #INSERTION_SORT_LIMIT} elements — it is
 *       the quadratic baseline and a million elements would take hours.</li>
 * </ul>
 *
 * <p>Usage: {@code mvn -q exec:java} or {@code java -jar target/daa-assignment1-1.0.jar [out.csv]}</p>
 */
public final class BenchmarkRunner {

    private static final int REPEATS = 5;
    private static final int INSERTION_SORT_LIMIT = 10_000;
    private static final int[] SIZES = {1_000, 10_000, 100_000, 1_000_000};
    private static final InputType[] INPUTS = {
            InputType.RANDOM, InputType.SORTED, InputType.DUPLICATES, InputType.REVERSED
    };
    private static final long SEED = 20260920L;

    /** One measured run: median time plus the counters of that very run. */
    private record Row(String algorithm, String input, int n, double timeMs,
                       long comparisons, int maxDepth, long swaps,
                       long allocations, long recursiveCalls) {
    }

    /** A single measurable execution: it prepares nothing, it just runs and reports. */
    private interface Case {
        void run(Metrics metrics);
    }

    public static void main(String[] args) throws IOException {
        Path out = Path.of(args.length > 0 ? args[0] : "results.csv");
        Path extended = Path.of(out.toString().replaceFirst("\\.csv$", "") + "_extended.csv");

        System.out.println("DAA Assignment 1 - benchmark");
        System.out.println("JVM: " + System.getProperty("java.version")
                + "  cores: " + Runtime.getRuntime().availableProcessors());
        System.out.println("repeats per case: " + REPEATS + " (median reported)");

        warmUp();

        List<Row> rows = new ArrayList<>();
        rows.addAll(sortingBenchmarks());
        rows.addAll(selectionBenchmarks());
        rows.addAll(closestPairBenchmarks());

        try (CsvWriter csv = new CsvWriter(out,
                List.of("algorithm", "input", "n", "time_ms", "comparisons", "max_depth"))) {
            for (Row r : rows) {
                csv.writeRow(List.of(r.algorithm(), r.input(), String.valueOf(r.n()),
                        fmt(r.timeMs()), String.valueOf(r.comparisons()),
                        String.valueOf(r.maxDepth())));
            }
        }

        try (CsvWriter csv = new CsvWriter(extended,
                List.of("algorithm", "input", "n", "time_ms", "comparisons", "max_depth",
                        "swaps", "allocations", "recursive_calls"))) {
            for (Row r : rows) {
                csv.writeRow(List.of(r.algorithm(), r.input(), String.valueOf(r.n()),
                        fmt(r.timeMs()), String.valueOf(r.comparisons()),
                        String.valueOf(r.maxDepth()), String.valueOf(r.swaps()),
                        String.valueOf(r.allocations()), String.valueOf(r.recursiveCalls())));
            }
        }

        System.out.println();
        System.out.println("wrote " + rows.size() + " rows to " + out.toAbsolutePath());
        System.out.println("wrote extended metrics to " + extended.toAbsolutePath());
    }

    // ------------------------------------------------------------------ cases

    private static List<Row> sortingBenchmarks() {
        List<Row> rows = new ArrayList<>();
        for (InputType input : INPUTS) {
            for (int n : SIZES) {
                int[] template = ArrayUtils.generate(input, n, new Random(SEED + n));

                rows.add(measure("mergesort", input.label(), n,
                        () -> template.clone(),
                        (data, metrics) -> MergeSort.sort(data, metrics)));

                rows.add(measure("quicksort", input.label(), n,
                        () -> template.clone(),
                        (data, metrics) -> QuickSort.sort(data, SEED, metrics)));

                if (n <= INSERTION_SORT_LIMIT) {
                    rows.add(measure("insertionsort", input.label(), n,
                            () -> template.clone(),
                            (data, metrics) -> InsertionSort.sort(data, metrics)));
                }
            }
        }
        return rows;
    }

    private static List<Row> selectionBenchmarks() {
        List<Row> rows = new ArrayList<>();
        for (InputType input : INPUTS) {
            for (int n : SIZES) {
                int[] template = ArrayUtils.generate(input, n, new Random(SEED + n));
                int k = n / 2;   // the median is the hardest case for selection

                rows.add(measure("quickselect", input.label(), n,
                        () -> template.clone(),
                        (data, metrics) -> QuickSelect.select(data, k, SEED, metrics)));

                rows.add(measure("deterministicselect", input.label(), n,
                        () -> template.clone(),
                        (data, metrics) -> DeterministicSelect.select(data, k, metrics)));
            }
        }
        return rows;
    }

    private static List<Row> closestPairBenchmarks() {
        List<Row> rows = new ArrayList<>();
        for (int n : new int[]{1_000, 10_000, 100_000, 1_000_000}) {
            Point[] template = randomPoints(n, new Random(SEED + n));
            rows.add(measureGeneric("closestpair", "random", n,
                    metrics -> ClosestPair.closestPair(template, metrics)));
        }
        // The quadratic reference, small sizes only, to show the gap.
        for (int n : new int[]{1_000, 2_000}) {
            Point[] template = randomPoints(n, new Random(SEED + n));
            rows.add(measureGeneric("closestpair_bruteforce", "random", n,
                    metrics -> ClosestPair.bruteForce(template, metrics)));
        }
        return rows;
    }

    private static Point[] randomPoints(int n, Random rnd) {
        Point[] pts = new Point[n];
        for (int i = 0; i < n; i++) {
            pts[i] = new Point(rnd.nextDouble() * 1e6, rnd.nextDouble() * 1e6);
        }
        return pts;
    }

    // -------------------------------------------------------------- machinery

    private interface ArraySupplier {
        int[] get();
    }

    private interface ArrayAlgorithm {
        void apply(int[] data, Metrics metrics);
    }

    /** Measures an int[] algorithm: the array is rebuilt outside the timer before every run. */
    private static Row measure(String algorithm, String input, int n,
                               ArraySupplier supplier, ArrayAlgorithm algo) {
        List<Metrics> runs = new ArrayList<>(REPEATS);
        for (int i = 0; i < REPEATS; i++) {
            int[] data = supplier.get();      // not timed
            Metrics metrics = new Metrics();
            metrics.startTimer();
            algo.apply(data, metrics);
            metrics.stopTimer();
            runs.add(metrics);
        }
        return medianRow(algorithm, input, n, runs);
    }

    /** Measures anything that only needs a Metrics object. */
    private static Row measureGeneric(String algorithm, String input, int n, Case body) {
        List<Metrics> runs = new ArrayList<>(REPEATS);
        for (int i = 0; i < REPEATS; i++) {
            Metrics metrics = new Metrics();
            metrics.startTimer();
            body.run(metrics);
            metrics.stopTimer();
            runs.add(metrics);
        }
        return medianRow(algorithm, input, n, runs);
    }

    private static Row medianRow(String algorithm, String input, int n, List<Metrics> runs) {
        runs.sort(Comparator.comparingDouble(Metrics::elapsedMillis));
        Metrics median = runs.get(runs.size() / 2);
        Row row = new Row(algorithm, input, n, median.elapsedMillis(), median.comparisons(),
                median.maxDepth(), median.swaps(), median.allocations(), median.recursiveCalls());
        System.out.printf(Locale.US, "  %-24s %-11s n=%-8d %8.3f ms  cmp=%-14d depth=%d%n",
                algorithm, input, n, row.timeMs(), row.comparisons(), row.maxDepth());
        return row;
    }

    /**
     * Gives the JIT something to compile before the first measurement, so that the numbers in
     * the CSV are steady-state numbers instead of interpreter numbers.
     */
    private static void warmUp() {
        System.out.print("warming up the JVM");
        Random rnd = new Random(SEED);
        for (int round = 0; round < 5; round++) {
            int n = 50_000;
            int[] random = ArrayUtils.randomArray(n, rnd);
            MergeSort.sort(random.clone(), new Metrics());
            QuickSort.sort(random.clone(), SEED, new Metrics());
            InsertionSort.sort(ArrayUtils.randomArray(2_000, rnd), new Metrics());
            QuickSelect.select(random.clone(), n / 2, SEED, new Metrics());
            DeterministicSelect.select(random.clone(), n / 2, new Metrics());
            ClosestPair.closestPair(randomPoints(20_000, rnd), new Metrics());
            System.out.print(".");
        }
        System.out.println(" done");
        System.out.println();
    }

    private static String fmt(double v) {
        return String.format(Locale.US, "%.4f", v);
    }

    private BenchmarkRunner() {
    }
}
