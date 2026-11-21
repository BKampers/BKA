/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package bka.awt.graphcanvas;

import java.awt.*;
import java.awt.geom.*;
import java.util.function.*;


public final class StadiumVertexPaintable extends VertexPaintable {

    public StadiumVertexPaintable(Dimension size) {
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
        graphics.draw(contour());
    }

    private void fill(Graphics2D graphics) {
        Paint fill = getPaint(FILL_PAINT_KEY);
        if (fill == null) {
            return;
        }
        graphics.setPaint(fill);
        graphics.fill(contour());
    }

    @Override
    public ResizeDirection resizeDirection(Point location, Point cursor) {
        double x = cursor.x - location.x;
        double y = cursor.y - location.y;
        double straightHalf = straightHalf();
        if (straightHalf == 0) {
            return super.resizeDirection(location, cursor);
        }
        if (Math.abs(x) <= straightHalf) {
            return (y <= 0) ? ResizeDirection.NORTH : ResizeDirection.SOUTH;
        }
        double capCenterX = (x < 0) ? -straightHalf : straightHalf;
        return capResizeDirection(x - capCenterX, y);
    }

    private static ResizeDirection capResizeDirection(double directionX, double directionY) {
        double angleFromMiddle = Math.abs(Math.atan2(directionY, Math.abs(directionX)));
        boolean east = 0 <= directionX;
        if (angleFromMiddle <= CAP_MIDDLE_MARGIN) {
            return east ? ResizeDirection.EAST : ResizeDirection.WEST;
        }
        if (directionY <= 0) {
            return east ? ResizeDirection.NORTH_EAST : ResizeDirection.NORTH_WEST;
        }
        return east ? ResizeDirection.SOUTH_EAST : ResizeDirection.SOUTH_WEST;
    }

    @Override
    public void resize(Point location, Point target, ResizeDirection direction) {
        switch (direction) {
            case NORTH, SOUTH ->
                setSize(getWidth(), height(location, target));
            case EAST, WEST ->
                setSize(width(location, target), getHeight());
            case NORTH_EAST, NORTH_WEST, SOUTH_EAST, SOUTH_WEST ->
                resizeCap(location, target, direction);
            default ->
                throw new IllegalStateException(direction.name());
        }
    }

    private void resizeCap(Point location, Point target, ResizeDirection direction) {
        double straightHalf = straightHalf();
        double capCenterX = switch (direction) {
            case NORTH_EAST, SOUTH_EAST -> straightHalf;
            case NORTH_WEST, SOUTH_WEST -> -straightHalf;
            default -> throw new IllegalStateException(direction.name());
        };
        double radius = Math.hypot(target.x - location.x - capCenterX, target.y - location.y);
        int diameter = (int) Math.round(radius * 2);
        setSize((int) Math.round(straightHalf * 2) + diameter, diameter);
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
        double clampedX = Math.clamp(x, -straightHalf(), straightHalf());
        return Math.hypot(x - clampedX, y) - capRadius();
    }

    @Override
    public Point getConnectorPoint(Point location, Point point) {
        if (location.equals(point)) {
            return point;
        }
        Point2D boundary = boundaryPoint(point.x - location.x, point.y - location.y);
        return CanvasUtil.getPoint(location.x + boundary.getX(), location.y + boundary.getY());
    }

    @Override
    public Supplier<Point> distancePositioner(Point location, Point point) {
        double x = point.x - location.x;
        double y = point.y - location.y;
        double straightHalf = straightHalf();
        if (0 < straightHalf && Math.abs(x) <= straightHalf) {
            boolean top = y <= 0;
            double yOffset = y - (top ? -capRadius() : capRadius());
            double ratio = (x + straightHalf) / (2 * straightHalf);
            return horizontalPositioner(location, top, ratio, yOffset);
        }
        double clampedX = Math.clamp(x, -straightHalf, straightHalf);
        double dx = x - clampedX;
        double dy = y;
        double spineDistance = Math.hypot(dx, dy);
        if (spineDistance == 0) {
            return capPositioner(location, true, 0, -1, -capRadius());
        }
        double directionX = dx / spineDistance;
        double directionY = dy / spineDistance;
        return capPositioner(location, 0 <= x, directionX, directionY, spineDistance - capRadius());
    }

    private Supplier<Point> horizontalPositioner(Point location, boolean top, double ratio, double yOffset) {
        return () -> {
            double straightHalf = straightHalf();
            double edgeX = -straightHalf + ratio * 2 * straightHalf;
            double edgeY = (top ? -capRadius() : capRadius()) + yOffset;
            return CanvasUtil.getPoint(location.x + edgeX, location.y + edgeY);
        };
    }

    private Supplier<Point> capPositioner(Point location, boolean right, double directionX, double directionY, double offset) {
        return () -> {
            double centerX = right ? straightHalf() : -straightHalf();
            double radial = capRadius() + offset;
            return CanvasUtil.getPoint(location.x + centerX + directionX * radial, location.y + directionY * radial);
        };
    }

    private Point2D boundaryPoint(double dx, double dy) {
        double radius = capRadius();
        double straightHalf = straightHalf();
        if (dy != 0) {
            double yEdge = Math.copySign(radius, dy);
            double xAtEdge = dx * yEdge / dy;
            if (Math.abs(xAtEdge) <= straightHalf) {
                return new Point2D.Double(xAtEdge, yEdge);
            }
        }
        double capCenterX = (dx < 0) ? -straightHalf : straightHalf;
        return capBoundary(dx, dy, capCenterX, radius);
    }

    private static Point2D capBoundary(double dx, double dy, double centerX, double radius) {
        double a = dx * dx + dy * dy;
        double b = -2 * dx * centerX;
        double discriminant = Math.max(0, b * b - 4 * a * (centerX * centerX - radius * radius));
        double sqrt = Math.sqrt(discriminant);
        double nearRayScale = (-b - sqrt) / (2 * a);
        double farRayScale = (-b + sqrt) / (2 * a);
        double rayScale = onOuterCap(nearRayScale, dx, centerX) ? nearRayScale : farRayScale;
        return new Point2D.Double(rayScale * dx, rayScale * dy);
    }

    private static boolean onOuterCap(double rayScale, double dx, double centerX) {
        if (rayScale <= RAY_ORIGIN_TOLERANCE) {
            return false;
        }
        double x = rayScale * dx;
        return (0 <= centerX) ? centerX - CAP_DIAMETER_TOLERANCE <= x : x <= centerX + CAP_DIAMETER_TOLERANCE;
    }

    private Shape contour() {
        double radius = capRadius();
        double straightHalf = straightHalf();
        double diameter = radius * 2;
        double top = -radius;
        Path2D path = new Path2D.Double();
        path.moveTo(-straightHalf, top);
        path.lineTo(straightHalf, top);
        path.append(new Arc2D.Double(straightHalf - radius, top, diameter, diameter, 90, -180, Arc2D.OPEN), true);
        path.lineTo(-straightHalf, radius);
        path.append(new Arc2D.Double(-straightHalf - radius, top, diameter, diameter, -90, -180, Arc2D.OPEN), true);
        path.closePath();
        return path;
    }

    private double capRadius() {
        return getHeight() / 2.0;
    }

    private double straightHalf() {
        return Math.max(0, (getWidth() - getHeight()) / 2.0);
    }

    private static final double RAY_ORIGIN_TOLERANCE = 1e-9;
    private static final double CAP_DIAMETER_TOLERANCE = 1e-6;
    private static final double CAP_MIDDLE_MARGIN = 0.1 * Math.PI;

}
