package com.taubay.daa.metrics;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MetricsTest {
    @Test
    @DisplayName("comparison helpers return the right answer and count exactly once each")
    void comparisonHelpersCount() {
        Metrics m = new Metrics();

        assertTrue(m.lessOrEqual(2, 2));
        assertTrue(m.greater(3, 2));
        assertEquals(-1, m.compare(1, 2));
        assertEquals(0, m.compare(2, 2));
        assertEquals(1, m.compare(3, 2));

        assertEquals(5, m.comparisons());
    }

    @Test
    @DisplayName("maxDepth is the peak of nested enter/exit pairs, not the total")
    void depthTracksThePeak() {
        Metrics m = new Metrics();

        m.enterRecursion();
        m.enterRecursion();
        m.enterRecursion();
        m.exitRecursion();
        m.exitRecursion();
        m.enterRecursion();
        m.exitRecursion();
        m.exitRecursion();

        assertEquals(3, m.maxDepth());
    }

    @Test
    @DisplayName("the timer accumulates a positive elapsed time and stops")
    void timerMeasuresElapsedTime() throws InterruptedException {
        Metrics m = new Metrics();

        m.startTimer();
        Thread.sleep(5);
        m.stopTimer();

        double first = m.elapsedMillis();
        assertTrue(first >= 4.0, "elapsed=" + first);
        Thread.sleep(5);
        assertEquals(first, m.elapsedMillis(), 1e-9, "a stopped timer must not keep running");
    }

    @Test
    @DisplayName("reset clears every counter")
    void resetClearsEverything() {
        Metrics m = new Metrics();
        m.compare(1, 2);
        m.recordAllocation(10);
        m.enterRecursion();
        m.startTimer();
        m.stopTimer();

        m.reset();

        assertEquals(0, m.comparisons());
        assertEquals(0, m.allocations());
        assertEquals(0, m.allocatedCells());
        assertEquals(0, m.maxDepth());
        assertEquals(0.0, m.elapsedMillis(), 1e-9);
    }
}
