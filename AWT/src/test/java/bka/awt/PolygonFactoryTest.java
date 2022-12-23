/*
** © Bart Kampers
*/

package bka.awt;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.*;

public class PolygonFactoryTest {

    @Test
    public void testToPolygon() {
        Polygon polygon = PolygonFactory.toPolygon(java.util.List.of(new Point2D.Double(0.0, 1.0)));
        assertEquals(1, polygon.npoints);
        assertArrayEquals(new int[]{ 0 }, xPoints(polygon));
        assertArrayEquals(new int[]{ 1 }, yPoints(polygon));
        polygon = PolygonFactory.toPolygon(java.util.List.of(new Point2D.Double(0.4, 0.5), new Point2D.Double(-0.4, -1.5)));
        assertEquals(2, polygon.npoints);
        assertArrayEquals(new int[]{ 0, 0 }, xPoints(polygon));
        assertArrayEquals(new int[]{ 1, -1 }, yPoints(polygon));
        polygon = PolygonFactory.toPolygon(java.util.List.of(
            new Point2D.Float(Integer.MAX_VALUE + 1.0f, Integer.MIN_VALUE - 1.0f),
            new Point2D.Double(Double.MAX_VALUE, -Double.MAX_VALUE),
            new Point2D.Double(Long.MAX_VALUE, Long.MIN_VALUE)));
        assertEquals(3, polygon.npoints);
        assertArrayEquals(new int[]{ Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE }, xPoints(polygon));
        assertArrayEquals(new int[]{ Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE }, yPoints(polygon));
    }

    private int[] xPoints(Polygon polygon) {
        return Arrays.copyOf(polygon.xpoints, polygon.npoints);
    }

    private int[] yPoints(Polygon polygon) {
        return Arrays.copyOf(polygon.ypoints, polygon.npoints);
    }

    @Test
    public void testCreatePolygon() {
        Polygon polygon = PolygonFactory.createPolygon(4, 1);
        assertEquals(4, polygon.npoints);
        assertArrayEquals(new int[]{ 0, 1, 0, -1 }, polygon.xpoints);
        assertArrayEquals(new int[]{ -1, 0, 1, 0 }, polygon.ypoints);
    }

    @Test
    public void testCreateRegularPolygon() {
        final double radius = 1.0;
        for (int count = 3; count <= 1000; ++count) {
            java.util.List<Point2D> polygon = PolygonFactory.createRegularPolygon(count, radius);
            assertEquals(count, polygon.size());
            Point2D p0 = polygon.get(polygon.size() - 1);
            for (Point2D p1 : polygon) {
                assertEquals(radius * radius, squareDistanceToOrigin(p1), PRECISION);
                assertEquals(2 * Math.PI / count, angle(p0, p1), PRECISION);
                p0 = p1;
            }
        }
    }

    @Test
    public void testCreateStarPolygon() {
        Polygon star = PolygonFactory.createStar(4, 1, 2);
        assertEquals(8, star.npoints);
        assertArrayEquals(new int[]{ 0, 1, 2, 1, 0, -1, -2, - 1 }, star.xpoints);
        assertArrayEquals(new int[]{ -2, -1, 0, 1, 2, 1, 0, -1 }, star.ypoints);
    }

    @Test
    public void testCreateStar() {
        final double innerRadius = 5.0;
        final double outerRadius = 10.0;
        final double squareInnerRadius = innerRadius * innerRadius;
        final double squareOuterRadius = outerRadius * outerRadius;
        for (int count = 2; count < 50; ++count) {
            final double[] expectedSquareRadius = new double[2];
            final double expectedAngle = Math.PI / count;
            java.util.List<Point2D> star = PolygonFactory.createStar(count, innerRadius, outerRadius);
            assertEquals(2 * count, star.size());
            Point2D p0 = star.get(star.size() - 1);
            for (int i = 0; i < star.size(); ++i) {
                Point2D p1 = star.get(i);
                assertEquals(expectedAngle, angle(p0, p1), PRECISION);
                double actualSquareRadius = squareDistanceToOrigin(p1);
                if (i == 0) {
                    if (nearEqual(squareOuterRadius, actualSquareRadius)) {
                        expectedSquareRadius[0] = squareOuterRadius;
                        expectedSquareRadius[1] = squareInnerRadius;
                    }
                    else if (nearEqual(squareInnerRadius, actualSquareRadius)) {
                        expectedSquareRadius[0] = squareInnerRadius;
                        expectedSquareRadius[1] = squareOuterRadius;
                    }
                    else {
                        fail("Unexpected radius. Expected: " + Math.sqrt(actualSquareRadius));
                    }
                }
                else {
                    assertEquals(expectedSquareRadius[i % 2], actualSquareRadius, PRECISION);
                }
                p0 = p1;
            }
        }
    }

    private static boolean nearEqual(final double value1, double value2) {
        return value1 - PRECISION < value2 && value2 < value1 + PRECISION;
    }

    private double squareDistanceToOrigin(Point2D point) {
        return squareDistanceToOrigin(point.getX(), point.getY());
    }

    private double squareDistanceToOrigin(double x, double y) {
        return x * x + y * y;
    }

    private double angle(Point2D p, Point2D q) {
        return angle(p.getX(), p.getY(), q.getX(), q.getY());
    }

    private double angle(double px, double py, double qx, double qy) {
        return Math.acos((px * qx + py * qy) / (Math.sqrt(px * px + py * py) * Math.sqrt(qx * qx + qy * qy)));
    }

    private static final double PRECISION = 0.000001;
}
