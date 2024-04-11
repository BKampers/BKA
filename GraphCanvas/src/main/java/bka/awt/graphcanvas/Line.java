/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package bka.awt.graphcanvas;

import java.awt.geom.*;
import java.util.*;


public final class Line {

    public Line(double slope, double offset) {
        if (Double.isNaN(slope) && !Double.isInfinite(slope)) {
            throw new IllegalArgumentException("Invalid slope: " + slope);
        }
        if (!Double.isFinite(offset)) {
            throw new IllegalArgumentException("Invalid offset: " + offset);
        }
        this.slope = slope;
        this.offset = offset;
    }

    public static Line through(Point2D point1, Point2D point2) {
        double slope = CanvasUtil.slope(requireValid(point1), requireValid(point2));
        return new Line(slope, (Double.isFinite(slope)) ? point1.getY() - point1.getX() * slope : point1.getX());
    }

    private static Point2D requireValid(Point2D point) {
        if (!Double.isFinite(point.getX()) || !Double.isFinite(point.getY())) {
            throw new IllegalArgumentException("Invalid point: " + point);
        }
        return point;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Line)) {
            return false;
        }
        Line line = (Line) other;
        return slope == line.slope && offset == line.offset;
    }

    @Override
    public int hashCode() {
        return Objects.hash(slope, offset);
    }

    public double getSlope() {
        return slope;
    }

    public double getOffset() {
        return offset;
    }

    public boolean isVertical() {
        return !Double.isFinite(slope);
    }

    public double getY(double x) {
        if (isVertical()) {
            return Double.NaN;
        }
        return slope * x + offset;
    }

    public boolean isParallelTo(Line line) {
        return slope == line.slope;
    }

    public Optional<Point2D> intersection(Line line) {
        if (isParallelTo(line)) {
            return Optional.empty();
        }
        if (isVertical()) {
            return Optional.of(new Point2D.Double(offset, line.getY(offset)));
        }
        if (line.isVertical()) {
            return Optional.of(new Point2D.Double(line.offset, getY(line.offset)));
        }
        double x = (offset - line.offset) / (line.slope - slope);
        return Optional.of(new Point2D.Double(x, getY(x)));
    }

    private final double slope;
    private final double offset;

}
