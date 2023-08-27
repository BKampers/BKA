/*
** © Bart Kampers
*/

package bka.demo.graphs;

import java.awt.*;


public class VertexRenderer implements Element {

    public VertexRenderer(Point point) {
        this.location = new Point(point);
    }

    @Override
    public Point getLocation() {
        return new Point(location);
    }

    public void setLocation(Point location) {
        this.location.move(location.x, location.y);
    }

    @Override
    public Dimension getDimension() {
        return new Dimension(size, size);
    }

    public void setDimension(Dimension dimension) {
        size = dimension.height;
    }

    @Override
    public void move(Point vector) {
        location.move(location.x + vector.x, location.y + vector.y);
    }

    public Point getConnectorPoint(Point edgePoint) {
        if (location.equals(edgePoint)) {
            return edgePoint;
        }
        float dx = edgePoint.x - location.x;
        float dy = edgePoint.y - location.y;
        double angle = Math.atan(dx / dy);
        float radius = size / ((dy < 0) ? -2f : 2f);
        float cx = (float) Math.sin(angle) * radius;
        float cy = (float) Math.cos(angle) * radius;
        return new Point(Math.round(location.x + cx), Math.round(location.y + cy));
    }

    public long squareDistance(Point point) {
        double distance = CanvasUtil.distance(point, location) - size / 2d;
        long squareDistance = Math.round(distance * distance);
        return (distance < 0) ? -squareDistance : squareDistance;
    }

    @Override
    public void paint(Graphics2D graphics) {
        int radius = size / 2;
        graphics.fillOval(location.x - radius, location.y - radius, size, size);
    }

    public void resize(Point target) {
        size = Math.abs((int) Math.round(CanvasUtil.distance(location, target) * 2));
    }

    private final Point location;
    private int size = 12;

}
