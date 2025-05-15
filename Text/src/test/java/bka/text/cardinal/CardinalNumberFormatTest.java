/*
** © Bart Kampers
*/

package bka.text.cardinal;

import java.text.*;
import java.util.*;
import java.util.function.*;
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
    public void testFormatWithField() {
        testBoundaries(
            new String[]{ "N", "E", "S", "W" },
            CardinalNumberFormat.cardinalFieldPostion());
        testBoundaries(
            new String[]{ "N", "NE", "E", "SE", "S", "SW", "W", "NW" },
            CardinalNumberFormat.intercardinalFieldPostion());
        testBoundaries(
            new String[]{ "N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW" },
            CardinalNumberFormat.secondaryIntercardinalFieldPostion());
        testBoundaries(
            new String[]{
                "N", "NbE", "NNE", "NEbN", "NE", "NEbE", "ENE", "EbN",
                "E", "EbS", "ESE", "SEbE", "SE", "SEbS", "SSE", "SbE",
                "S", "SbW", "SSW", "SWbS", "SW", "SWbW", "WSW", "WbS",
                "W", "WbN", "WNW", "NWbW", "NW", "NWbN", "NNW", "NbW" },
            CardinalNumberFormat.tertiaryIntercardinalFieldPostion());
    }

    private void testBoundaries(String[] expected, FieldPosition fieldPosition) {
        CardinalNumberFormat format = new CardinalNumberFormat();
        final double interval = 360d / expected.length;
        final double precision = 0.001;
        double degrees = 3 * -360 - 0.5 * interval;
        while (degrees < 360 * 3) {
            for (String direction : expected) {
                assertEquals(direction, format.format(degrees, new StringBuffer(), fieldPosition).toString(), message(degrees, fieldPosition));
                degrees += interval;
                assertEquals(direction, format.format(degrees - precision, new StringBuffer(), fieldPosition).toString(), message(degrees, fieldPosition));
            }
        }
    }

    private static Supplier<String> message(double degrees, FieldPosition fieldPosition) {
        double normalized = degrees - Math.floor(degrees / 360) * 360;
        return () -> "degrees = " + Double.toString(degrees) + "; (" + normalized + ") field = " + fieldPosition.getField();
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
    @ValueSource(strings = { "de", "en", "nl" })
    public void testFormatLocale(String locale) {
        FieldPosition fieldPosition = new FieldPosition(4);
        CardinalNumberFormat format = new CardinalNumberFormat(Locale.of(locale));
        assertEquals(translated("N", locale), format.format(0.0, new StringBuffer(), fieldPosition).toString());
        assertEquals(translated("NbE", locale), format.format(11.25, new StringBuffer(), fieldPosition).toString());
        assertEquals(translated("NNE", locale), format.format(22.5, new StringBuffer(), fieldPosition).toString());
        assertEquals(translated("NEbN", locale), format.format(33.75, new StringBuffer(), fieldPosition).toString());
        assertEquals(translated("NE", locale), format.format(45.0, new StringBuffer(), fieldPosition).toString());
        assertEquals(translated("NEbE", locale), format.format(56.25, new StringBuffer(), fieldPosition).toString());
        assertEquals(translated("ENE", locale), format.format(67.5, new StringBuffer(), fieldPosition).toString());
        assertEquals(translated("EbN", locale), format.format(78.75, new StringBuffer(), fieldPosition).toString());
        assertEquals(translated("E", locale), format.format(90.0, new StringBuffer(), fieldPosition).toString());
        assertEquals(translated("EbS", locale), format.format(101.25, new StringBuffer(), fieldPosition).toString());
        assertEquals(translated("ESE", locale), format.format(112.5, new StringBuffer(), fieldPosition).toString());
        assertEquals(translated("SEbE", locale), format.format(123.75, new StringBuffer(), fieldPosition).toString());
        assertEquals(translated("SE", locale), format.format(135.0, new StringBuffer(), fieldPosition).toString());
        assertEquals(translated("SEbS", locale), format.format(146.25, new StringBuffer(), fieldPosition).toString());
        assertEquals(translated("SSE", locale), format.format(157.5, new StringBuffer(), fieldPosition).toString());
        assertEquals(translated("SbE", locale), format.format(168.75, new StringBuffer(), fieldPosition).toString());
        assertEquals(translated("S", locale), format.format(180.0, new StringBuffer(), fieldPosition).toString());
        assertEquals(translated("SbW", locale), format.format(191.25, new StringBuffer(), fieldPosition).toString());
        assertEquals(translated("SSW", locale), format.format(202.5, new StringBuffer(), fieldPosition).toString());
        assertEquals(translated("SWbS", locale), format.format(213.25, new StringBuffer(), fieldPosition).toString());
        assertEquals(translated("SW", locale), format.format(225.0, new StringBuffer(), fieldPosition).toString());
        assertEquals(translated("SWbW", locale), format.format(236.75, new StringBuffer(), fieldPosition).toString());
        assertEquals(translated("WSW", locale), format.format(247.5, new StringBuffer(), fieldPosition).toString());
        assertEquals(translated("WbS", locale), format.format(258.75, new StringBuffer(), fieldPosition).toString());
        assertEquals(translated("W", locale), format.format(270.0, new StringBuffer(), fieldPosition).toString());
        assertEquals(translated("WbN", locale), format.format(281.25, new StringBuffer(), fieldPosition).toString());
        assertEquals(translated("WNW", locale), format.format(292.5, new StringBuffer(), fieldPosition).toString());
        assertEquals(translated("NWbW", locale), format.format(303.75, new StringBuffer(), fieldPosition).toString());
        assertEquals(translated("NW", locale), format.format(315.0, new StringBuffer(), fieldPosition).toString());
        assertEquals(translated("NWbN", locale), format.format(326.25, new StringBuffer(), fieldPosition).toString());
        assertEquals(translated("NNW", locale), format.format(337.5, new StringBuffer(), fieldPosition).toString());
        assertEquals(translated("NbW", locale), format.format(348.75, new StringBuffer(), fieldPosition).toString());
    }

    @ParameterizedTest
    @ValueSource(strings = { "de", "en", "nl" })
    public void testParseLocale(String locale) throws ParseException {
        CardinalNumberFormat format = new CardinalNumberFormat(Locale.of(locale));
        assertEquals(0.0, format.parse(translated("N", locale)));
        assertEquals(11.25, format.parse(translated("NbE", locale)));
        assertEquals(22.5, format.parse(translated("NNE", locale)));
        assertEquals(33.75, format.parse(translated("NEbN", locale)));
        assertEquals(45.0, format.parse(translated("NE", locale)));
        assertEquals(56.25, format.parse(translated("NEbE", locale)));
        assertEquals(67.5, format.parse(translated("ENE", locale)));
        assertEquals(78.75, format.parse(translated("EbN", locale)));
        assertEquals(90.0, format.parse(translated("E", locale)));
        assertEquals(101.25, format.parse(translated("EbS", locale)));
        assertEquals(112.5, format.parse(translated("ESE", locale)));
        assertEquals(123.75, format.parse(translated("SEbE", locale)));
        assertEquals(135.0, format.parse(translated("SE", locale)));
        assertEquals(146.25, format.parse(translated("SEbS", locale)));
        assertEquals(157.5, format.parse(translated("SSE", locale)));
        assertEquals(168.75, format.parse(translated("SbE", locale)));
        assertEquals(180.0, format.parse(translated("S", locale)));
        assertEquals(191.25, format.parse(translated("SbW", locale)));
        assertEquals(202.5, format.parse(translated("SSW", locale)));
        assertEquals(213.75, format.parse(translated("SWbS", locale)));
        assertEquals(225.0, format.parse(translated("SW", locale)));
        assertEquals(236.25, format.parse(translated("SWbW", locale)));
        assertEquals(247.5, format.parse(translated("WSW", locale)));
        assertEquals(258.75, format.parse(translated("WbS", locale)));
        assertEquals(270.0, format.parse(translated("W", locale)));
        assertEquals(281.25, format.parse(translated("WbN", locale)));
        assertEquals(292.5, format.parse(translated("WNW", locale)));
        assertEquals(303.75, format.parse(translated("NWbW", locale)));
        assertEquals(315.0, format.parse(translated("NW", locale)));
        assertEquals(326.25, format.parse(translated("NWbN", locale)));
        assertEquals(337.5, format.parse(translated("NNW", locale)));
        assertEquals(348.75, format.parse(translated("NbW", locale)));
    }

    @Test
    public void testParseWithPosition() throws ParseException {
        CardinalNumberFormat format = new CardinalNumberFormat();
        ParsePosition position = new ParsePosition(0);
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