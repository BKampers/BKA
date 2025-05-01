/*
** © Bart Kampers
*/

package bka.demo.clock.weatherstation;

import java.text.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.*;

public class CardinalNumberFormatTest {

    @BeforeEach
    public void init() {
        format = new CardinalNumberFormat();
    }

    @Test
    public void testFormat() {
        assertEquals("N", format.format(0));
        assertEquals("NNO", format.format(20));
        assertEquals("NO", format.format(40));
        assertEquals("ONO", format.format(67));
        assertEquals("O", format.format(89.99));
        assertEquals("OZO", format.format(472.5));
        assertEquals("ZO", format.format(495.0001));
        assertEquals("ZZO", format.format(157.5));
        assertEquals("Z", format.format(-540));
        assertEquals("ZZW", format.format(360 * 10000 + 202.5));
        assertEquals("ZW", format.format(360 * -9999 + 225));
        assertEquals("WZW", format.format(247.5));
        assertEquals("W", format.format(-90));
        assertEquals("WNW", format.format(292.5));
        assertEquals("NW", format.format(315.0));
        assertEquals("NNW", format.format(337.5));
        assertEquals("N", format.format(360));
    }

    @Test
    public void testParse() throws ParseException {
        assertEquals(0.0, format.parse("N"));
        assertEquals(22.5, format.parse("NNO"));
        assertEquals(45.0, format.parse("NO"));
        assertEquals(67.5, format.parse("ONO"));
        assertEquals(90.0, format.parse("O"));
        assertEquals(112.5, format.parse("OZO"));
        assertEquals(135.0, format.parse("ZO"));
        assertEquals(157.5, format.parse("ZZO"));
        assertEquals(180.0, format.parse("Z"));
        assertEquals(202.5, format.parse("ZZW"));
        assertEquals(225.0, format.parse("ZW"));
        assertEquals(247.5, format.parse("WZW"));
        assertEquals(270.0, format.parse("W"));
        assertEquals(292.5, format.parse("WNW"));
        assertEquals(315.0, format.parse("NW"));
        assertEquals(337.5, format.parse("NNW"));
        assertThrows(ParseException.class, () -> format.parse("NNNW"));
    }

    private CardinalNumberFormat format;

}