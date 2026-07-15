package run.pascal;

import java.util.Objects;
import java.util.Optional;
import run.Engine;
import uml.structure.Type;


/**
 * Reference to a variable in the current scope.
 */
public final class ScopeVariableExpression extends AbstractPascalExpression {

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
    public java.lang.Object evaluate(Engine engine) {
        return engine.loadFromScope(engine.getCurrentScope(), name);
    }

    @Override
    public String toString() {
        return name;
    }

    private final String name;
    private final Type type;

}
