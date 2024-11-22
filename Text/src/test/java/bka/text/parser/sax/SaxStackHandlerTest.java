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

    @Before
    public void init() {
        handler = new SaxStackHandler(new ArrayModel());
    }

    @Test
    public void testArray() throws SAXException, ParserConfigurationException, IOException {
        createSaxParser().parse("src/test/resources/xml/array.xml", handler);
        assertEquals(List.of("Apple", "Banana", "Cherry"), ((List) handler.getObjects().get(0)));
    }

    @Test
    public void testBookShelf() throws SAXException, ParserConfigurationException, IOException {
        SaxStackHandler handler = new SaxStackHandler((qualifiedName, children, characters) -> {
            return switch (qualifiedName) {
                case "shelf" ->
                    children.get("book");
                case "book" ->
                    createBook(children);
                case "authors" ->
                    children.getStrings("author");
                case "title", "author" ->
                    characters;
                case "release" ->
                    Integer.parseInt(characters);
                default ->
                    throw new IllegalArgumentException("Cannot create element for qualified name '" + qualifiedName + "'");
            };
        });
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

    private Book createBook(SaxStackHandler.Children properties) {
        return new Book(
            properties.getString("title").get(),
            properties.getStringList("authors"),
            //            properties.getChildren("authors").getStrings("author"),
            properties.getInteger("release").get()
        );
    }

    private static SAXParser createSaxParser() throws SAXException, ParserConfigurationException {
        return SAXParserFactory.newInstance().newSAXParser();
    }

    private class ArrayModel implements SaxStackHandler.Model {

        @Override
        public Object createObject(String qualifiedName, SaxStackHandler.Children children, String characters) {
            return switch (qualifiedName) {
                case "root" ->
                    children.getStrings("element");
                case "element" ->
                    characters;
                default ->
                    throw new IllegalArgumentException("Cannot create element for qualified name '" + qualifiedName + "'");
            };
        }
    }

    private class Book {

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

    private SaxStackHandler handler;


}
