/*
** © Bart Kampers
*/

package bka.awt.grapcanvas;

import bka.awt.graphcanvas.CanvasUtil;
import java.awt.*;
import java.awt.geom.*;
import static org.junit.Assert.assertEquals;
import org.junit.*;

public class CanvasUtilTest {

    @Test
    public void testSquareDistancePointLine() {
        assertEquals(2, CanvasUtil.squareDistance(new Point(0, 0), new Point(0, 2), new Point(2, 0)));
        assertEquals(0, CanvasUtil.squareDistance(new Point(1, 1), new Point(0, 2), new Point(2, 0)));
        assertEquals(0, CanvasUtil.squareDistance(new Point(0, 0), new Point(0, 0), new Point(1, 0)));
        assertEquals(0, CanvasUtil.squareDistance(new Point(0, 0), new Point(0, 1), new Point(0, 0)));
        assertEquals(4, CanvasUtil.squareDistance(new Point(2, 2), new Point(0, Integer.MIN_VALUE), new Point(0, Integer.MAX_VALUE)));
        assertEquals(9, CanvasUtil.squareDistance(new Point(3, 3), new Point(Integer.MIN_VALUE, 0), new Point(Integer.MAX_VALUE, 0)));
        assertEquals(8, CanvasUtil.squareDistance(new Point(0, 0), new Point(2, 2), new Point(3, 3)));
        assertEquals(8, CanvasUtil.squareDistance(new Point(5, 5), new Point(2, 2), new Point(3, 3)));
    }

    @Test
    public void testSquareDistancePoints() {
        assertEquals(2, CanvasUtil.squareDistance(new Point(0, 0), new Point(1, 1)));
        assertEquals(5, CanvasUtil.squareDistance(new Point(0, 0), new Point(1, -2)));
        assertEquals(8, CanvasUtil.squareDistance(new Point(0, 0), new Point(-2, 2)));
        assertEquals(13, CanvasUtil.squareDistance(new Point(0, 0), new Point(-3, -2)));
    }

    @Test
    public void testIntersectionPoint() {
        assertEquals(new Point2D.Double(1.0, 1.0), CanvasUtil.intersectionPoint(new Point(0, 0), new Point(2, 0), new Point(0, 2)));
        assertEquals(new Point2D.Double(0.5, 0.5), CanvasUtil.intersectionPoint(new Point(0, 0), new Point(1, 0), new Point(0, 1)));
        assertEquals(new Point2D.Double(2.0, 1.0), CanvasUtil.intersectionPoint(new Point(1, 3), new Point(0, 0), new Point(4, 2)));
    }

    @Test
    public void testSlope() {
        final double PRECISION = 0;
        assertEquals(2.0, CanvasUtil.slope(new Point(0, 0), new Point(1, 2)), PRECISION);
        assertEquals(0.5, CanvasUtil.slope(new Point(0, 0), new Point(2, 1)), PRECISION);
        assertEquals(-2.0, CanvasUtil.slope(new Point(0, 0), new Point(-1, 2)), PRECISION);
        assertEquals(-0.5, CanvasUtil.slope(new Point(0, 0), new Point(-2, 1)), PRECISION);
        assertEquals(-2.0, CanvasUtil.slope(new Point(0, 0), new Point(1, -2)), PRECISION);
        assertEquals(-0.5, CanvasUtil.slope(new Point(0, 0), new Point(2, -1)), PRECISION);
        assertEquals(2.0, CanvasUtil.slope(new Point(0, 0), new Point(-1, -2)), PRECISION);
        assertEquals(0.5, CanvasUtil.slope(new Point(0, 0), new Point(-2, -1)), PRECISION);
        assertEquals(0.0, CanvasUtil.slope(new Point(0, 0), new Point(2, 0)), PRECISION);
        assertEquals(0.0, CanvasUtil.slope(new Point(0, 0), new Point(-2, 0)), PRECISION);
        assertEquals(Double.POSITIVE_INFINITY, CanvasUtil.slope(new Point(0, 0), new Point(0, 2)), PRECISION);
        assertEquals(Double.NEGATIVE_INFINITY, CanvasUtil.slope(new Point(0, 0), new Point(0, -2)), PRECISION);
        assertEquals(Double.NaN, CanvasUtil.slope(new Point(0, 0), new Point(0, 0)), PRECISION);
    }

}