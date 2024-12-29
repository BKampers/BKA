/*
** © Bart Kampers
*/

package bka.text.parser.sax;

import java.io.*;
import java.util.*;
import java.util.function.*;
import javax.xml.parsers.*;
import static org.junit.Assert.assertEquals;
import org.junit.*;
import org.xml.sax.*;

public class SaxStackHandlerTest {

    @Test
    public void testArray() throws SAXException, ParserConfigurationException, IOException {
        SaxStackHandler handler = new SaxStackHandler(getArrayModel());
        createParser().parse("src/test/resources/xml/array.xml", handler);
        assertEquals(List.of("Apple", "Banana", "Cherry"), handler.getRoot());
    }

    @Test
    public void testBookShelf() throws SAXException, ParserConfigurationException, IOException {
        SaxStackHandler handler = new SaxStackHandler(getBookshlefModel());
        createParser().parse("src/test/resources/xml/books.xml", handler);
        List<Book> shelf = handler.getRoot();
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
        createParser().parse("src/test/resources/xml/integers.xml", handler);
        List<Number> numbers = handler.getRoot();
        assertEquals(4, numbers.size());
        assertEquals((byte) 1, numbers.get(0));
        assertEquals((short) 2, numbers.get(1));
        assertEquals((int) 4, numbers.get(2));
        assertEquals((long) 8, numbers.get(3));
    }

    @Test
    public void testHtmlTable() throws SAXException, ParserConfigurationException, IOException {
        SaxStackHandler handler = new SaxStackHandler(getHtmlTableModel());
        createParser().parse("src/test/resources/xml/html-table.xml", handler);
        Map<String, List<String>> table = handler.getRoot();
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

    @Test
    public void testNamespaces() throws SAXException, ParserConfigurationException, IOException {
        SaxStackHandler handler = new SaxStackHandler(getTableModel());
        createNamespaceParser().parse("src/test/resources/xml/namespaces.xml", handler);
        List<Object> list = handler.getRoot();
        assertEquals(4, list.size());
        assertEquals("HTML 4", list.get(0));
        assertEquals(
            List.of(List.of(List.of("Apples", "Bananas"), List.of("round", "long"))),
            list.get(1));
        assertEquals("Furniture", list.get(2));
        assertEquals(
            List.of(Map.of(
                "name", "African Coffee Table",
                "width", 80,
                "length", 120)),
            list.get(3));
    }

    private static Function<XmlElement, Object> getTableModel() {
        return element -> switch (element.getUri()) {
            case "" ->
                handleTableRoot(element);
            case "http://www.w3.org/TR/html4/" ->
                handleHtmlXmlElement(element);
            case "http://www.w3schools.com/furniture" ->
                handleFurnitureXmlElement(element);
            default ->
                throw new IllegalArgumentException(unsupportedUri(element));
        };
    }

    private static Object handleTableRoot(XmlElement element) {
        return switch (element.getLocalName()) {
            case "root" ->
                List.of(
                (Object) element.getChild("http://www.w3.org/TR/html4/", "subject"),
                (Object) element.getChildren("http://www.w3.org/TR/html4/", "table"),
                (Object) element.getChild("http://www.w3schools.com/furniture", "subject"),
                (Object) element.getChildren("http://www.w3schools.com/furniture", "table"));
            default ->
                throw new IllegalArgumentException(unsupportedLocalName(element));
        };
    }

    private static Object handleHtmlXmlElement(XmlElement element) {
        return switch (element.getLocalName()) {
            case "table" ->
                element.getLocalChildren("tr");
            case "tr" ->
                element.getLocalChildren("td");
            case "subject", "td" ->
                element.getCharacters();
            default ->
                throw new IllegalArgumentException(unsupportedLocalName(element));
        };
    }

    private static Object handleFurnitureXmlElement(XmlElement element) {
        return switch (element.getLocalName()) {
            case "table" ->
                createTable(element);
            case "subject", "name" ->
                element.getCharacters();
            case "width", "length" ->
                Integer.valueOf(element.getCharacters());
            default ->
                throw new IllegalArgumentException(unsupportedLocalName(element));
        };
    }

    private static Map<String, Object> createTable(XmlElement properties) {
        return Map.of(
            "name", properties.getLocalChild("name"),
            "width", properties.getLocalChild("width"),
            "length", properties.getLocalChild("length"));
    }

    private static SAXParser createParser() throws SAXException, ParserConfigurationException {
        return createSaxParser(false);
    }

    private static SAXParser createNamespaceParser() throws SAXException, ParserConfigurationException {
        return createSaxParser(true);
    }

    private static SAXParser createSaxParser(boolean namespaceAware) throws SAXException, ParserConfigurationException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(namespaceAware);
        return factory.newSAXParser();
    }

    private static Function<XmlElement, Object> getArrayModel() {
        return (element) -> switch (element.getQualifiedName()) {
            case "root" ->
                element.getChildren("element");
            case "element" ->
                element.getCharacters();
            default ->
                throw new IllegalArgumentException(unsupportedQualifiedName(element));
        };
    }

    private static Function<XmlElement, Object> getBookshlefModel() {
        return (element) -> switch (element.getQualifiedName()) {
            case "shelf" ->
                element.getChildren("book");
            case "book" ->
                createBook(element);
            case "authors" ->
                element.getChildren("author");
            case "title", "author" ->
                element.getCharacters();
            case "release" ->
                Integer.parseInt(element.getCharacters());
            default ->
                throw new IllegalArgumentException(unsupportedQualifiedName(element));
        };
    }

    private static Book createBook(XmlElement properties) {
        return new Book(
            properties.getChild("title"),
            properties.getChild("authors"),
            properties.getChild("release")
        );
    }

    private static Function<XmlElement, Object> getIntegerModel() {
        return (element) -> switch (element.getQualifiedName()) {
            case "integers" ->
                element.getChildren("integer");
            case "integer" ->
                getInteger(element.getAttributes().getValue("width"), element.getCharacters());
            default ->
                throw new IllegalArgumentException(unsupportedQualifiedName(element));
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

    private static Function<XmlElement, Object> getHtmlTableModel() {
        return (element) -> switch (element.getQualifiedName()) {
            case "table" ->
                getTable(element);
            case "thead", "tbody" ->
                element.getChildren("tr");
            case "tr" ->
                getColumns(element);
            case "th", "td" ->
                element.getCharacters().replace(NBSP, ' ').strip();
            default ->
                throw new IllegalArgumentException(unsupportedQualifiedName(element));
        };
    }

    private static Map<String, List<String>> getTable(XmlElement children) {
        return Map.of(
            "Headers", children.getChild("thead"),
            "Rows", children.getChild("tbody"));
    }

    private static List<String> getColumns(XmlElement children) {
        List<String> columns = children.getChildren("td");
        if (columns.isEmpty()) {
            return children.getChildren("th");
        }
        return columns;
    }


    private static String unsupportedUri(XmlElement element) {
        return "Unsupported uri " + quoted(element.getUri());
    }

    private static String unsupportedLocalName(XmlElement element) {
        return "Unsupported local name " + quoted(element.getLocalName());
    }

    private static String unsupportedQualifiedName(XmlElement element) {
        return "Unsupported qualified name " + quoted(element.getQualifiedName());
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
