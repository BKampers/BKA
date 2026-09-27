/*
** © Bart Kampers
*/

package bka.awt.graphcanvas;

import java.awt.*;
import org.junit.*;
import static org.junit.Assert.*;

public class EdgeComponentTest {

    @Before
    public void init() {
        start = new VertexComponent(new RoundVertexPaintable(new Dimension(10, 10)), new Point(0, 0));
        end = new VertexComponent(new RoundVertexPaintable(new Dimension(10, 10)), new Point(100, 0));
    }

    @Test
    public void testDiamondStaysAtStart() {
        EdgeComponent edge = new EdgeComponent(start, end, PolygonPaintable::create, DiamondPaintable::new);
        EdgeDecorationPaintable diamond = (EdgeDecorationPaintable) edge.getDecorationPaintable();
        assertDecorationLine(edge, diamond, 0);
        edge.addPoint(new Point(25, 20));
        assertDecorationLine(edge, diamond, 0);
        edge.addPoint(new Point(50, 20));
        assertDecorationLine(edge, diamond, 0);
        edge.addPoint(1, new Point(40, 30));
        assertDecorationLine(edge, diamond, 0);
        edge.addPoint(0, new Point(10, 10));
        assertDecorationLine(edge, diamond, 0);
    }

    @Test
    public void testArrowheadOnMiddleLine() {
        EdgeComponent edge = new EdgeComponent(start, end, PolygonPaintable::create, ArrowheadPaintable::new);
        EdgeDecorationPaintable arrowhead = (EdgeDecorationPaintable) edge.getDecorationPaintable();
        assertDecorationLine(edge, arrowhead, 0);
        edge.addPoint(new Point(25, 20));
        assertDecorationLine(edge, arrowhead, 0);
        edge.addPoint(new Point(50, 20));
        assertDecorationLine(edge, arrowhead, 1);
        edge.addPoint(new Point(75, 20));
        assertDecorationLine(edge, arrowhead, 1);
    }

    private static void assertDecorationLine(EdgeComponent edge, EdgeDecorationPaintable decoration, int expectedIndex) {
        assertEquals(edge.getPoint(expectedIndex), decoration.getStartPoint().get());
        assertEquals(edge.getPoint(expectedIndex + 1), decoration.getEndPoint().get());
    }

    private VertexComponent start;
    private VertexComponent end;

}
