/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package bka.text.parser.sax;

import java.util.*;
import java.util.stream.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

public class SaxStackHandler extends DefaultHandler {

    public class Children {

        private Children(Map<String, List<Object>> children) {
            this.children = children;
        }

        private void insert(String qualifiedName, Element element) {
            children
                .computeIfAbsent(qualifiedName, name -> new ArrayList<>())
                .add(model.createObject(qualifiedName, element.getChildren(), element.getCharacters()));
        }

        public <T> List<T> getList(String qualifiedName) {
            List<Object> elements = children.get(qualifiedName);
            if (elements == null) {
                return Collections.emptyList();
            }
            return elements.stream().map(element -> (T) element).collect(Collectors.toList());
        }

        /**
         * @param <T> type of element
         * @param qualifiedName of the element yp get
         * @return Single element with given qualified name
         * @throws RuntimeException if zero or more than one elements with given qualifieName are available or if the single element is not of
         * specified element type
         */
        public <T> T getElement(String qualifiedName) {
            List<Object> elements = children.get(qualifiedName);
            if (elements == null) {
                throw new NoSuchElementException(qualifiedName);
            }
            if (elements.size() > 1) {
                throw new IllegalStateException("Multiple elements of '" + qualifiedName + "'");
            }
            return (T) elements.get(0);
        }

        public Children getChildren(String qualifiedName) {
            return (Children) children.get(qualifiedName).get(0);
        }

        private final Map<String, List<Object>> children;
    }

    public interface Model {
        Object createObject(String qualifiedName, Children children, String characters);
    }

    public List<Object> getObjects() {
        return Collections.unmodifiableList(objects);
    }

    @Override
    public void startElement(String uri, String localName, String qualifiedName, Attributes attributes) {
        stack.push(new Element(qualifiedName));
    }

    public SaxStackHandler(Model model) {
        this.model = Objects.requireNonNull(model);
    }

    @Override
    public void endElement(String uri, String localName, String qualifiedName) throws SAXException {
        Element element = stack.pop();
        if (stack.isEmpty()) {
            objects.add(model.createObject(qualifiedName, element.getChildren(), element.getCharacters()));
        }
        else {
            stack.peek().getChildren().insert(qualifiedName, element);
        }
    }
    
    @Override
    public void characters(char buffer[], int start, int length) {
        stack.peek().appendCharacters(buffer, start, length);
    }


    private class Element {

        public Element(String qualifiedName) {
            this.qualifiedName = qualifiedName;
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

        public void setObject(Object object) {
            this.object = object;
        }

        private final String qualifiedName;
        private final StringBuilder characters = new StringBuilder();
        private final Children children = new Children(new HashMap<>());

        private Object object;
    }


    private final Model model;

    private final List<Object> objects = new ArrayList<>();
    private final Deque<Element> stack = new LinkedList<>();

}