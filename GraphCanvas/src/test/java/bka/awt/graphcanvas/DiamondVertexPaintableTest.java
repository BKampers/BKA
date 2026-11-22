/*
** © Bart Kampers
*/

package bka.awt.graphcanvas;

import java.awt.*;
import org.junit.*;
import static org.junit.Assert.*;


public final class DiamondVertexPaintableTest {

    @Test
    public void testDiamondStandsOnACorner() {
        DiamondVertexPaintable diamond = new DiamondVertexPaintable(new Dimension(100, 40));
        Point location = new Point(0, 0);
        assertEquals(0, diamond.distance(location, new Point(0, -20)), DISTANCE_TOLERANCE);
        assertEquals(0, diamond.distance(location, new Point(50, 0)), DISTANCE_TOLERANCE);
        assertEquals(0, diamond.distance(location, new Point(0, 20)), DISTANCE_TOLERANCE);
        assertEquals(0, diamond.distance(location, new Point(-50, 0)), DISTANCE_TOLERANCE);
        assertTrue(diamond.distance(location, new Point(50, -20)) > 0);
        assertTrue(diamond.distance(location, new Point(0, 0)) < 0);
    }

    @Test
    public void testConnectorMeetsTheBoundary() {
        DiamondVertexPaintable diamond = new DiamondVertexPaintable(new Dimension(100, 40));
        Point location = new Point(10, 10);
        assertEquals(new Point(10, -10), diamond.getConnectorPoint(location, new Point(10, -100)));
        assertEquals(new Point(60, 10), diamond.getConnectorPoint(location, new Point(200, 10)));
        assertEquals(new Point(35, 0), diamond.getConnectorPoint(location, new Point(60, -10)));
    }

    @Test
    public void testResizePointsAndEdges() {
        DiamondVertexPaintable diamond = new DiamondVertexPaintable(new Dimension(100, 40));
        Point location = new Point(0, 0);
        diamond.resize(location, new Point(0, -30), ResizeDirection.NORTH);
        assertEquals(100, diamond.getWidth());
        assertEquals(60, diamond.getHeight());

        diamond.resize(location, new Point(40, 0), ResizeDirection.EAST);
        assertEquals(80, diamond.getWidth());
        assertEquals(60, diamond.getHeight());

        Point onEdge = new Point(20, -15);
        diamond.resize(location, onEdge, ResizeDirection.NORTH_EAST);
        assertEquals(80, diamond.getWidth());
        assertEquals(60, diamond.getHeight());
        assertEquals(0, diamond.distance(location, onEdge), 1.0);

        diamond.resize(location, new Point(40, -30), ResizeDirection.NORTH_EAST);
        assertEquals(160, diamond.getWidth());
        assertEquals(120, diamond.getHeight());
        assertEquals(0, diamond.distance(location, new Point(40, -30)), 1.0);
    }

    @Test
    public void testResizeToCenterDoesNotThrow() {
        DiamondVertexPaintable diamond = new DiamondVertexPaintable(new Dimension(20, 20));
        Point location = new Point(50, 50);
        diamond.resize(location, location, ResizeDirection.NORTH_EAST);
        assertNotNull(diamond.getConnectorPoint(location, new Point(80, 60)));
        assertEquals(2, diamond.getWidth());
        assertEquals(2, diamond.getHeight());
    }

    private static final double DISTANCE_TOLERANCE = 1e-6;

}
