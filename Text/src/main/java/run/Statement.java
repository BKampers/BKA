/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans, humanity, the environment or the universe.
*/
package run;

import bka.text.parser.*;
import bka.text.parser.pascal.*;
import java.util.*;
import java.util.function.*;
import java.util.logging.*;
import java.util.stream.*;
import uml.statechart.*;
import uml.structure.Type;

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
                return memory -> evaluate(operand, memory).value;
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
                    Result left = evaluate(leftOperand, memory);
                    Result right = evaluate(rightOperand, memory);
                    return predicate.test(requireComparable(left.value).compareTo(requireComparable(right.value)));
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
        return UmlStateFactory.createActionState(Action.of(statement));
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
        Result result = evaluate(expression, memory);
        if (assignable.isPresent()) {
            if (result.type.equals("Void")) {
                throw new IllegalStateException(assignable.get() + " cannot be assigned with void");
            }
            store(memory, result.value);
        }
        else if (!result.type.equals("Void")) {
            getLogger().log(Level.WARNING, "Evaluation ({1}) of '{0}' is ignored", new Object[]{expression, result.value});
        }
    }

    private Result evaluate(Node node, Memory memory) throws StateMachineException {
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
            case "Identifier" ->
                new Result(memory.load(node.content()), "var");
            case "Call" ->
                evaluateCall(node, memory);
            default ->
                throw new StateMachineException("Cannot evaluate " + node);
        };
    }

    private Result evaluateStatement(Node node, Memory memory) throws StateMachineException {
        return switch (node.getChildren().getFirst().getSymbol()) {
            case "Identifier" ->
                evaluateIdentifier(node.getChildren().getFirst(), memory);
            default ->
                throw new StateMachineException("Cannot evaluate statement: " + node);
        };
    }

    private Result evaluateExpression(Node node, Memory memory) throws StateMachineException {
        return switch (node.getChildren().size()) {
            case 1 ->
                evaluateComparable(node.getChildren().getFirst(), memory);
            case 3 ->
                evaluateRelationalOperation(node, memory);
            default ->
                throw new StateMachineException("Cannot evaluate expression " + node);
        };
    }

    private Result evaluateRelationalOperation(Node node, Memory memory) throws StateMachineException {
        Comparable left = requireComparable(evaluate(node.getChildren().getFirst(), memory).value);
        Comparable right = requireComparable(evaluate(node.getChildren().getLast(), memory).value);
        int comparison = left.compareTo(right);
        Node operator = node.getChild("RelationalOperator").getChildren().getFirst();
        return switch (operator.getSymbol()) {
            case "\\=" ->
                new Result(comparison == 0, "Boolean");
            case "\\<\\>" ->
                new Result(comparison != 0, "Boolean");
            case "\\<\\=" ->
                new Result(comparison <= 0, "Boolean");
            case "\\<" ->
                new Result(comparison < 0, "Boolean");
            case "\\>\\=" ->
                new Result(comparison >= 0, "Boolean");
            case "\\>" ->
                new Result(comparison > 0, "Boolean");
            default ->
                throw new StateMachineException("Unsupported relational operator" + operator.content());
        };
    }

    private Result evaluateComparable(Node node, Memory memory) throws StateMachineException {
        return evaluateOperation(
            evaluateTerm(node.getChild("Term"), memory),
            node.getChild("AdditiveOperation"),
            this::evaluateTerm,
            Statement::additiveOperation,
            memory);
    }


    private static Object additiveOperation(Object left, String operator, Object right) throws StateMachineException {
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

    private Result evaluateTerm(Node node, Memory memory) throws StateMachineException {
        return evaluateOperation(
            evaluateFactor(node.getChild("Factor"), memory),
            node.getChild("MultiplicativeOperation"),
            this::evaluateFactor,
            Statement::multiplicativeOperation,
            memory);
    }

    private Result evaluateOperation(Result left, Node node, OperandEvaluator operandEvaluator, DyadicOperation operation, Memory memory) throws StateMachineException {
        List<Node> nodes = node.getChildren();
        if (nodes.isEmpty()) {
            return left;
        }
        Object result = operation.compute(
            left.value,
            nodes.getFirst().getChildren().getFirst().getSymbol(),
            operandEvaluator.evaluate(nodes.get(1), memory).value);
        return evaluateOperation(
            new Result(result, (result instanceof Boolean) ? "Boolean" : (result instanceof Integer) ? "Integer" : "Real"),
            nodes.getLast(),
            operandEvaluator,
            operation,
            memory);
    }

    private static Object multiplicativeOperation(Object left, String operator, Object right) throws StateMachineException {
        return switch (operator) {
            case "\\*" ->
                product(requireNumber(left), requireNumber(right));
            case "\\/" ->
                requireNumber(left).doubleValue() / requireNumber(right).doubleValue();
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
        return left.doubleValue() * right.doubleValue();
    }

    private static Number sum(Number left, Number right) {
        if (left instanceof Integer && right instanceof Integer) {
            return left.intValue() + right.intValue();
        }
        return left.doubleValue() + right.doubleValue();
    }

    private static Number difference(Number left, Number right) {
        if (left instanceof Integer && right instanceof Integer) {
            return left.intValue() - right.intValue();
        }
        return left.doubleValue() - right.doubleValue();
    }

    private static Object and(Object left, Object right) throws StateMachineException {
        if (left instanceof Boolean leftBoolean) {
            return leftBoolean & requireBoolean(right);
        }
        return requireInteger(left) & requireInteger(right);
    }

    private static Object or(Object left, Object right) throws StateMachineException {
        if (left instanceof Boolean leftBoolean) {
            return leftBoolean | requireBoolean(right);
        }
        return requireInteger(left) | requireInteger(right);
    }

    private static Object xor(Object left, Object right) throws StateMachineException {
        if (left instanceof Boolean leftBoolean) {
            return leftBoolean ^ requireBoolean(right);
        }
        return requireInteger(left) ^ requireInteger(right);
    }

    private Result evaluateCall(Node node, Memory memory) throws StateMachineException {
        String name = node.getChild("Identifier").content();
        Collection<Transition<Event, GuardCondition, Action>> methodBody = methodProperties.getBody(name);
        if (methodBody == null) {
            throw new StateMachineException("No such procedure or function: '" + name + '\'');
        }
        List<Node> arguments = createArgumentList(node.findChild("ArgumentList"));
        List<Node> signatureParameters = methodProperties.getParameters(name);
        int parameterCount = signatureParameters.size();
        if (arguments.size() != parameterCount) {
            throw new StateMachineException("Invalid numer of parameters for: '" + name + "\'. required: " + parameterCount + ", actual: " + arguments.size());
        }
        Map<String, Object> parameters = new HashMap<>();
        for (int i = 0; i < parameterCount; ++i) {
            parameters.put(identifier(signatureParameters.get(i)), evaluate(arguments.get(i), memory).value);
        }
        boolean isProcedure = "Void".equals(methodProperties.getType(name));
        Collection<String> locals = new ArrayList<>(localNames(name));
        if (!isProcedure) {
            locals.add(name);
        }
        StateMachine stateMachine = new StateMachine(methodBody, memory, parameters, locals);
        stateMachine.start();
        for (int i = 0; i < parameterCount; ++i) {
            if (signatureParameters.get(i).findChild("VAR\\b").isPresent()) {
                memory.store(identifier(arguments.get(i)), stateMachine.getMemoryObject(identifier(signatureParameters.get(i))));
            }
        }
        return (isProcedure)
            ? new Result(null, "Void")
            : new Result(stateMachine.getMemoryObject(name), methodProperties.getType(name));
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

    private Result evaluateIdentifier(Node node, Memory memory) throws StateMachineException {
        String name = node.content();
        Collection<Transition<Event, GuardCondition, Action>> method = methodProperties.getBody(name);
        return (method != null)
            ? evaluate(name, method, memory)
            : evaluate(node, memory);
    }

    private Result evaluate(String name, Collection<Transition<Event, GuardCondition, Action>> method, Memory memory) throws StateMachineException {
        Collection<String> identifiers = new ArrayList<>(localNames(name));
        identifiers.add(name);
        StateMachine stateMachine = new StateMachine(method, memory, identifiers);
        stateMachine.start();
        return new Result(stateMachine.getMemoryObject(name), methodProperties.getType(name));
    }

    private Result evaluateUnaryOperation(Node operator, Result operand) throws StateMachineException {
        Node head = operator.getChildren().getFirst();
        if ("\\-".equals(head.getSymbol())) {
            if ("Integer".equals(operand.type())) {
                return new Result(-requireInteger(operand.value()), operand.type());
            }
            if ("Real".equals(operand.type())) {
                return new Result(-requireReal(operand.value()), operand.type());
            }
            throw new StateMachineException("Integer or Real expected: " + operand.value());
        }
        if ("NOT\\b".equals(head.getSymbol())) {
            return new Result(!requireBoolean(operand.value()), "Boolean");
        }
        throw new StateMachineException("Unsupported unnary operator: " + operator.content());
    }

    private Result evaluateFactor(Node node, Memory memory) throws StateMachineException {
        Node head = node.getChildren().getFirst();
        return switch (head.getSymbol()) {
            case "Call" ->
                evaluateCall(head, memory);
            case "Identifier" ->
                evaluateIdentifier(head, memory);
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

    private Result evaluateLiteral(Node node) throws StateMachineException {
        Node head = node.getChildren().getFirst();
        return switch (head.getSymbol()) {
            case "\\'" ->
                new Result(node.getChild("[^']*").content(), "String");
            case "RealLiteral" ->
                parseReal(head.content());
            case "IntegerLiteral" ->
                parseInteger(head.getChildren());
            case "FALSE\\b" ->
                new Result(Boolean.FALSE, "Boolean");
            case "TRUE\\b" ->
                new Result(Boolean.TRUE, "Boolean");
            default ->
                throw new StateMachineException("Cannot evaluate literal " + node);
        };
    }

    private static Result parseReal(String string) throws StateMachineException {
        try {
            return new Result(Double.valueOf(string), "Real");
        }
        catch (NumberFormatException ex) {
            throw new StateMachineException("Invalid real: " + string, ex);
        }
    }

    private static Result parseInteger(List<Node> nodes) throws StateMachineException {
        return switch (nodes.getFirst().getSymbol()) {
            case "\\d+" ->
                parseInteger(nodes.getFirst().content(), 10);
            case "\\$[0-9A-F]+" ->
                parseInteger(nodes.getFirst().content().substring(1), 0x10);
            default ->
                throw new StateMachineException("Unsupported integer symbol: " + nodes.getFirst().getSymbol());
        };
    }

    private static Result parseInteger(String string, int radix) throws StateMachineException {
        try {
            return new Result(Integer.valueOf(string, radix), "Integer");
        }
        catch (NumberFormatException ex) {
            throw new StateMachineException("Invalid integer: " + string, ex);
        }
    }

    private int intValue(Node indexExpression, Memory memory) throws StateMachineException {
        return requireInteger(evaluate(indexExpression, memory).value);
    }

    private static Comparable requireComparable(Object object) throws StateMachineException {
        if (object instanceof Comparable comparable) {
            return comparable;
        }
        throw new StateMachineException("Not a comparable" + object);
    }

    private static Number requireNumber(Object object) throws StateMachineException {
        if (object instanceof Number number) {
            return number;
        }
        throw new StateMachineException("Not a number: " + object);
    }

    private static Double requireReal(Object object) throws StateMachineException {
        if (object instanceof Double real) {
            return real;
        }
        throw new StateMachineException("Not a real: " + object);
    }

    private static Integer requireInteger(Object object) throws StateMachineException {
        if (object instanceof Integer integer) {
            return integer;
        }
        throw new StateMachineException("Not an integer: " + object);
    }

    private static Boolean requireBoolean(Object object) throws StateMachineException {
        if (object instanceof Boolean bool) {
            return bool;
        }
        throw new StateMachineException("Not a boolean: " + object);
    }

    private void store(Memory memory, Object value) throws StateMachineException {
        Node target = assignable.get();
        Optional<Node> indirection = assignable.get().findChild("Indirection");
        if (indirection.isPresent() && !indirection.get().getChildren().isEmpty()) {
            Optional<Node> indexExpression = indirection.get().findChild("Expression");
            if (indexExpression.isPresent()) {
                loadArray(memory, target)[intValue(indexExpression.get(), memory)] = value;
            }
            else {
                if (indirection.get().getChildren().size() >= 2 && "\\.".equals(indirection.get().getChildren().get(0).getSymbol())) {
                    loadRecord(memory, target).put(indirection.get().getChildren().get(1).content(), value);
                }
            }
        }
        else {
            Optional<Node> identifier = target.findChild("Identifier");
            if (identifier.isPresent()) {
                memory.store(identifier.get().content(), value);
            }
            else {
                memory.store(target.content(), value);
            }
        }
    }

    private static Object[] loadArray(Memory memory, Node target) throws StateMachineException {
        return (Object[]) memory.load(identifier(target));
    }

    private static Map<String, Object> loadRecord(Memory memory, Node target) throws StateMachineException {
        return (Map<String, Object>) memory.load(identifier(target));
    }

    private static String identifier(Node node) throws StateMachineException {
        return switch (node.getSymbol()) {
            case "Identifier" ->
                node.content();
            case "Expression" ->
                expressionIdentifier(node);
            case "Comparable" ->
                identifier(node.getChild("Term"));
            case "Term" ->
                identifier(node.getChild("Factor"));
            case "Factor" ->
                factorIdentifier(node);
            case "ParameterExpression", "Assignable" ->
                node.getChild("Identifier").content();
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
        if (assignable.isPresent()) {
            builder.append(assignable.get().content()).append(" \u21D0 ");
        }
        builder.append(expression.content());
        return builder.toString();
    }

    private static Logger getLogger() {
        return Logger.getLogger(Statement.class.getName());
    }

    private record Result(java.lang.Object value, String type) {

    }


    private interface OperandEvaluator {

        Result evaluate(Node nodes, Memory memory) throws StateMachineException;
    }


    private interface DyadicOperation {

        Object compute(Object left, String operator, Object right) throws StateMachineException;
    }

    private final Optional<Node> assignable;
    private final Node expression;
    private final PascalCompiler.MethodProperties methodProperties;

}
