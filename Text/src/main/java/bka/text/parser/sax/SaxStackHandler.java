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
        Object createObject(String qualifiedName, Map<String, List<Object>> children, String characters);
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
            stack.peek().getChildren().computeIfAbsent(qualifiedName, k -> new ArrayList<>()).add(model.createObject(qualifiedName, element.getChildren(), element.getCharacters()));
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

//    private final Map<Object, Object> pool = new HashMap<>();

}