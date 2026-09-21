package com.taubay.daa.algorithms;

import com.taubay.daa.metrics.Metrics;

import java.util.Random;

public final class QuickSort {
    private QuickSort() {
    }

    public static void sort(int[] a, Metrics metrics) {
        sort(a, new Random(), metrics);
    }

    public static void sort(int[] a, long seed, Metrics metrics) {
        sort(a, new Random(seed), metrics);
    }

    public static void sort(int[] a, Random rnd, Metrics metrics) {
        if (a == null) {
            throw new IllegalArgumentException("array must not be null");
        }
        if (a.length < 2) {
            return;
        }
        quickSort(a, 0, a.length - 1, rnd, metrics);
    }

    private static void quickSort(int[] a, int lo, int hi, Random rnd, Metrics metrics) {
        metrics.enterRecursion();
        try {
            while (lo < hi) {
                long bounds = Partition.threeWayRandom(a, lo, hi, rnd, metrics);
                int lt = Partition.lt(bounds);
                int gt = Partition.gt(bounds);

                int leftSize = lt - lo;
                int rightSize = hi - gt;

                if (leftSize < rightSize) {
                    if (leftSize > 1) {
                        quickSort(a, lo, lt - 1, rnd, metrics);
                    }
                    lo = gt + 1;
                } else {
                    if (rightSize > 1) {
                        quickSort(a, gt + 1, hi, rnd, metrics);
                    }
                    hi = lt - 1;
                }
            }
        } finally {
            metrics.exitRecursion();
        }
    }
}
