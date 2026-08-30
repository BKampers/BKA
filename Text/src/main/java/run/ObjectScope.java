/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans, humanity, the environment or the universe.
*/

package run;

import java.util.*;
import uml.structure.*;


/**
 * Scope with a parent chain, backed by a {@link MutableObject}.
 *
 * <p>Each scope stores its variables as attributes on a {@link MutableObject}.
 * {@link #find(String)} returns the stored {@link Expression} in this frame only.
 * {@link #store(String, Expression)} writes in this frame or a parent that declares the name.
 */
public final class ObjectScope {

    public static final java.lang.Object UNINITIALIZED = new java.lang.Object() {
        @Override
        public String toString() {
            return "@uninitialized";
        }
    };

    public ObjectScope(MutableObject object) {
        this(null, object);
    }

    public ObjectScope(ObjectScope parent, MutableObject object) {
        this.parent = parent;
        this.object = Objects.requireNonNull(object);
    }

    public Optional<ObjectScope> getParent() {
        return Optional.ofNullable(parent);
    }

    public MutableObject getObject() {
        return object;
    }

    public Optional<Expression> find(String name) {
        return findAttribute(name).map(attribute -> requireExpression(object.get(attribute)));
    }

    public void store(String name, Expression expression) {
        Optional<Attribute> attribute = findAttribute(name);
        if (attribute.isPresent()) {
            object.set(attribute.get(), expression);
            return;
        }
        if (parent != null) {
            parent.store(name, expression);
            return;
        }
        throw new IllegalStateException("Memory does not contain identifier '" + name + "'");
    }

    public void storeExpression(String name, Expression expression) {
        store(name, expression);
    }

    private Optional<Attribute> findAttribute(String name) {
        return object.getAttributes().stream()
            .filter(attribute -> attribute.getName().isPresent() && name.equalsIgnoreCase(attribute.getName().get()))
            .findAny();
    }

    private static Expression requireExpression(ValueSpecification valueSpecification) {
        if (valueSpecification instanceof Expression expression) {
            return expression;
        }
        throw new IllegalStateException("Not an expression: " + valueSpecification);
    }

    private final ObjectScope parent;
    private final MutableObject object;

}
