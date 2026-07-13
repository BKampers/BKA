/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package bka.text.parser.sax;

import java.util.*;
import java.util.function.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
 * SAX content handler that turns XML into a single application object via a user-defined converter.
 *
 * <p>The converter is called once for every XML element, from the inside out. Each {@link XmlElement} already
 * contains its converted child objects, so the converter can assemble a parent value from them. The object
 * returned for the document root is the parse result, available through {@link #getRoot()}.
 *
 * <p>The converter decides the shape of that result. It may return strings, numbers, lists, maps, or domain
 * objects such as {@code Book}. There is no fixed schema: the same handler can produce a {@code List<String>}
 * for one document and a {@code Map<String, List<String>>} for another. Dispatch on
 * {@link XmlElement#getQualifiedName()}, {@link XmlElement#getLocalName()}, or {@link XmlElement#getUri()}
 * to handle each element type. Use {@link XmlElement#getChildren(String)} and {@link XmlElement#getChild(String)}
 * to collect already-converted children; use {@link XmlElement#getCharacters()} for text content.
 *
 * <p>Examples of converters and resulting types can be found in
 * {@code bka.text.parser.sax.SaxStackHandlerTest}.
 *
 * @see Element
 * @see XmlElement
 */
public class SaxStackHandler extends DefaultHandler {

    /**
     * Creates a handler that uses the given converter to build the parse result.
     *
     * @param converter returns an object for each XML element; children are already converted. The return value
     *                  for the document root becomes {@link #getRoot()}; it must not be {@code null}
     */
    public SaxStackHandler(Function<XmlElement, Object> converter) {
        this.converter = Objects.requireNonNull(converter);
    }

    /**
     * Returns the object that the converter produced for the document root.
     *
     * <p>The type is determined entirely by the converter — for example a {@code List}, {@code Map}, or a
     * domain object. Cast via the type parameter {@code T}.
     *
     * @param <T> expected result type
     * @return the converted root object
     * @throws IllegalStateException if parsing has not completed yet
     * @throws ClassCastException if the root object is not assignable to {@code T}
     */
    @SuppressWarnings("unchecked") // casts root Object to user defined T
    public <T> T getRoot() {
        if (root == null) {
            throw new IllegalStateException("Nothing has been parsed yet");
        }
        return (T) root;
    }

    @Override
    public void startElement(String uri, String localName, String qualifiedName, Attributes attributes) {
        stack.push(new Element(uri, localName, qualifiedName, attributes, stack.isEmpty() ? null : stack.peek(), namespaces::get));
        if (!uri.isEmpty() && !namespaces.containsKey(uri)) {
            namespaces.put(uri, getNamespace(qualifiedName, localName));
        }
    }

    @Override
    public void endElement(String uri, String localName, String qualifiedName) throws SAXException {
        Element element = stack.pop();
        if (stack.isEmpty()) {
            root = Objects.requireNonNull(converter.apply(element), "Root element must not be null");
        }
        else {
            stack.peek().addChild(qualifiedName, converter.apply(element));
        }
    }
    
    @Override
    public void characters(char buffer[], int start, int length) {
        stack.peek().appendCharacters(buffer, start, length);
    }

    /**
     * Extracts the namespace prefix from a qualified element name.
     *
     * @param qualifiedName element name including an optional prefix, for example {@code "h:table"}
     * @param localName local part of the name, for example {@code "table"}
     * @return the namespace prefix, or an empty string when {@code qualifiedName} equals {@code localName}
     */
    public static String getNamespace(String qualifiedName, String localName) {
        return qualifiedName.substring(0, qualifiedName.length() - localName.length());
    }

    /**
     * @return namespace URI-to-prefix mappings discovered while parsing
     */
    public Map<String, String> getNamespaces() {
        return Collections.unmodifiableMap(namespaces);
    }


    private final Function<XmlElement, Object> converter;
    private final Deque<Element> stack = new LinkedList<>();
    private final Map<String, String> namespaces = new HashMap<>();

    private Object root;

}
