package run;

import java.util.*;
import uml.structure.*;

/**
 * Mutable implementation of {@link uml.structure.Object}.
 *
 * <p>Attribute values can be read and updated after construction via {@link #set}.
 */
public class MutableObject implements uml.structure.Object {

    /**
     * Creates an object with the given name, type and attribute values.
     *
     * @param name object name, may be {@code null}
     * @param type object type
     * @param attributeValues initial attribute values
     */
    public MutableObject(String name, Type type, Map<Attribute, run.Expression> attributeValues) {
        this.name = name;
        this.type = Objects.requireNonNull(type);
        this.attributeValues = new HashMap<>(attributeValues);
    }

    /**
     * @return the name of this object
     */
    @Override
    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    /**
     * @return the type of this object
     */
    @Override
    public Optional<Type> getType() {
        return Optional.of(type);
    }

    /**
     * @return an unmodifiable view of the attribute values of this object
     */
    @Override
    public Map<Attribute, uml.structure.Expression> getAttributeValues() {
        return Collections.unmodifiableMap(attributeValues);
    }

    /**
     * @return the attributes of this object
     */
    public Collection<Attribute> getAttributes() {
        return attributeValues.keySet();
    }

    /**
     * Sets the value of the given attribute.
     *
     * @param attribute attribute to set
     * @param expression new value
     * @throws NoSuchElementException if this object has no such attribute
     */
    public void set(Attribute attribute, uml.structure.Expression expression) {
        requireAttribute(attribute);
        attributeValues.put(attribute, expression);
    }

    /**
     * Returns the value of the given attribute.
     *
     * @param attribute attribute to get
     * @return the value of the attribute
     * @throws NoSuchElementException if this object has no such attribute
     */
    public uml.structure.Expression get(Attribute attribute) {
        requireAttribute(attribute);
        return attributeValues.get(attribute);
    }

    private void requireAttribute(Attribute attribute) throws NoSuchElementException {
        if (!attributeValues.containsKey(attribute)) {
            throw new NoSuchElementException("No such attribute: " + attribute);
        }
    }

    private final String name;
    private final Type type;
    private final Map<Attribute, uml.structure.Expression> attributeValues;

}
