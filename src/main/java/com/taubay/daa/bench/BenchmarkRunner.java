package com.taubay.daa.bench;

import com.taubay.daa.algorithms.InsertionSort;
import com.taubay.daa.algorithms.MergeSort;
import com.taubay.daa.algorithms.QuickSelect;
import com.taubay.daa.algorithms.QuickSort;
import com.taubay.daa.metrics.Metrics;
import com.taubay.daa.util.ArrayUtils;
import com.taubay.daa.util.ArrayUtils.InputType;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public final class BenchmarkRunner {
    private static final int REPEATS = 5;
    private static final int INSERTION_SORT_LIMIT = 10_000;
    private static final int[] SIZES = {1_000, 10_000, 100_000, 1_000_000};
    private static final InputType[] INPUTS = {InputType.RANDOM, InputType.SORTED, InputType.DUPLICATES};
    private static final long SEED = 20260920L;

    private record Row(String algorithm, String input, int n, double timeMs,
                       long comparisons, int maxDepth) {
    }

    private interface ArrayAlgorithm {
        void apply(int[] data, long seed, Metrics metrics);
    }

    public static void main(String[] args) throws IOException {
        Path out = Path.of(args.length > 0 ? args[0] : "results.csv");

        System.out.println("DAA Assignment 1 - benchmark");
        System.out.println("JVM: " + System.getProperty("java.vendor") + " " + System.getProperty("java.version")
                + "  cores: " + Runtime.getRuntime().availableProcessors()
                + "  max heap: " + Runtime.getRuntime().maxMemory() / (1024 * 1024) + " MB");
        System.out.println("repeats per case: " + REPEATS + " (median reported)");

        warmUp();

        List<Row> rows = new ArrayList<>();
        for (InputType input : INPUTS) {
            for (int n : SIZES) {
                int[] template = ArrayUtils.generate(input, n, new Random(SEED + n));
                int k = n / 2;

                rows.add(measure("mergesort", input, n, template,
                        (data, seed, metrics) -> MergeSort.sort(data, metrics)));
                rows.add(measure("quicksort", input, n, template,
                        (data, seed, metrics) -> QuickSort.sort(data, seed, metrics)));
                if (n <= INSERTION_SORT_LIMIT) {
                    rows.add(measure("insertionsort", input, n, template,
                            (data, seed, metrics) -> InsertionSort.sort(data, metrics)));
                }
                rows.add(measure("quickselect", input, n, template,
                        (data, seed, metrics) -> QuickSelect.select(data, k, seed, metrics)));
            }
        }

        try (CsvWriter csv = new CsvWriter(out,
                List.of("algorithm", "input", "n", "time_ms", "comparisons", "max_depth"))) {
            for (Row r : rows) {
                csv.writeRow(List.of(r.algorithm(), r.input(), String.valueOf(r.n()),
                        String.format(Locale.US, "%.4f", r.timeMs()), String.valueOf(r.comparisons()),
                        String.valueOf(r.maxDepth())));
            }
        }

        System.out.println();
        System.out.println("wrote " + rows.size() + " rows to " + out.toAbsolutePath());
    }

    private static Row measure(String algorithm, InputType input, int n, int[] template, ArrayAlgorithm algo) {
        double[] times = new double[REPEATS];
        long[] comparisons = new long[REPEATS];
        long[] depths = new long[REPEATS];
        for (int i = 0; i < REPEATS; i++) {
            int[] data = template.clone();
            Metrics metrics = new Metrics();
            metrics.startTimer();
            algo.apply(data, SEED + i, metrics);
            metrics.stopTimer();
            times[i] = metrics.elapsedMillis();
            comparisons[i] = metrics.comparisons();
            depths[i] = metrics.maxDepth();
        }
        Arrays.sort(times);
        Arrays.sort(comparisons);
        Arrays.sort(depths);
        Row row = new Row(algorithm, input.label(), n, times[REPEATS / 2],
                comparisons[REPEATS / 2], (int) depths[REPEATS / 2]);
        System.out.printf(Locale.US, "  %-14s %-11s n=%-8d %9.3f ms  cmp=%-12d depth=%d%n",
                algorithm, row.input(), n, row.timeMs(), row.comparisons(), row.maxDepth());
        return row;
    }

    private static void warmUp() {
        System.out.print("warming up the JVM");
        Random rnd = new Random(SEED);
        for (int round = 0; round < 5; round++) {
            int[] random = ArrayUtils.randomArray(50_000, rnd);
            MergeSort.sort(random.clone(), new Metrics());
            QuickSort.sort(random.clone(), SEED, new Metrics());
            QuickSelect.select(random.clone(), random.length / 2, SEED, new Metrics());
            InsertionSort.sort(ArrayUtils.randomArray(2_000, rnd), new Metrics());
            System.out.print(".");
        }
        System.out.println(" done");
        System.out.println();
    }

    private BenchmarkRunner() {
    }
}
