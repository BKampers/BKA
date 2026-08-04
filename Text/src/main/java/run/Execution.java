package run;


import java.util.*;
import java.util.function.*;
import java.util.logging.*;
import java.util.stream.*;
import run.pascal.*;
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

    private void assign(Evaluable assignable, Evaluable valueExpression, ObjectScope scope) {
        assignValue(assignable, evaluate(valueExpression, scope), scope);
    }

    private void assignValue(Evaluable assignable, java.lang.Object value, ObjectScope scope) {
        switch (assignable) {
            case ScopeVariableExpression variable ->
                scope.storeExpression(variable.getName(), PascalValues.valueOf(variable.getType().get(), value));
            case IndexAccessExpression indexAccess ->
                resolveArrayContainer(indexAccess.getBase(), scope)[IndexAccessExpression.arraySlot(indexAccess.getArrayType(), (Integer) evaluate(indexAccess.getIndex(), scope))] = value;
            case MemberAccessExpression memberAccess ->
                assignMember(memberAccess, value, scope);
            default ->
                throw new IllegalStateException("Unsupported assignable: " + assignable);
        }
    }

    public java.lang.Object[] resolveArrayContainer(Evaluable base, ObjectScope scope) {
        if (base instanceof ScopeVariableExpression) {
            return (java.lang.Object[]) evaluate(base, scope);
        }
        if (base instanceof MemberAccessExpression memberAccess) {
            return (java.lang.Object[]) evaluate(memberAccess, scope);
        }
        if (base instanceof IndexAccessExpression indexAccess) {
            java.lang.Object[] array = resolveArrayContainer(indexAccess.getBase(), scope);
            return (java.lang.Object[]) array[IndexAccessExpression.arraySlot(indexAccess.getArrayType(), (Integer) evaluate(indexAccess.getIndex(), scope))];
        }
        throw new IllegalStateException("Unsupported array base: " + base);
    }

    public MutableObject mutableObject(Evaluable expression, ObjectScope scope) {
        if (expression instanceof ScopeVariableExpression) {
            return (MutableObject) evaluate(expression, scope);
        }
        if (expression instanceof MemberAccessExpression memberAccess) {
            MutableObject parent = mutableObject(memberAccess.getReceiver(), scope);
            return (MutableObject) evaluate(asEvaluable(parent.get(MemberAccessExpression.findRecordAttribute(parent, memberAccess.getMember()))), scope);
        }
        if (expression instanceof IndexAccessExpression indexAccess) {
            return (MutableObject) evaluate(indexAccess, scope);
        }
        throw new IllegalStateException("Not a record reference: " + expression);
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

    private void assignMember(MemberAccessExpression memberAccess, java.lang.Object value, ObjectScope scope) {
        MutableObject record = mutableObject(memberAccess.getReceiver(), scope);
        Attribute attribute = MemberAccessExpression.findRecordAttribute(record, memberAccess.getMember());
        record.set(attribute, PascalValues.valueOf(attribute.getType().get(), value));
    }

    private void writeBackInOutParameters(Map<Parameter, Evaluable> arguments, ObjectScope parentScope, ObjectScope callScope) {
        arguments.entrySet().stream()
            .filter(entry -> entry.getKey().getDirection() == Parameter.Direction.INOUT)
            .forEach(entry -> assign(
                entry.getValue(),
                PascalValues.valueOf(entry.getKey().getType().get(), loadFromScope(callScope, entry.getKey().getName().get())),
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

    private void execute(Statement statement, ObjectScope scope) {
        if (statement.equals(Statement.NO_OPERATION)) {
            return;
        }
        switch (statement) {
            case CompoundStatement compound ->
                compound.getStatements().forEach(child -> execute(child, scope));
            case ExpressionStatement expressionStatement ->
                executeExpression(expressionStatement, scope);
            case LoopStatement loop ->
                executeLoop(loop, scope);
            case BranchStatement branch ->
                executeBranch(branch, scope);
            default ->
                throw new IllegalStateException("Unsupported statement: " + statement.getClass().getName());
        }
    }

    private void executeExpression(ExpressionStatement expressionStatement, ObjectScope scope) {
        java.lang.Object result = evaluate(expressionStatement.getExpression(), scope);
        expressionStatement.getAssignable().ifPresentOrElse(
            assignable -> assignValue(assignable, result, scope),
            () -> Logger.getLogger(getClass().getName()).log(Level.INFO, "Return value of {0} ignored", expressionStatement));
    }

    private void executeBranch(BranchStatement branch, ObjectScope scope) {
        java.lang.Object value = evaluate(branch.getCondition(), scope);
        for (Map.Entry<Evaluable, Statement> choice : branch.getChoices().entrySet()) {
            if (Objects.equals(value, evaluate(choice.getKey(), scope))) {
                execute(choice.getValue(), scope);
                return;
            }
        }
        branch.getDefaultChoice().ifPresent(statement -> execute(statement, scope));
    }

    private void executeLoop(LoopStatement loop, ObjectScope scope) {
        if (loop.getExitCondition().isPresent()) {
            do {
                execute(loop.getAction(), scope);
            } while (!requireBoolean(evaluate(loop.getExitCondition().get(), scope)));
            return;
        }
        if (loop.getIncrementAction().isPresent()) {
            executeForLoop(loop, scope);
            return;
        }
        while (requireBoolean(evaluate(loop.getEntryCondition().get(), scope))) {
            execute(loop.getAction(), scope);
        }
    }

    private void executeForLoop(LoopStatement loop, ObjectScope scope) {
        Evaluable entryCondition = loop.getEntryCondition().orElseThrow(() -> new IllegalStateException("No loop condition"));
        if (!(entryCondition instanceof BinaryOperatorExpression loopCondition && loopCondition.getOperator() == Operator.LESS_EQUAL)) {
            throw new IllegalStateException("Unsupported for loop condition: " + entryCondition);
        }
        Evaluable incrementCondition = new OperatorExpression(
            loopCondition.getLeft(),
            Operator.LESS_THAN,
            loopCondition.getRight());
        boolean doLoop = requireBoolean(evaluate(entryCondition, scope));
        while (doLoop) {
            execute(loop.getAction(), scope);
            if (requireBoolean(evaluate(incrementCondition, scope))) {
                execute(loop.getIncrementAction().get(), scope);
            }
            else {
                doLoop = false;
            }
        }
    }

    private static boolean requireBoolean(java.lang.Object value) {
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        throw new IllegalStateException("Boolean expected: " + value);
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

    private static final java.lang.Object VOID = new java.lang.Object() {
        @Override
        public String toString() {
            return "@VOID";
        }
    };
    
    private final Engine engine;

    private MutableObject programObject;

}
