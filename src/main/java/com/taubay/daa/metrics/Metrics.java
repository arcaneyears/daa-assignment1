package com.taubay.daa.metrics;

public final class Metrics {
    private long comparisons;
    private long allocations;
    private long allocatedCells;

    private int currentDepth;
    private int maxDepth;

    private long startNanos;
    private long elapsedNanos;
    private boolean running;

    public void reset() {
        comparisons = 0;
        allocations = 0;
        allocatedCells = 0;
        currentDepth = 0;
        maxDepth = 0;
        startNanos = 0;
        elapsedNanos = 0;
        running = false;
    }

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

    public boolean lessOrEqual(int a, int b) {
        comparisons++;
        return a <= b;
    }

    public boolean greater(int a, int b) {
        comparisons++;
        return a > b;
    }

    public int compare(int a, int b) {
        comparisons++;
        return Integer.compare(a, b);
    }

    public long comparisons() {
        return comparisons;
    }

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

    public void enterRecursion() {
        currentDepth++;
        if (currentDepth > maxDepth) {
            maxDepth = currentDepth;
        }
    }

    public void exitRecursion() {
        currentDepth--;
    }

    public int maxDepth() {
        return maxDepth;
    }

    @Override
    public String toString() {
        return String.format(
                "Metrics{time=%.3f ms, comparisons=%d, allocations=%d (%d cells), maxDepth=%d}",
                elapsedMillis(), comparisons, allocations, allocatedCells, maxDepth);
    }
}
