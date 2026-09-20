package com.taubay.daa.geometry;

/** Immutable 2D point with double coordinates. */
public record Point(double x, double y) {

    public double distanceTo(Point other) {
        double dx = x - other.x;
        double dy = y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /** Squared distance — useful when only the ordering of distances matters. */
    public double squaredDistanceTo(Point other) {
        double dx = x - other.x;
        double dy = y - other.y;
        return dx * dx + dy * dy;
    }

    @Override
    public String toString() {
        return String.format("(%.3f, %.3f)", x, y);
    }
}
