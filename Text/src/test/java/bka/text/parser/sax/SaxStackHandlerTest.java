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

    private static SAXParser createSaxParser() throws SAXException, ParserConfigurationException {
        return SAXParserFactory.newInstance().newSAXParser();
    }

    private static SaxStackHandler.Model getArrayModel() {
        return (localName, qualifiedName, attribuets, children, characters) -> {
            if (!localName.isEmpty()) {
                throw new IllegalArgumentException("Unsupported local name: " + localName);
            }
            return switch (qualifiedName) {
                case "root" ->
                    children.getList("element");
                case "element" ->
                    characters;
                default ->
                    throw new IllegalArgumentException("Cannot create element for qualified name '" + qualifiedName + "'");
            };
        };
    }

    private static SaxStackHandler.Model getBookshlefModel() {
        return (localName, qualifiedName, attributes, children, characters) -> switch (qualifiedName) {
            case "shelf" ->
                children.getList("book");
            case "book" ->
                createBook(children);
            case "authors" ->
                children;
            case "title", "author" ->
                characters;
            case "release" ->
                Integer.parseInt(characters);
            default ->
                throw new IllegalArgumentException("Cannot create element for qualified name '" + qualifiedName + "'");
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
        return (localName, qualifiedName, attributes, children, characters) -> switch (localName) {
            case "" ->
                switch (qualifiedName) {
                    case "integers" ->
                        children.getList("integer");
                    case "integer" ->
                        getInteger(attributes.getValue("width"), characters);
                    default ->
                        throw new IllegalArgumentException("Cannot create element for qualified name '" + qualifiedName + "'");
                };
            default ->
                throw new IllegalArgumentException("Unsupported local name '" + localName + "'");
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
                throw new IllegalArgumentException("Unsupported width: '" + width + "'");
        };
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

}
