package com.taubay.daa.algorithms;

import com.taubay.daa.metrics.Metrics;

import java.util.Random;

/**
 * Dutch-national-flag (3-way) partition shared by {@link QuickSort}, {@link QuickSelect}
 * and {@link DeterministicSelect}.
 *
 * <p>After {@link #threeWay} the range looks like</p>
 * <pre>
 *   [lo .. lt-1] &lt; pivot     [lt .. gt] == pivot     [gt+1 .. hi] &gt; pivot
 * </pre>
 *
 * <p>The equal block is already in its final position, so the recursion never looks at it
 * again. That is what keeps arrays with few distinct keys (for example values 0..9) linear
 * per level instead of degenerating to Θ(n²) the way a 2-way partition does.</p>
 *
 * <p>The two boundaries are returned packed into a single {@code long} on purpose: returning
 * an {@code int[]} would allocate one small array per partition call, which is exactly the
 * memory churn the assignment asks us to avoid.</p>
 */
public final class Partition {

    private Partition() {
    }

    /** Picks a uniformly random index in {@code [lo, hi]}. */
    public static int randomIndex(int lo, int hi, Random rnd) {
        return lo + rnd.nextInt(hi - lo + 1);
    }

    /**
     * Partitions {@code a[lo..hi]} around {@code pivotValue} in a single left-to-right pass.
     * Θ(hi - lo + 1) comparisons, no allocation.
     *
     * @return the packed boundaries; decode with {@link #lt(long)} and {@link #gt(long)}
     */
    public static long threeWay(int[] a, int lo, int hi, int pivotValue, Metrics metrics) {
        int lt = lo;
        int i = lo;
        int gt = hi;
        while (i <= gt) {
            int cmp = metrics.compare(a[i], pivotValue);
            if (cmp < 0) {
                metrics.swap(a, lt++, i++);
            } else if (cmp > 0) {
                metrics.swap(a, i, gt--);
            } else {
                i++;
            }
        }
        return pack(lt, gt);
    }

    /** Convenience wrapper that draws a random pivot from the range and partitions around it. */
    public static long threeWayRandom(int[] a, int lo, int hi, Random rnd, Metrics metrics) {
        int pivotValue = a[randomIndex(lo, hi, rnd)];
        return threeWay(a, lo, hi, pivotValue, metrics);
    }

    public static long pack(int lt, int gt) {
        return ((long) lt << 32) | (gt & 0xFFFFFFFFL);
    }

    /** First index of the "equal to pivot" block. */
    public static int lt(long packed) {
        return (int) (packed >>> 32);
    }

    /** Last index of the "equal to pivot" block. */
    public static int gt(long packed) {
        return (int) packed;
    }
}
