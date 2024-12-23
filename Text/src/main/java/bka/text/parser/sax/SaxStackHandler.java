/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package bka.text.parser.sax;

import java.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

public class SaxStackHandler extends DefaultHandler {

    public interface Model {

        Object createObject(Element element);
    }

    public class Element {

        private Element(String uri, String localName, String qualifiedName, Attributes attributes) {
            this.uri = uri;
            this.localName = localName;
            this.qualifiedName = qualifiedName;
            this.attributes = attributes;
            children = new Children(this);
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

        private void appendCharacters(char buffer[], int start, int length) {
            characters.append(buffer, start, length);
        }

        public String getCharacters() {
            return characters.toString();
        }

        public Children getChildren() {
            return children;
        }

        private String getNamespace() {
            return SaxStackHandler.getNamespace(qualifiedName, localName);
        }

        private final String uri;
        private final String localName;
        private final String qualifiedName;
        private final Attributes attributes;
        private final StringBuilder characters = new StringBuilder();
        private final Children children;
    }


    public class Children {

        private Children(Element parent) {
            this.parent = parent;
        }

        private void insert(String qualifiedName, Element element) {
            children
                .computeIfAbsent(qualifiedName, name -> new ArrayList<>())
                .add(model.createObject(element));
        }

        /**
         * @param <T> type of elements
         * @param qualifiedName of the elements to get
         * @return List of elements with given qualified name, an empty list if no such elements are available
         */
        public <T> List<T> getList(String qualifiedName) {
            List<Object> elements = children.get(qualifiedName);
            if (elements == null) {
                return Collections.emptyList();
            }
            return (List<T>) Collections.unmodifiableList(elements);
        }

        /**
         * @param <T> type of elements
         * @param uri of the elements to get
         * @param localName of the elements to get
         * @return List of elements with given uri and local name, an empty list if no such elements are available
         */
        public <T> List<T> getList(String uri, String localName) {
            return getList(getQualifiedName(uri, localName));
        }

        /**
         * @param <T> type of elements
         * @param localName of the elements to get
         * @return List of elements with given local name in the namespace of the parent element. An empty list if no elements with local name are
         * available for the parent's namespace.
         * @throws IllegalStateException if the parent element has no namespace
         */
        public <T> List<T> getLocalList(String localName) {
            Optional<String> qualifiedName = getQualifiedName(localName);
            if (qualifiedName.isEmpty()) {
                return Collections.emptyList();
            }
            return getList(getQualifiedName(localName).get());
        }

        /**
         * @param <T> type of element
         * @param qualifiedName of the element to get
         * @return Single element with given qualified name
         * @throws NoSuchElementException if no element with given qualifiedName is available
         * @throws ClassCastException if the single element is not of type T
         * @throws IllegalArgumentException if multiple elements with given qualifieName are available
         */
        public <T> T getElement(String qualifiedName) {
            List<Object> elements = children.get(qualifiedName);
            if (elements == null) {
                throw new NoSuchElementException(qualifiedName);
            }
            if (elements.size() > 1) {
                throw new IllegalArgumentException("Multiple elements of '" + qualifiedName + "'");
            }
            return (T) elements.get(0);
        }

        /**
         * @param <T> type of element
         * @param uri of the element to get
         * @param localName of the element to get
         * @return Single element with given qualified name
         * @throws NoSuchElementException if no element with given uri and localName is available
         * @throws ClassCastException if the single element is not of type T
         * @throws IllegalArgumentException if multiple elements with given uri and localName are available
         */
        public <T> T getElement(String uri, String localName) {
            return getElement(getQualifiedName(uri, localName));
        }

        /**
         * @param <T> type of element
         * @param localName of the element to get
         * @return Single element with given local name in the namespace of the parent element
         * @throws IllegalStateException if the parent element has no namespace
         * @throws NoSuchElementException if no elements with given localName are available in the parents namespace
         * @throws ClassCastException if the single element is not of type T
         * @throws IllegalArgumentException if multiple elements with given qualifieName are available
         */
        public <T> T getLocalElement(String localName) {
            return getElement(getQualifiedName(localName).get());
        }

        private String getQualifiedName(String uri, String localName) {
            if (!namespaces.containsKey(uri)) {
                return ":" + localName;
            }
            return namespaces.get(uri) + localName;
        }

        private Optional<String> getQualifiedName(String localName) {
            String namespace = parent.getNamespace();
            if (namespace.isEmpty()) {
                throw new IllegalStateException("Cannot determine namespace for " + localName);
            }
            return children.keySet().stream().filter(key -> key.equals(namespace + localName)).findAny();
        }

        public Children getChildren(String qualifiedName) {
            return (Children) children.get(qualifiedName).get(0);
        }

        private final Element parent;
        private final Map<String, List<Object>> children = new HashMap<>();
    }

    public SaxStackHandler(Model model) {
        this.model = Objects.requireNonNull(model);
    }

    public <T> T getRoot() {
        if (root == null) {
            throw new IllegalStateException("Nothing has been parsed yet");
        }
        return (T) root;
    }

    @Override
    public void startElement(String uri, String localName, String qualifiedName, Attributes attributes) {
        stack.push(new Element(uri, localName, qualifiedName, attributes));
        if (!uri.isEmpty() && !namespaces.containsKey(uri)) {
            namespaces.put(uri, getNamespace(qualifiedName, localName));
        }
    }

    private static String getNamespace(String qualifiedName, String localName) {
        return qualifiedName.substring(0, qualifiedName.length() - localName.length());
    }

    @Override
    public void endElement(String uri, String localName, String qualifiedName) throws SAXException {
        Element element = stack.pop();
        if (stack.isEmpty()) {
            root = Objects.requireNonNull(model.createObject(element), "Root element must not be null");
        }
        else {
            stack.peek().getChildren().insert(qualifiedName, element);
        }
    }
    
    @Override
    public void characters(char buffer[], int start, int length) {
        stack.peek().appendCharacters(buffer, start, length);
    }

    private final Model model;
    private final Deque<Element> stack = new LinkedList<>();
    private final Map<String, String> namespaces = new HashMap<>();
    private Object root;

}