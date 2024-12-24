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


public class Element {

    public Element(String uri, String localName, String qualifiedName, Attributes attributes, Element parent, Function<String, String> namespaces) {
        this.uri = Objects.requireNonNull(uri);
        this.localName = Objects.requireNonNull(localName);
        this.qualifiedName = Objects.requireNonNull(qualifiedName);
        this.attributes = Objects.requireNonNull(attributes);
        children = new Children();
        this.parent = parent;
        this.namespaces = Objects.requireNonNull(namespaces);
    }

    public String getUri() {
        return uri;
    }

    public String getLocalName() {
        return localName;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public Attributes getAttributes() {
        return attributes;
    }

    void appendCharacters(char buffer[], int start, int length) {
        characters.append(buffer, start, length);
    }

    public String getCharacters() {
        return characters.toString();
    }

    Children getChildren() {
        return children;
    }

    /**
     * @param <T> type of child
     * @param qualifiedName of the child to get
     * @return Single child of given qualified name
     * @throws NoSuchElementException if no child of given qualifiedName is present
     * @throws ClassCastException if the single child is not of type T
     * @throws IllegalArgumentException if multiple children of given qualifieName are present
     */
    public <T> T getChild(String qualifiedName) {
        return children.getElement(qualifiedName);
    }

    /**
     * @param <T> type of children
     * @param qualifiedName of the children to get
     * @return List of children with given qualified name, an empty list if no such children are present
     */
    public <T> List<T> getChildren(String qualifiedName) {
        return children.getList(qualifiedName);
    }

    /**
     * @param <T> type of child
     * @param uri of the child to get
     * @param localName of the child to get
     * @return Single child of uri and local name
     * @throws NoSuchElementException if no child of given uri and localName is present
     * @throws ClassCastException if the single child is not of type T
     * @throws IllegalArgumentException if multiple children of given uri and localName are available
     */
    public <T> T getChild(String uri, String localName) {
        return getChild(getQualifiedName(uri, localName));
    }

    /**
     * @param <T> type of children
     * @param uri of the children to get
     * @param localName of the children to get
     * @return List of children with given uri and local name, an empty list if no such children are present
     */
    public <T> List<T> getChildren(String uri, String localName) {
        return getChildren(getQualifiedName(uri, localName));
    }

    /**
     * @param <T> type of child
     * @param localName of the child to get
     * @return Single child of given local name in the parent's namespace
     * @throws IllegalStateException if the parent element has no namespace
     * @throws NoSuchElementException if no elements with given localName are available in the parents namespace
     * @throws ClassCastException if the single element is not of type T
     * @throws IllegalArgumentException if multiple elements with given qualifieName are available
     */
    public <T> T getLocalChild(String localName) {
        return getChild(getQualifiedName(localName).get());
    }

    /**
     * @param <T> type of children
     * @param localName of the children to get
     * @return List of children with given local name in the parent's namespace. An empty list if no children with local name are present for the
     * parent's namespace.
     * @throws IllegalStateException if the parent element has no namespace
     */
    public <T> List<T> getLocalChildren(String localName) {
        return getChildren(getQualifiedName(localName).get());
    }

    public String getNamespace() {
        return SaxStackHandler.getNamespace(qualifiedName, localName);
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
    private final Children children;
    private final Function<String, String> namespaces;
}
