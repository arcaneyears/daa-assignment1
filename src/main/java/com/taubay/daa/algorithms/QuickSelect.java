package com.taubay.daa.algorithms;

import com.taubay.daa.metrics.Metrics;

import java.util.Random;

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
            metrics.enterRecursion();
            try {
                if (lo == hi) {
                    return a[lo];
                }
                long bounds = Partition.threeWayRandom(a, lo, hi, rnd, metrics);
                int lt = Partition.lt(bounds);
                int gt = Partition.gt(bounds);

                if (k < lt) {
                    hi = lt - 1;
                } else if (k > gt) {
                    lo = gt + 1;
                } else {
                    return a[k];
                }
            } finally {
                metrics.exitRecursion();
            }
        }
    }

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
