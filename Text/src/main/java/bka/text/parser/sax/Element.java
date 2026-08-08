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
 * Default {@link XmlElement} implementation used by {@link SaxStackHandler} while parsing XML.
 *
 * <p>An {@code Element} is created for each start tag and holds the element's namespace URI, names,
 * a copy of the SAX attributes, and accumulated text content. Attributes are copied because the SAX
 * parser reuses the {@link Attributes} instance. When the matching end tag is reached, the handler's
 * converter produces an application object for that element; child objects are stored in document
 * order, each with its qualified name.
 *
 * <p>Converters receive fully populated elements: character data is available through
 * {@link #getCharacters()}, and child objects are available through {@link #getChild(String)} and
 * {@link #getChildren(String)}. This class is not intended for direct use outside the SAX handler.
 *
 * @see SaxStackHandler
 * @see XmlElement
 */
public class Element implements XmlElement {

    /**
     * Creates an element node for the current parse position.
     *
     * @param uri namespace URI of the element
     * @param localName local part of the element name
     * @param qualifiedName qualified name including an optional namespace prefix
     * @param attributes SAX attributes of the start tag
     * @param namespaces maps namespace URIs to prefixes discovered during parsing
     */
    public Element(String uri, String localName, String qualifiedName, Attributes attributes, Function<String, String> namespaces) {
        this.uri = Objects.requireNonNull(uri);
        this.localName = Objects.requireNonNull(localName);
        this.qualifiedName = Objects.requireNonNull(qualifiedName);
        this.attributes = new AttributesImpl(attributes);
        this.namespaces = Objects.requireNonNull(namespaces);
    }

    @Override
    public String getUri() {
        return uri;
    }

    @Override
    public String getLocalName() {
        return localName;
    }

    @Override
    public String getQualifiedName() {
        return qualifiedName;
    }

    @Override
    public Attributes getAttributes() {
        return attributes;
    }

    /**
     * Appends character data reported by the SAX parser for this element.
     *
     * @param buffer character buffer from the parser
     * @param start index of the first character to append
     * @param length number of characters to append
     */
    public void appendCharacters(char buffer[], int start, int length) {
        characters.append(buffer, start, length);
    }

    @Override
    public String getCharacters() {
        return characters.toString();
    }

    @Override
    public List<Object> getChildren() {
        return children.stream().map(Child::object).toList();
    }

    @Override
    @SuppressWarnings("unchecked") // casts Object child to user defined T
    public <T> T getChild(String qualifiedName) {
        List<T> elements = getChildren(qualifiedName);
        if (elements.isEmpty()) {
            throw new NoSuchElementException(qualifiedName);
        }
        if (elements.size() > 1) {
            throw new IllegalArgumentException("Multiple elements of '" + qualifiedName + "'");
        }
        return elements.getFirst();
    }

    @Override
    @SuppressWarnings("unchecked") // casts Object child to user defined T
    public <T> List<T> getChildren(String qualifiedName) {
        return (List<T>) children.stream()
            .filter(child -> child.qualifiedName().equals(qualifiedName))
            .map(Child::object)
            .toList();
    }

    @Override
    public <T> T getChild(String uri, String localName) {
        return getChild(getQualifiedName(uri, localName));
    }

    @Override
    public <T> List<T> getChildren(String uri, String localName) {
        return getChildren(getQualifiedName(uri, localName));
    }

    @Override
    public <T> T getLocalChild(String localName) {
        return getChild(getQualifiedName(localName).get());
    }

    @Override
    public <T> List<T> getLocalChildren(String localName) {
        return getChildren(getQualifiedName(localName).get());
    }

    /**
     * @return the namespace prefix of this element's qualified name, or an empty string when this element has no prefix
     */
    public String getNamespace() {
        return SaxStackHandler.getNamespace(qualifiedName, localName);
    }

    /**
     * Registers a converted child object under the given qualified name.
     *
     * @param qualifiedName qualified name of the child element
     * @param child object returned by the converter for that child
     */
    public void addChild(String qualifiedName, Object child) {
        children.add(new Child(qualifiedName, child));
    }

    private String getQualifiedName(String uri, String localName) {
        if (namespaces.apply(uri) == null) {
            return ":" + localName;
        }
        return namespaces.apply(uri) + localName;
    }

    private Optional<String> getQualifiedName(String localName) {
        String namespace = getNamespace();
        if (namespace.isEmpty()) {
            throw new IllegalStateException("Cannot determine namespace for " + localName);
        }
        String expectedName = namespace + localName;
        return children.stream()
            .map(Child::qualifiedName)
            .filter(expectedName::equals)
            .findAny();
    }


    private record Child(String qualifiedName, Object object) {
    }


    private final String uri;
    private final String localName;
    private final String qualifiedName;
    private final Attributes attributes;
    private final StringBuilder characters = new StringBuilder();
    private final List<Child> children = new ArrayList<>();
    private final Function<String, String> namespaces;
}
