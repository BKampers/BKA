/*
** © Bart Kampers
*/

package bka.text.parser.sax;

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import static org.junit.Assert.assertEquals;
import org.junit.*;
import org.xml.sax.*;

public class SaxStackHandlerTest {

    @Test
    public void testArray() throws SAXException, ParserConfigurationException, IOException {
        SaxStackHandler handler = new SaxStackHandler(getArrayModel());
        createSaxParser().parse("src/test/resources/xml/array.xml", handler);
        assertEquals(List.of("Apple", "Banana", "Cherry"), ((List) handler.getObjects().get(0)));
    }

    @Test
    public void testBookShelf() throws SAXException, ParserConfigurationException, IOException {
        SaxStackHandler handler = new SaxStackHandler(getBookshlefModel());
        createSaxParser().parse("src/test/resources/xml/books.xml", handler);
        List<Book> shelf = (List<Book>) handler.getObjects().get(0);
        assertEquals(2, shelf.size());
        assertEquals("The Unified Modeling Language User Guide", shelf.get(0).getTitle());
        assertEquals(List.of("Booch", "Jacobson", "Rumbaugh"), shelf.get(0).getAuthors());
        assertEquals(1999, shelf.get(0).getRelease());
        assertEquals("The Java Programming Language", shelf.get(1).getTitle());
        assertEquals(List.of("Ken Arnold", "James Gosling"), shelf.get(1).getAuthors());
        assertEquals(1998, shelf.get(1).getRelease());
    }

    @Test
    public void testIntegers() throws SAXException, ParserConfigurationException, IOException {
        SaxStackHandler handler = new SaxStackHandler(getIntegerModel());
        createSaxParser().parse("src/test/resources/xml/integers.xml", handler);
        assertEquals((byte) 1, ((List) handler.getObjects().get(0)).get(0));
        assertEquals((short) 2, ((List) handler.getObjects().get(0)).get(1));
        assertEquals((int) 4, ((List) handler.getObjects().get(0)).get(2));
        assertEquals((long) 8, ((List) handler.getObjects().get(0)).get(3));
    }

    @Test
    public void testHtmlTable() throws SAXException, ParserConfigurationException, IOException {
        SaxStackHandler handler = new SaxStackHandler(getHtmlTableModel());
        createSaxParser().parse("src/test/resources/xml/html-table.xml", handler);
        Map table = (Map) handler.getObjects().get(0);
        assertEquals(
            List.of(
                List.of("Station", "Weer", "Temp (°C)", "Chill (°C)", "RV (%)", "Wind (bft)", "Wind (m/s)", "Windstoot (km/uur)", "Zicht (m)", "Druk (hPa)")),
            table.get("Headers"));
        assertEquals(
            List.of(
                List.of("Lauwersoog", "", "6.9", "3.3", "65", "ZZO 4", "6", "29", "", ""),
                List.of("Terschelling", "", "6.3", "2.5", "78", "ZZO 4", "6", "31", "31000", "1015.0"),
                List.of("Vlieland", "zwaar bewolkt", "6.6", "2.0", "75", "ZZO 5", "9", "40", "35000", "1014.4")),
            table.get("Rows"));
    }

    private static SAXParser createSaxParser() throws SAXException, ParserConfigurationException {
        return SAXParserFactory.newInstance().newSAXParser();
    }

    private static SaxStackHandler.Model getArrayModel() {
        return (element) -> switch (element.getQualifiedName()) {
            case "root" ->
                element.getChildren().getList("element");
            case "element" ->
                element.getCharacters();
            default ->
                throw new IllegalArgumentException(unsupportedQualifiedName(element));
        };
    }

    private static SaxStackHandler.Model getBookshlefModel() {
        return (element) -> {
            if (!element.getLocalName().isEmpty()) {
                throw new IllegalArgumentException(unsupportedLocalName(element));
            }
            return switch (element.getQualifiedName()) {
                case "shelf" ->
                    element.getChildren().getList("book");
                case "book" ->
                    createBook(element.getChildren());
                case "authors" ->
                    element.getChildren();
                case "title", "author" ->
                    element.getCharacters();
                case "release" ->
                    Integer.parseInt(element.getCharacters());
                default ->
                    throw new IllegalArgumentException(unsupportedQualifiedName(element));
            };
        };
    }

    private static Book createBook(SaxStackHandler.Children properties) {
        return new Book(
            properties.getElement("title"),
            properties.getChildren("authors").getList("author"),
            properties.getElement("release")
        );
    }

    private static SaxStackHandler.Model getIntegerModel() {
        return (element) -> switch (element.getLocalName()) {
            case "" ->
                switch (element.getQualifiedName()) {
                    case "integers" ->
                        element.getChildren().getList("integer");
                    case "integer" ->
                        getInteger(element.getAttributes().getValue("width"), element.getCharacters());
                    default ->
                        throw new IllegalArgumentException(unsupportedQualifiedName(element));
                };
            default ->
                throw new IllegalArgumentException(unsupportedLocalName(element));
        };
    }

    private static Number getInteger(String width, String characters) {
        return switch (width) {
            case "8" ->
                Byte.valueOf(characters);
            case "16" ->
                Short.valueOf(characters);
            case "32" ->
                Integer.valueOf(characters);
            case "64" ->
                Long.valueOf(characters);
            default ->
                throw new IllegalArgumentException("Unsupported width: " + quoted(width));
        };
    }

    private static SaxStackHandler.Model getHtmlTableModel() {
        return (element) -> switch (element.getLocalName()) {
            case "" ->
                switch (element.getQualifiedName()) {
                    case "table" ->
                        getTable(element.getChildren());
                    case "thead", "tbody" ->
                        element.getChildren().getList("tr");
                    case "tr" ->
                        getColumns(element.getChildren());
                    case "th", "td" ->
                        element.getCharacters().replace(NBSP, ' ').trim();
                    default ->
                        throw new IllegalArgumentException(unsupportedQualifiedName(element));
                };
            default ->
                throw new IllegalArgumentException(unsupportedLocalName(element));
        };
    }

    private static Map<String, List<String>> getTable(SaxStackHandler.Children children) {
        return Map.of(
            "Headers", children.getElement("thead"),
            "Rows", children.getElement("tbody"));
    }

    private static List<String> getColumns(SaxStackHandler.Children children) {
        List<String> columns = children.getList("td");
        if (columns.isEmpty()) {
            return children.getList("th");
        }
        return columns;
    }


    private static String unsupportedLocalName(SaxStackHandler.Element element) {
        return "Unsupported local name " + quoted(element.getLocalName());
    }

    private static String unsupportedQualifiedName(SaxStackHandler.Element element) {
        return "Cannot create element for qualified name " + quoted(element.getQualifiedName());
    }

    private static String quoted(String string) {
        return "'" + string + "'";
    }

    private static class Book {

        public Book(String title, List<String> authors, int release) {
            this.title = title;
            this.authors = authors;
            this.release = release;
        }

        public String getTitle() {
            return title;
        }

        public List<String> getAuthors() {
            return authors;
        }

        public int getRelease() {
            return release;
        }

        private final String title;
        private final List<String> authors;
        private final int release;
    }

    private static final char NBSP = '\u00a0';

}
