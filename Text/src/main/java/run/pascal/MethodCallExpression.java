package run.pascal;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import run.CallExpression;
import run.Engine;
import run.Expression;
import uml.structure.Operation;
import uml.structure.Parameter;
import uml.structure.Type;


/**
 * Call to a Pascal procedure or function.
 */
public final class MethodCallExpression extends AbstractPascalExpression {

    public MethodCallExpression(Operation operation, Map<Parameter, Expression> arguments) {
        this.operation = Objects.requireNonNull(operation);
        this.arguments = Map.copyOf(arguments);
    }

    public Operation getOperation() {
        return operation;
    }

    public Map<Parameter, Expression> getArguments() {
        return arguments;
    }

    @Override
    public Optional<Type> getType() {
        return operation.getType();
    }

    @Override
    public java.lang.Object evaluate(Engine engine) {
        return engine.execute(operation, engine.getCurrentScope(), arguments);
    }

    @Override
    public String toString() {
        return new CallExpression(operation, arguments).toString();
    }

    private final Operation operation;
    private final Map<Parameter, Expression> arguments;

}
