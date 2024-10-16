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
import static org.junit.Assert.assertSame;
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

    @Test
    public void testDuplicates() throws SAXException, ParserConfigurationException, IOException {
        createSaxParser().parse("src/test/resources/plists/duplicates.plist", handler);
        Map<String, Object> map1 = (Map) ((List) handler.getContent()).get(0);
        Map<String, Object> map2 = (Map) ((List) handler.getContent()).get(1);
        assertSameKeys(map1, map2);
        assertSame(map1.get("String"), map2.get("String"));
        List<Integer> integers1 = (List) map1.get("Integers");
        List<Integer> integers2 = (List) map2.get("Integers");
        assertSame(integers1.get(0), integers2.get(0));
        assertSame(integers1.get(1), integers2.get(1));
    }

    private void assertSameKeys(Map<String, Object> map1, Map<String, Object> map2) {
        map1.keySet().forEach(key1 -> {
            map2.keySet().stream().filter(key2 -> key2.equals(key1)).forEach(key2 -> {
                assertSame(key1, key2);
            });
        });
    }

    private static SAXParser createSaxParser() throws SAXException, ParserConfigurationException {
        return SAXParserFactory.newInstance().newSAXParser();
    }

    private final PListSaxHandler handler = new PListSaxHandler();

}