package com.taubay.daa.geometry;

import com.taubay.daa.metrics.Metrics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClosestPairTest {

    private static final long SEED = 20260920L;
    private static final double EPS = 1e-9;

    private static Point[] randomPoints(int n, Random rnd) {
        Point[] pts = new Point[n];
        for (int i = 0; i < n; i++) {
            pts[i] = new Point(rnd.nextDouble() * 10_000, rnd.nextDouble() * 10_000);
        }
        return pts;
    }

    @Test
    @DisplayName("agrees with the O(n^2) brute force on 100 random sets (n <= 2000)")
    void agreesWithBruteForceOnRandomSets() {
        Random rnd = new Random(SEED);
        for (int trial = 0; trial < 100; trial++) {
            int n = 2 + rnd.nextInt(300);
            Point[] pts = randomPoints(n, rnd);

            double fast = ClosestPair.closestPair(pts, new Metrics()).distance();
            double slow = ClosestPair.bruteForce(pts, new Metrics()).distance();

            assertEquals(slow, fast, EPS, "trial " + trial + " with n=" + n);
        }
    }

    @Test
    @DisplayName("agrees with brute force at the n = 2000 boundary")
    void agreesWithBruteForceAtTwoThousandPoints() {
        Random rnd = new Random(SEED + 1);
        for (int trial = 0; trial < 5; trial++) {
            Point[] pts = randomPoints(2_000, rnd);

            double fast = ClosestPair.closestPair(pts, new Metrics()).distance();
            double slow = ClosestPair.bruteForce(pts, new Metrics()).distance();

            assertEquals(slow, fast, EPS);
        }
    }

    @Test
    @DisplayName("handles clustered points, duplicates and collinear points")
    void degenerateConfigurations() {
        // Two identical points anywhere in the set means distance 0.
        Point[] withDuplicate = {
                new Point(0, 0), new Point(5, 5), new Point(100, 3),
                new Point(5, 5), new Point(-7, 12)
        };
        assertEquals(0.0, ClosestPair.closestPair(withDuplicate, new Metrics()).distance(), EPS);

        // All points on one vertical line.
        Point[] vertical = new Point[500];
        for (int i = 0; i < vertical.length; i++) {
            vertical[i] = new Point(3.0, i * 2.0);
        }
        assertEquals(2.0, ClosestPair.closestPair(vertical, new Metrics()).distance(), EPS);

        // All points on one horizontal line.
        Point[] horizontal = new Point[500];
        for (int i = 0; i < horizontal.length; i++) {
            horizontal[i] = new Point(i * 0.5, -4.0);
        }
        assertEquals(0.5, ClosestPair.closestPair(horizontal, new Metrics()).distance(), EPS);

        // Points sharing the same x in a grid, which stresses the strip scan.
        Random rnd = new Random(SEED + 2);
        Point[] grid = new Point[1_000];
        for (int i = 0; i < grid.length; i++) {
            grid[i] = new Point(rnd.nextInt(20), rnd.nextInt(20));
        }
        assertEquals(ClosestPair.bruteForce(grid, new Metrics()).distance(),
                ClosestPair.closestPair(grid, new Metrics()).distance(), EPS);
    }

    @Test
    @DisplayName("smallest legal input is two points")
    void twoPoints() {
        Point[] pts = {new Point(0, 0), new Point(3, 4)};
        ClosestPair.Result result = ClosestPair.closestPair(pts, new Metrics());
        assertEquals(5.0, result.distance(), EPS);
    }

    @Test
    @DisplayName("fewer than two points or null input is rejected")
    void invalidInputRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> ClosestPair.closestPair(new Point[]{new Point(1, 1)}, new Metrics()));
        assertThrows(IllegalArgumentException.class,
                () -> ClosestPair.closestPair(new Point[0], new Metrics()));
        assertThrows(IllegalArgumentException.class,
                () -> ClosestPair.closestPair(null, new Metrics()));
        assertThrows(IllegalArgumentException.class,
                () -> ClosestPair.closestPair(new Point[]{new Point(1, 1), null}, new Metrics()));
    }

    @Test
    @DisplayName("the input array is left untouched")
    void inputIsNotModified() {
        Random rnd = new Random(SEED + 3);
        Point[] pts = randomPoints(200, rnd);
        Point[] snapshot = pts.clone();

        ClosestPair.closestPair(pts, new Metrics());

        for (int i = 0; i < pts.length; i++) {
            assertEquals(snapshot[i], pts[i], "position " + i + " changed");
        }
    }

    @Test
    @DisplayName("scales to 200 000 points with logarithmic recursion depth and linearithmic work")
    void scalesToLargeInput() {
        int n = 200_000;
        Point[] pts = randomPoints(n, new Random(SEED + 4));
        Metrics metrics = new Metrics();

        ClosestPair.Result result = ClosestPair.closestPair(pts, metrics);

        assertTrue(result.distance() > 0);
        double log2n = Math.log(n) / Math.log(2);
        assertTrue(metrics.maxDepth() <= log2n + 2,
                "maxDepth=" + metrics.maxDepth());
        assertTrue(metrics.comparisons() <= 20L * n * (long) Math.ceil(log2n),
                "comparisons=" + metrics.comparisons());
    }
}
