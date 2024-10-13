/*
** © Bart Kampers
*/

package bka.text.parser.sax;

import java.io.*;
import java.math.*;
import java.time.*;
import java.util.*;
import javax.xml.parsers.*;
import static org.junit.Assert.assertEquals;
import org.junit.*;
import org.xml.sax.*;

public class PListSaxHandlerTest {

    @Test
    public void testAllTypes() throws SAXException, ParserConfigurationException, IOException {
        final Object expected = List.of(
            "Text",
            BigInteger.ZERO,
            BigDecimal.ZERO,
            ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneId.of("Z")),
            Boolean.TRUE,
            Boolean.FALSE);
        createSaxParser().parse("src/test/resources/plists/all-types.plist", handler);
        assertEquals(expected, handler.getContent());
    }

    @Test
    public void testArray() throws SAXException, ParserConfigurationException, IOException {
        final Object expected = List.of(
            List.of(
                "Text",
                BigInteger.ZERO,
                BigDecimal.ZERO,
                ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneId.of("Z")),
                Boolean.TRUE,
                Boolean.FALSE));
        createSaxParser().parse("src/test/resources/plists/array.plist", handler);
        assertEquals(expected, handler.getContent());
    }

    @Test
    public void testTwoElements() throws SAXException, ParserConfigurationException, IOException {
        final Object expected = List.of(
            Map.of(
                "String", "Text",
                "Integer", BigInteger.ZERO),
            List.of(
                Boolean.FALSE,
                Boolean.TRUE));
        createSaxParser().parse("src/test/resources/plists/two-elements.plist", handler);
        assertEquals(expected, handler.getContent());
    }

    @Test
    public void testNested() throws SAXException, ParserConfigurationException, IOException {
        final Object expected = List.of(
            Map.of(
                "Map", Map.of(
                    "String", "Text",
                    "Integer", BigInteger.ZERO,
                    "List", List.of(
                        Boolean.FALSE,
                        Boolean.TRUE))));
        createSaxParser().parse("src/test/resources/plists/nested.plist", handler);
        assertEquals(expected, handler.getContent());
    }

    private static SAXParser createSaxParser() throws SAXException, ParserConfigurationException {
        return SAXParserFactory.newInstance().newSAXParser();
    }

    private final PListSaxHandler handler = new PListSaxHandler();

}