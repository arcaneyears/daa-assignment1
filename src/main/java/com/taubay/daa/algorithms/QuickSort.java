package com.taubay.daa.algorithms;

import com.taubay.daa.metrics.Metrics;

import java.util.Random;

/**
 * QuickSort that cannot blow the stack and does not degrade on duplicate-heavy input.
 *
 * <p>Three defences, one per classic failure mode:</p>
 * <ol>
 *   <li><b>Random pivot</b> — the adversary cannot choose an input that forces bad splits,
 *       because the splits do not depend on the input order. Sorted input is therefore just
 *       another average case, not the Θ(n²) worst case.</li>
 *   <li><b>Recurse into the smaller side, loop on the larger one</b> — every real recursive
 *       call at least halves the range, so the number of live frames is at most
 *       ⌊log₂ n⌋ + 1 <i>deterministically</i>, whatever the pivots turn out to be. The larger
 *       side is handled by updating {@code lo}/{@code hi} and going round the {@code while}
 *       loop again (manual tail-call elimination).</li>
 *   <li><b>3-way partition</b> — keys equal to the pivot are placed once and never revisited,
 *       so an array of n equal keys is sorted in a single Θ(n) partition.</li>
 * </ol>
 *
 * <p>Recurrence with a balanced split: T(n) = 2·T(n/2) + Θ(n) → Θ(n log n). With a random
 * pivot the expected number of comparisons is 2n·ln n ≈ 1.39·n·log₂ n = Θ(n log n);
 * the Θ(n²) worst case survives only as an event of vanishing probability.</p>
 */
public final class QuickSort {

    private QuickSort() {
    }

    /** Sorts with a fresh, unseeded random source. */
    public static void sort(int[] a, Metrics metrics) {
        sort(a, new Random(), metrics);
    }

    /** Sorts with a seeded random source, so a benchmark run can be reproduced exactly. */
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

                int leftSize = lt - lo;      // size of a[lo .. lt-1]
                int rightSize = hi - gt;     // size of a[gt+1 .. hi]

                if (leftSize < rightSize) {
                    // Recurse into the smaller (left) side, iterate on the larger (right) one.
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
