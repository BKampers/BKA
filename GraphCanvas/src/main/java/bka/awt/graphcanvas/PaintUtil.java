/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
 */
package bka.awt.graphcanvas;

import java.awt.*;


public class PaintUtil {

    public static void paintConnectorPoint(Graphics2D graphics, Point location) {
        paintDot(graphics, location, EDGE_POINT_RADIUS, CONNECTOR_POINT_PAINT);
    }

    public static void paintNewConnectorPoint(Graphics2D graphics, Point location) {
        paintCircle(graphics, location, NEW_CONNECTOR_POINT_RADIUS, CONNECTOR_POINT_PAINT);
    }

    public static void paintEdgePoint(Graphics2D graphics, Point location) {
        paintDot(graphics, location, EDGE_POINT_RADIUS, EDGE_POINT_PAINT);
    }

    public static void paintNewEdgePoint(Graphics2D graphics, Point location) {
        paintCircle(graphics, location, EDGE_POINT_RADIUS, EDGE_POINT_PAINT);
    }

    public static void paintDot(Graphics2D graphics, Point location, int radius, Paint paint) {
        int size = radius * 2;
        graphics.setPaint(paint);
        graphics.setStroke(DEFAULT_STROKE);
        graphics.fillOval(location.x - radius, location.y - radius, size, size);
    }

    public static void paintCircle(Graphics2D graphics, Point location, int radius, Paint paint) {
        int size = radius * 2;
        graphics.setPaint(paint);
        graphics.setStroke(DEFAULT_STROKE);
        graphics.drawOval(location.x - radius, location.y - radius, size, size);
    }

    private PaintUtil() {
    }

    private static final int EDGE_POINT_RADIUS = 3;
    private static final int NEW_CONNECTOR_POINT_RADIUS = 4;

    private static final Color EDGE_POINT_PAINT = Color.RED;
    private static final Color CONNECTOR_POINT_PAINT = Color.MAGENTA;

    private static final Stroke DEFAULT_STROKE = new BasicStroke();

}
