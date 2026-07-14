package run;

import java.util.*;
import uml.structure.*;


/**
 * {@link Memory} implementation with a parent chain, backed by a {@link MutableObject}.
 *
 * <p>Each scope stores its variables as attributes on a {@link MutableObject}. {@link #load(String)}
 * evaluates the attribute's expression; {@link #store(String, Object)} updates the attribute with a
 * {@link ValueExpression}. When an identifier is not declared in the current scope, lookup and
 * assignment are delegated to the parent scope.
 *
 * @see StateMachine
 */
public class ObjectScope implements Memory {

    public ObjectScope(MutableObject object) {
        this(null, object);
    }

    public ObjectScope(Memory parent, MutableObject object) {
        this.parent = parent;
        this.object = Objects.requireNonNull(object);
    }

    public Optional<Memory> getParent() {
        return Optional.ofNullable(parent);
    }

    public MutableObject getObject() {
        return object;
    }

    @Override
    public java.lang.Object load(String name) throws MemoryException {
        Optional<Attribute> attribute = findAttribute(name);
        if (attribute.isPresent()) {
            return evaluate(object.get(attribute.get()));
        }
        if (parent != null) {
            return parent.load(name);
        }
        throw new MemoryException("Memory does not contain value for identifier '" + name + "'");
    }

    @Override
    public void store(String name, java.lang.Object value) throws MemoryException {
        Optional<Attribute> attribute = findAttribute(name);
        if (attribute.isPresent()) {
            object.set(attribute.get(), new ValueExpression(value, attribute.get().getType().get()));
            return;
        }
        if (parent != null) {
            parent.store(name, value);
            return;
        }
        throw new MemoryException("Memory does not contain identifier '" + name + "'");
    }

    /**
     * Stores an expression for the given identifier in this scope or a parent scope.
     *
     * @param name identifier to store
     * @param expression expression to store
     */
    public void storeExpression(String name, Expression expression) {
        Optional<Attribute> attribute = findAttribute(name);
        if (attribute.isPresent()) {
            object.set(attribute.get(), expression);
            return;
        }
        if (parent instanceof ObjectScope objectScope) {
            objectScope.storeExpression(name, expression);
            return;
        }
        throw new IllegalStateException("Memory does not contain identifier '" + name + "'");
    }

    private Optional<Attribute> findAttribute(String name) {
        return object.getAttributes().stream()
            .filter(attribute -> attribute.getName().isPresent() && name.equalsIgnoreCase(attribute.getName().get()))
            .findAny();
    }

    private static java.lang.Object evaluate(uml.structure.Expression expression) throws MemoryException {
        if (expression instanceof RuntimeExpression runtimeExpression) {
            return runtimeExpression.evaluate();
        }
        throw new MemoryException("Cannot evaluate expression: " + expression);
    }

    private final Memory parent;
    private final MutableObject object;

}
