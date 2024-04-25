/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package bka.awt.graphcanvas;

import java.awt.*;
import java.awt.geom.*;
import java.util.List;
import java.util.function.*;
import java.util.stream.*;


public class SquareVertexPaintable extends VertexPaintable {

    public SquareVertexPaintable(Dimension size) {
        super(size);
    }

    public SquareVertexPaintable(VertexPaintable vertexPaintable) {
        super(vertexPaintable);
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
        graphics.drawRect(-widthRadius(), -heightRadius(), getSize().width, getSize().height);
    }

    private void fill(Graphics2D graphics) {
        graphics.setPaint(getPaint(FILL_PAINT_KEY));
        graphics.fillRect(-widthRadius(), -heightRadius(), getSize().width, getSize().height);
    }


    @Override
    public void resize(Point location, Point target, ResizeDirection direction) {
        switch (direction) {
            case NORTH, SOUTH ->
                setSize(getSize().width, height(location, target));
            case EAST, WEST ->
                setSize(width(location, target), getSize().height);
            case NORTH_EAST, NORTH_WEST, SOUTH_EAST, SOUTH_WEST ->
                setSize(width(location, target), height(location, target));
            default ->
                throw new IllegalStateException(direction.toString());
        }
    }

    private static int width(Point location, Point target) {
        return Math.abs(target.x - location.x) * 2;
    }

    private static int height(Point location, Point target) {
        return Math.abs(target.y - location.y) * 2;
    }

    @Override
    public double distance(Point location, Point point) {
        double distance = Math.sqrt(
            contour2D(location).stream()
                .map(line -> squareDistance(point, line))
                .sorted()
                .findFirst().get());
        return (new Rectangle(topLeft(location), getSize()).contains(point)) ? -distance : distance;
    }

    public long squareDistance(Point2D point, Line2D line) {
        return CanvasUtil.squareDistance(CanvasUtil.round(point), CanvasUtil.round(line.getP1()), CanvasUtil.round(line.getP2()));
    }

    @Override
    public Point getConnectorPoint(Point location, Point point) {
        if (location.equals(point)) {
            return point;
        }
        Point2D nearest = null;
        for (Point2D intersectionPoint : intersectionPoints(location, Line.through(location, point))) {
            if (nearest == null || Math.abs(nearest.distance(point)) > Math.abs(intersectionPoint.distance(point))) {
                nearest = intersectionPoint;
            }
        }
        return (nearest == null) ? location : CanvasUtil.round(nearest);
    }

    private java.util.List<Point2D> intersectionPoints(Point location, Line incomming) {
        return contour(location).stream()
            .map(contourLine -> contourLine.intersection(incomming))
            .flatMap(optional -> optional.isPresent() ? Stream.of(optional.get()) : Stream.empty())
            .filter(intersectionPoint -> inBounds(intersectionPoint, location))
            .collect(Collectors.toList());
    }

    private boolean inBounds(Point2D intersectionPoint, Point location) {
        return location.x - widthRadius() <= intersectionPoint.getX() && intersectionPoint.getX() <= location.x + widthRadius()
            && location.y - heightRadius() <= intersectionPoint.getY() && intersectionPoint.getY() <= location.y + heightRadius();
    }

    private Point topLeft(Point location) {
        return new Point(location.x - widthRadius(), location.y - heightRadius());
    }

    private Point topRight(Point location) {
        return new Point(location.x + widthRadius(), location.y - heightRadius());
    }

    private Point bottomLeft(Point location) {
        return new Point(location.x - widthRadius(), location.y + heightRadius());
    }

    private Point bottomRight(Point location) {
        return new Point(location.x + widthRadius(), location.y + heightRadius());
    }

    private int widthRadius() {
        return getSize().width / 2;
    }

    private int heightRadius() {
        return getSize().height / 2;
    }

    @Override
    public Supplier<Point> distancePositioner(Point location, Point point) {
        return DistanceToLinePositioner.create(point, nearestLineIndex(location, point), index -> getPoint(getContour(location), index));
    }

    private int nearestLineIndex(Point location, Point point) {
        List<Point> points = getContour(location);
        int index = -1;
        long shortestDistance = Long.MAX_VALUE;
        for (int i = 0; i < points.size(); ++i) {
            Point startPoint = getPoint(points, i);
            Point endPoint = getPoint(points, i + 1);
            long distance = CanvasUtil.squareDistance(point, startPoint, endPoint);
            if (distance < shortestDistance) {
                shortestDistance = distance;
                index = i;
            }
        }
        return index;
    }

    private Point getPoint(List<Point> points, int index) {
        if (index < points.size()) {
            return points.get(index);
        }
        return points.get(0);
    }

    private List<Line> contour(Point location) {
        return contour2D(location).stream().map(line -> Line.through(line.getP1(), line.getP2())).collect(Collectors.toList());
    }

    private List<Line2D> contour2D(Point location) {
        return java.util.List.of(
            new Line2D.Double(topLeft(location), bottomLeft(location)),
            new Line2D.Double(bottomLeft(location), bottomRight(location)),
            new Line2D.Double(bottomRight(location), topRight(location)),
            new Line2D.Double(topRight(location), topLeft(location))
        );
    }

    private List<Point> getContour(Point location) {
        return java.util.List.of(
            topLeft(location),
            bottomLeft(location),
            bottomRight(location),
            topRight(location));
    }

}
