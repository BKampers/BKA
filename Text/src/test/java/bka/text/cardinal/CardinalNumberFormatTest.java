/*
** © Bart Kampers
*/

package bka.text.cardinal;

import java.text.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

public class CardinalNumberFormatTest {

    @BeforeAll
    public static void initialize() {
        Locale.setDefault(Locale.ENGLISH);
    }

    @Test
    public void testFormat() {
        CardinalNumberFormat format = new CardinalNumberFormat();
        assertEquals("N", format.format(-360));
        assertEquals("N", format.format(-0.0000001));
        assertEquals("N", format.format(-0.0));
        assertEquals("N", format.format(0));
        assertEquals("NNE", format.format(20));
        assertEquals("NE", format.format(40));
        assertEquals("ENE", format.format(67));
        assertEquals("E", format.format(89.99));
        assertEquals("ESE", format.format(472.5));
        assertEquals("SE", format.format(495.0001));
        assertEquals("SSE", format.format(157.5));
        assertEquals("S", format.format(-540));
        assertEquals("SSW", format.format(360 * 10000 + 202.5));
        assertEquals("SW", format.format(360 * -9999 + 225));
        assertEquals("WSW", format.format(247.5));
        assertEquals("W", format.format(-90));
        assertEquals("WNW", format.format(292.5));
        assertEquals("NW", format.format(314.999999));
        assertEquals("NNW", format.format(337.5));
        assertEquals("N", format.format(360));
        assertEquals("N", format.format(360.01));
        assertThrows(IllegalArgumentException.class, () -> format.format(Double.NaN));
    }

    @ParameterizedTest
    @ValueSource(strings = { "de", "en", "nl", "?" })
    public void testFormatLocale(String locale) {
        CardinalNumberFormat format = new CardinalNumberFormat(Locale.of(locale));
        assertEquals(translated("N", locale), format.format(0.0));
        assertEquals(translated("NNE", locale), format.format(22.5));
        assertEquals(translated("NE", locale), format.format(45.0));
        assertEquals(translated("ENE", locale), format.format(67.5));
        assertEquals(translated("E", locale), format.format(90.0));
        assertEquals(translated("ESE", locale), format.format(112.5));
        assertEquals(translated("SE", locale), format.format(135.0));
        assertEquals(translated("SSE", locale), format.format(157.5));
        assertEquals(translated("S", locale), format.format(180.0));
        assertEquals(translated("SSW", locale), format.format(202.5));
        assertEquals(translated("SW", locale), format.format(225.0));
        assertEquals(translated("WSW", locale), format.format(247.5));
        assertEquals(translated("W", locale), format.format(270.0));
        assertEquals(translated("WNW", locale), format.format(292.5));
        assertEquals(translated("NW", locale), format.format(315.0));
        assertEquals(translated("NNW", locale), format.format(337.5));
    }

    @ParameterizedTest
    @ValueSource(strings = { "de", "en", "nl", "?" })
    public void testParseLocale(String locale) throws ParseException {
        CardinalNumberFormat format = new CardinalNumberFormat(Locale.of(locale));
        assertEquals(0.0, format.parse(translated("N", locale)));
        assertEquals(22.5, format.parse(translated("NNE", locale)));
        assertEquals(45.0, format.parse(translated("NE", locale)));
        assertEquals(67.5, format.parse(translated("ENE", locale)));
        assertEquals(90.0, format.parse(translated("E", locale)));
        assertEquals(112.5, format.parse(translated("ESE", locale)));
        assertEquals(135.0, format.parse(translated("SE", locale)));
        assertEquals(157.5, format.parse(translated("SSE", locale)));
        assertEquals(180.0, format.parse(translated("S", locale)));
        assertEquals(202.5, format.parse(translated("SSW", locale)));
        assertEquals(225.0, format.parse(translated("SW", locale)));
        assertEquals(247.5, format.parse(translated("WSW", locale)));
        assertEquals(270.0, format.parse(translated("W", locale)));
        assertEquals(292.5, format.parse(translated("WNW", locale)));
        assertEquals(315.0, format.parse(translated("NW", locale)));
        assertEquals(337.5, format.parse(translated("NNW", locale)));
    }

    @Test
    public void testParseWithPosition() throws ParseException {
        NumberFormat format = new DecimalFormat();
        ParsePosition position = new ParsePosition(1);
        assertEquals(1.2, format.parse("X1.2X", position));
        assertEquals(4, position.getIndex());
        format = new CardinalNumberFormat();
        position = new ParsePosition(0);
        assertEquals(0.0, format.parse("N_", position));
        assertEquals(1, position.getIndex());
        assertEquals(-1, position.getErrorIndex());
        position = new ParsePosition(1);
        assertEquals(135.0, format.parse("_SE_", position));
        assertEquals(3, position.getIndex());
        assertEquals(-1, position.getErrorIndex());
        position = new ParsePosition(2);
        assertEquals(247.5, format.parse("__WSW", position));
        assertEquals(5, position.getIndex());
        assertEquals(-1, position.getErrorIndex());
        position = new ParsePosition(3);
        assertNull(format.parse("NNE", position));
        assertEquals(3, position.getErrorIndex());
        position = new ParsePosition(0);
        assertNull(format.parse("", position));
        assertEquals(0, position.getErrorIndex());
        position = new ParsePosition(0);
        assertNull(format.parse("12345", position));
        assertEquals(0, position.getErrorIndex());
    }

    private static String translated(String direction, String locale) {
        return switch (locale) {
            case "de" ->
                direction.replace('E', 'O').replace('b', 'z');
            case "nl" ->
                direction.replace('S', 'Z').replace('E', 'O').replace('b', 't');
            default ->
                direction;
        };
    }

}