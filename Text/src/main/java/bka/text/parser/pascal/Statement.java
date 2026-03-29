/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans, humanity, the environment or the universe.
*/
package bka.text.parser.pascal;

import bka.text.parser.*;
import java.util.*;
import java.util.function.*;
import java.util.logging.*;
import java.util.stream.*;
import run.*;
import uml.statechart.*;
import uml.structure.*;

/**
 * Statement defined by a pascal parser node and executable by a state machine.
 */
public final class Statement {

    public Statement(Node expression, PascalCompiler.MethodProperties properties) {
        this(Optional.empty(), expression, properties);
    }

    private Statement(Node assignable, Node expression, PascalCompiler.MethodProperties properties) {
        this(Optional.of(assignable), expression, properties);
    }

    private Statement(Optional<Node> assignable, Node expression, PascalCompiler.MethodProperties properties) {
        this.assignable = assignable;
        this.expression = Objects.requireNonNull(expression);
        this.methodProperties = Objects.requireNonNull(properties);
    }

    public void createTransitions(ActivityDiagramBuilder diagram) {
        if (!expression.getChildren().isEmpty()) {
            switch (expression.getChildren().getFirst().getSymbol()) {
                case "IF\\b" ->
                    addIfThenElseTransitions(diagram);
                case "FOR\\b" ->
                    addForLoopTransitions(diagram);
                case "WHILE\\b" ->
                    addWhileLoopTransitions(diagram);
                case "REPEAT\\b" ->
                    addRepeatLoopTransitions(diagram);
                case "Assignable" ->
                    addAssignementTransitions(diagram);
                case "Identifier" ->
                    diagram.add(createActionState(new Statement(expression, methodProperties)));
                case "Call" ->
                    addProcedureCall(diagram);
                default ->
                    throw new IllegalStateException("Unexpected symbol '" + expression.getChildren().getFirst().getSymbol() + "' in expression " + expression + " on line " + expression.startLine());
            }
        }
        else if (!"Statement".equals(expression.getSymbol())) {
            getLogger().log(Level.WARNING, "Empty statenemt: {0}", expression);
        }
    }

    private void addIfThenElseTransitions(ActivityDiagramBuilder diagram) {
        Decision<Evaluator> decision = createDecision(expression.getChild("Expression"));
        diagram.add(leave -> UmlTransitionFactory.createTransition(leave, decision), decision);
        createTransitions(expression.getChild("Statement"), diagram);
        diagram.addGuardCondition(transition -> decision.equals(transition.getSource()), UmlGuardConditionFactory.pass(decision), "then");
        Node elseClause = expression.getChild("ElseClause");
        if (elseClause.getChildren().isEmpty()) {
            diagram.addLeaf(decision);
        }        
        else {
            diagram.fork(decision);
            createTransitions(elseClause.getChild("Statement"), diagram);
            diagram.addStereotype(transition -> decision.equals(transition.getSource()) && transition.getGuardCondition().isEmpty(), "else");
            diagram.join();
        }
    }

    private void addForLoopTransitions(ActivityDiagramBuilder diagram) {
        Node identifier = expression.getChild("Identifier");
        List<Node> expressions = expression.findChildren("Expression");
        ActionState<Action> loopInitialization = createActionState(new Statement(identifier, expressions.getFirst(), methodProperties));
        Decision<Evaluator> loopStartDecision = createDecision(identifier, expressions.getLast(), ".LE.", i -> i <= 0);
        diagram.add(loopInitialization, loopStartDecision);
        createTransitions(expression.getChild("Statement"), diagram);
        diagram.addGuardCondition(loopStartDecision, UmlGuardConditionFactory.pass(loopStartDecision), "for");
        TransitionTarget loopStart = diagram.targetOf(loopStartDecision);
        Decision<Evaluator> loopEndDecision = createDecision(identifier, expressions.getLast(), ".LT.", i -> i < 0);
        diagram.add(loopEndDecision, UmlStateFactory.createActionState(createIncrementAction(identifier)), loopStart, "for");
        diagram.addLeaf(loopStartDecision);
    }

    private Action createIncrementAction(Node identifier) {
        return createAction(
            memory -> memory.store(identifier.content(), ((Integer) memory.load(identifier.content())) + 1),
            () -> ".INC. " + identifier.content());
    }

    private static Action createAction(Action action, Supplier<String> toString) {
        return new Action() {
            @Override 
            public void perform(Memory memory)  throws StateMachineException {
                action.perform(memory);
            }
            @Override 
            public String toString() {
                return toString.get();
            }
        };
    }
    
    private void addWhileLoopTransitions(ActivityDiagramBuilder diagram) {
        Decision<Evaluator> decision = createDecision(expression.getChild("Expression"));
        diagram.add(leave -> UmlTransitionFactory.createTransition(leave, decision), decision);
        createTransitions(expression.getChild("Statement"), diagram);
        diagram.addGuardCondition(transition -> decision.equals(transition.getSource()), UmlGuardConditionFactory.pass(decision), "while");
        diagram.add(decision, UmlStereotypeFactory.createStereotypes("loop"));
    }

    private void addRepeatLoopTransitions(ActivityDiagramBuilder diagram) {
        TransitionSource loopRoot = diagram.anyLeaf();
        createTransitions(expression.getChild("Statements"), diagram);
        Decision<Evaluator> decision = createDecision(expression.getChild("Expression"));
        diagram.add(leave -> UmlTransitionFactory.createTransition(leave, decision), decision);
        TransitionTarget loopStart = diagram.targetOf(loopRoot);
        diagram.addTransition(decision, loopStart, UmlGuardConditionFactory.fail(decision), "repeat");
    }

    private Decision<Evaluator> createDecision(Node node) {
        return createDecision(node, node.content());
    }

    private Decision<Evaluator> createDecision(Node operand, String name) {
        return new Decision<>() {
            @Override
            public Evaluator getExpression() {
                return memory -> evaluate(operand, memory).object();
            }
            @Override
            public Optional<Type> getType() {
                return Optional.of(new Type() {
                    @Override
                    public boolean isAbstract() {
                        return false;
                    }
                    @Override
                    public Optional<String> getName() {
                        return Optional.of("Decision-Object");
                    }
                });
            }
            @Override
            public Optional<String> getName() {
                return Optional.of(name);
            }
        };
    }

    private Decision<Evaluator> createDecision(Node leftOperand, Node rightOperand, String name, Predicate<Integer> predicate) {
        return new Decision<>() {
            @Override
            public Evaluator getExpression() {
                return memory -> {
                    Value left = evaluate(leftOperand, memory);
                    Value right = evaluate(rightOperand, memory);
                    return predicate.test(requireComparable(left.object()).compareTo(requireComparable(right.object())));
                };
            }

            @Override
            public Optional<Type> getType() {
                return Optional.of(new Type() {
                    @Override
                    public boolean isAbstract() {
                        return false;
                    }

                    @Override
                    public Optional<String> getName() {
                        return Optional.of("Decision-Boolean");
                    }
                });
            }

            @Override
            public Optional<String> getName() {
                return Optional.of(name);
            }

        };
    }

    private void addAssignementTransitions(ActivityDiagramBuilder diagram) {
        diagram.add(createActionState(new Statement(expression.getChild("Assignable"), expression.getChild("Expression"), methodProperties)));
    }

    private void addProcedureCall(ActivityDiagramBuilder diagram) {
        diagram.add(createActionState(new Statement(expression.getChild("Call"), methodProperties)));
    }

    private static ActionState<Action> createActionState(Statement statement) {
        return UmlStateFactory.createActionState(statement::execute);
    }

    private void createTransitions(Node statements, ActivityDiagramBuilder diagram) {
        if (statements.startsWith("CompoundStatement")) {
            createStatementSequence(statements.getChild("CompoundStatement").getChild("Statements"), diagram);
        }
        else if (statements.getSymbol().equals("Statements")) {
            createStatementSequence(statements, diagram);
        }
        else {
            new Statement(statements, methodProperties).createTransitions(diagram);
        }
    }

    private void createStatementSequence(Node statements, ActivityDiagramBuilder diagram) {
        Optional<Node> next = Optional.of(statements);
        while (next.isPresent()) {
            Statement statement = new Statement(next.get().getChild("Statement"), methodProperties);
            statement.createTransitions(diagram);
            next = next.get().findChild("Statements");
        }
    }

    private Collection<String> localNames(String methodName) {
        return methodProperties.getLocals(methodName).stream()
            .map(uml.structure.Object::getName)
            .map(Optional::get)
            .collect(Collectors.toList());
    }

    public void execute(Memory memory) throws StateMachineException {
        Value value = evaluate(expression, memory);
        if (assignable.isPresent()) {
            if (value.type().equals("Void")) {
                throw new IllegalStateException(assignable.get() + " cannot be assigned with void");
            }
            store(memory, value.object());
        }
        else if (!value.type().equals("Void")) {
            getLogger().log(Level.WARNING, "Evaluation ({1}) of ''{0}'' is ignored", new java.lang.Object[]{expression.content(), value.object()});
        }
    }

    private Value evaluate(Node node, Memory memory) throws StateMachineException {
        return switch (node.getSymbol()) {
            case "Statement" ->
                evaluateStatement(node, memory);
            case "Expression" ->
                evaluateExpression(node, memory);
            case "Term" ->
                evaluateTerm(node, memory);
            case "Factor" ->
                evaluateFactor(node, memory);
            case "Comparable" ->
                evaluateComparable(node, memory);
            case "Call" ->
                evaluateCall(node, memory);
            case "Identifier" ->
                new Value(memory.load(node.content().toLowerCase()), typeOf(node));
            default ->
                throw new StateMachineException("Cannot evaluate " + node);
        };
    }

    private Type typeOf(Node node) {
        Type type = methodProperties.determineTypeOf(node.content());
        if (type.getName().isEmpty()) {
            return UmlTypeFactory.create("anonimous?");
        }
        return type;
    }

    private Value evaluateStatement(Node node, Memory memory) throws StateMachineException {
        return switch (node.getChildren().getFirst().getSymbol()) {
            case "Identifier" ->
                evaluateIdentifier(node.getChildren().getFirst(), memory);
            default ->
                throw new StateMachineException("Cannot evaluate statement: " + node);
        };
    }

    private Value evaluateExpression(Node node, Memory memory) throws StateMachineException {
        return switch (node.getChildren().size()) {
            case 1 ->
                evaluateComparable(node.getChildren().getFirst(), memory);
            case 3 ->
                evaluateRelationalOperation(node, memory);
            default ->
                throw new StateMachineException("Cannot evaluate expression " + node);
        };
    }

    private Value evaluateRelationalOperation(Node node, Memory memory) throws StateMachineException {
        Comparable left = requireComparable(evaluate(node.getChildren().getFirst(), memory).object());
        Comparable right = requireComparable(evaluate(node.getChildren().getLast(), memory).object());
        int comparison = left.compareTo(right);
        Node operator = node.getChild("RelationalOperator").getChildren().getFirst();
        return switch (operator.getSymbol()) {
            case "\\=" ->
                new Value(comparison == 0, PascalCompiler.BOOLEAN);
            case "\\<\\>" ->
                new Value(comparison != 0, PascalCompiler.BOOLEAN);
            case "\\<\\=" ->
                new Value(comparison <= 0, PascalCompiler.BOOLEAN);
            case "\\<" ->
                new Value(comparison < 0, PascalCompiler.BOOLEAN);
            case "\\>\\=" ->
                new Value(comparison >= 0, PascalCompiler.BOOLEAN);
            case "\\>" ->
                new Value(comparison > 0, PascalCompiler.BOOLEAN);
            default ->
                throw new StateMachineException("Unsupported relational operator" + operator.content());
        };
    }

    private Value evaluateComparable(Node node, Memory memory) throws StateMachineException {
        return evaluateOperation(
            evaluateTerm(node.getChild("Term"), memory),
            node.getChild("AdditiveOperation"),
            this::evaluateTerm,
            Statement::additiveOperation,
            memory);
    }


    private static java.lang.Object additiveOperation(java.lang.Object left, String operator, java.lang.Object right) throws StateMachineException {
        return switch (operator) {
            case "\\+" ->
                sum(requireNumber(left), requireNumber(right));
            case "\\-" ->
                difference(requireNumber(left), requireNumber(right));
            case "OR\\b" ->
                or(left, right);
            case "XOR\\b" ->
                xor(left, right);
            default ->
                throw new StateMachineException("Unsupported additive operator: " + operator);
        };
    }

    private Value evaluateTerm(Node node, Memory memory) throws StateMachineException {
        return evaluateOperation(
            evaluateFactor(node.getChild("Factor"), memory),
            node.getChild("MultiplicativeOperation"),
            this::evaluateFactor,
            Statement::multiplicativeOperation,
            memory);
    }

    private Value evaluateOperation(Value left, Node node, OperandEvaluator operandEvaluator, DyadicOperation operation, Memory memory) throws StateMachineException {
        List<Node> nodes = node.getChildren();
        if (nodes.isEmpty()) {
            return left;
        }
        java.lang.Object result = operation.compute(
            left.object(),
            nodes.getFirst().getChildren().getFirst().getSymbol(),
            operandEvaluator.evaluate(nodes.get(1), memory).object());
        return evaluateOperation(
            new Value(result, (result instanceof Boolean) ? PascalCompiler.BOOLEAN : (result instanceof Integer) ? PascalCompiler.INTEGER : PascalCompiler.REAL),
            nodes.getLast(),
            operandEvaluator,
            operation,
            memory);
    }

    private static java.lang.Object multiplicativeOperation(java.lang.Object left, String operator, java.lang.Object right) throws StateMachineException {
        return switch (operator) {
            case "\\*" ->
                product(requireNumber(left), requireNumber(right));
            case "\\/" ->
                requireNumber(left).floatValue() / requireNumber(right).floatValue();
            case "DIV\\b" ->
                requireInteger(left) / requireInteger(right);
            case "MOD\\b" ->
                requireInteger(left) % requireInteger(right);
            case "AND\\b" ->
                and(left, right);
            default ->
                throw new StateMachineException("Unsupported multiplicative operator: " + operator);
        };
    }

    private static Number product(Number left, Number right) {
        if (left instanceof Integer && right instanceof Integer) {
            return left.intValue() * right.intValue();
        }
        return left.floatValue() * right.floatValue();
    }

    private static Number sum(Number left, Number right) {
        if (left instanceof Integer && right instanceof Integer) {
            return left.intValue() + right.intValue();
        }
        return left.floatValue() + right.floatValue();
    }

    private static Number difference(Number left, Number right) {
        if (left instanceof Integer && right instanceof Integer) {
            return left.intValue() - right.intValue();
        }
        return left.floatValue() - right.floatValue();
    }

    private static java.lang.Object and(java.lang.Object left, java.lang.Object right) throws StateMachineException {
        if (left instanceof Boolean leftBoolean) {
            return leftBoolean & requireBoolean(right);
        }
        return requireInteger(left) & requireInteger(right);
    }

    private static java.lang.Object or(java.lang.Object left, java.lang.Object right) throws StateMachineException {
        if (left instanceof Boolean leftBoolean) {
            return leftBoolean | requireBoolean(right);
        }
        return requireInteger(left) | requireInteger(right);
    }

    private static java.lang.Object xor(java.lang.Object left, java.lang.Object right) throws StateMachineException {
        if (left instanceof Boolean leftBoolean) {
            return leftBoolean ^ requireBoolean(right);
        }
        return requireInteger(left) ^ requireInteger(right);
    }

    private Value evaluateCall(Node node, Memory memory) throws StateMachineException {
        String name = node.getChild("Identifier").content();
        Collection<Transition<Event, GuardCondition, Action>> methodBody = methodProperties.getBody(name);
        if (methodBody == null) {
            throw new StateMachineException("No such procedure or function: '" + name + '\'');
        }
        List<Node> arguments = createArgumentList(node.findChild("ArgumentList"));
        List<Parameter> signatureParameters = methodProperties.getParameters(name);
        int parameterCount = signatureParameters.size();
        if (arguments.size() != parameterCount) {
            throw new StateMachineException("Invalid numer of parameters for: '" + name + "\'. required: " + parameterCount + ", actual: " + arguments.size());
        }
        Map<String, java.lang.Object> parameters = new HashMap<>();
        for (int i = 0; i < parameterCount; ++i) {
            parameters.put(signatureParameters.get(i).getName().get(), evaluate(arguments.get(i), memory).object());
        }
        Collection<String> locals = new ArrayList<>(localNames(name));
        if (!methodProperties.isProcedure(name)) {
            locals.add(name);
        }
        StateMachine stateMachine = new StateMachine(methodBody, memory, parameters, locals);
        stateMachine.start();
        for (int i = 0; i < parameterCount; ++i) {
            if (signatureParameters.get(i).getDirection() == Parameter.Direction.INOUT) {
                memory.store(identifier(arguments.get(i)), stateMachine.getMemoryObject(signatureParameters.get(i).getName().get()));
            }
        }
        return (methodProperties.isProcedure(name))
            ? Value.VOID
            : new Value(stateMachine.getMemoryObject(name), methodProperties.getType(name));
    }

    private List<Node> createArgumentList(Optional<Node> argumentList) {
        if (argumentList.isEmpty()) {
            return Collections.emptyList();
        }
        List<Node> arguments = new ArrayList<>();
        createArgumentList(argumentList.get(), arguments);
        return arguments;
    }

    private void createArgumentList(Node argumentList, List<Node> arguments) {
        arguments.add(argumentList.getChild("Expression"));
        Optional<Node> remainder = argumentList.findChild("ArgumentList");
        if (remainder.isPresent()) {
            createArgumentList(remainder.get(), arguments);
        }
    }

    private Value evaluateIdentifier(Node node, Memory memory) throws StateMachineException {
        String name = node.content();
        Collection<Transition<Event, GuardCondition, Action>> method = methodProperties.getBody(name);
        return (method != null)
            ? evaluate(name, method, memory)
            : evaluate(node, memory);
    }

    private Value evaluate(String name, Collection<Transition<Event, GuardCondition, Action>> method, Memory memory) throws StateMachineException {
        Collection<String> identifiers = new ArrayList<>(localNames(name));
        identifiers.add(name);
        StateMachine stateMachine = new StateMachine(method, memory, identifiers);
        stateMachine.start();
        return new Value(stateMachine.getMemoryObject(name), methodProperties.getType(name));
    }

    private Value evaluateUnaryOperation(Node operator, Value operand) throws StateMachineException {
        Node head = operator.getChildren().getFirst();
        if ("\\-".equals(head.getSymbol())) {
            if (PascalCompiler.INTEGER.equals(operand.type())) {
                return new Value(-requireInteger(operand.object()), operand.type());
            }
            if (PascalCompiler.REAL.equals(operand.type())) {
                return new Value(-requireReal(operand.object()), operand.type());
            }
            throw new StateMachineException("Integer or Real expected: " + operand.object());
        }
        if ("NOT\\b".equals(head.getSymbol())) {
            return new Value(!requireBoolean(operand.object()), PascalCompiler.BOOLEAN);
        }
        throw new StateMachineException("Unsupported unnary operator: " + operator.content());
    }

    private Value evaluateFactor(Node node, Memory memory) throws StateMachineException {
        Node head = node.getChildren().getFirst();
        return switch (head.getSymbol()) {
            case "Call" ->
                evaluateCall(head, memory);
            case "Identifier" ->
                evaluateIdentifierExpression(node, memory);
            case "UnaryOperator" ->
                evaluateUnaryOperation(head, evaluateFactor(node.getChild("Factor"), memory));
            case "\\(" ->
                evaluateExpression(node.getChild("Expression"), memory);
            case "Literal" ->
                evaluateLiteral(node.getChildren().getFirst());
            default ->
                throw new StateMachineException("Cannot evaluate factor " + node);
        };
    }

    private Value evaluateIdentifierExpression(Node expression, Memory memory) throws StateMachineException {
        Value value = evaluateIdentifier(expression.getChildren().getFirst(), memory);
        for (Node indirection : getIndirections(expression)) {
            Type type = methodProperties.determineTypeOf(expression);
            switch (indirection.getSymbol()) {
                case "Identifier":
                    value = new Value(((Map<String, java.lang.Object>) value.object()).get(indirection.content().toLowerCase()), type);
                    break;
                case "Expression":
                    int index = (int) evaluateExpression(indirection, memory).object();
                    value = new Value(((java.lang.Object[]) value.object())[index], type);
                    break;
                default:
                    throw new IllegalStateException("Invalid Indirection");
            }
        }
        return value;
    }

    private static List<Node> getIndirections(Node expression) {
        List<Node> indirections = new ArrayList<>();
        Optional<Node> next = expression.findChild("Indirection");
        while (next.isPresent()) {
            Node indirection = next.get();
            if (indirection.getChildren().isEmpty()) {
                next = Optional.empty();
            }
            else {
                if (indirection.startsWith("\\.")) {
                    indirections.add(indirection.getChild("Identifier"));
                }
                else if (indirection.startsWith("\\[")) {
                    indirections.add(indirection.getChild("Expression"));
                }
                else {
                    throw new IllegalStateException("Invalid indirection");
                }
                next = indirection.findChild("Indirection");
            }
        }
        return indirections;
    }

    private Value evaluateLiteral(Node node) throws StateMachineException {
        Node head = node.getChildren().getFirst();
        return switch (head.getSymbol()) {
            case "\\'" ->
                new Value(node.getChild("[^']*").content(), PascalCompiler.STRING);
            case "RealLiteral" ->
                parseReal(head.content());
            case "IntegerLiteral" ->
                parseInteger(head.getChildren());
            case "FALSE\\b" ->
                new Value(Boolean.FALSE, PascalCompiler.BOOLEAN);
            case "TRUE\\b" ->
                new Value(Boolean.TRUE, PascalCompiler.BOOLEAN);
            default ->
                throw new StateMachineException("Cannot evaluate literal " + node);
        };
    }

    private static Value parseReal(String string) throws StateMachineException {
        try {
            return new Value(Float.valueOf(string), PascalCompiler.REAL);
        }
        catch (NumberFormatException ex) {
            throw new StateMachineException("Invalid real: " + string, ex);
        }
    }

    private static Value parseInteger(List<Node> nodes) throws StateMachineException {
        return switch (nodes.getFirst().getSymbol()) {
            case "\\d+" ->
                parseInteger(nodes.getFirst().content(), 10);
            case "\\$[0-9A-F]+" ->
                parseInteger(nodes.getFirst().content().substring(1), 0x10);
            default ->
                throw new StateMachineException("Unsupported integer symbol: " + nodes.getFirst().getSymbol());
        };
    }

    private static Value parseInteger(String string, int radix) throws StateMachineException {
        try {
            return new Value(Integer.valueOf(string, radix), PascalCompiler.INTEGER);
        }
        catch (NumberFormatException ex) {
            throw new StateMachineException("Invalid integer: " + string, ex);
        }
    }

    private int intValue(Node indexExpression, Memory memory) throws StateMachineException {
        return requireInteger(evaluate(indexExpression, memory).object());
    }

    private static Comparable requireComparable(java.lang.Object object) throws StateMachineException {
        if (object instanceof Comparable comparable) {
            return comparable;
        }
        throw new StateMachineException("Not a comparable" + object);
    }

    private static Number requireNumber(java.lang.Object object) throws StateMachineException {
        if (object instanceof Number number) {
            return number;
        }
        throw new StateMachineException("Not a number: " + object);
    }

    private static Float requireReal(java.lang.Object object) throws StateMachineException {
        if (object instanceof Float real) {
            return real;
        }
        throw new StateMachineException("Not a real: " + object);
    }

    private static Integer requireInteger(java.lang.Object object) throws StateMachineException {
        if (object instanceof Integer integer) {
            return integer;
        }
        throw new StateMachineException("Not an integer: " + object);
    }

    private static Boolean requireBoolean(java.lang.Object object) throws StateMachineException {
        if (object instanceof Boolean bool) {
            return bool;
        }
        throw new StateMachineException("Not a boolean: " + object);
    }

    private void store(Memory memory, java.lang.Object value) throws StateMachineException {
        List<Node> indirections = getIndirections(assignable.get());
        if (indirections.isEmpty()) {
            storeDirect(memory, value);
        }
        else {
            storeIndirect(indirections, memory, value);
        }
    }

    private void storeDirect(Memory memory, java.lang.Object value) throws StateMachineException {
        Node target = assignable.get();
        Optional<Node> identifier = target.findChild("Identifier");
        if (identifier.isPresent()) {
            memory.store(identifier(target), value);
        }
        else {
            memory.store(target.content(), value);
        }
    }

    private void storeIndirect(List<Node> indirections, Memory memory, java.lang.Object value) throws StateMachineException {
        java.lang.Object targetValue = indirectTargetValue(indirections, memory);
        Node indirection = indirections.get(indirections.size() - 1);
        switch (indirection.getSymbol()) {
            case "Identifier" ->
                ((Map<String, java.lang.Object>) targetValue).put(indirection.content().toLowerCase(), value);
            case "Expression" ->
                ((java.lang.Object[]) targetValue)[intValue(indirection, memory)] = value;
            default ->
                throw new StateMachineException("Unsupported indirection" + assignable.get().content());
        }
    }

    private java.lang.Object indirectTargetValue(List<Node> indirections, Memory memory) throws StateMachineException {
        java.lang.Object targetValue = memory.load(identifier(assignable.get()));
        int lastIndex = indirections.size() - 1;
        for (int i = 0; i < lastIndex; ++i) {
            Node indirection = indirections.get(i);
            targetValue = switch (indirection.getSymbol()) {
                case "Identifier" ->
                    ((Map<String, java.lang.Object>) targetValue).get(indirection.content().toLowerCase());
                case "Expression" ->
                    ((java.lang.Object[]) targetValue)[intValue(indirection, memory)];
                default ->
                    throw new StateMachineException("Unsupported indirection" + assignable.get().content());
            };
        }
        return targetValue;
    }

    private static String identifier(Node node) throws StateMachineException {
        return switch (node.getSymbol()) {
            case "Identifier" ->
                node.content().toLowerCase();
            case "Expression" ->
                expressionIdentifier(node);
            case "Comparable" ->
                identifier(node.getChild("Term"));
            case "Term" ->
                identifier(node.getChild("Factor"));
            case "Factor" ->
                factorIdentifier(node);
            case "ParameterExpression", "Assignable" ->
                identifier(node.getChild("Identifier"));
            default ->
                throw new StateMachineException("Not an identifier: " + node);
        };
    }

    private static String expressionIdentifier(Node node) throws StateMachineException {
        Optional<Node> comparableNode = node.findChild("Comparable");
        if (comparableNode.isEmpty() || node.getChildren().size() != 1) {
            throw new StateMachineException("Expression is not an identifier: " + node);
        }
        return identifier(comparableNode.get());
    }

    private static String factorIdentifier(Node node) throws StateMachineException {
        Optional<Node> identifierNode = node.findChild("Identifier");
        if (identifierNode.isEmpty() || node.getChildren().size() != 2) {
            throw new StateMachineException("Factor is not an identifier");
        }
        return identifier(identifierNode.get());

    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder().append('(').append(expression.getSymbol()).append(") ");
        assignable.ifPresent(node -> builder.append(node.content()).append(" \u21D0 "));
        builder.append(expression.content());
        return builder.toString();
    }

    private static Logger getLogger() {
        return Logger.getLogger(Statement.class.getName());
    }

    private interface OperandEvaluator {
        Value evaluate(Node nodes, Memory memory) throws StateMachineException;
    }


    private interface DyadicOperation {
        java.lang.Object compute(java.lang.Object left, String operator, java.lang.Object right) throws StateMachineException;
    }

    private record Value(java.lang.Object object, Type type) {

        private static Value VOID = new Value(null, UmlTypeFactory.create("Void"));

    }

    private final Optional<Node> assignable;
    private final Node expression;
    private final PascalCompiler.MethodProperties methodProperties;

}
