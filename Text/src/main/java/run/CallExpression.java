package run;

import java.util.*;
import java.util.stream.*;
import uml.structure.*;


/**
 */
public class CallExpression extends Expression {

    public CallExpression(Operation operation) {
        this(operation, Collections.emptyMap());
    }

    public CallExpression(Operation operation, Map<Parameter, Expression> arguments) {
        Collection<Parameter> missingParameters = operation.getParameters().stream().filter(parameter -> !arguments.containsKey(parameter)).collect(Collectors.toList());
        if (!missingParameters.isEmpty()) {
            throw new IllegalArgumentException("Missing parameter(s): " + missingParameters.stream().map(parameter -> parameter.toString()).collect(Collectors.joining(", ")));
        }
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

    private final Operation operation;
    private final Map<Parameter, Expression> arguments;

}
