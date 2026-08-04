package run.pascal;

import java.util.*;
import java.util.stream.*;
import run.*;
import uml.structure.*;


/**
 * Call to a Pascal procedure or function.
 */
public final class MethodCallExpression implements Evaluable {

    public MethodCallExpression(Operation operation, Map<Parameter, Evaluable> arguments) {
        Objects.requireNonNull(operation);
        if (arguments.size() != operation.getParameters().size()) {
            throw new IllegalArgumentException("Invalid number of parameters: " + arguments.size());
        }
        List<Parameter> missingParameters = operation.getParameters().stream()
            .filter(parameter -> !arguments.containsKey(parameter))
            .toList();
        if (!missingParameters.isEmpty()) {
            throw new IllegalArgumentException("Missing parameter(s): " + missingParameters.stream()
                .map(Objects::toString)
                .collect(Collectors.joining(", ")));
        }
        this.operation = operation;
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
        StringBuilder string = new StringBuilder();
        operation.getName().ifPresent(string::append);
        string.append(arguments.values().stream()
            .map(Objects::toString)
            .collect(Collectors.joining(", ", "(", ")")));
        return string.toString();
    }

    private final Operation operation;
    private final Map<Parameter, Evaluable> arguments;

}
