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
     * @param type object type
     * @param attributeValues initial attribute values
     * @return new anonymous Mutable with the given type and attribute values
     * @throws IllegalArgumentException if attributeValues do not match type's attributes
     */
    public static MutableObject constructAnonymous(uml.structure.Class type, Map<Attribute, ? extends ValueSpecification> attributeValues) {
        return new MutableObject(Optional.empty(), type, attributeValues);
    }

    /**
     * @param name object name
     * @param type object type
     * @param attributeValues initial attribute values
     * @return new MutableObject with the given name, type and attribute values
     * @throws IllegalArgumentException if attributeValues do not match type's attributes
     */
    public static MutableObject construct(String name, uml.structure.Class type, Map<Attribute, ? extends ValueSpecification> attributeValues) {
        return new MutableObject(Optional.of(name), type, attributeValues);
    }

    private MutableObject(Optional<String> name, uml.structure.Class type, Map<Attribute, ? extends ValueSpecification> attributeValues) {
        if (attributeValues.size() != type.getAttributes().size() || !attributeValues.keySet().containsAll(type.getAttributes())) {
            throw new IllegalArgumentException("attributeValues keys do not match type attributes ");
        }
        this.name = name;
        this.type = type;
        this.attributeValues = new HashMap<>(attributeValues);
    }

    @Override
    public Optional<String> getName() {
        return name;
    }

    @Override
    public Optional<Type> getType() {
        return Optional.of(type);
    }

    @Override
    public Map<Attribute, ValueSpecification> getAttributeValues() {
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
     * @param valueSpecification new value
     * @throws NoSuchElementException if this object has no such attribute
     */
    public void set(Attribute attribute, ValueSpecification valueSpecification) {
        requireAttribute(attribute);
        attributeValues.put(attribute, valueSpecification);
    }

    /**
     * Returns the value of the given attribute.
     *
     * @param attribute attribute to get
     * @return the value of the attribute
     * @throws NoSuchElementException if this object has no such attribute
     */
    public ValueSpecification get(Attribute attribute) {
        requireAttribute(attribute);
        return attributeValues.get(attribute);
    }

    private void requireAttribute(Attribute attribute) throws NoSuchElementException {
        if (!attributeValues.containsKey(attribute)) {
            throw new NoSuchElementException("No such attribute: " + attribute);
        }
    }

    private final Optional<String> name;
    private final Type type;
    private final Map<Attribute, ValueSpecification> attributeValues;

}
