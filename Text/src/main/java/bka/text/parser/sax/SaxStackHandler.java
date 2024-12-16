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

        public void appendCharacters(char buffer[], int start, int length) {
            characters.append(buffer, start, length);
        }

        public String getCharacters() {
            return characters.toString();
        }

        public Children getChildren() {
            return children;
        }

        private final String uri;
        private final String localName;
        private final String qualifiedName;
        private final Attributes attributes;
        private final StringBuilder characters = new StringBuilder();
        private final Children children = new Children(new HashMap<>());
    }


    public class Children {

        private Children(Map<String, List<Object>> children) {
            this.children = children;
        }

        private void insert(String qualifiedName, Element element) {
            children
                .computeIfAbsent(qualifiedName, name -> new ArrayList<>())
                .add(model.createObject(element));
        }

        public <T> List<T> getList(String qualifiedName) {
            List<Object> elements = children.get(qualifiedName);
            if (elements == null) {
                return Collections.emptyList();
            }
            return (List<T>) Collections.unmodifiableList(elements);
        }

        /**
         * @param <T> type of element
         * @param qualifiedName of the element yp get
         * @return Single element with given qualified name
         * @throws RuntimeException if zero or more than one elements with given qualifieName are available or if the single element is not of type T
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

        public Children getChildren(String qualifiedName) {
            return (Children) children.get(qualifiedName).get(0);
        }

        private final Map<String, List<Object>> children;
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
    }

    @Override
    public void endElement(String uri, String localName, String qualifiedName) throws SAXException {
        Element element = stack.pop();
        if (stack.isEmpty()) {
            root = Objects.requireNonNull(model.createObject(element), "Root ellemnent must not be null");
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
    private Object root;

}