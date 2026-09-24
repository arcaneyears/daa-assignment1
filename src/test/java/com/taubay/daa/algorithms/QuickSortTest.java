package com.taubay.daa.algorithms;

import com.taubay.daa.metrics.Metrics;
import com.taubay.daa.util.ArrayUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuickSortTest {
    private static final long SEED = 20260920L;

    @Test
    @DisplayName("matches Arrays.sort on 200 random arrays of random length")
    void matchesLibrarySortOnRandomArrays() {
        Random rnd = new Random(SEED);
        for (int trial = 0; trial < 200; trial++) {
            int n = rnd.nextInt(500);
            int[] actual = ArrayUtils.randomArray(n, rnd);
            int[] expected = actual.clone();
            Arrays.sort(expected);

            QuickSort.sort(actual, SEED + trial, new Metrics());

            assertArrayEquals(expected, actual, "failed on trial " + trial + " with n=" + n);
        }
    }

    @Test
    @DisplayName("matches Arrays.sort on duplicate-heavy arrays (values 0..9)")
    void matchesLibrarySortOnDuplicateHeavyArrays() {
        Random rnd = new Random(SEED + 1);
        for (int trial = 0; trial < 100; trial++) {
            int n = rnd.nextInt(2_000);
            int[] actual = ArrayUtils.randomArray(n, 10, rnd);
            int[] expected = actual.clone();
            Arrays.sort(expected);

            QuickSort.sort(actual, SEED + trial, new Metrics());

            assertArrayEquals(expected, actual);
        }
    }

    @Test
    @DisplayName("edge cases: empty, single, two elements, all equal, sorted, reversed")
    void edgeCases() {
        int[] empty = {};
        QuickSort.sort(empty, SEED, new Metrics());
        assertArrayEquals(new int[]{}, empty);

        int[] single = {42};
        QuickSort.sort(single, SEED, new Metrics());
        assertArrayEquals(new int[]{42}, single);

        int[] two = {2, 1};
        QuickSort.sort(two, SEED, new Metrics());
        assertArrayEquals(new int[]{1, 2}, two);

        int[] allEqual = new int[10_000];
        Arrays.fill(allEqual, -3);
        int[] expectedEqual = allEqual.clone();
        QuickSort.sort(allEqual, SEED, new Metrics());
        assertArrayEquals(expectedEqual, allEqual);

        int[] sorted = ArrayUtils.sortedArray(10_000);
        QuickSort.sort(sorted, SEED, new Metrics());
        assertArrayEquals(ArrayUtils.sortedArray(10_000), sorted);

        int[] reversed = new int[10_000];
        for (int i = 0; i < reversed.length; i++) {
            reversed[i] = reversed.length - i;
        }
        int[] expectedReversed = reversed.clone();
        Arrays.sort(expectedReversed);
        QuickSort.sort(reversed, SEED, new Metrics());
        assertArrayEquals(expectedReversed, reversed);
    }

    @Test
    @DisplayName("null input is rejected with IllegalArgumentException")
    void nullInputRejected() {
        assertThrows(IllegalArgumentException.class, () -> QuickSort.sort(null, new Metrics()));
    }

    @Test
    @DisplayName("recursion depth on a sorted array of 100 000 stays below 2*log2(n)")
    void depthStaysBoundedOnSortedInput() {
        int n = 100_000;
        double bound = 2.0 * (Math.log(n) / Math.log(2));

        for (int trial = 0; trial < 20; trial++) {
            Metrics metrics = new Metrics();
            QuickSort.sort(ArrayUtils.sortedArray(n), SEED + trial, metrics);
            assertTrue(metrics.maxDepth() <= bound,
                    "maxDepth=" + metrics.maxDepth() + " exceeded 2*log2(n)=" + bound
                            + " on trial " + trial);
        }
    }

    @Test
    @DisplayName("recursion depth stays bounded on random and duplicate input too")
    void depthStaysBoundedOnEveryInputShape() {
        int n = 100_000;
        double bound = 2.0 * (Math.log(n) / Math.log(2));
        Random rnd = new Random(SEED + 5);

        for (ArrayUtils.InputType type : ArrayUtils.InputType.values()) {
            for (int trial = 0; trial < 5; trial++) {
                Metrics metrics = new Metrics();
                QuickSort.sort(ArrayUtils.generate(type, n, rnd), SEED + trial, metrics);
                assertTrue(metrics.maxDepth() <= bound,
                        type + ": maxDepth=" + metrics.maxDepth() + " exceeded " + bound);
            }
        }
    }

    @Test
    @DisplayName("a 1 000 000 element sorted array sorts without StackOverflowError")
    void noStackOverflowOnLargeSortedInput() {
        int n = 1_000_000;
        int[] a = ArrayUtils.sortedArray(n);
        Metrics metrics = new Metrics();

        QuickSort.sort(a, SEED, metrics);

        assertTrue(ArrayUtils.isSorted(a));
        assertTrue(metrics.maxDepth() <= 2.0 * (Math.log(n) / Math.log(2)),
                "maxDepth=" + metrics.maxDepth());
    }

    @Test
    @DisplayName("an array of equal keys costs a linear number of comparisons (3-way partition)")
    void duplicatesCostLinearWork() {
        int n = 200_000;
        int[] a = new int[n];
        Arrays.fill(a, 11);
        Metrics metrics = new Metrics();

        QuickSort.sort(a, SEED, metrics);

        assertTrue(metrics.comparisons() <= 2L * n,
                "comparisons=" + metrics.comparisons() + " should be linear for all-equal input");
        assertTrue(metrics.maxDepth() <= 2, "maxDepth=" + metrics.maxDepth());
    }

    @Test
    @DisplayName("comparison count stays inside the c*n*log2(n) envelope on random input")
    void comparisonsFollowNLogN() {
        int n = 500_000;
        double log2n = Math.log(n) / Math.log(2);
        Metrics metrics = new Metrics();

        QuickSort.sort(ArrayUtils.randomArray(n, new Random(SEED + 9)), SEED, metrics);

        assertTrue(metrics.comparisons() <= 3.0 * n * log2n,
                "comparisons=" + metrics.comparisons() + " exceeded 3 n log2 n");
    }
}
