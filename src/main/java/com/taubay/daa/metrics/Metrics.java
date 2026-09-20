package com.taubay.daa.metrics;

/**
 * Mutable counter object that is threaded explicitly through every algorithm.
 *
 * <p>No global/static state is used: a caller creates one {@code Metrics} instance per
 * measured run and passes it down the recursion. That keeps runs independent and makes
 * the algorithms safe to use from several threads (one instance per thread).</p>
 *
 * <p>Tracked quantities:</p>
 * <ul>
 *   <li><b>comparisons</b> — every key-to-key comparison performed by the algorithm;</li>
 *   <li><b>swaps / array writes</b> — element movements (swap counts as one);</li>
 *   <li><b>allocations</b> — number of {@code new int[...]} performed by the algorithm,
 *       used to prove that MergeSort allocates exactly one buffer;</li>
 *   <li><b>maxDepth</b> — maximum number of simultaneously active recursive frames;</li>
 *   <li><b>elapsedNanos</b> — wall-clock time measured with {@link System#nanoTime()}.</li>
 * </ul>
 */
public final class Metrics {

    private long comparisons;
    private long swaps;
    private long allocations;
    private long allocatedCells;

    private int currentDepth;
    private int maxDepth;

    private long startNanos;
    private long elapsedNanos;
    private boolean running;

    /** Clears every counter so the same object can be reused for the next run. */
    public void reset() {
        comparisons = 0;
        swaps = 0;
        allocations = 0;
        allocatedCells = 0;
        currentDepth = 0;
        maxDepth = 0;
        startNanos = 0;
        elapsedNanos = 0;
        running = false;
    }

    // ---------------------------------------------------------------- timing

    public void startTimer() {
        running = true;
        startNanos = System.nanoTime();
    }

    public void stopTimer() {
        if (running) {
            elapsedNanos += System.nanoTime() - startNanos;
            running = false;
        }
    }

    public long elapsedNanos() {
        return running ? elapsedNanos + (System.nanoTime() - startNanos) : elapsedNanos;
    }

    public double elapsedMillis() {
        return elapsedNanos() / 1_000_000.0;
    }

    // ------------------------------------------------------------ comparisons

    /** Counts one comparison and reports {@code a < b}. */
    public boolean less(int a, int b) {
        comparisons++;
        return a < b;
    }

    /** Counts one comparison and reports {@code a <= b}. */
    public boolean lessOrEqual(int a, int b) {
        comparisons++;
        return a <= b;
    }

    /** Counts one comparison and reports {@code a > b}. */
    public boolean greater(int a, int b) {
        comparisons++;
        return a > b;
    }

    /** Counts one three-way comparison and returns -1, 0 or 1. */
    public int compare(int a, int b) {
        comparisons++;
        return Integer.compare(a, b);
    }

    /** Adds comparisons performed in a tight loop that was counted manually. */
    public void addComparisons(long n) {
        comparisons += n;
    }

    public long comparisons() {
        return comparisons;
    }

    // ----------------------------------------------------------- data movement

    public void swap(int[] a, int i, int j) {
        int tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
        swaps++;
    }

    public void addSwaps(long n) {
        swaps += n;
    }

    public long swaps() {
        return swaps;
    }

    // ------------------------------------------------------------- allocations

    /** Records one auxiliary array allocation of {@code cells} ints. */
    public void recordAllocation(int cells) {
        allocations++;
        allocatedCells += cells;
    }

    public long allocations() {
        return allocations;
    }

    public long allocatedCells() {
        return allocatedCells;
    }

    // ------------------------------------------------------------------- depth

    /**
     * Must be called on entry to a recursive method. Returns nothing but updates the
     * running maximum, so {@link #maxDepth()} is the peak number of live frames.
     */
    public void enterRecursion() {
        currentDepth++;
        if (currentDepth > maxDepth) {
            maxDepth = currentDepth;
        }
    }

    /** Must be called (ideally from a {@code finally} block) on exit from a recursive method. */
    public void exitRecursion() {
        currentDepth--;
    }

    public int currentDepth() {
        return currentDepth;
    }

    public int maxDepth() {
        return maxDepth;
    }

    @Override
    public String toString() {
        return String.format(
                "Metrics{time=%.3f ms, comparisons=%d, swaps=%d, allocations=%d (%d cells), maxDepth=%d}",
                elapsedMillis(), comparisons, swaps, allocations, allocatedCells, maxDepth);
    }
}
