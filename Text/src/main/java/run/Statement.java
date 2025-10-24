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
import java.util.stream.Collectors;
import uml.statechart.*;


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
                case "Call" ->
                    addProcedureCall(diagram);
                default ->
                    throw new IllegalStateException("Unexpected symbol " + expression.getChildren().getFirst().getSymbol());
            }
        }
        else if (!"Statement".equals(expression.getSymbol())) {
            Logger.getLogger(PascalCompiler.class.getName()).log(Level.WARNING, "Empty statenemt: {0}", expression);
        }
    }

    private void addIfThenElseTransitions(ActivityDiagramBuilder diagram) {
        Decision decision = UmlStateFactory.createDecision(createParseTreeExpression());
        diagram.add(leave -> UmlTransitionFactory.createTransition(leave, decision), decision);
        Statement.this.createTransitions(expression.getChild("Statement"), diagram);
        diagram.addGuardCondition(transition -> decision.equals(transition.getSource()), UmlGuardConditionFactory.pass(decision), "then");
        Node elseClause = expression.getChild("ElseClause");
        if (elseClause.getChildren().isEmpty()) {
            diagram.addLeaf(decision);
        }        
        else {
            diagram.fork(decision);
            Statement.this.createTransitions(elseClause.getChild("Statement"), diagram);
            diagram.addStereotype(transition -> decision.equals(transition.getSource()) && transition.getGuardCondition().isEmpty(), "else");
            diagram.join();
        }
    }

    private void addForLoopTransitions(ActivityDiagramBuilder diagram) {
        Node identifier = expression.getChild("Identifier");
        List<Node> expressions = expression.findChildren("Expression");
        ActionState<Action> loopInitialization = createActionState(new Statement(identifier, expressions.getFirst(), methodProperties));
        Decision decision = UmlStateFactory.createDecision(createLessEqualExpression(identifier, expressions.getLast()));
        diagram.add(loopInitialization, decision);
        Statement.this.createTransitions(expression.getChild("Statement"), diagram);
        diagram.add(UmlStateFactory.createActionState(createIncrementAction(identifier)), decision, "for");
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
        Decision decision = UmlStateFactory.createDecision(createParseTreeExpression());
        diagram.add(leave -> UmlTransitionFactory.createTransition(leave, decision), decision);
        createTransitions(expression.getChild("Statement"), diagram);
        diagram.addGuardCondition(transition -> decision.equals(transition.getSource()), UmlGuardConditionFactory.pass(decision), "while");
        diagram.add(decision, UmlStereotypeFactory.createStereotypes("loop"));
    }

    private void addRepeatLoopTransitions(ActivityDiagramBuilder diagram) {
        TransitionSource loopRoot = diagram.anyLeaf();
        createTransitions(expression.getChild("Statements"), diagram);
        ParseTreeExpression condition = createParseTreeExpression();
        Decision decision = UmlStateFactory.createDecision(condition);
        diagram.add(leave -> UmlTransitionFactory.createTransition(leave, decision), decision);
        TransitionTarget loopStart = diagram.targetOf(loopRoot);
        diagram.addTransition(decision, loopStart, UmlGuardConditionFactory.fail(decision), "repeat");
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

    private String typeOf(List<Node> expression) {
        if (expression.size() == 1) {
            return "*";
        }
        if (RELATIONAL_OPERATORS.contains(expression.get(1).content())) {
            return "Boolean";
        }
        return generalType(getExpressionTree(expression.getFirst()).type(), getExpressionTree(expression.getLast()).type());
    }

    private static String generalType(String type1, String type2) {
        if (type1.equals(type2)) {
            return type1;
        }
        if (type1.equals("*")) {
            return type2;
        }
        if (type2.equals("*")) {
            return type1;
        }
        if (type1.equals("Integer") && type2.equals("Real") || type1.equals("Real") && type2.equals("Integer")) {
            return "Real";
        }
        throw new IllegalStateException("Cannot determine general type");
    }

    public Optional<ParseTreeExpression> getExpressionTree() {
        if ("Expression".equals(expression.getSymbol())) {
            return Optional.of(getExpressionTree(expression));
        }
        if ("Call".equals(expression.getSymbol())) {
            return Optional.of(getExpressionTree(expression));
        }
        throw new IllegalStateException("Not an expression: " + expression);
    }

    private ParseTreeExpression getExpressionTree(Node expression) {
        if ("Expression".equals(expression.getSymbol())) {
            return createParseTreeExpression(expression);
        }
        if ("Call".equals(expression.getSymbol())) {
            return createParseTreeExpression(expression);
        }
        if ("Identifier".equals(expression.getSymbol())) {
            return createParseTreeExpression(expression);
        }
        throw new IllegalStateException("Not an expression: " + expression);
    }

    private ParseTreeExpression createLessEqualExpression(Node leftOperand, Node rightOperand) {
        Evaluator evaluator = memory -> {
            Value left = createParseTreeExpression(leftOperand).evaluate(memory);
            Value right = createParseTreeExpression(rightOperand).evaluate(memory);
            return ((Comparable) left.get()).compareTo((Comparable) right.get()) <= 0;
        };
        return ParseTreeExpression.of("Boolean", leftOperand.content() + " .LE. " + rightOperand.content(), evaluator);
    }

    private ParseTreeExpression createParseTreeExpression() {
        return createParseTreeExpression(expression.getChild("Expression"));
    }

    private ParseTreeExpression createParseTreeExpression(Node node) {
        if (node.getChildren().size() == 1) {
            return createSimpleParseTreeExpression(node);
        }
        if ("Call".equals(node.getSymbol())) {
            return createCallParseTreeExpression(node);
        }
        if (node.getChildren().size() == 3 && "BinaryOperator".equals(node.getChildren().get(1).getSymbol())) {
            return createOperatorParseTreeExpression(node);
        }
        throw new IllegalStateException("Invalid expression");
    }

    private ParseTreeExpression createSimpleParseTreeExpression(Node node) {
        Node first = node.getChildren().getFirst();
        if ("Literal".equals(first.getSymbol())) {
            return createLiteralParseTreeExpression(first);
        }
        if ("Identifier".equals(first.getSymbol())) {
            String name = first.getChildren().getFirst().content();
            return ParseTreeExpression.of("*", name, identifierEvaluator(name));
        }
        if ("Identifier".equals(node.getSymbol())) {
            String name = first.content();
            return ParseTreeExpression.of("*", name, memory -> memory.load(name));
        }
        if ("Expression".equals(node.getSymbol())) {
            return createParseTreeExpression(node.getChildren().getFirst());
        }
        throw new IllegalStateException("Cannot create parse tree expression");
    }

    private ParseTreeExpression createLiteralParseTreeExpression(Node first) throws IllegalArgumentException {
        String type = switch (first.getChildren().getFirst().getSymbol()) {
            case "IntegerLiteral" ->
                "Integer";
            case "'" ->
                "String";
            default ->
                throw new IllegalArgumentException("Unexpected literal: " + first.getChildren().getFirst().getSymbol());
        };
        String value = first.content();
        Evaluator evaluator = memory -> {
            return switch (first.getChildren().getFirst().getSymbol()) {
                case "IntegerLiteral" ->
                    parseInteger(value);
                case "'" ->
                    value;
                default ->
                    throw new IllegalArgumentException("Cannot evaluate literal: " + first.getChildren().getFirst().getSymbol());
            };
        };
        return ParseTreeExpression.of(type, value, evaluator);
    }

    private ParseTreeExpression createCallParseTreeExpression(Node node) {
        String name = node.getChildren().getFirst().content();
        Evaluator evaluator = memory -> {
            final boolean isProcedure = "Void".equals(methodProperties.getType(name));
            Collection<Transition<Event, GuardCondition, Action>> methodBody = methodProperties.getBody(name);
            if (methodBody == null) {
                throw new IllegalStateException("No such procedure or function: '" + name + '\'');
            }
            List<Node> arguments = createArgumentList(node.getChildren());
            Map<String, Object> parameters = evaluateArguments(arguments, node.getChildren(), name, memory);
            StateMachine stateMachine = (isProcedure)
                ? new StateMachine(methodBody, memory, parameters)
                : new StateMachine(methodBody, memory, parameters, List.of(name));
            stateMachine.start();
            for (int i = 0; i < methodProperties.getParameters(name).size(); ++i) {
                if (methodProperties.getParameters(name).get(i).findChild("VAR\\b").isPresent()) {
                    memory.store(
                        identifier(arguments.get(i)),
                        stateMachine.getMemoryObject(identifier(methodProperties.getParameters(name).get(i))));
                }
            }
            return (isProcedure) ? VOID : stateMachine.getMemoryObject(name);
        };
        return ParseTreeExpression.of(methodProperties.getType(name), name, evaluator);
    }

    private ParseTreeExpression createOperatorParseTreeExpression(Node node) {
        Node first = node.getChildren().getFirst();
        String value
            = createParseTreeExpression(first).value()
            + " (Operator)" + node.getChildren().get(1).content() + " "
            + createParseTreeExpression(node.getChildren().getLast()).value() + ")";
        Evaluator evaluator = memory -> {
            Value v = getDyadicOperator(node.getChildren().get(1)).evaluate(
                createParseTreeExpression(first),
                createParseTreeExpression(node.getChildren().getLast()),
                memory);
            return v.get();
        };
        return ParseTreeExpression.of(typeOf(node.getChildren()), value, evaluator);
    }

    private Evaluator identifierEvaluator(String name) {
        return memory -> {
            Collection<Transition<Event, GuardCondition, Action>> method = methodProperties.getBody(name);
            if (method != null) {
                StateMachine stateMachine = new StateMachine(method, memory, List.of(name));
                stateMachine.start();
                return stateMachine.getMemoryObject(name);
            }
            return memory.load(name);
        };
    }

    private Map<String, Object> evaluateArguments(List<Node> arguments, List<Node> expression, String name, Memory memory) throws StateMachineException {
        List<Node> methodParameters = methodProperties.getParameters(name);
        final int parameterCount = methodParameters.size();
        if (parameterCount != arguments.size()) {
            throw new IllegalStateException("Invalid number of arguments.");
        }
        if (parameterCount == 0) {
            return Collections.emptyMap();
        }
        ActivityDiagramBuilder diagram = new ActivityDiagramBuilder();
        for (int i = 0; i < parameterCount; ++i) {
            diagram.add(UmlStateFactory.createActionState(Action.of(new Statement(methodParameters.get(i), arguments.get(i), methodProperties))));
        }
        diagram.addFinalState();
        List<String> parameterNames = methodParameters.stream().map(Node::content).collect(Collectors.toList());
        StateMachine parameterEvaluator = new StateMachine(diagram.getTransitions(), memory, parameterNames);
        parameterEvaluator.start();
        Map<String, Object> parameters = new HashMap<>();
        for (int i = 0; i < parameterCount; ++i) {
            parameters.put(identifier(methodParameters.get(i)), parameterEvaluator.getMemoryObject(parameterNames.get(i)));
        }
        return parameters;
    }

    private List<Node> createArgumentList(List<Node> expression) {
        if (expression.size() > 2 && "ArgumentList".equals(expression.get(2).getSymbol())) {
            return createArgumentList(expression.get(2));
        }
        return Collections.emptyList();
    }

    private List<Node> createArgumentList(Node argumentList) {
        List<Node> arguments = new ArrayList<>();
        arguments.add(argumentList.getChild("Expression"));
        Optional<Node> remainder = argumentList.findChild("ArgumentList");
        if (remainder.isPresent()) {
            arguments.addAll(createArgumentList(remainder.get()));
        }
        return arguments;
    }

    private static String identifier(Node node) {
        return node.getChild("Identifier").content();
    }

    private static java.lang.Object parseInteger(String literal) {
        return (literal.startsWith("$"))
            ? Integer.parseInt(literal, 1, literal.length(), 0x10)
            : Integer.valueOf(literal);
    }

    private static DyadicOperator getDyadicOperator(Node node) {
        return switch (node.content().toLowerCase()) {
            case "^" ->
                createArithmicOperator((left, right) -> power(left, right));
            case "*" ->
                createArithmicOperator((left, right) -> product(left, right));
            case "/" ->
                createArithmicOperator((left, right) -> quotient(left, right));
            case "div" ->
                createArithmicOperator((left, right) -> division(left, right));
            case "mod" ->
                createArithmicOperator((left, right) -> modulus(left, right));
            case "+" ->
                createArithmicOperator((left, right) -> sum(left, right));
            case "-" ->
                createArithmicOperator((left, right) -> difference(left, right));
            case "=" ->
                createRelationalOperator((left, right) -> left.compareTo(right) == 0);
            case "<>" ->
                createRelationalOperator((left, right) -> left.compareTo(right) != 0);
            case "<" ->
                createRelationalOperator((left, right) -> left.compareTo(right) < 0);
            case "<=" ->
                createRelationalOperator((left, right) -> left.compareTo(right) <= 0);
            case ">" ->
                createRelationalOperator((left, right) -> left.compareTo(right) > 0);
            case ">=" ->
                createRelationalOperator((left, right) -> left.compareTo(right) > 0);
            case "and" ->
                createLogicalOperator((left, right) -> left && right);
            case "or" ->
                createLogicalOperator((left, right) -> left || right);
            case "xor" ->
                createLogicalOperator((left, right) -> !left.equals(right));
            default ->
                throw new IllegalStateException("Unsupported binary operator: '" + node.content() + "'");
        };
    }

    private static Number power(Number left, Number right) {
        if (left instanceof Integer && right instanceof Integer) {
            int result = 1;
            for (int i = 1; i <= right.intValue(); i++) {
                result *= left.intValue();
            }
            return result;
        }
        return Math.pow(left.doubleValue(), right.doubleValue());
    }

    private static Number product(Number left, Number right) {
        if (left instanceof Integer && right instanceof Integer) {
            return left.intValue() * right.intValue();
        }
        return left.doubleValue() * right.doubleValue();
    }

    private static Double quotient(Number left, Number right) {
        return left.doubleValue() / right.doubleValue();
    }

    private static Integer division(Number left, Number right) {
        return left.intValue() / right.intValue();
    }

    private static Integer modulus(Number left, Number right) {
        return left.intValue() % right.intValue();
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

    private static DyadicOperator createArithmicOperator(BiFunction<Number, Number, Number> function) {
        return (ParseTreeExpression leftOperand, ParseTreeExpression rightOperand, Memory memory) -> {
            Number value = function.apply(requireNumber(leftOperand, memory), requireNumber(rightOperand, memory));
            return Value.of(() -> value, (value instanceof Integer) ? "Integer" : "Real");
        };
    }

    private static Number requireNumber(ParseTreeExpression expression, Memory memory) throws StateMachineException {
        if (expression.evaluate(memory).get() instanceof Number number) {
            return number;
        }
        throw new StateMachineException(expression.type() + " is not a number");
    }

    private static DyadicOperator createLogicalOperator(BiFunction<Boolean, Boolean, Boolean> function) {
        return (ParseTreeExpression leftOperand, ParseTreeExpression rightOperand, Memory memory) -> {
            return Value.of(() -> function.apply(requireBoolean(leftOperand, memory), requireBoolean(rightOperand, memory)), "Boolean");
        };
    }

    private static Boolean requireBoolean(ParseTreeExpression expression, Memory memory) throws StateMachineException {
        if (expression.evaluate(memory).get() instanceof Boolean bool) {
            return bool;
        }
        throw new StateMachineException(expression.type() + " is not a boolean");
    }

    private static DyadicOperator createRelationalOperator(BiFunction<Comparable, Comparable, Boolean> function) {
        return (ParseTreeExpression leftOperand, ParseTreeExpression rightOperand, Memory memory) -> {
            return Value.of(() -> function.apply(requireComparable(leftOperand, memory), requireComparable(rightOperand, memory)), "Boolean");
        };
    }

    private static Comparable requireComparable(ParseTreeExpression expression, Memory memory) throws StateMachineException {
        if (expression.evaluate(memory).get() instanceof Comparable comparable) {
            return comparable;
        }
        throw new StateMachineException(expression.type() + " is not a comparable");
    }

    public Optional<Node> getAssignable() {
        return assignable;
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

    private final Optional<Node> assignable;
    private final Node expression;
    private final PascalCompiler.MethodProperties methodProperties;

    private static final List<String> RELATIONAL_OPERATORS = List.of("<", "<=", "=", ">", ">=", "<>");

    private static final ParseTreeExpression VOID = new ParseTreeExpression() {
        @Override
        public String type() {
            return "Void";
        }

        @Override
        public String value() {
            throw new UnsupportedOperationException("Void expressions have no value");
        }

        @Override
        public Value evaluate(Memory memory) throws StateMachineException {
            throw new UnsupportedOperationException("Void expression cannot be evaluated");
        }

    };

}
