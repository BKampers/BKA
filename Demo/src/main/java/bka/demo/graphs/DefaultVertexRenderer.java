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
        float angle = (float) Math.atan(dx / dy);
        float radius = size / ((dy < 0) ? -2f : 2f);
        float cx = (float) Math.sin(angle) * radius;
        float cy = (float) Math.cos(angle) * radius;
        return new Point(Math.round(location.x + cx), Math.round(location.y + cy));
    }

    @Override
    public long squareDistance(Point point) {
        long dx = location.x - point.x;
        long dy = location.y - point.y;
        long radius = size / 2;
        return (dx * dx + dy * dy) - radius * radius;
    }

    @Override
    public void paint(Graphics2D graphics) {
        int radius = size / 2;
        graphics.fillOval(location.x - radius, location.y - radius, size, size);
    }

    @Override
    public void resize(Point target) {
        size = Math.abs((int) Math.round(Math.sqrt(CanvasUtil.squareDistance(location, target)) * 2));
    }

    private Point location;
    private int size = 12;

}
