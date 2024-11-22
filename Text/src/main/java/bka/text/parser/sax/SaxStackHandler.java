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

        public List<Object> get(String qualifiedName) {
            return Collections.unmodifiableList(children.get(qualifiedName));
        }

        public Children getChildren(String qualifiedName) {
            return (Children) children.get(qualifiedName).get(0);
        }

        public List<String> getStrings(String qualifiedName) {
            return children.get(qualifiedName).stream().map(element -> (String) element).collect(Collectors.toList());
        }

        public List<String> getStringList(String qualifiedName) {
            return (List<String>) children.get("authors").get(0);
        }

        public Optional<String> getString(String qualifiedName) {
            List<Object> elements = children.get(qualifiedName);
            if (elements == null) {
                return Optional.empty();
            }
            if (elements.size() > 1) {
                throw new IllegalArgumentException("Multiple elements of '" + qualifiedName + "'");
            }
            return Optional.of((String) elements.get(0));
        }

        public Optional<Integer> getInteger(String qualifiedName) {
            List<Object> elements = children.get(qualifiedName);
            if (elements == null) {
                return Optional.empty();
            }
            if (elements.size() > 1) {
                throw new IllegalArgumentException("Multiple elements of '" + qualifiedName + "'");
            }
            return Optional.of((Integer) elements.get(0));
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
            objects.add(model.createObject(qualifiedName, new Children(element.getChildren()), element.getCharacters()));
        }
        else {
            stack.peek().getChildren().computeIfAbsent(qualifiedName, k -> new ArrayList<>()).add(model.createObject(qualifiedName, new Children(element.getChildren()), element.getCharacters()));
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

        public Map<String, List<Object>> getChildren() {
            return children;
        }

        public void setObject(Object object) {
            this.object = object;
        }

        private final String qualifiedName;
        private final StringBuilder characters = new StringBuilder();
        private final Map<String, List<Object>> children = new HashMap<>();

        private Object object;
    }


    private final Model model;

    private final List<Object> objects = new ArrayList<>();
    private final Deque<Element> stack = new LinkedList<>();

}