/*
** © Bart Kampers
*/

package bka.awt.graphcanvas;

import java.awt.*;
import java.awt.geom.*;
import org.junit.*;
import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LineTest {

    @Test
    public void testInvalidPoint() {
        assertThrows(IllegalArgumentException.class, () -> {
            Line.through(new Point2D.Double(Double.NEGATIVE_INFINITY, 0), new Point2D.Double());
        });
        assertThrows(IllegalArgumentException.class, () -> {
            Line.through(new Point2D.Double(0, Double.NaN), new Point2D.Double());
        });
        assertThrows(IllegalArgumentException.class, () -> {
            Line.through(new Point2D.Double(), new Point2D.Double(Double.NaN, 0));
        });
        assertThrows(IllegalArgumentException.class, () -> {
            Line.through(new Point2D.Double(), new Point2D.Double(0, Double.POSITIVE_INFINITY));
        });
    }

    @Test
    public void testEquals() {
        assertEquals(Y_EQUALS_X, Line.through(new Point(0, 0), new Point(-1, -1)));
        assertEquals(Y_EQUALS_MINUS_X, Line.through(new Point(0, 0), new Point(-1, 1)));
    }

    @Test
    public void testGetSlope() {
        final double PRECISION = 0;
        assertEquals(1.0, Y_EQUALS_X.getSlope(), PRECISION);
        assertEquals(-1.0, Y_EQUALS_MINUS_X.getSlope(), PRECISION);
        assertEquals(0.5, Y_EQUALS_HALF_X.getSlope(), PRECISION);
        assertEquals(2.0, Y_EQUALS_TWO_X.getSlope(), PRECISION);
        assertEquals(0.0, Y_EQUALS_ZERO.getSlope(), PRECISION);
        assertEquals(Double.POSITIVE_INFINITY, X_EQUALS_ZERO.getSlope(), PRECISION);
        assertEquals(Double.NEGATIVE_INFINITY, Line.through(new Point(0, 0), new Point(0, -1)).getSlope(), PRECISION);
    }

    @Test
    public void testGetOffset() {
        final double PRECISION = 0;
        assertEquals(0.0, Y_EQUALS_X.getOffset(), PRECISION);
        assertEquals(0.0, Y_EQUALS_HALF_X.getOffset(), PRECISION);
        assertEquals(1.0, Y_EQUALS_X_PLUS_ONE.getOffset(), PRECISION);
        assertEquals(0.5, Y_EQUALS_X_PLUS_A_HALF.getOffset(), PRECISION);
        assertEquals(-0.5, Y_EQUALS_X_MINUS_A_HALF.getOffset(), PRECISION);
        assertEquals(2.0, Line.through(new Point(0, 2), new Point(1, 2)).getOffset(), PRECISION);
        assertEquals(0.0, Line.through(new Point(0, 0), new Point(0, 1)).getOffset(), PRECISION);
        assertEquals(1.0, Line.through(new Point(1, 0), new Point(1, -1)).getOffset(), PRECISION);
    }

    @Test
    public void testIsVertical() {
        assertTrue(X_EQUALS_ZERO.isVertical());
        assertTrue(X_EQUALS_ONE.isVertical());
        assertFalse(Y_EQUALS_ZERO.isVertical());
        assertFalse(Y_EQUALS_X.isVertical());
    }

    @Test
    public void testIsParallelTo() {
        assertTrue(X_EQUALS_ZERO.isParallelTo(X_EQUALS_ONE));
        assertTrue(Y_EQUALS_ONE.isParallelTo(Y_EQUALS_ZERO));
        assertTrue(Y_EQUALS_X.isParallelTo(Y_EQUALS_X_PLUS_ONE));
        assertFalse(X_EQUALS_ZERO.isParallelTo(Y_EQUALS_ONE));
        assertFalse(Y_EQUALS_X.isParallelTo(X_EQUALS_ONE));
        assertFalse(Y_EQUALS_X.isParallelTo(Y_EQUALS_ONE));
        assertFalse(Y_EQUALS_X.isParallelTo(Y_EQUALS_TWO_X));
    }

    @Test
    public void testGetY() {
        final double PRECISION = 0;
        assertEquals(0.0, Y_EQUALS_ZERO.getY(0), PRECISION);
        assertEquals(1.0, new Line(0.5, 1).getY(0), PRECISION);
        assertEquals(1.25, new Line(0.5, 1).getY(0.5), PRECISION);
        assertEquals(1.5, new Line(0.5, 1).getY(1), PRECISION);
        assertEquals(0.5, new Line(0.5, 1).getY(-1), PRECISION);
        assertEquals(2.0, Line.through(new Point(0, 2), new Point(1, 2)).getY(0.0), PRECISION);
        assertEquals(Double.NaN, Line.through(new Point(0, 0), new Point(0, 1)).getY(0.0), PRECISION);
        assertEquals(Double.NaN, Line.through(new Point(0, 0), new Point(0, 1)).getY(1.0), PRECISION);
    }

    @Test
    public void testIntersection() {
        assertEquals(new Point2D.Double(0, 0), Y_EQUALS_HALF_X.intersection(Y_EQUALS_TWO_X).get());
        assertEquals(new Point2D.Double(2, 1), Y_EQUALS_HALF_X.intersection(Y_EQUALS_TWO_X_MINUS_THREE).get());
        assertEquals(new Point2D.Double(0, 0), X_EQUALS_ZERO.intersection(Y_EQUALS_ZERO).get());
        assertEquals(new Point2D.Double(0, 1), X_EQUALS_ZERO.intersection(Y_EQUALS_X_PLUS_ONE).get());
        assertEquals(new Point2D.Double(-1, 0), Y_EQUALS_ZERO.intersection(Y_EQUALS_X_PLUS_ONE).get());
        assertFalse(Y_EQUALS_X.intersection(Y_EQUALS_X_PLUS_ONE).isPresent());
        assertFalse(Y_EQUALS_ZERO.intersection(Y_EQUALS_ONE).isPresent());
        assertFalse(X_EQUALS_ZERO.intersection(X_EQUALS_ONE).isPresent());
    }

    private static final Line Y_EQUALS_X = Line.through(new Point(0, 0), new Point(1, 1));
    private static final Line Y_EQUALS_MINUS_X = Line.through(new Point(0, 0), new Point(1, -1));
    private static final Line Y_EQUALS_HALF_X = Line.through(new Point(0, 0), new Point(2, 1));
    private static final Line Y_EQUALS_TWO_X = Line.through(new Point(0, 0), new Point(1, 2));
    private static final Line Y_EQUALS_X_MINUS_A_HALF = Line.through(new Point(-1, 0), new Point(1, -1));
    private static final Line Y_EQUALS_X_PLUS_A_HALF = Line.through(new Point(-1, 0), new Point(1, 1));
    private static final Line Y_EQUALS_X_PLUS_ONE = Line.through(new Point(0, 1), new Point(1, 2));
    private static final Line Y_EQUALS_TWO_X_MINUS_THREE = new Line(2, -3);
    private static final Line Y_EQUALS_ZERO = Line.through(new Point(0, 0), new Point(1, 0));
    private static final Line Y_EQUALS_ONE = Line.through(new Point(0, 1), new Point(1, 1));
    private static final Line X_EQUALS_ZERO = Line.through(new Point(0, 0), new Point(0, 1));
    private static final Line X_EQUALS_ONE = Line.through(new Point(1, 0), new Point(1, 1));
}
