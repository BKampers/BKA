/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package bka.awt.graphcanvas;

import java.awt.*;
import java.util.function.*;


public final class DiamondVertexPaintable extends VertexPaintable {

    public DiamondVertexPaintable(Dimension size) {
        super(size);
    }

    @Override
    public void paint(Graphics2D graphics) {
        fill(graphics);
        paint(graphics, getPaint(BORDER_PAINT_KEY), getStroke(BORDER_STROKE_KEY));
    }

    @Override
    public void paint(Graphics2D graphics, Paint paint, Stroke stroke) {
        graphics.setPaint(paint);
        graphics.setStroke(stroke);
        graphics.drawPolygon(xPoints(), yPoints(), CORNER_COUNT);
    }

    private void fill(Graphics2D graphics) {
        graphics.setPaint(getPaint(FILL_PAINT_KEY));
        graphics.fillPolygon(xPoints(), yPoints(), CORNER_COUNT);
    }

    private int[] xPoints() {
        return new int[] { 0, halfWidth(), 0, -halfWidth() };
    }

    private int[] yPoints() {
        return new int[] { -halfHeight(), 0, halfHeight(), 0 };
    }

    @Override
    public void resize(Point location, Point target, ResizeDirection direction) {
        switch (direction) {
            case NORTH, SOUTH ->
                setSize(getWidth(), height(location, target));
            case EAST, WEST ->
                setSize(width(location, target), getHeight());
            case NORTH_EAST, NORTH_WEST, SOUTH_EAST, SOUTH_WEST ->
                resizeDiagonal(location, target);
            default ->
                throw new IllegalStateException(direction.name());
        }
    }

    private void resizeDiagonal(Point location, Point target) {
        double scale = Math.abs(target.x - location.x) / (double) halfWidth() + Math.abs(target.y - location.y) / (double) halfHeight();
        setSize((int) Math.round(getWidth() * scale), (int) Math.round(getHeight() * scale));
    }

    private static int width(Point location, Point target) {
        return Math.abs(target.x - location.x) * 2;
    }

    private static int height(Point location, Point target) {
        return Math.abs(target.y - location.y) * 2;
    }

    @Override
    public double distance(Point location, Point point) {
        double x = point.x - location.x;
        double y = point.y - location.y;
        boolean inside = Math.abs(x) / halfWidth() + Math.abs(y) / halfHeight() <= 1;
        double distance = Double.MAX_VALUE;
        for (int index = 0; index < CORNER_COUNT; index++) {
            distance = Math.min(distance, distanceToSegment(point, corner(location, index), corner(location, index + 1)));
        }
        return inside ? -distance : distance;
    }

    @Override
    public Point getConnectorPoint(Point location, Point point) {
        if (location.equals(point)) {
            return point;
        }
        double x = point.x - location.x;
        double y = point.y - location.y;
        double scale = 1 / (Math.abs(x) / halfWidth() + Math.abs(y) / halfHeight());
        return CanvasUtil.getPoint(location.x + x * scale, location.y + y * scale);
    }

    @Override
    public Supplier<Point> distancePositioner(Point location, Point point) {
        return DistanceToLinePositioner.create(point, nearestEdgeIndex(location, point), index -> corner(location, index));
    }

    private int nearestEdgeIndex(Point location, Point point) {
        int index = -1;
        double shortestDistance = Double.MAX_VALUE;
        for (int i = 0; i < CORNER_COUNT; ++i) {
            double distance = distanceToSegment(point, corner(location, i), corner(location, i + 1));
            if (distance < shortestDistance) {
                shortestDistance = distance;
                index = i;
            }
        }
        return index;
    }

    private Point corner(Point location, int index) {
        return switch (index % CORNER_COUNT) {
            case 0 ->
                new Point(location.x, location.y - halfHeight());
            case 1 ->
                new Point(location.x + halfWidth(), location.y);
            case 2 ->
                new Point(location.x, location.y + halfHeight());
            case 3 ->
                new Point(location.x - halfWidth(), location.y);
            default ->
                throw new IllegalStateException();
        };
    }

    private static double distanceToSegment(Point point, Point start, Point end) {
        long squareDistance = CanvasUtil.squareDistance(start, end);
        if (squareDistance == 0) {
            return point.distance(start);
        }
        double dx = end.x - start.x;
        double dy = end.y - start.y;
        double ratio = Math.clamp(((point.x - start.x) * dx + (point.y - start.y) * dy) / squareDistance, 0, 1);
        return Math.hypot(point.x - (start.x + ratio * dx), point.y - (start.y + ratio * dy));
    }

    private int halfWidth() {
        return getWidth() / 2;
    }

    private int halfHeight() {
        return getHeight() / 2;
    }

    private static final int CORNER_COUNT = 4;

}
