package com.taubay.daa.algorithms;

import com.taubay.daa.metrics.Metrics;

public final class MergeSort {
    public static final int CUTOFF = 15;

    private MergeSort() {
    }

    public static void sort(int[] a, Metrics metrics) {
        sort(a, CUTOFF, metrics);
    }

    public static void sort(int[] a, int cutoff, Metrics metrics) {
        if (a == null) {
            throw new IllegalArgumentException("array must not be null");
        }
        if (a.length < 2) {
            return;
        }

        int[] buffer = new int[a.length];
        metrics.recordAllocation(a.length);
        sortRange(a, buffer, 0, a.length - 1, Math.max(1, cutoff), metrics);
    }

    private static void sortRange(int[] a, int[] buffer, int lo, int hi, int cutoff, Metrics metrics) {
        metrics.enterRecursion();
        try {
            if (hi - lo + 1 <= cutoff) {
                InsertionSort.sort(a, lo, hi, metrics);
                return;
            }
            int mid = lo + ((hi - lo) >>> 1);
            sortRange(a, buffer, lo, mid, cutoff, metrics);
            sortRange(a, buffer, mid + 1, hi, cutoff, metrics);
            merge(a, buffer, lo, mid, hi, metrics);
        } finally {
            metrics.exitRecursion();
        }
    }

    private static void merge(int[] a, int[] buffer, int lo, int mid, int hi, Metrics metrics) {
        System.arraycopy(a, lo, buffer, lo, hi - lo + 1);
        metrics.addSwaps(hi - lo + 1);

        int i = lo;
        int j = mid + 1;
        int k = lo;

        while (i <= mid && j <= hi) {
            if (metrics.lessOrEqual(buffer[i], buffer[j])) {
                a[k++] = buffer[i++];
            } else {
                a[k++] = buffer[j++];
            }
        }

        while (i <= mid) {
            a[k++] = buffer[i++];
        }
        while (j <= hi) {
            a[k++] = buffer[j++];
        }
    }
}
