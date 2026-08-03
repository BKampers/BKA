package run.pascal;

import java.util.*;
import run.*;
import uml.structure.*;


/**
 * Call to a Pascal procedure or function.
 */
public final class MethodCallExpression implements Evaluable {

    public MethodCallExpression(Operation operation, Map<Parameter, Evaluable> arguments) {
        this.operation = Objects.requireNonNull(operation);
        this.arguments = Map.copyOf(arguments);
    }

    public Operation getOperation() {
        return operation;
    }

    public Map<Parameter, Evaluable> getArguments() {
        return arguments;
    }

    @Override
    public Optional<Type> getType() {
        return operation.getType();
    }

    @Override
    public java.lang.Object evaluate(Execution execution, ObjectScope scope) {
        return execution.execute(operation, scope, arguments);
    }

    @Override
    public String toString() {
        return new CallExpression(operation, arguments).toString();
    }

    private final Operation operation;
    private final Map<Parameter, Evaluable> arguments;

}
