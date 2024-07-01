package bka.awt.graphcanvas;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author bartkampers
 */
public class VectorTest {

    @Test
    public void testMagnitude() {
        assertEquals(Math.sqrt(1), new Vector(0, 1).magnitude(), 0);
        assertEquals(Math.sqrt(2), new Vector(1, 1).magnitude(), 0);
        assertEquals(Math.sqrt(5), new Vector(1, 2).magnitude(), 0);
        assertEquals(Math.sqrt(8), new Vector(2, 2).magnitude(), 0);
        assertEquals(Math.sqrt(13), new Vector(3, 2).magnitude(), 0);
    }
    
    @Test
    public void testDotProduct() {
        Vector v1 = new Vector(0, 1);
        Vector v2 = new Vector(1, 0);
        Vector v3 = new Vector(1, 1);
        Vector v4 = new Vector(1, 2);
        assertEquals(0, Vector.dotProduct(v1, v2), 0);
        assertEquals(1, Vector.dotProduct(v1, v3), 0);
        assertEquals(1, Vector.dotProduct(v2, v3), 0);
        assertEquals(3, Vector.dotProduct(v3, v4), 0);
    }
   
}
