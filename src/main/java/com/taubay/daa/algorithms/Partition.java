package com.taubay.daa.algorithms;

import com.taubay.daa.metrics.Metrics;

import java.util.Random;

public final class Partition {
    private Partition() {
    }

    public static int randomIndex(int lo, int hi, Random rnd) {
        return lo + rnd.nextInt(hi - lo + 1);
    }

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

    public static long threeWayRandom(int[] a, int lo, int hi, Random rnd, Metrics metrics) {
        int pivotValue = a[randomIndex(lo, hi, rnd)];
        return threeWay(a, lo, hi, pivotValue, metrics);
    }

    public static long pack(int lt, int gt) {
        return ((long) lt << 32) | (gt & 0xFFFFFFFFL);
    }

    public static int lt(long packed) {
        return (int) (packed >>> 32);
    }

    public static int gt(long packed) {
        return (int) packed;
    }
}
