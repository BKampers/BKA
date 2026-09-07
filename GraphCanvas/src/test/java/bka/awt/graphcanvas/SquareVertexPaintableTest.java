/*
** © Bart Kampers
*/

package bka.awt.graphcanvas;

import java.awt.*;
import org.junit.*;
import static org.junit.Assert.*;


public final class SquareVertexPaintableTest {

    @Test
    public void testResizeToCenterDoesNotThrow() {
        SquareVertexPaintable square = new SquareVertexPaintable(new Dimension(20, 20));
        Point location = new Point(50, 50);
        square.resize(location, location, ResizeDirection.NORTH_EAST);
        Point connector = square.getConnectorPoint(location, new Point(80, 60));
        assertNotNull(connector);
        assertEquals(2, square.getSize().width);
        assertEquals(2, square.getSize().height);
    }

}
