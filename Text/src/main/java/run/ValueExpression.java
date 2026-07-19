package run;

import java.util.*;
import uml.structure.*;


/**
 * Expression that holds a runtime value.
 */
public final class ValueExpression implements Expression {

    public ValueExpression(java.lang.Object value, Type type) {
        this.value = Objects.requireNonNull(value);
        this.type = Objects.requireNonNull(type);
    }

    public java.lang.Object getValue() {
        return value;
    }

    @Override
    public java.lang.Object evaluate(Engine engine) {
        return value;
    }

    @Override
    public Optional<Type> getType() {
        return Optional.of(type);
    }

    private final java.lang.Object value;
    private final Type type;

}
