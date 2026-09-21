package com.taubay.daa.algorithms;

import com.taubay.daa.metrics.Metrics;

public final class DeterministicSelect {
    private static final int GROUP = 5;

    private DeterministicSelect() {
    }

    public static int select(int[] a, int k, Metrics metrics) {
        if (a == null) {
            throw new IllegalArgumentException("array must not be null");
        }
        if (a.length == 0) {
            throw new IllegalArgumentException("cannot select from an empty array");
        }
        if (k < 0 || k >= a.length) {
            throw new IllegalArgumentException(
                    "k must be in [0, " + (a.length - 1) + "] but was " + k);
        }
        return selectRange(a, 0, a.length - 1, k, metrics);
    }

    public static int selectOnCopy(int[] a, int k, Metrics metrics) {
        if (a == null) {
            throw new IllegalArgumentException("array must not be null");
        }
        return select(a.clone(), k, metrics);
    }

    private static int selectRange(int[] a, int lo, int hi, int k, Metrics metrics) {
        metrics.enterRecursion();
        try {
            while (true) {
                if (hi - lo + 1 <= GROUP) {
                    InsertionSort.sort(a, lo, hi, metrics);
                    return a[k];
                }

                int pivotValue = medianOfMedians(a, lo, hi, metrics);
                long bounds = Partition.threeWay(a, lo, hi, pivotValue, metrics);
                int lt = Partition.lt(bounds);
                int gt = Partition.gt(bounds);

                if (k < lt) {
                    hi = lt - 1;
                } else if (k > gt) {
                    lo = gt + 1;
                } else {
                    return a[k];
                }
            }
        } finally {
            metrics.exitRecursion();
        }
    }

    private static int medianOfMedians(int[] a, int lo, int hi, Metrics metrics) {
        int n = hi - lo + 1;
        int groups = 0;
        for (int start = lo; start <= hi; start += GROUP) {
            int end = Math.min(start + GROUP - 1, hi);
            InsertionSort.sort(a, start, end, metrics);
            int median = start + ((end - start) >>> 1);

            metrics.swap(a, lo + groups, median);
            groups++;
        }
        int medianIndex = lo + (groups - 1) / 2;
        return selectRange(a, lo, lo + groups - 1, medianIndex, metrics);
    }
}
