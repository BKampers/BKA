/*
 * © Bart Kampers
 */

package bka.awt.chart;

import org.junit.*;
import static org.junit.Assert.*;


public class RangeTest {


    @Test
    public void testSetMin() {
        Range range = new Range(1, null);
        range.setMin(0);
        assertEquals(0, range.getMin());
        range.setMin(2);
        assertEquals(2, range.getMin());
        assertNull(range.getMax());
    }

    @Test
    public void testSetMax() {
        Range range = new Range(null, 1);
        range.setMax(2);
        assertEquals(2, range.getMax());
        range.setMax(0);
        assertEquals(0, range.getMax());
        assertNull(range.getMin());
    }

    @Test
    public void testSet() {
        Range range = new Range(null, null);
        range.set(0, 1);
        assertEquals(0, range.getMin());
        assertEquals(1, range.getMax());
        range.set(2, 3);
        assertEquals(2, range.getMin());
        assertEquals(3, range.getMax());
        range.set(null, null);
        assertNull(range.getMin());
        assertNull(range.getMax());
    }

    @Test
    public void testAdjustMin() {
        Range range = new Range(1, null);
        range.adjustMin(0);
        assertEquals(0, range.getMin());
        range.adjustMin(2);
        assertEquals(0, range.getMin());
        assertNull(range.getMax());
    }

    @Test
    public void testAdjustMax() {
        Range range = new Range(null, 1);
        range.adjustMax(2);
        assertEquals(2, range.getMax());
        range.adjustMax(0);
        assertEquals(2, range.getMax());
        assertNull(range.getMin());
    }

    @Test
    public void testAdjust() {
        Range range = new Range(null, null);
        range.adjust(0);
        assertEquals(0, range.getMin());
        assertEquals(0, range.getMax());
        range.adjust(1);
        assertEquals(0, range.getMin());
        assertEquals(1, range.getMax());
        range.adjust(-1);
        assertEquals(-1, range.getMin());
        assertEquals(1, range.getMax());
        range.adjust(0);
        assertEquals(-1, range.getMin());
        assertEquals(1, range.getMax());
    }

    @Test
    public void testIsInitialized() {
        Range range = new Range(null, null);
        assertFalse(range.isInitialized());
        range.set(null, 1);
        assertFalse(range.isInitialized());
        range.set(1, null);
        assertFalse(range.isInitialized());
        range.set(1, 2);
        assertTrue(range.isInitialized());
    }


    @Test
    public void testIncludes() {
        Range range = new Range(null, null);
        assertTrue(range.includes(0));
        range.set(null, 1);
        assertTrue(range.includes(0));
        assertTrue(range.includes(1));
        assertFalse(range.includes(2));
        range.set(1, null);
        assertFalse(range.includes(0));
        assertTrue(range.includes(1));
        assertTrue(range.includes(2));
        range.set(1, 1);
        assertFalse(range.includes(0));
        assertTrue(range.includes(1));
        assertFalse(range.includes(2));
        range.set(1, 3);
        assertFalse(range.includes(0.999));
        assertTrue(range.includes(1));
        assertTrue(range.includes(2));
        assertTrue(range.includes(3));
        assertFalse(range.includes(3.0001));
    }

}