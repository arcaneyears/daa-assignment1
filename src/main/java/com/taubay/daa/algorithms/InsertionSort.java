package com.taubay.daa.algorithms;

import com.taubay.daa.metrics.Metrics;

public final class InsertionSort {
    private InsertionSort() {
    }

    public static void sort(int[] a, Metrics metrics) {
        if (a == null) {
            throw new IllegalArgumentException("array must not be null");
        }
        sort(a, 0, a.length - 1, metrics);
    }

    public static void sort(int[] a, int lo, int hi, Metrics metrics) {
        for (int i = lo + 1; i <= hi; i++) {
            int key = a[i];
            int j = i - 1;

            while (j >= lo && metrics.greater(a[j], key)) {
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = key;
        }
    }
}
