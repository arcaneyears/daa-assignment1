package com.taubay.daa.algorithms;

import com.taubay.daa.metrics.Metrics;

import java.util.Random;

/**
 * Randomised selection of the k-th smallest element (k is 0-based).
 *
 * <p>It reuses the very same {@link Partition#threeWay} routine as {@link QuickSort}. The only
 * difference is what happens after the partition: QuickSort recurses into <em>both</em> sides,
 * QuickSelect keeps only the one side that can still contain position k, and if k has landed
 * inside the equal-to-pivot block the answer is already known.</p>
 *
 * <p>Because there is a single recursive call and it is in tail position, it is written as a
 * {@code while} loop: the stack depth is Θ(1) no matter how unlucky the pivots are, so a bad
 * pivot sequence costs time but can never cause a {@code StackOverflowError}.</p>
 *
 * <p>Recurrence with a balanced split: T(n) = T(n/2) + Θ(n). Here a = 1, b = 2,
 * f(n) = Θ(n) and n^(log_b a) = n^0 = 1, so f(n) dominates — Master Theorem case 3 —
 * and T(n) = Θ(n). Expected cost with a random pivot is 2n + o(n) comparisons;
 * the worst case is Θ(n²).</p>
 *
 * <p><b>The array is reordered in place</b> (that is what makes it linear and allocation-free).
 * Callers that need the original order should pass a clone.</p>
 */
public final class QuickSelect {

    private QuickSelect() {
    }

    public static int select(int[] a, int k, Metrics metrics) {
        return select(a, k, new Random(), metrics);
    }

    public static int select(int[] a, int k, long seed, Metrics metrics) {
        return select(a, k, new Random(seed), metrics);
    }

    public static int select(int[] a, int k, Random rnd, Metrics metrics) {
        validate(a, k);

        int lo = 0;
        int hi = a.length - 1;
        while (true) {
            // One "logical recursion level" per partition round; the stack itself stays flat.
            metrics.enterRecursion();
            try {
                if (lo == hi) {
                    return a[lo];
                }
                long bounds = Partition.threeWayRandom(a, lo, hi, rnd, metrics);
                int lt = Partition.lt(bounds);
                int gt = Partition.gt(bounds);

                if (k < lt) {
                    hi = lt - 1;          // keep only the "< pivot" side
                } else if (k > gt) {
                    lo = gt + 1;          // keep only the "> pivot" side
                } else {
                    return a[k];          // k fell inside the "== pivot" block: done
                }
            } finally {
                metrics.exitRecursion();
            }
        }
    }

    /** Convenience wrapper that leaves the caller's array untouched. */
    public static int selectOnCopy(int[] a, int k, long seed, Metrics metrics) {
        validate(a, k);
        return select(a.clone(), k, new Random(seed), metrics);
    }

    private static void validate(int[] a, int k) {
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
    }
}
