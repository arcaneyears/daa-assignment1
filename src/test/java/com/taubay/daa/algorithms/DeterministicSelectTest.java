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

class DeterministicSelectTest {

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

            int actual = DeterministicSelect.select(data.clone(), k, new Metrics());

            assertEquals(sorted[k], actual, "trial " + trial + " n=" + n + " k=" + k);
        }
    }

    @Test
    @DisplayName("equals sorted[k] for every k of a 400 element array")
    void matchesSortedElementForEveryK() {
        int n = 400;
        int[] data = ArrayUtils.randomArray(n, new Random(SEED + 1));
        int[] sorted = data.clone();
        Arrays.sort(sorted);

        for (int k = 0; k < n; k++) {
            assertEquals(sorted[k], DeterministicSelect.select(data.clone(), k, new Metrics()), "k=" + k);
        }
    }

    @Test
    @DisplayName("agrees with QuickSelect on every input shape")
    void agreesWithQuickSelect() {
        Random rnd = new Random(SEED + 2);
        int n = 5_000;
        for (ArrayUtils.InputType type : ArrayUtils.InputType.values()) {
            int[] data = ArrayUtils.generate(type, n, rnd);
            for (int k : new int[]{0, 1, n / 4, n / 2, n - 1}) {
                int deterministic = DeterministicSelect.select(data.clone(), k, new Metrics());
                int randomized = QuickSelect.select(data.clone(), k, SEED, new Metrics());
                assertEquals(randomized, deterministic, type + " k=" + k);
            }
        }
    }

    @Test
    @DisplayName("edge cases: one element, all equal, tiny arrays of every length up to 20")
    void edgeCases() {
        assertEquals(9, DeterministicSelect.select(new int[]{9}, 0, new Metrics()));

        int[] allEqual = new int[1_000];
        Arrays.fill(allEqual, 4);
        assertEquals(4, DeterministicSelect.select(allEqual, 500, new Metrics()));

        Random rnd = new Random(SEED + 3);
        for (int n = 1; n <= 20; n++) {
            int[] data = ArrayUtils.randomArray(n, 50, rnd);
            int[] sorted = data.clone();
            Arrays.sort(sorted);
            for (int k = 0; k < n; k++) {
                assertEquals(sorted[k], DeterministicSelect.select(data.clone(), k, new Metrics()),
                        "n=" + n + " k=" + k);
            }
        }
    }

    @Test
    @DisplayName("invalid input throws IllegalArgumentException with a clear message")
    void invalidInputRejected() {
        IllegalArgumentException empty = assertThrows(IllegalArgumentException.class,
                () -> DeterministicSelect.select(new int[0], 0, new Metrics()));
        assertTrue(empty.getMessage().contains("empty"), empty.getMessage());

        assertThrows(IllegalArgumentException.class,
                () -> DeterministicSelect.select(new int[]{1}, 1, new Metrics()));
        assertThrows(IllegalArgumentException.class,
                () -> DeterministicSelect.select(null, 0, new Metrics()));
    }

    @Test
    @DisplayName("worst-case linear: comparisons stay below c*n even on sorted input")
    void staysLinearOnEveryInput() {
        int n = 200_000;
        for (ArrayUtils.InputType type : ArrayUtils.InputType.values()) {
            int[] data = ArrayUtils.generate(type, n, new Random(SEED + 4));
            Metrics metrics = new Metrics();

            DeterministicSelect.select(data, n / 2, metrics);

            assertTrue(metrics.comparisons() <= 25L * n,
                    type + ": comparisons=" + metrics.comparisons() + " should be O(n)");
            assertTrue(metrics.maxDepth() <= 40,
                    type + ": maxDepth=" + metrics.maxDepth());
        }
    }
}
