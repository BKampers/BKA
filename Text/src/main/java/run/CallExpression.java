package run;

import java.util.*;
import java.util.stream.*;
import uml.structure.*;


/**
 * Structural description of a call; not executable.
 * Prefer {@link run.pascal.MethodCallExpression} for evaluation.
 */
public final class CallExpression implements Expression {

    public CallExpression(Operation operation) {
        this(operation, Collections.emptyMap());
    }

    public CallExpression(Operation operation, Map<Parameter, ? extends Expression> arguments) {
        Collection<Parameter> missingParameters = operation.getParameters().stream()
            .filter(parameter -> !arguments.containsKey(parameter))
            .toList();
        if (!missingParameters.isEmpty()) {
            throw new IllegalArgumentException("Missing parameter(s): " + missingParameters.stream()
                .map(Objects::toString)
                .collect(Collectors.joining(", ")));
        }
        this.operation = Objects.requireNonNull(operation);
        Map<Parameter, Expression> copy = new LinkedHashMap<>();
        copy.putAll(arguments);
        this.arguments = Collections.unmodifiableMap(copy);
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
    public String toString() {
        StringBuilder string = new StringBuilder();
        operation.getName().ifPresent(string::append);
        string.append(arguments.values().stream()
            .map(Objects::toString)
            .collect(Collectors.joining(", ", "(", ")")));
        return string.toString();
    }

    private final Operation operation;
    private final Map<Parameter, Expression> arguments;

}
