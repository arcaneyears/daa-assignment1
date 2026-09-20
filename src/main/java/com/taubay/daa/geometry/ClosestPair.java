package com.taubay.daa.geometry;

import com.taubay.daa.metrics.Metrics;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Closest pair of points in the plane by divide and conquer, Θ(n log n) (bonus task B).
 *
 * <p>Outline:</p>
 * <ol>
 *   <li>Sort the points by x once, up front — Θ(n log n).</li>
 *   <li>Split at the middle x, solve both halves recursively and take δ = min(δ_left, δ_right).</li>
 *   <li>Merge the two halves <em>by y</em> on the way back up. Doing the y-ordering inside the
 *       merge step costs Θ(n) per level instead of re-sorting the strip at every level, which is
 *       what keeps the total at Θ(n log n) rather than Θ(n log² n).</li>
 *   <li>Scan the vertical strip of half-width δ around the split line in y order. For each point
 *       only the next 7 points can possibly be closer than δ — a δ×2δ rectangle cannot hold more
 *       than 8 points that are pairwise ≥ δ apart — so the strip scan is Θ(n).</li>
 * </ol>
 *
 * <p>Recurrence: T(n) = 2·T(n/2) + Θ(n) → Master Theorem case 2 → Θ(n log n), the same shape as
 * MergeSort.</p>
 *
 * <p>Both working arrays (merge buffer and strip) are allocated once in the entry point and
 * threaded through the recursion, so the recursion itself allocates nothing.</p>
 */
public final class ClosestPair {

    /** Ranges of this size or smaller are solved by brute force. */
    private static final int BRUTE_FORCE_CUTOFF = 3;

    /** How many following points in y order can possibly be closer than the current δ. */
    private static final int STRIP_NEIGHBOURS = 7;

    private ClosestPair() {
    }

    /** The closest pair found, together with its distance. */
    public record Result(Point first, Point second, double distance) {

        static final Result NONE = new Result(null, null, Double.POSITIVE_INFINITY);

        Result best(Result other) {
            return other.distance < distance ? other : this;
        }

        @Override
        public String toString() {
            return String.format("%s - %s : %.6f", first, second, distance);
        }
    }

    /** Θ(n log n) divide-and-conquer solution. The input array is not modified. */
    public static Result closestPair(Point[] points, Metrics metrics) {
        validate(points);
        Point[] byX = points.clone();
        Arrays.sort(byX, Comparator.comparingDouble(Point::x).thenComparingDouble(Point::y));

        Point[] buffer = new Point[byX.length];
        Point[] strip = new Point[byX.length];
        metrics.recordAllocation(2 * byX.length);

        return solve(byX, buffer, strip, 0, byX.length - 1, metrics);
    }

    /** Θ(n²) reference implementation used to validate the fast one in the tests. */
    public static Result bruteForce(Point[] points, Metrics metrics) {
        validate(points);
        Result best = Result.NONE;
        for (int i = 0; i < points.length; i++) {
            for (int j = i + 1; j < points.length; j++) {
                best = best.best(pair(points[i], points[j], metrics));
            }
        }
        return best;
    }

    /**
     * Solves {@code a[lo..hi]}, which must be sorted by x on entry, and leaves it sorted by y
     * on exit.
     */
    private static Result solve(Point[] a, Point[] buffer, Point[] strip, int lo, int hi, Metrics metrics) {
        metrics.enterRecursion();
        try {
            int n = hi - lo + 1;
            if (n <= BRUTE_FORCE_CUTOFF) {
                Result best = bruteForceRange(a, lo, hi, metrics);
                sortRangeByY(a, lo, hi, metrics);
                return best;
            }

            int mid = lo + ((hi - lo) >>> 1);
            double midX = a[mid].x();

            Result best = solve(a, buffer, strip, lo, mid, metrics)
                    .best(solve(a, buffer, strip, mid + 1, hi, metrics));

            mergeByY(a, buffer, lo, mid, hi, metrics);

            // Collect the strip in y order; it inherits the ordering from the merge above.
            int stripSize = 0;
            for (int i = lo; i <= hi; i++) {
                if (Math.abs(a[i].x() - midX) < best.distance()) {
                    strip[stripSize++] = a[i];
                }
            }

            for (int i = 0; i < stripSize; i++) {
                int limit = Math.min(i + STRIP_NEIGHBOURS, stripSize - 1);
                for (int j = i + 1; j <= limit; j++) {
                    // Points are y-sorted, so once the vertical gap alone exceeds the best
                    // distance no later j can help.
                    if (strip[j].y() - strip[i].y() >= best.distance()) {
                        break;
                    }
                    best = best.best(pair(strip[i], strip[j], metrics));
                }
            }
            return best;
        } finally {
            metrics.exitRecursion();
        }
    }

    private static Result bruteForceRange(Point[] a, int lo, int hi, Metrics metrics) {
        Result best = Result.NONE;
        for (int i = lo; i <= hi; i++) {
            for (int j = i + 1; j <= hi; j++) {
                best = best.best(pair(a[i], a[j], metrics));
            }
        }
        return best;
    }

    /** Merges the y-sorted ranges {@code a[lo..mid]} and {@code a[mid+1..hi]} in Θ(n). */
    private static void mergeByY(Point[] a, Point[] buffer, int lo, int mid, int hi, Metrics metrics) {
        System.arraycopy(a, lo, buffer, lo, hi - lo + 1);
        metrics.addSwaps(hi - lo + 1);
        int i = lo;
        int j = mid + 1;
        int k = lo;
        while (i <= mid && j <= hi) {
            metrics.addComparisons(1);
            if (buffer[i].y() <= buffer[j].y()) {
                a[k++] = buffer[i++];
            } else {
                a[k++] = buffer[j++];
            }
        }
        while (i <= mid) {
            a[k++] = buffer[i++];
        }
        while (j <= hi) {
            a[k++] = buffer[j++];
        }
    }

    /** Insertion sort by y for the tiny base-case ranges. */
    private static void sortRangeByY(Point[] a, int lo, int hi, Metrics metrics) {
        for (int i = lo + 1; i <= hi; i++) {
            Point key = a[i];
            int j = i - 1;
            while (j >= lo) {
                metrics.addComparisons(1);
                if (a[j].y() <= key.y()) {
                    break;
                }
                a[j + 1] = a[j];
                metrics.addSwaps(1);
                j--;
            }
            a[j + 1] = key;
        }
    }

    private static Result pair(Point p, Point q, Metrics metrics) {
        metrics.addComparisons(1);
        return new Result(p, q, p.distanceTo(q));
    }

    private static void validate(Point[] points) {
        if (points == null) {
            throw new IllegalArgumentException("points must not be null");
        }
        if (points.length < 2) {
            throw new IllegalArgumentException(
                    "at least two points are required but got " + points.length);
        }
        for (Point p : points) {
            if (p == null) {
                throw new IllegalArgumentException("points must not contain null");
            }
        }
    }
}
