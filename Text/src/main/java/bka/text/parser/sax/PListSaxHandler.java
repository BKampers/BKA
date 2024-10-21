package bka.text.parser.sax;

import java.math.*;
import java.time.*;
import java.util.*;
import java.util.function.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

public class PListSaxHandler extends DefaultHandler {

    public List<Object> getContent() {
        return getContent(0);
    }

    public List<Object> getContent(int index) {
        return plists.get(index);
    }

    @Override
    public void startElement(String uri, String localName, String qualifiedName, Attributes attributes) {
        switch (qualifiedName) {
            case "key" ->
                stack.push(new Element(null, ElementType.KEY));
            case "plist" ->
                stack.push(new Element(null, ElementType.PLIST));
            case "dict" ->
                stack.push(new Element(stack.peek().getContentKey(), ElementType.DICT));
            case "array" ->
                stack.push(new Element(stack.peek().getContentKey(), ElementType.ARRAY));
            case "string" ->
                stack.push(new Element(stack.peek().getContentKey(), ElementType.STRING));
            case "date" ->
                stack.push(new Element(stack.peek().getContentKey(), ElementType.DATE));
            case "real" ->
                stack.push(new Element(stack.peek().getContentKey(), ElementType.REAL));
            case "integer" ->
                stack.push(new Element(stack.peek().getContentKey(), ElementType.INTEGER));
            case "false" ->
                stack.push(new Element(stack.peek().getContentKey(), ElementType.FALSE));
            case "true" ->
                stack.push(new Element(stack.peek().getContentKey(), ElementType.TRUE));
            default ->
                throw new IllegalStateException("Unsupported qualified name: " + qualifiedName);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qualifiedName) throws SAXException {
        Element element = stack.pop();
        if (stack.isEmpty()) {
            plists.add(Collections.unmodifiableList((List) element.getValue()));
        }
        else {
            element.getType().assignChildToParent(stack.peek(), element);
        }
    }
    
    @Override
    public void characters(char buffer[], int start, int length) {
        stack.peek().appendCharacters(buffer, start, length);
    }

    private Object finalObject(Object object) {
        if (object instanceof List list) {
            return Collections.unmodifiableList(list);
        }
        if (object instanceof Map map) {
            return Collections.unmodifiableMap(map);
        }
        return pool.computeIfAbsent(object, o -> object);
    }


    private enum ElementType {
        
        PLIST(
            ArrayList::new,
            (parent, child) -> {
                throw new IllegalStateException("PList is expected be the root element");
        }),
        DICT(
            HashMap::new,
            (parent, child) -> parent.add(child.getKey(), child.getValue())
        ),
        ARRAY(
            ArrayList::new,
            (parent, child) -> parent.add(child.getKey(), child.getValue())
        ),
        KEY(
            StringBuilder::new,
            (parent, child) -> parent.setContentKey(child.getCharacters())
        ),
        STRING(
            StringBuilder::new,
            (parent, child) -> parent.add(parent.getContentKey(), child.getCharacters())
        ),
        DATE(
            StringBuilder::new,
            (parent, child) -> parent.add(parent.getContentKey(), ZonedDateTime.parse(child.getCharacters()))
        ),
        REAL(
            StringBuilder::new,
            (parent, child) -> parent.add(parent.getContentKey(), new BigDecimal(child.getCharacters()))
        ),
        INTEGER(
            StringBuilder::new, (parent, child) -> parent.add(parent.getContentKey(), new BigInteger(child.getCharacters()))
        ),
        FALSE(
            () -> Boolean.FALSE,
            (parent, child) -> parent.add(parent.getContentKey(), child.getValue())
        ),
        TRUE(
            () -> Boolean.TRUE,
            (parent, child) -> parent.add(parent.getContentKey(), child.getValue())
        );

        private ElementType(Supplier<Object> createValue, BiConsumer<Element, Element> handler) {
            this.createValue = createValue;
            this.childToParent = handler;
        }
        
        public Object createValue() {
            return createValue.get();
        }

        public void assignChildToParent(Element parent, Element child) {
            childToParent.accept(parent, child);
        }
    
        private final Supplier<Object> createValue;
        private final BiConsumer<Element, Element> childToParent;
        
    };


    private class Element {

        public Element(String key, ElementType type) {
            this.key = key;
            this.type = type;
            value = type.createValue();
        }
        
        public void add(String childKey, Object childValue) {
            switch(type) {
                case PLIST, ARRAY -> 
                    ((List) value).add(finalObject(childValue));
                case DICT ->
                    ((Map) value).put(childKey, finalObject(childValue));
                case STRING, DATE, INTEGER, REAL, FALSE, TRUE, KEY ->
                    throw new IllegalStateException("Cannot add '" + childValue.toString() + "' to " + type.name());
                default ->
                    throw new IllegalStateException(type.name());
            }
        }

        public String getKey() {
            return key;
        }

        public ElementType getType() {
            return type;
        }

        public Object getValue() {
            return value;
        }

        public void setContentKey(String contentKey) {
            this.contentKey = (String) pool.computeIfAbsent(contentKey, k -> contentKey);
        }
        
        public String getContentKey() {
            return contentKey;
        }
        
        public void appendCharacters(char buffer[], int start, int length) {
            characters.append(buffer, start, length);            
        }
        
        public String getCharacters() {
            return characters.toString();
        }
        
        private final String key;
        private final ElementType type;
        private final Object value;
        private String contentKey;
        private final StringBuilder characters = new StringBuilder();
    }


    private final List<List<Object>> plists = new ArrayList<>();
  
    private final Deque<Element> stack = new LinkedList<>();

    private final Map<Object, Object> pool = new HashMap<>();

}