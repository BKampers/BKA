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


public class Element implements XmlElement {

    public Element(String uri, String localName, String qualifiedName, Attributes attributes, Element parent, Function<String, String> namespaces) {
        this.uri = Objects.requireNonNull(uri);
        this.localName = Objects.requireNonNull(localName);
        this.qualifiedName = Objects.requireNonNull(qualifiedName);
        this.attributes = Objects.requireNonNull(attributes);
        this.parent = parent;
        this.namespaces = Objects.requireNonNull(namespaces);
    }

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

    void appendCharacters(char buffer[], int start, int length) {
        characters.append(buffer, start, length);
    }

    @Override
    public String getCharacters() {
        return characters.toString();
    }

    @Override
    public <T> T getChild(String qualifiedName) {
        List<Object> elements = children.get(qualifiedName);
        if (elements == null) {
            throw new NoSuchElementException(qualifiedName);
        }
        if (elements.size() > 1) {
            throw new IllegalArgumentException("Multiple elements of '" + qualifiedName + "'");
        }
        return (T) elements.get(0);
    }

    @Override
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

    public String getNamespace() {
        return SaxStackHandler.getNamespace(qualifiedName, localName);
    }

    void addChild(String qualifiedName, Object child) {
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
