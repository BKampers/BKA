/*
** © Bart Kampers
*/

package bka.demo.graphs;

import java.awt.*;
import java.awt.geom.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.*;

public class CanvasUtilTest {

    @Test
    public void testSquareDistancePointLine() {
        assertEquals(2, CanvasUtil.squareDistance(new Point(0, 0), new Point(0, 2), new Point(2, 0)), PRECISION);
        assertEquals(0, CanvasUtil.squareDistance(new Point(1, 1), new Point(0, 2), new Point(2, 0)), PRECISION);
        assertEquals(0, CanvasUtil.squareDistance(new Point(0, 0), new Point(0, 0), new Point(1, 0)), PRECISION);
        assertEquals(0, CanvasUtil.squareDistance(new Point(0, 0), new Point(0, 1), new Point(0, 0)), PRECISION);
        assertEquals(4, CanvasUtil.squareDistance(new Point(2, 2), new Point(0, Integer.MIN_VALUE), new Point(0, Integer.MAX_VALUE)), PRECISION);
        assertEquals(9, CanvasUtil.squareDistance(new Point(3, 3), new Point(Integer.MIN_VALUE, 0), new Point(Integer.MAX_VALUE, 0)), PRECISION);
        assertEquals(8, CanvasUtil.squareDistance(new Point(0, 0), new Point(2, 2), new Point(3, 3)), PRECISION);
        assertEquals(8, CanvasUtil.squareDistance(new Point(5, 5), new Point(2, 2), new Point(3, 3)), PRECISION);
    }

    @Test
    public void testSquareDistancePoints() {
        assertEquals(2, CanvasUtil.squareDistance(new Point(0, 0), new Point(1, 1)), PRECISION);
    }

    @Test
    public void testDistancePoints() {
        assertEquals(Math.sqrt(2), CanvasUtil.distance(new Point(0, 0), new Point(1, 1)), PRECISION);
    }

    @Test
    public void testSlope() {
        assertEquals(0.5, CanvasUtil.slope(new Point(0, 0), new Point(2, 1)), PRECISION);
        assertEquals(2.0, CanvasUtil.slope(new Point(1, 2), new Point(0, 0)), PRECISION);
        assertEquals(-0.5, CanvasUtil.slope(new Point(0, 0), new Point(2, -1)), PRECISION);
        assertEquals(-2.0, CanvasUtil.slope(new Point(1, -2), new Point(0, 0)), PRECISION);
        assertEquals(0.0, CanvasUtil.slope(new Point(0, 0), new Point(1, 0)), PRECISION);
        assertEquals(0.0, CanvasUtil.slope(new Point(-2, 0), new Point(0, 0)), PRECISION);
        assertEquals(Double.POSITIVE_INFINITY, CanvasUtil.slope(new Point(0, 0), new Point(0, 1)), PRECISION);
        assertEquals(Double.NEGATIVE_INFINITY, CanvasUtil.slope(new Point(0, 0), new Point(0, -1)), PRECISION);
        assertTrue(Double.isNaN(CanvasUtil.slope(new Point(0, 0), new Point(0, 0))));
    }

    @Test
    public void testAngle() {
        assertEquals(0.00 * Math.PI, CanvasUtil.angle(Double.NEGATIVE_INFINITY), PRECISION);
        assertEquals(0.25 * Math.PI, CanvasUtil.angle(1.0), PRECISION);
        assertEquals(0.50 * Math.PI, CanvasUtil.angle(0.0), PRECISION);
        assertEquals(0.75 * Math.PI, CanvasUtil.angle(-1.0), PRECISION);
        assertEquals(1.00 * Math.PI, CanvasUtil.angle(Double.POSITIVE_INFINITY), PRECISION);
    }

    @Test
    public void testGetPointOnLine() {
        assertEquals(new Point(1, 1), CanvasUtil.getPointOnLine(new Point(0, 0), SQRT_2, 1.0));
        assertEquals(new Point(-1, -1), CanvasUtil.getPointOnLine(new Point(0, 0), -SQRT_2, 1.0));
        assertEquals(new Point(1, -1), CanvasUtil.getPointOnLine(new Point(0, 0), SQRT_2, -1.0));
        assertEquals(new Point(-1, 1), CanvasUtil.getPointOnLine(new Point(0, 0), -SQRT_2, -1.0));
        assertEquals(new Point(2, 2), CanvasUtil.getPointOnLine(new Point(0, 0), 2 * SQRT_2, 1.0));
        assertEquals(new Point(2, 2), CanvasUtil.getPointOnLine(new Point(1, 1), SQRT_2, 1.0));
    }

    @Test
    public void testIntersectionPoint() {
        assertEquals(new Point2D.Double(1.0, 1.0), CanvasUtil.intersectionPoint(new Point(0, 0), new Point(2, 0), new Point(0, 2)));
        assertEquals(new Point2D.Double(0.5, 0.5), CanvasUtil.intersectionPoint(new Point(0, 0), new Point(1, 0), new Point(0, 1)));
    }

    private static final double SQRT_2 = Math.sqrt(2);
    private static final double PRECISION = 0.000001;

}