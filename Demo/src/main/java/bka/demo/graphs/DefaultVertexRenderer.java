/*
** © Bart Kampers
*/

package bka.demo.graphs;

import java.awt.*;
import java.util.*;


public class DefaultVertexRenderer implements VertexRenderer {

    public DefaultVertexRenderer(Point point) {
        this.location = Objects.requireNonNull(point);
    }

    @Override
    public Point getLocation() {
        return location;
    }

    @Override
    public void setLocation(Point location) {
        this.location = location;
    }

    @Override
    public Point getConnectorPoint(Point edgePoint) {
        float dx = edgePoint.x - location.x;
        float dy = edgePoint.y - location.y;
        double angle = Math.atan(dx / dy);
        float radius = size / ((dy < 0) ? -2f : 2f);
        float cx = (float) Math.sin(angle) * radius;
        float cy = (float) Math.cos(angle) * radius;
        return new Point(Math.round(location.x + cx), Math.round(location.y + cy));
    }

    @Override
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

    @Override
    public void resize(Point target) {
        size = Math.abs((int) Math.round(CanvasUtil.distance(location, target) * 2));
    }

    private Point location;
    private int size = 12;

}
