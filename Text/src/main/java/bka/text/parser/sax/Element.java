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


/**
 * Default {@link XmlElement} implementation used by {@link SaxStackHandler} while parsing XML.
 *
 * <p>An {@code Element} is created for each start tag and holds the element's namespace URI, names,
 * SAX attributes, and accumulated text content. When the matching end tag is reached, the handler's
 * converter produces an application object for that element; child objects are stored on the parent
 * element, keyed by qualified name.
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
     * @param parent parent element, or {@code null} for the document root
     * @param namespaces maps namespace URIs to prefixes discovered during parsing
     */
    public Element(String uri, String localName, String qualifiedName, Attributes attributes, Element parent, Function<String, String> namespaces) {
        this.uri = Objects.requireNonNull(uri);
        this.localName = Objects.requireNonNull(localName);
        this.qualifiedName = Objects.requireNonNull(qualifiedName);
        this.attributes = Objects.requireNonNull(attributes);
        this.parent = parent;
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
    @SuppressWarnings("unchecked") // casts Object child to user defined T
    public <T> T getChild(String qualifiedName) {
        List<Object> elements = children.get(qualifiedName);
        if (elements == null || elements.isEmpty()) {
            throw new NoSuchElementException(qualifiedName);
        }
        if (elements.size() > 1) {
            throw new IllegalArgumentException("Multiple elements of '" + qualifiedName + "'");
        }
        return (T) elements.get(0);
    }

    @Override
    @SuppressWarnings("unchecked") // casts List<Object> with children to user defined List<T>
    public <T> List<T> getChildren(String qualifiedName) {
        List<Object> elements = children.get(qualifiedName);
        if (elements == null) {
            return Collections.emptyList();
        }
        return (List<T>) Collections.unmodifiableList(elements);
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
        children
            .computeIfAbsent(qualifiedName, name -> new ArrayList<>())
            .add(child);
    }

    private String getQualifiedName(String uri, String localName) {
        if (namespaces.apply(uri) == null) {
            return ":" + localName;
        }
        return namespaces.apply(uri) + localName;
    }

    private Optional<String> getQualifiedName(String localName) {
        if (parent == null) {
            throw new IllegalStateException("Cannot determine namespace for " + localName);
        }
        String namespace = getNamespace();
        if (namespace.isEmpty()) {
            throw new IllegalStateException("Cannot determine namespace for " + localName);
        }
        return children.keySet().stream().filter(key -> key.equals(namespace + localName)).findAny();
    }


    private final Element parent;
    private final String uri;
    private final String localName;
    private final String qualifiedName;
    private final Attributes attributes;
    private final StringBuilder characters = new StringBuilder();
    private final Map<String, List<Object>> children = new HashMap<>();
    private final Function<String, String> namespaces;
}
