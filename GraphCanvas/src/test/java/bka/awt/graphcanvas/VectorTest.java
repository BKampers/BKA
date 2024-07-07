package bka.awt.graphcanvas;

import org.junit.Test;
import static org.junit.Assert.*;


public class VectorTest {
    
    @Test
    public void testMagnitude() {
        assertEquals(0, new Vector(0, 0).magnitude(), PRECISION);
        assertEquals(Math.sqrt(1), new Vector(0, 1).magnitude(), PRECISION);
        assertEquals(Math.sqrt(2), new Vector(1, 1).magnitude(), PRECISION);
        assertEquals(Math.sqrt(5), new Vector(1, 2).magnitude(), PRECISION);
        assertEquals(Math.sqrt(8), new Vector(2, 2).magnitude(), PRECISION);
        assertEquals(Math.sqrt(13), new Vector(3, 2).magnitude(), PRECISION);
    }
    
    @Test
    public void testScale() {
        Vector vector = new Vector(1, 1);
        Vector scaled = vector.scale(2);
        assertEquals(2 * vector.magnitude(), scaled.magnitude(), PRECISION);
        Vector reverted = vector.scale(-1);
        assertEquals(vector.magnitude(), reverted.magnitude(), PRECISION);
    }
    
    @Test
    public void testAdd() {
        Vector vector = new Vector(1, 1);
        Vector resultant = vector.add(new Vector(0, 1));
        assertEquals(new Vector(1, 2).magnitude(), resultant.magnitude(), PRECISION);
    }
    
    @Test
    public void testSubtract() {
        Vector vector = new Vector(1, 1);
        Vector resultant = vector.subtract(new Vector(0, 1));
        assertEquals(new Vector(1, 0).magnitude(), resultant.magnitude(), PRECISION);
    }
    
    @Test
    public void testDotProduct() {
        Vector v1 = new Vector(0, 1);
        Vector v2 = new Vector(1, 0);
        Vector v3 = new Vector(1, 1);
        Vector v4 = new Vector(1, 2);
        assertEquals(0, Vector.dotProduct(v1, v2), PRECISION);
        assertEquals(1, Vector.dotProduct(v1, v3), PRECISION);
        assertEquals(1, Vector.dotProduct(v2, v3), PRECISION);
        assertEquals(3, Vector.dotProduct(v3, v4), PRECISION);
    }
    
    @Test
    public void testNormalized() {
        assertEquals(Double.NaN, new Vector(0, 0).normalized().magnitude(), PRECISION);
        assertEquals(1, new Vector(1, 1).normalized().magnitude(), PRECISION);
        assertEquals(1, new Vector(2, 2).normalized().magnitude(), PRECISION);
        assertEquals(1, new Vector(3, 4).normalized().magnitude(), PRECISION);
    }

    @Test
    public void testCosine() {
        assertEquals(Math.cos(0), Vector.cosine(new Vector(0, 1), new Vector(0, 1)), PRECISION);
        assertEquals(Math.cos(0.5 * Math.PI), Vector.cosine(new Vector(0, 1), new Vector(-1, 0)), PRECISION);
        assertEquals(Math.cos(0.25 * Math.PI), Vector.cosine(new Vector(0, 1), new Vector(1, 1)), PRECISION);
        assertEquals(Math.cos(0.75 * Math.PI), Vector.cosine(new Vector(0, -1), new Vector(1, 1)), PRECISION);
        assertEquals(Math.cos(Math.PI), Vector.cosine(new Vector(0, -1), new Vector(0, 1)), PRECISION);
        assertEquals(Math.cos(1.25 * Math.PI), Vector.cosine(new Vector(0, -1), new Vector(1, 1)), PRECISION);
        assertEquals(Math.cos(1.5 * Math.PI), Vector.cosine(new Vector(0, -1), new Vector(1, 0)), PRECISION);
        assertEquals(Math.cos(1.75 * Math.PI), Vector.cosine(new Vector(0, -1), new Vector(-1, -1)), PRECISION);
    }
    
    private static final double PRECISION = 1e-15;
   
}
