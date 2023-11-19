/*
** © Bart Kampers
*/

package bka.demo.graphs;

import java.awt.*;
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
    public void testGetPoint() {
        assertEquals(new Point(1, 1), CanvasUtil.getPoint(new Point(0, 0), Math.sqrt(2.0), 1.0));
        assertEquals(new Point(-1, -1), CanvasUtil.getPoint(new Point(0, 0), -Math.sqrt(2.0), 1.0));
        assertEquals(new Point(1, -1), CanvasUtil.getPoint(new Point(0, 0), Math.sqrt(2.0), -1.0));
        assertEquals(new Point(-1, 1), CanvasUtil.getPoint(new Point(0, 0), -Math.sqrt(2.0), -1.0));
    }

    private static final double PRECISION = 0.000001;

}