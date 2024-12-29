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

public class SaxStackHandler extends DefaultHandler {

    public SaxStackHandler(Function<XmlElement, Object> converter) {
        this.converter = Objects.requireNonNull(converter);
    }

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

    public static String getNamespace(String qualifiedName, String localName) {
        return qualifiedName.substring(0, qualifiedName.length() - localName.length());
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

    public Map<String, String> getNamespaces() {
        return Collections.unmodifiableMap(namespaces);
    }


    private final Function<XmlElement, Object> converter;
    private final Deque<Element> stack = new LinkedList<>();
    private final Map<String, String> namespaces = new HashMap<>();

    private Object root;

}
