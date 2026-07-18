package run;

import java.util.*;
import uml.structure.*;


/**
 * Expression that holds a runtime value.
 */
public final class ValueExpression extends Expression implements RuntimeExpression {

    public ValueExpression(java.lang.Object value, Type type) {
        this.value = Objects.requireNonNull(value);
        this.type = Optional.of(type);
    }

    @Override
    public java.lang.Object evaluate() {
        return value;
    }

    @Override
    public Optional<Type> getType() {
        return type;
    }

    private final java.lang.Object value;
    private final Optional<Type> type;

}
