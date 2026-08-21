package run.pascal;

import java.util.*;
import run.*;
import uml.structure.*;


/**
 * Reference to a variable in the current scope.
 */
public final class ScopeVariableExpression implements Assignable {

    public ScopeVariableExpression(String name, Type type) {
        this.name = Objects.requireNonNull(name);
        this.type = Objects.requireNonNull(type);
    }

    public String getName() {
        return name;
    }

    @Override
    public Optional<Type> getType() {
        return Optional.of(type);
    }

    @Override
    public java.lang.Object evaluate(Execution execution, ObjectScope scope) {
        return execution.loadFromScope(scope, name);
    }

    @Override
    public void assign(Execution execution, java.lang.Object value, ObjectScope scope) {
        scope.storeExpression(name, PascalValues.valueOf(type, value));
    }

    @Override
    public String toString() {
        return name;
    }

    private final String name;
    private final Type type;

}
