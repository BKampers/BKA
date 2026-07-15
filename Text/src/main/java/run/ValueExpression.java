package run;

import java.util.*;
import uml.structure.*;


/**
 * Expression that holds a runtime value.
 */
public final class ValueExpression extends Expression implements RuntimeExpression {

    public ValueExpression(java.lang.Object value, Type type) {
        this.value = value;
        this.type = Objects.requireNonNull(type);
    }

    @Override
    public java.lang.Object evaluate() {
        return value;
    }

    public java.lang.Object getValue() {
        return value;
    }

    @Override
    public Optional<Type> getType() {
        return Optional.of(type);
    }

    private final java.lang.Object value;
    private final Type type;

}
