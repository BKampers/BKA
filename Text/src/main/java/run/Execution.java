package run;


import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import run.pascal.PascalValues;
import uml.annotation.*;
import uml.structure.*;


/**
 * Mutable state for one program run. Created by {@link Engine#execute()}.
 */
public final class Execution {

    public Execution(Engine engine) {
        this.engine = Objects.requireNonNull(engine);
    }

    public void run() {
        Operation mainOperation = engine.getProgramClass().getOperations().stream()
            .filter(Execution::hasMainStereotype)
            .findAny()
            .orElseThrow(NoSuchElementException::new);
        Map<Attribute, Evaluable> attributeValues = engine.getProgramClass().getAttributes().stream().collect(Collectors.toMap(
            Function.identity(),
            attribute -> PascalValues.uninitialized(attribute.getType().get())));
        programObject = MutableObject.constructAnonymous(engine.getProgramClass(), attributeValues);
        ObjectScope programScope = new ObjectScope(programObject);
        execute(engine.getMethods().get(mainOperation), programScope);
        programObject.getAttributeValues().forEach((attribute, valueSpecification) -> {
            Evaluable expression = asEvaluable(valueSpecification);
            System.out.println(attribute.getName().get() + " = (" + typeName(expression) + ") " + displayValue(expression, programScope));
        });
    }

    private static boolean hasMainStereotype(Operation operation) {
        return operation.getStereotypes().stream().anyMatch(Execution::isMainStereotype);
    }

    private static boolean isMainStereotype(Stereotype stereotype) {
        return "Main".equals(stereotype.getName());
    }
    
    /**
     * Executes a method in a new {@link ObjectScope}.
     *
     * @param operation method to execute
     * @param parentScope scope from which the call is made
     * @param arguments actual arguments keyed by formal parameters
     * @return the function result, or {@code VOID} if the operation is a procedure
     */
    public java.lang.Object execute(Operation operation, ObjectScope parentScope, Map<Parameter, Evaluable> arguments) {
        Map<Parameter, java.lang.Object> argumentValues = new LinkedHashMap<>();
        arguments.forEach((parameter, expression) -> argumentValues.put(parameter, evaluate(expression, parentScope)));
        ObjectScope callScope = createCallScope(operation, parentScope, argumentValues);
        execute(engine.getMethods().get(operation), callScope);
        writeBackInOutParameters(arguments, parentScope, callScope);
        if (isProcedure(operation)) {
            return VOID;
        }
        return loadFromScope(callScope, operation.getName().get());
    }

    public java.lang.Object evaluate(Evaluable expression, ObjectScope scope) {
        return expression.evaluate(this, scope);
    }

    public boolean evaluateBoolean(Evaluable expression, ObjectScope scope) {
        java.lang.Object value = evaluate(expression, scope);
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        throw new IllegalStateException("Boolean expected: " + value);
    }

    public void execute(Statement statement, ObjectScope scope) {
        statement.execute(this, scope);
    }

    public java.lang.Object getVariableValue(String name) {
        Attribute attribute = findProgramAttribute(name);
        return evaluate(asEvaluable(programObject.get(attribute)), new ObjectScope(programObject));
    }

    public Map<String, java.lang.Object> getRecordValue(String name) {
        return toRecordMap(getVariableValue(name));
    }

    public Map<String, java.lang.Object> toRecordMap(java.lang.Object value) {
        return toMap(value);
    }

    private void writeBackInOutParameters(Map<Parameter, Evaluable> arguments, ObjectScope parentScope, ObjectScope callScope) {
        arguments.entrySet().stream()
            .filter(entry -> entry.getKey().getDirection() == Parameter.Direction.INOUT)
            .forEach(entry -> asAssignable(entry.getValue()).assign(
                this,
                evaluate(PascalValues.valueOf(entry.getKey().getType().get(), loadFromScope(callScope, entry.getKey().getName().get())), parentScope),
                parentScope));
    }

    public java.lang.Object loadFromScope(ObjectScope scope, String name) {
        Optional<Attribute> attribute = findScopeAttribute(scope, name);
        if (attribute.isPresent()) {
            return evaluate(asEvaluable(scope.getObject().get(attribute.get())), scope);
        }
        if (scope.getParent().isPresent() && scope.getParent().get() instanceof ObjectScope parentScope) {
            return loadFromScope(parentScope, name);
        }
        throw new IllegalStateException("No such variable in scope: " + name);
    }

    private static Optional<Attribute> findScopeAttribute(ObjectScope scope, String name) {
        return scope.getObject().getAttributes().stream()
            .filter(attribute -> attribute.getName().isPresent() && name.equalsIgnoreCase(attribute.getName().get()))
            .findAny();
    }

    private ObjectScope createCallScope(Operation operation, ObjectScope parentScope, Map<Parameter, java.lang.Object> argumentValues) {
        UmlClassBuilder builder = new UmlClassBuilder(operation.getName().orElse("anonymous"));
        Map<Attribute, Evaluable> values = new LinkedHashMap<>();
        for (Parameter parameter : operation.getParameters()) {
            Attribute attribute = builder.withAttribute(parameter.getName().get(), parameter.getType().get(), Member.Visibility.PRIVATE);
            java.lang.Object argumentValue = argumentValues.get(parameter);
            if (argumentValue == null && !argumentValues.containsKey(parameter)) {
                throw new IllegalStateException("Missing argument for parameter: " + parameter.getName().get());
            }
            values.put(attribute, PascalValues.valueOf(parameter.getType().get(), argumentValue));
        }
        for (uml.structure.Object local : engine.getMethods().get(operation).getLocals()) {
            Attribute attribute = builder.withAttribute(local.getName().get(), local.getType().get(), Member.Visibility.PRIVATE);
            values.put(attribute, PascalValues.uninitialized(local.getType().get()));
        }
        if (!isProcedure(operation)) {
            Attribute attribute = builder.withAttribute(operation.getName().get(), operation.getType().get(), Member.Visibility.PRIVATE);
            values.put(attribute, PascalValues.uninitialized(operation.getType().get()));
        }
        uml.structure.Class frameType = builder.build();
        return new ObjectScope(parentScope, MutableObject.constructAnonymous(frameType, values));
    }

    private static boolean isProcedure(Operation operation) {
        return operation.getType().isEmpty();
    }

    private static String typeName(Expression expression) {
        if (expression.getType().isEmpty()) {
            return "@Void";
        }
        return expression.getType().get().getName().orElse("@Anonimous");
    }

    private String displayValue(Evaluable expression, ObjectScope scope) {
        java.lang.Object value = evaluate(expression, scope);
        if (value instanceof java.lang.Object[] array) {
            return Arrays.stream(array).map(java.lang.Object::toString).collect(Collectors.joining(",", "[", "]"));
        }
        return value.toString();
    }

    private Map<String, java.lang.Object> toMap(java.lang.Object value) {
        if (!(value instanceof MutableObject record)) {
            throw new IllegalArgumentException("Not a record value: " + value);
        }
        ObjectScope scope = new ObjectScope(record);
        Map<String, java.lang.Object> map = new LinkedHashMap<>();
        for (Attribute attribute : record.getAttributes()) {
            String fieldName = attribute.getName().get().toLowerCase();
            java.lang.Object fieldValue = evaluate(asEvaluable(record.get(attribute)), scope);
            if (fieldValue instanceof MutableObject nested) {
                map.put(fieldName, toMap(nested));
            }
            else {
                map.put(fieldName, fieldValue);
            }
        }
        return map;
    }

    private Attribute findProgramAttribute(String name) {
        return programObject.getAttributes().stream()
            .filter(attribute -> attribute.getName().isPresent() && name.equalsIgnoreCase(attribute.getName().get()))
            .findAny()
            .orElseThrow(() -> new NoSuchElementException("No such program variable: " + name));
    }

    public static Evaluable asEvaluable(ValueSpecification valueSpecification) {
        if (valueSpecification instanceof Evaluable evaluable) {
            return evaluable;
        }
        throw new IllegalStateException("Not an evaluable expression: " + valueSpecification);
    }

    public static Assignable asAssignable(Evaluable expression) {
        if (expression instanceof Assignable assignable) {
            return assignable;
        }
        throw new IllegalStateException("Not an assignable expression: " + expression);
    }

    private static final java.lang.Object VOID = new java.lang.Object() {
        @Override
        public String toString() {
            return "@VOID";
        }
    };
    
    private final Engine engine;

    private MutableObject programObject;

}
