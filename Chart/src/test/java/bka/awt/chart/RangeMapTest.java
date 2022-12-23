/*
** © Bart Kampers
*/

package bka.awt.chart;

import static org.junit.Assert.assertEquals;
import org.junit.*;

/**
 */
public class RangeMapTest {


    @Before
    public void setUp() {
        rangeMap = new RangeMap(null, null);
    }


    @Test
    public void test() {
        assertEquals(new Range(null, null), rangeMap.getDefault());
    }


    RangeMap rangeMap;

}