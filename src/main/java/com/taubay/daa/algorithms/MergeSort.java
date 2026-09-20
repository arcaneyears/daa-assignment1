package com.taubay.daa.algorithms;

import com.taubay.daa.metrics.Metrics;

/**
 * Top-down MergeSort with the three properties required by the assignment.
 *
 * <ol>
 *   <li><b>One reusable buffer.</b> The single auxiliary {@code int[]} is allocated in the
 *       public entry point and passed down the recursion; no recursive call ever executes
 *       {@code new int[...]}. {@link Metrics#allocations()} is therefore exactly 1 for any
 *       array of two or more elements.</li>
 *   <li><b>Small-array cutoff.</b> A range of {@value #CUTOFF} elements or fewer is finished
 *       with {@link InsertionSort}, which avoids the call overhead where the asymptotics do
 *       not matter yet.</li>
 *   <li><b>Linear merge.</b> Merging two sorted halves of total length {@code n} touches every
 *       element a constant number of times, so it is Θ(n).</li>
 * </ol>
 *
 * <p>Recurrence: T(n) = 2·T(n/2) + Θ(n) → Master Theorem case 2 → Θ(n log n) in the best,
 * average and worst case. The cutoff does not change the asymptotics: it replaces the bottom
 * log₂(CUTOFF) levels of the recursion tree with Θ(n) work.</p>
 */
public final class MergeSort {

    /** Ranges of this length or shorter are finished with insertion sort. */
    public static final int CUTOFF = 15;

    private MergeSort() {
    }

    public static void sort(int[] a, Metrics metrics) {
        sort(a, CUTOFF, metrics);
    }

    /** Entry point with a configurable cutoff, used by the cutoff-tuning experiment. */
    public static void sort(int[] a, int cutoff, Metrics metrics) {
        if (a == null) {
            throw new IllegalArgumentException("array must not be null");
        }
        if (a.length < 2) {
            return;
        }
        // The one and only allocation of the whole algorithm.
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

    /**
     * Merges the sorted ranges {@code a[lo..mid]} and {@code a[mid+1..hi]} using the shared
     * buffer. Θ(n) time, zero allocations.
     */
    private static void merge(int[] a, int[] buffer, int lo, int mid, int hi, Metrics metrics) {
        System.arraycopy(a, lo, buffer, lo, hi - lo + 1);
        metrics.addSwaps(hi - lo + 1);

        int i = lo;        // cursor in the left half of the buffer
        int j = mid + 1;   // cursor in the right half of the buffer
        int k = lo;        // write cursor in the original array

        while (i <= mid && j <= hi) {
            // One comparison decides one output position: this is what makes the merge linear.
            if (metrics.lessOrEqual(buffer[i], buffer[j])) {
                a[k++] = buffer[i++];
            } else {
                a[k++] = buffer[j++];
            }
        }
        // At most one of the two tails is non-empty; copying it needs no comparisons.
        while (i <= mid) {
            a[k++] = buffer[i++];
        }
        while (j <= hi) {
            a[k++] = buffer[j++];
        }
    }
}
