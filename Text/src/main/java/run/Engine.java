package run;


import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import run.pascal.*;
import uml.structure.*;


/**
 * Executes compiled Pascal programs and procedure/function calls using {@link ObjectScope}.
 */
public final class Engine {

    public Engine(uml.structure.Class programClass, Map<Operation, CompoundStatement> methods) {
        this.programClass = Objects.requireNonNull(programClass);
        this.methods = Map.copyOf(methods);
    }

    public void execute() {
        Operation mainOperation = programClass.getOperations().stream()
            .filter(operation -> operation.getStereotypes().stream().anyMatch(stereotype -> "Main".equals(stereotype.getName())))
            .findAny().get();
        Map<Attribute, Expression> attributeValues = programClass.getAttributes().stream().collect(Collectors.toMap(
            Function.identity(), 
            attribute -> PascalValues.uninitialized(attribute.getType().get())));
        programObject = MutableObject.constructAnonymous(
            programClass,
            attributeValues);
        currentScope = new ObjectScope(programObject);
        methods.get(mainOperation).getStatements().forEach(this::execute);
        programObject.getAttributeValues().forEach((attribute, valueSpecification) -> {
            Expression expression = asExpression(valueSpecification);
            System.out.println(attribute.getName().get() + " = (" + typeName(expression) + ") " + displayValue(expression));
        });
    }

    /**
     * Executes a procedure or function in a new {@link ObjectScope}.
     */
    public java.lang.Object execute(Operation operation, ObjectScope parentScope, Map<Parameter, Expression> arguments) {
        Map<Parameter, java.lang.Object> argumentValues = new LinkedHashMap<>();
        arguments.forEach((parameter, expression) -> argumentValues.put(parameter, evaluate(expression)));
        ObjectScope callScope = createCallScope(operation, parentScope, argumentValues);
        ObjectScope previousScope = currentScope;
        currentScope = callScope;
        try {
            execute(methods.get(operation));
        }
        finally {
            currentScope = previousScope;
        }
        writeBackInOutParameters(arguments, callScope);
        if (isProcedure(operation)) {
            return VOID;
        }
        return loadFromScope(callScope, operation.getName().get());
    }

    public ObjectScope getCurrentScope() {
        return currentScope;
    }

    public java.lang.Object evaluate(Expression expression) {
        return expression.evaluate(this);
    }

    public void assign(Expression assignable, Expression valueExpression) {
        assignValue(assignable, evaluate(valueExpression));
    }

    private void assignValue(Expression assignable, java.lang.Object value) {
        switch (assignable) {
            case ScopeVariableExpression variable ->
                getCurrentScope().storeExpression(variable.getName(), PascalValues.valueOf(variable.getType().get(), value));
            case IndexAccessExpression indexAccess ->
                resolveArrayContainer(indexAccess.getBase())[IndexAccessExpression.arraySlot(indexAccess.getArrayType(), (Integer) evaluate(indexAccess.getIndex()))] = value;
            case MemberAccessExpression memberAccess ->
                assignMember(memberAccess, value);
            default ->
                throw new IllegalStateException("Unsupported assignable: " + assignable);
        }
    }

    public java.lang.Object[] resolveArrayContainer(AbstractPascalExpression base) {
        if (base instanceof ScopeVariableExpression) {
            return (java.lang.Object[]) evaluate(base);
        }
        if (base instanceof MemberAccessExpression memberAccess) {
            return (java.lang.Object[]) evaluate(memberAccess);
        }
        if (base instanceof IndexAccessExpression indexAccess) {
            java.lang.Object[] array = resolveArrayContainer(indexAccess.getBase());
            return (java.lang.Object[]) array[IndexAccessExpression.arraySlot(indexAccess.getArrayType(), (Integer) evaluate(indexAccess.getIndex()))];
        }
        throw new IllegalStateException("Unsupported array base: " + base);
    }

    public MutableObject mutableObject(AbstractPascalExpression expression) {
        if (expression instanceof ScopeVariableExpression) {
            return (MutableObject) evaluate(expression);
        }
        if (expression instanceof MemberAccessExpression memberAccess) {
            MutableObject parent = mutableObject(memberAccess.getReceiver());
            return (MutableObject) evaluate(asExpression(parent.get(MemberAccessExpression.findRecordAttribute(parent, memberAccess.getMember()))));
        }
        if (expression instanceof IndexAccessExpression indexAccess) {
            return (MutableObject) evaluate(indexAccess);
        }
        throw new IllegalStateException("Not a record reference: " + expression);
    }

    public MutableObject getProgramObject() {
        return Objects.requireNonNull(programObject);
    }

    public java.lang.Object getVariableValue(String name) {
        Attribute attribute = findProgramAttribute(name);
        return evaluate(asExpression(programObject.get(attribute)));
    }

    public Map<String, java.lang.Object> getRecordValue(String name) {
        return toRecordMap(getVariableValue(name));
    }

    public Map<String, java.lang.Object> toRecordMap(java.lang.Object value) {
        return toMap(value);
    }

    private void assignMember(MemberAccessExpression memberAccess, java.lang.Object value) {
        MutableObject record = mutableObject(memberAccess.getReceiver());
        Attribute attribute = MemberAccessExpression.findRecordAttribute(record, memberAccess.getMember());
        record.set(attribute, PascalValues.valueOf(attribute.getType().get(), value));
    }

    private void writeBackInOutParameters(Map<Parameter, Expression> arguments, ObjectScope callScope) {
        arguments.entrySet().stream()
            .filter(entry -> entry.getKey().getDirection() == Parameter.Direction.INOUT)
            .forEach(entry -> assign(
                entry.getValue(),
                PascalValues.valueOf(entry.getKey().getType().get(), loadFromScope(callScope, entry.getKey().getName().get()))));
    }

    public java.lang.Object loadFromScope(ObjectScope scope, String name) {
        Optional<Attribute> attribute = findScopeAttribute(scope, name);
        if (attribute.isPresent()) {
            return evaluate(asExpression(scope.getObject().get(attribute.get())));
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
        Map<Attribute, Expression> values = new LinkedHashMap<>();
        for (Parameter parameter : operation.getParameters()) {
            Attribute attribute = builder.withAttribute(parameter.getName().get(), parameter.getType().get(), Member.Visibility.PRIVATE);
            java.lang.Object argumentValue = argumentValues.get(parameter);
            if (argumentValue == null && !argumentValues.containsKey(parameter)) {
                throw new IllegalStateException("Missing argument for parameter: " + parameter.getName().get());
            }
            values.put(attribute, PascalValues.valueOf(parameter.getType().get(), argumentValue));
        }
        for (uml.structure.Object local : methods.get(operation).getLocals()) {
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

    private boolean isProcedure(Operation operation) {
        return operation.getType().map(PascalTypes.VOID::equals).orElse(true);
    }

    private static String typeName(Expression expression) {
        if (expression.getType().isEmpty()) {
            return "@Void";
        }
        return expression.getType().get().getName().orElse("@Anonimous");
    }

    private String displayValue(Expression expression) {
        java.lang.Object value = evaluate(expression);
        if (value instanceof java.lang.Object[] array) {
            return Arrays.stream(array).map(java.lang.Object::toString).collect(Collectors.joining(",", "[", "]"));
        }
        return value.toString();
    }

    private Map<String, java.lang.Object> toMap(java.lang.Object value) {
        if (!(value instanceof MutableObject record)) {
            throw new IllegalArgumentException("Not a record value: " + value);
        }
        Map<String, java.lang.Object> map = new LinkedHashMap<>();
        for (Attribute attribute : record.getAttributes()) {
            String fieldName = attribute.getName().get().toLowerCase();
            java.lang.Object fieldValue = evaluate(asExpression(record.get(attribute)));
            if (fieldValue instanceof MutableObject nested) {
                map.put(fieldName, toMap(nested));
            }
            else {
                map.put(fieldName, fieldValue);
            }
        }
        return map;
    }

    private void execute(Statement statement) {
        if (statement.equals(Statement.NO_OPERATION)) {
            return;
        }
        switch (statement) {
            case CompoundStatement compound ->
                compound.getStatements().forEach(this::execute);
            case ExpressionStatement expressionStatement ->
                executeExpression(expressionStatement);
            case LoopStatement loop ->
                executeLoop(loop);
            case BranchStatement branch ->
                executeBranch(branch);
            default ->
                throw new IllegalStateException("Unsupported statement: " + statement.getClass().getName());
        }
    }

    private void executeExpression(ExpressionStatement expressionStatement) {
        java.lang.Object result = evaluate(expressionStatement.getExpression());
        expressionStatement.getAssignable().ifPresentOrElse(
            assignable -> assignValue(assignable, result), 
            () -> java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.INFO, "Return value of {0} ignored", expressionStatement));
    }

    private void executeBranch(BranchStatement branch) {
        java.lang.Object value = evaluate(branch.getCondition());
        for (Map.Entry<Expression, Statement> choice : branch.getChoices().entrySet()) {
            if (Objects.equals(value, evaluate(choice.getKey()))) {
                execute(choice.getValue());
                return;
            }
        }
        branch.getDefaultChoice().ifPresent(this::execute);
    }

    private void executeLoop(LoopStatement loop) {
        if (loop.getExitCondition().isPresent()) {
            do {
                execute(loop.getAction());
            } while (!requireBoolean(evaluate(loop.getExitCondition().get())));
            return;
        }
        if (loop.getIncrementAction().isPresent()) {
            executeForLoop(loop);
            return;
        }
        while (requireBoolean(evaluate(loop.getEntryCondition().get()))) {
            execute(loop.getAction());
        }
    }

    private void executeForLoop(LoopStatement loop) {
        Expression entryCondition = loop.getEntryCondition().orElseThrow(() -> new IllegalStateException("No loop condition"));
        if (!(entryCondition instanceof BinaryOperatorExpression loopCondition && loopCondition.getOperator() == Operator.LESS_EQUAL)) {
            throw new IllegalStateException("Unsupported for loop condition: " + entryCondition);
        }
        Expression incrementCondition = new OperatorExpression(
            (AbstractPascalExpression) loopCondition.getLeft(),
            Operator.LESS_THAN,
            (AbstractPascalExpression) loopCondition.getRight());
        boolean doLoop = requireBoolean(evaluate(entryCondition));
        while (doLoop) {
            execute(loop.getAction());
            if (requireBoolean(evaluate(incrementCondition))) {
                execute(loop.getIncrementAction().get());
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

    public static Expression asExpression(ValueSpecification valueSpecification) {
        if (valueSpecification instanceof Expression expression) {
            return expression;
        }
        throw new IllegalStateException("Not a runtime expression: " + valueSpecification);
    }

    private static final java.lang.Object VOID = new java.lang.Object() {
        @Override
        public String toString() {
            return "@VOID";
        }
    };

    private final uml.structure.Class programClass;
    private final Map<Operation, CompoundStatement> methods;

    private MutableObject programObject;
    private ObjectScope currentScope;

}
