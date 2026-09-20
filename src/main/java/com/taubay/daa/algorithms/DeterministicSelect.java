package com.taubay.daa.algorithms;

import com.taubay.daa.metrics.Metrics;

/**
 * Median-of-Medians selection (BFPRT): the k-th smallest element in Θ(n) <b>worst case</b>,
 * with no randomness at all (bonus task A).
 *
 * <p>Algorithm: split the range into groups of 5, sort each group with insertion sort, move
 * the 5-element medians to the front of the range, recursively select the median of those
 * medians, and use it as the partition pivot. That pivot is guaranteed to be greater than at
 * least 3n/10 elements and smaller than at least 3n/10 elements, so the surviving side never
 * exceeds 7n/10.</p>
 *
 * <p>Recurrence: T(n) ≤ T(n/5) + T(7n/10) + Θ(n). The Master Theorem does not apply (the two
 * subproblems have different sizes), but since 1/5 + 7/10 = 9/10 &lt; 1 the work per level
 * decays geometrically and substitution gives T(n) ≤ 10·c·n = Θ(n).</p>
 *
 * <p>Compared with {@link QuickSelect} it trades a bigger constant (the group sorting and the
 * extra recursive call for the pivot) for a guarantee: no input can make it quadratic.</p>
 */
public final class DeterministicSelect {

    /** Ranges of this size are solved directly by insertion sort. */
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

    /** Convenience wrapper that leaves the caller's array untouched. */
    public static int selectOnCopy(int[] a, int k, Metrics metrics) {
        if (a == null) {
            throw new IllegalArgumentException("array must not be null");
        }
        return select(a.clone(), k, metrics);
    }

    /** {@code k} is an absolute index into {@code a}, not an offset inside the range. */
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

    /**
     * Sorts each group of five in place, gathers the group medians into {@code a[lo..lo+g-1]}
     * and returns the median of that prefix, found by a recursive call to the same selector.
     */
    private static int medianOfMedians(int[] a, int lo, int hi, Metrics metrics) {
        int n = hi - lo + 1;
        int groups = 0;
        for (int start = lo; start <= hi; start += GROUP) {
            int end = Math.min(start + GROUP - 1, hi);
            InsertionSort.sort(a, start, end, metrics);
            int median = start + ((end - start) >>> 1);
            // Park the median of this group at a[lo + groups]; no extra array is allocated.
            metrics.swap(a, lo + groups, median);
            groups++;
        }
        int medianIndex = lo + (groups - 1) / 2;
        return selectRange(a, lo, lo + groups - 1, medianIndex, metrics);
    }
}
