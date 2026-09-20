package com.taubay.daa.algorithms;

import com.taubay.daa.metrics.Metrics;

/**
 * Straight insertion sort over an inclusive range.
 *
 * <p>It is used on its own (as the baseline in the asymptotic table) and as the small-array
 * cutoff inside {@link MergeSort}. On an already sorted range it performs exactly
 * {@code n - 1} comparisons and no moves, which is the Θ(n) best case; on a reversed range it
 * performs Θ(n²) comparisons and moves.</p>
 */
public final class InsertionSort {

    private InsertionSort() {
    }

    public static void sort(int[] a, Metrics metrics) {
        if (a == null) {
            throw new IllegalArgumentException("array must not be null");
        }
        sort(a, 0, a.length - 1, metrics);
    }

    /** Sorts {@code a[lo..hi]} in place; {@code hi} is inclusive. */
    public static void sort(int[] a, int lo, int hi, Metrics metrics) {
        for (int i = lo + 1; i <= hi; i++) {
            int key = a[i];
            int j = i - 1;
            // Every iteration of this loop is one comparison plus one shift.
            while (j >= lo && metrics.greater(a[j], key)) {
                a[j + 1] = a[j];
                metrics.addSwaps(1);
                j--;
            }
            a[j + 1] = key;
        }
    }
}
