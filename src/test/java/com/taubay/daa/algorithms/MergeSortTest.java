package com.taubay.daa.algorithms;

import com.taubay.daa.metrics.Metrics;
import com.taubay.daa.util.ArrayUtils;
import com.sun.management.ThreadMXBean;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class MergeSortTest {
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

            MergeSort.sort(actual, new Metrics());

            assertArrayEquals(expected, actual, "failed on trial " + trial + " with n=" + n);
        }
    }

    @Test
    @DisplayName("matches Arrays.sort on duplicate-heavy arrays")
    void matchesLibrarySortOnDuplicateHeavyArrays() {
        Random rnd = new Random(SEED + 1);
        for (int trial = 0; trial < 100; trial++) {
            int n = rnd.nextInt(2_000);
            int[] actual = ArrayUtils.randomArray(n, 10, rnd);
            int[] expected = actual.clone();
            Arrays.sort(expected);

            MergeSort.sort(actual, new Metrics());

            assertArrayEquals(expected, actual);
        }
    }

    @Test
    @DisplayName("edge cases: empty, single element, two elements, all equal, sorted, reversed")
    void edgeCases() {
        int[] empty = {};
        MergeSort.sort(empty, new Metrics());
        assertArrayEquals(new int[]{}, empty);

        int[] single = {42};
        MergeSort.sort(single, new Metrics());
        assertArrayEquals(new int[]{42}, single);

        int[] two = {2, 1};
        MergeSort.sort(two, new Metrics());
        assertArrayEquals(new int[]{1, 2}, two);

        int[] allEqual = new int[1_000];
        Arrays.fill(allEqual, 7);
        MergeSort.sort(allEqual, new Metrics());
        int[] stillAllEqual = new int[1_000];
        Arrays.fill(stillAllEqual, 7);
        assertArrayEquals(stillAllEqual, allEqual);

        int[] sorted = ArrayUtils.sortedArray(5_000);
        MergeSort.sort(sorted, new Metrics());
        assertArrayEquals(ArrayUtils.sortedArray(5_000), sorted);

        int[] reversed = new int[5_000];
        for (int i = 0; i < reversed.length; i++) {
            reversed[i] = reversed.length - i;
        }
        int[] expectedReversed = reversed.clone();
        Arrays.sort(expectedReversed);
        MergeSort.sort(reversed, new Metrics());
        assertArrayEquals(expectedReversed, reversed);
    }

    @Test
    @DisplayName("null input is rejected with IllegalArgumentException")
    void nullInputRejected() {
        assertThrows(IllegalArgumentException.class, () -> MergeSort.sort(null, new Metrics()));
    }

    @Test
    @DisplayName("allocates exactly one auxiliary buffer regardless of n")
    void allocatesSingleBuffer() {
        Random rnd = new Random(SEED + 2);
        for (int n : new int[]{16, 1_000, 100_000}) {
            Metrics metrics = new Metrics();
            MergeSort.sort(ArrayUtils.randomArray(n, rnd), metrics);
            assertEquals(1, metrics.allocations(), "more than one buffer allocated for n=" + n);
            assertEquals(n, metrics.allocatedCells(), "buffer has the wrong size for n=" + n);
        }

        Metrics trivial = new Metrics();
        MergeSort.sort(new int[]{1}, trivial);
        assertEquals(0, trivial.allocations());
    }

    @Test
    @DisplayName("recursion depth stays logarithmic and comparisons stay within the n log n envelope")
    void depthAndComparisonsFollowTheory() {
        int n = 100_000;
        Metrics metrics = new Metrics();
        MergeSort.sort(ArrayUtils.randomArray(n, new Random(SEED + 3)), metrics);

        double log2n = Math.log(n) / Math.log(2);
        assertTrue(metrics.maxDepth() <= log2n + 2,
                "maxDepth=" + metrics.maxDepth() + " exceeded log2(n)+2");
        assertTrue(metrics.comparisons() <= 2.0 * n * log2n,
                "comparisons=" + metrics.comparisons() + " exceeded 2 n log2 n");
    }

    @Test
    @DisplayName("heap allocation during the sort is one buffer, not one array per merge")
    void heapAllocationMatchesOneBuffer() {
        ThreadMXBean bean = (ThreadMXBean) ManagementFactory.getThreadMXBean();
        assumeTrue(bean.isThreadAllocatedMemorySupported(), "allocation counter not available");
        bean.setThreadAllocatedMemoryEnabled(true);

        int n = 200_000;
        long threadId = Thread.currentThread().getId();
        for (int round = 0; round < 3; round++) {
            MergeSort.sort(ArrayUtils.randomArray(n, new Random(SEED + round)), new Metrics());
        }

        int[] data = ArrayUtils.randomArray(n, new Random(SEED + 5));
        Metrics metrics = new Metrics();
        long before = bean.getThreadAllocatedBytes(threadId);
        MergeSort.sort(data, metrics);
        long allocated = bean.getThreadAllocatedBytes(threadId) - before;

        long oneBuffer = 4L * n;
        assertTrue(allocated < 2 * oneBuffer,
                "allocated " + allocated + " bytes, one buffer is " + oneBuffer);
        assertTrue(ArrayUtils.isSorted(data));
    }
}
