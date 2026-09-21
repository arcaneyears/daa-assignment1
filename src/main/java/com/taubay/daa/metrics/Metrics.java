package com.taubay.daa.metrics;

public final class Metrics {
    private long comparisons;
    private long swaps;
    private long allocations;
    private long allocatedCells;

    private int currentDepth;
    private int maxDepth;
    private long recursiveCalls;

    private long startNanos;
    private long elapsedNanos;
    private boolean running;

    public void reset() {
        comparisons = 0;
        swaps = 0;
        allocations = 0;
        allocatedCells = 0;
        currentDepth = 0;
        maxDepth = 0;
        recursiveCalls = 0;
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

    public boolean less(int a, int b) {
        comparisons++;
        return a < b;
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

    public void addComparisons(long n) {
        comparisons += n;
    }

    public long comparisons() {
        return comparisons;
    }

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
        recursiveCalls++;
        currentDepth++;
        if (currentDepth > maxDepth) {
            maxDepth = currentDepth;
        }
    }

    public void exitRecursion() {
        currentDepth--;
    }

    public int currentDepth() {
        return currentDepth;
    }

    public int maxDepth() {
        return maxDepth;
    }

    public long recursiveCalls() {
        return recursiveCalls;
    }

    @Override
    public String toString() {
        return String.format(
                "Metrics{time=%.3f ms, comparisons=%d, swaps=%d, allocations=%d (%d cells), maxDepth=%d, calls=%d}",
                elapsedMillis(), comparisons, swaps, allocations, allocatedCells, maxDepth, recursiveCalls);
    }
}
