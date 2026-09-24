package com.taubay.daa.algorithms;

import com.taubay.daa.metrics.Metrics;
import com.taubay.daa.util.ArrayUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuickSelectTest {
    private static final long SEED = 20260920L;

    @Test
    @DisplayName("equals sorted[k] on 150 random arrays with a random k")
    void matchesSortedElementOnRandomArrays() {
        Random rnd = new Random(SEED);
        for (int trial = 0; trial < 150; trial++) {
            int n = 1 + rnd.nextInt(600);
            int[] data = ArrayUtils.randomArray(n, rnd);
            int[] sorted = data.clone();
            Arrays.sort(sorted);
            int k = rnd.nextInt(n);

            int actual = QuickSelect.select(data.clone(), k, SEED + trial, new Metrics());

            assertEquals(sorted[k], actual,
                    "trial " + trial + " n=" + n + " k=" + k);
        }
    }

    @Test
    @DisplayName("equals sorted[k] for every k of a 500 element array")
    void matchesSortedElementForEveryK() {
        int n = 500;
        int[] data = ArrayUtils.randomArray(n, new Random(SEED + 1));
        int[] sorted = data.clone();
        Arrays.sort(sorted);

        for (int k = 0; k < n; k++) {
            assertEquals(sorted[k], QuickSelect.select(data.clone(), k, SEED + k, new Metrics()),
                    "k=" + k);
        }
    }

    @Test
    @DisplayName("works on random, sorted and duplicate-heavy input")
    void worksOnStructuredInput() {
        Random rnd = new Random(SEED + 2);
        int n = 5_000;
        for (ArrayUtils.InputType type : ArrayUtils.InputType.values()) {
            int[] data = ArrayUtils.generate(type, n, rnd);
            int[] sorted = data.clone();
            Arrays.sort(sorted);
            for (int k : new int[]{0, 1, n / 3, n / 2, n - 2, n - 1}) {
                assertEquals(sorted[k], QuickSelect.select(data.clone(), k, SEED, new Metrics()),
                        type + " k=" + k);
            }
        }
    }

    @Test
    @DisplayName("single element array returns that element for k = 0")
    void singleElement() {
        assertEquals(9, QuickSelect.select(new int[]{9}, 0, SEED, new Metrics()));
    }

    @Test
    @DisplayName("invalid input throws IllegalArgumentException with a clear message")
    void invalidInputRejected() {
        IllegalArgumentException empty = assertThrows(IllegalArgumentException.class,
                () -> QuickSelect.select(new int[0], 0, new Metrics()));
        assertTrue(empty.getMessage().contains("empty"), empty.getMessage());

        IllegalArgumentException negative = assertThrows(IllegalArgumentException.class,
                () -> QuickSelect.select(new int[]{1, 2, 3}, -1, SEED, new Metrics()));
        assertTrue(negative.getMessage().contains("k must be in"), negative.getMessage());

        IllegalArgumentException tooLarge = assertThrows(IllegalArgumentException.class,
                () -> QuickSelect.select(new int[]{1, 2, 3}, 3, SEED, new Metrics()));
        assertTrue(tooLarge.getMessage().contains("k must be in"), tooLarge.getMessage());

        assertThrows(IllegalArgumentException.class,
                () -> QuickSelect.select(null, 0, SEED, new Metrics()));
    }

    @Test
    @DisplayName("selectOnCopy leaves the caller's array untouched")
    void selectOnCopyDoesNotMutate() {
        int[] original = {5, 3, 9, 1, 7};
        int[] snapshot = original.clone();

        assertEquals(5, QuickSelect.selectOnCopy(original, 2, SEED, new Metrics()));

        assertTrue(Arrays.equals(snapshot, original), "input array was modified");
    }

    @Test
    @DisplayName("stack depth stays constant and expected comparisons stay linear")
    void linearExpectedWorkWithConstantStack() {
        int n = 1_000_000;
        int[] data = ArrayUtils.randomArray(n, new Random(SEED + 3));
        Metrics metrics = new Metrics();

        QuickSelect.select(data, n / 2, SEED, metrics);

        assertEquals(1, metrics.maxDepth(), "QuickSelect must not nest stack frames");
        assertTrue(metrics.comparisons() <= 10L * n,
                "comparisons=" + metrics.comparisons() + " should be O(n)");
    }
}
