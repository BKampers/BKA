/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans, humanity, the environment or the universe.
*/
package run;

import bka.text.parser.Node;
import bka.text.parser.pascal.*;
import java.util.*;
import java.util.function.*;
import java.util.logging.*;
import java.util.stream.*;
import uml.statechart.*;

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
        Decision decision = UmlStateFactory.createDecision(createParseTreeExpression());
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
        Decision loopStartDecision = UmlStateFactory.createDecision(createLessEqualExpression(identifier, expressions.getLast()));
        diagram.add(loopInitialization, loopStartDecision);
        createTransitions(expression.getChild("Statement"), diagram);
        diagram.addGuardCondition(loopStartDecision, UmlGuardConditionFactory.pass(loopStartDecision), "for");
        TransitionTarget loopStart = diagram.targetOf(loopStartDecision);
        Decision loopEndDecision = UmlStateFactory.createDecision(createLessThanExpression(identifier, expressions.getLast()));
        diagram.add(loopEndDecision, UmlStateFactory.createActionState(createIncrementAction(identifier)), loopStart, "for");
        diagram.addLeaf(loopStartDecision);
    }

//    private Decision<Expression> createDecision() {
//        return new Decision<Expression>() {
//            @Override
//            public Expression getExpression() {
//                throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
//            }
//
//            @Override
//            public Optional<Type> getType() {
//                throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
//            }
//
//            @Override
//            public Optional<String> getName() {
//                throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
//            }
//
//        };
//    }

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
        if (expression.size() == 2 && Set.of("Factor").contains(expression.get(0).getSymbol()) && Set.of("AdditiveOperation", "MultiplicativeOperation").contains(expression.get(1).getSymbol())) {
            return "*";
        }
        if (expression.get(0).findChild("RelationalOperator").isPresent()) {
            return "Boolean";
        }
        if (RELATIONAL_OPERATORS.contains(expression.get(1).content())) {
            return "Boolean";
        }
        return "?";//generalType(typeOf(expression.getFirst()), typeOf(expression.getLast()).type());
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

    private ParseTreeExpression createLessEqualExpression(Node leftOperand, Node rightOperand) {
        Evaluator evaluator = memory -> {
            V left = evaluate(leftOperand, memory);
            V right = evaluate(rightOperand, memory);
            return requireComparable(left.value).compareTo(requireComparable(right.value)) <= 0;
        };
        return ParseTreeExpression.of("Boolean", leftOperand.content() + " .LE. " + rightOperand.content(), evaluator);
    }

    private ParseTreeExpression createLessThanExpression(Node leftOperand, Node rightOperand) {
        Evaluator evaluator = memory -> {
            V leftV = evaluate(leftOperand, memory);
            V rightV = evaluate(rightOperand, memory);
            return requireComparable(leftV.value).compareTo(requireComparable(rightV.value)) < 0;
        };
        return ParseTreeExpression.of("Boolean", leftOperand.content() + " .LT. " + rightOperand.content(), evaluator);
    }

    private static Comparable requireComparable(Object object) throws StateMachineException {
        if (object instanceof Comparable comparable) {
            return comparable;
        }
        throw new StateMachineException("Not a comparable" + object);
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
        if ("Expression".equals(node.getSymbol())) {
            return createAdditiveOperationParseTreeExpression(node);
        }
        throw new IllegalStateException("Invalid expression: " + node);
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
            Collection<Transition<Event, GuardCondition, Action>> method = methodProperties.getBody(name);
            if (method != null) {
                Evaluator procedureCall = memory -> {
                    new StateMachine(method, memory, localNames(name)).start();
                    return VOID;
                };
                return ParseTreeExpression.of("*", name, procedureCall);
            }
            return ParseTreeExpression.of("*", name, memory -> memory.load(name));
        }
        if ("Expression".equals(node.getSymbol())) {
            return createParseTreeExpression(node.getChildren().getFirst());
        }
        throw new IllegalStateException("Cannot create parse tree expression: " + node);
    }

    private Collection<String> localNames(String methodName) {
        return methodProperties.getLocals(methodName).stream()
            .map(uml.structure.Object::getName)
            .map(Optional::get)
            .collect(Collectors.toList());
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
            Collection<Transition<Event, GuardCondition, Action>> methodBody = methodProperties.getBody(name);
            if (methodBody == null) {
                throw new IllegalStateException("No such procedure or function: '" + name + '\'');
            }
            final boolean isProcedure = "Void".equals(methodProperties.getType(name));
            List<Node> arguments = createArgumentList(node.getChildren());
            Map<String, Object> parameters = evaluateArguments(arguments, name, memory);
            Collection<String> identifiers = new ArrayList<>(localNames(name));
            if (!isProcedure) {
                identifiers.add(name);
            }
            StateMachine stateMachine = new StateMachine(methodBody, memory, parameters, identifiers);
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

    private ParseTreeExpression createAdditiveOperationParseTreeExpression(Node node) {
        if (node.getChildren().isEmpty()) {
            return ParseTreeExpression.of(EMPTY_OPERATION_RESULT, EMPTY_ADDITIVE_OPERATION.toString(), memory -> EMPTY_ADDITIVE_OPERATION);
        }
        ParseTreeExpression left = createTermParseTreeExpression(node.getChild("Term"));
        ParseTreeExpression right = createAdditiveOperationParseTreeExpression(node.getChild("AdditiveOperation"));
        Optional<Node> operator = node.getChild("AdditiveOperation").findChild("AdditiveOperator");
        String operatorSymbol = (operator.isEmpty()) ? "+" : operator.get().content();
        return ParseTreeExpression.of(
            generalType(left.type(), right.type()),
            left.toString() + operatorSymbol + right.toString(),
            memory -> getDyadicOperator(operatorSymbol).evaluate(left, right, memory).evaluate());
    }

    private ParseTreeExpression createTermParseTreeExpression(Node node) {
        ParseTreeExpression left = createFactorParseTreeExpression(node.getChild("Factor"));
        ParseTreeExpression right = createMultiplicativeOperationParseTreeExpression(node.getChild("MultiplicativeOperation"));
        return ParseTreeExpression.of(
            typeOf(node.getChildren()),
            left.toString() + "+" + right.toString(),
            memory -> getDyadicOperator("+").evaluate(left, right, memory).evaluate());
    }

    private ParseTreeExpression createMultiplicativeOperationParseTreeExpression(Node node) {
        if (node.getChildren().isEmpty()) {
            return ParseTreeExpression.of(EMPTY_OPERATION_RESULT, EMPTY_MULTIPLICATIVE_OPERATION.toString(), memory -> EMPTY_MULTIPLICATIVE_OPERATION);
        }
        ParseTreeExpression left = createFactorParseTreeExpression(node.getChild("Factor"));
        ParseTreeExpression right = createMultiplicativeOperationParseTreeExpression(node.getChild("MultiplicativeOperation"));
        Node operator = node.getChild("MultiplicativeOperator");
        return ParseTreeExpression.of(
            typeOf(node.getChildren()),
            operatorExpressionString(left, operator, right),
            memory -> getDyadicOperator(operator).evaluate(left, right, memory).evaluate());
    }

    private ParseTreeExpression createFactorParseTreeExpression(Node node) {
        if (node.getChildren().size() == 1) {
            return createComparableParseTreeExpression(node.getChild("Comparable"));
        }
        ParseTreeExpression left = createComparableParseTreeExpression(node.getChildren().getFirst());
        ParseTreeExpression right = createComparableParseTreeExpression(node.getChildren().getLast());
        Node operator = node.getChild("RelationalOperator");
        return ParseTreeExpression.of(
            typeOf(node.getChildren()),
            operatorExpressionString(left, operator, right),
            memory -> getDyadicOperator(operator).evaluate(left, right, memory).evaluate());
    }

    private ParseTreeExpression createComparableParseTreeExpression(Node node) {
        if ("Comparable".equals(node.getSymbol())) {
            return createSimpleParseTreeExpression(node);
        }
        return createSimpleParseTreeExpression(node.getChildren().getFirst());
    }

//    private ParseTreeExpression createOperatorParseTreeExpression(Node node) {
//        ParseTreeExpression left = createParseTreeExpression(node.getChildren().getFirst());
//        Node operator = node.getChildren().get(1);
//        ParseTreeExpression right = createParseTreeExpression(node.getChildren().getLast());
//        return ParseTreeExpression.of(
//            typeOf(node.getChildren()),
//            operatorExpressionString(left, operator, right),
//            memory -> getDyadicOperator(operator).evaluate(left, right, memory).evaluate());
//    }

    private static String operatorExpressionString(ParseTreeExpression left, Node operator, ParseTreeExpression right) {
        return String.format("%s (Operator)%s %s", left.value(), operator.content(), right.value());
    }

    private Evaluator identifierEvaluator(String name) {
        return memory -> {
            Collection<Transition<Event, GuardCondition, Action>> method = methodProperties.getBody(name);
            if (method != null) {
                Collection<String> identifiers = new ArrayList<>(localNames(name));
                identifiers.add(name);
                StateMachine stateMachine = new StateMachine(method, memory, identifiers);
                stateMachine.start();
                return stateMachine.getMemoryObject(name);
            }
            return memory.load(name);
        };
    }

    private Map<String, Object> evaluateArguments(List<Node> arguments, String name, Memory memory) throws StateMachineException {
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

    private static String identifier(Node node) throws StateMachineException {
        return switch (node.getSymbol()) {
            case "Identifier" ->
                node.content();
            case "Expression" ->
                identifier(node.getChild("Term"));
            case "Term" ->
                identifier(node.getChild("Factor"));
            case "Factor" ->
                factorIdentifier(node);
            case "Comparable" ->
                comparableIdentifier(node);
            case "ParameterExpression", "Assignable" ->
                node.getChild("Identifier").content();
            default ->
                throw new StateMachineException("Not an identifier: " + node);
        };
    }

    private static String factorIdentifier(Node node) throws StateMachineException {
        if (node.getChildren().size() == 1) {
            return identifier(node.getChild("Comparable"));
        }
        throw new StateMachineException("Factor is not an identifier");
    }

    private static String comparableIdentifier(Node node) throws StateMachineException {
        if (node.getChildren().size() == 1 && node.startsWith("Identifier")) {
            return node.content();
        }
        throw new StateMachineException("Comparable is not an identifier");
    }

    private static java.lang.Object parseInteger(String literal) {
        return (literal.startsWith("$"))
            ? Integer.parseInt(literal, 1, literal.length(), 0x10)
            : Integer.valueOf(literal);
    }

    private static DyadicOperator getDyadicOperator(Node node) {
        return getDyadicOperator(node.content().toLowerCase());
    }

    private static DyadicOperator getDyadicOperator(String symbol) {
        return switch (symbol) {
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
                createRelationalOperator((left, right) -> left.compareTo(right) >= 0);
            case "and" ->
                createLogicalOperator((left, right) -> left & right);
            case "or" ->
                createLogicalOperator((left, right) -> left | right);
            case "xor" ->
                createLogicalOperator((left, right) -> left ^ right);
            default ->
                throw new IllegalStateException("Unsupported dyadic operator: '" + symbol + "'");
        };
    }

    private static Number product(Number left, Number right) {
        if (left instanceof Integer && right instanceof Integer) {
            return left.intValue() * right.intValue();
        }
        return left.doubleValue() * right.doubleValue();
    }

    private static Number quotient(Number left, Number right) {
        return left.doubleValue() / right.doubleValue();
    }

    private static Number division(Number left, Number right) {
        return left.intValue() / right.intValue();
    }

    private static Number modulus(Number left, Number right) {
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
            if (EMPTY_OPERATION_RESULT.equals(rightOperand.type())) {
                Object value = leftOperand.evaluate(memory).evaluate();
                return Value.of(() -> value, (value instanceof Boolean) ? "Boolean" : (value instanceof Integer) ? "Integer" : "Real");
            }
            Number value = function.apply(requireNumber(leftOperand, memory), requireNumber(rightOperand, memory));
            return Value.of(() -> value, (value instanceof Integer) ? "Integer" : "Real");
        };
    }

    private static Number requireNumber(ParseTreeExpression expression, Memory memory) throws StateMachineException {
        if (expression.evaluate(memory).evaluate() instanceof Number number) {
            return number;
        }
        throw new StateMachineException(expression.type() + " is not a number: " + expression.evaluate(memory).evaluate());
    }

    private static DyadicOperator createLogicalOperator(BiFunction<Boolean, Boolean, Boolean> function) {
        return (ParseTreeExpression leftOperand, ParseTreeExpression rightOperand, Memory memory) -> {
            return Value.of(() -> function.apply(requireBoolean(leftOperand, memory), requireBoolean(rightOperand, memory)), "Boolean");
        };
    }

    private static Boolean requireBoolean(ParseTreeExpression expression, Memory memory) throws StateMachineException {
        if (expression.evaluate(memory).evaluate() instanceof Boolean bool) {
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
        if (expression.evaluate(memory).evaluate() instanceof Comparable comparable) {
            return comparable;
        }
        throw new StateMachineException(expression.type() + " is not a comparable");
    }

    public Optional<Node> getAssignable() {
        return assignable;
    }

    public void execute(Memory memory) throws StateMachineException {
        V v = evaluate(expression, memory);
        if (assignable.isPresent()) {
            if (v.type.equals("Void")) {
                throw new IllegalStateException(assignable.get() + " cannot be assigned with void");
            }
            store(memory, v.value);
        }
        else if (!v.type.equals("Void")) {
            getLogger().log(Level.WARNING, "Evaluation ({1}) of '{0}' is ignored", new Object[]{expression, v.value});
        }
    }

//    private Optional<ParseTreeExpression> getExpressionTree() {
//        if ("Expression".equals(expression.getSymbol())) {
//            return Optional.of(getExpressionTree(expression));
//        }
//        if ("Call".equals(expression.getSymbol())) {
//            return Optional.of(getExpressionTree(expression));
//        }
//        if ("Statement".equals(expression.getSymbol())) {
//            return Optional.of(getExpressionTree(expression.getChild("Identifier")));
//        }
//        throw new IllegalStateException("Not an expression: " + expression);
//    }
//
//    private ParseTreeExpression getExpressionTree(Node expression) {
//        if ("Expression".equals(expression.getSymbol())) {
//            return createParseTreeExpression(expression);
//        }
//        if ("Factor".equals(expression.getSymbol())) {
//            return createFactorParseTreeExpression(expression);
//        }
//        if ("Term".equals(expression.getSymbol())) {
//            return createTermParseTreeExpression(expression);
//        }
//        if ("AdditiveOperation".equals(expression.getSymbol())) {
//            return createAdditiveOperationParseTreeExpression(expression);
//        }
//        if ("Call".equals(expression.getSymbol())) {
//            return createParseTreeExpression(expression);
//        }
//        if ("Identifier".equals(expression.getSymbol())) {
//            return createParseTreeExpression(expression);
//        }
//        throw new IllegalStateException("Not an expression: " + expression);
//    }

    private V evaluate(Node node, Memory memory) throws StateMachineException {
        return switch (node.getSymbol()) {
            case "Statement" ->
                evaluateStatement(node, memory);
            case "Expression" ->
                evaluateExpression(node.getChildren(), memory);
            case "Term" ->
                evaluateTerm(node.getChildren(), memory);
            case "Factor" ->
                evaluateFactor(node.getChildren(), memory);
            case "Comparable" ->
                evaluateComparable(node.getChildren(), memory);
            case "Identifier" ->
                new V(memory.load(node.content()), "var");
            case "Call" ->
                evaluateCall(node, memory);
            default ->
                throw new StateMachineException("Cannot evaluate " + node);
        };
    }

    private V evaluateStatement(Node node, Memory memory) throws StateMachineException {
        return switch (node.getChildren().getFirst().getSymbol()) {
            case "Identifier" ->
                evaluateIdentifier(node.getChildren().getFirst(), memory);
            default ->
                throw new StateMachineException("Cannot evluate statement: " + node);
        };
    }

    private V evaluateExpression(List<Node> nodes, Memory memory) throws StateMachineException {
        return evaluateAdditiveOperation(
            evaluateTerm(nodes.getFirst().getChildren(), memory),
            nodes.getLast().getChildren(),
            memory);
    }

    private V evaluateAdditiveOperation(V left, List<Node> nodes, Memory memory) throws StateMachineException {
        if (nodes.isEmpty()) {
            return left;
        }
        V right = evaluateTerm(nodes.get(1).getChildren(), memory);
        if (left.value instanceof Boolean && right.value instanceof Boolean) {
            if ("OR\\b".equals(nodes.getFirst().getChildren().getFirst().getSymbol())) {
                return new V((Boolean) left.value || (Boolean) right.value, "Boolean");
            }
            if ("XOR\\b".equals(nodes.getFirst().getChildren().getFirst().getSymbol())) {
                return new V((Boolean) left.value ^ (Boolean) right.value, "Boolean");
            }
            throw new StateMachineException("Unsupported boolean multiplicative operator");
        }
        if (!((left.value instanceof Number) && (right.value instanceof Number))) {
            throw new StateMachineException("Multiplicative operation requires numbers");
        }
        if (!((left.value instanceof Number) && (right.value instanceof Number))) {
            throw new StateMachineException("Additive operation requires numbers");
        }
        Number sum = switch (nodes.getFirst().getChildren().getFirst().getSymbol()) {
            case "\\+" ->
                sum((Number) left.value, (Number) right.value);
            case "\\-" ->
                difference((Number) left.value, (Number) right.value);
            default ->
                throw new StateMachineException("Unsupported additive operator");
        };
        return evaluateAdditiveOperation(
            new V(sum, (sum instanceof Integer) ? "Integer" : "Real"),
            nodes.getLast().getChildren(),
            memory);
    }

    private V evaluateTerm(List<Node> nodes, Memory memory) throws StateMachineException {
        return evaluateMultiplicativeOperation(
            evaluateFactor(nodes.getFirst().getChildren(), memory),
            nodes.getLast().getChildren(),
            memory);
    }

    private V evaluateMultiplicativeOperation(V left, List<Node> nodes, Memory memory) throws StateMachineException {
        if (nodes.isEmpty()) {
            return left;
        }
        V right = evaluateFactor(nodes.get(1).getChildren(), memory);
        if (left.value instanceof Boolean && right.value instanceof Boolean) {
            if ("AND\\b".equals(nodes.getFirst().getChildren().getFirst().getSymbol())) {
                return new V((Boolean) left.value && (Boolean) right.value, "Boolean");
            }
            throw new StateMachineException("Unsupported boolean multiplicative operator");
        }
        if (!((left.value instanceof Number) && (right.value instanceof Number))) {
            throw new StateMachineException("Multiplicative operation requires numbers");
        }
        Number product = switch (nodes.getFirst().getChildren().getFirst().getSymbol()) {
            case "\\*" ->
                product(requireNumber(left.value), requireNumber(right.value));
            case "\\/" ->
                requireNumber(left.value).doubleValue() / requireNumber(right).doubleValue();
            case "DIV\\b" ->
                requireInteger(left.value) / requireInteger(right.value);
            case "MOD\\b" ->
                requireInteger(left.value) % requireInteger(right.value);
            case "AND\\b" ->
                requireInteger(left.value) & requireInteger(right.value);
            default ->
                throw new StateMachineException("Unsupported multiplicative operator");
        };
        return evaluateMultiplicativeOperation(
            new V(product, (product instanceof Integer) ? "Integer" : "Real"),
            nodes.getLast().getChildren(),
            memory);
    }

    private static Number requireNumber(Object object) throws StateMachineException {
        if (object instanceof Number number) {
            return number;
        }
        throw new StateMachineException("Not a number: " + object);
    }

    private static Integer requireInteger(Object object) throws StateMachineException {
        if (object instanceof Integer integer) {
            return integer;
        }
        throw new StateMachineException("Not an integer: " + object);
    }

    private V evaluateComparable(List<Node> nodes, Memory memory) throws StateMachineException {
        return switch (nodes.getFirst().getSymbol()) {
            case "Call" ->
                evaluateCall(nodes.getFirst(), memory);
            case "Identifier" ->
                evaluateIdentifier(nodes.getFirst(), memory);
            case "UnaryOperator" ->
                evaluateUnaryOperation(nodes.getFirst(), evaluateComparable(nodes.getLast().getChildren(), memory));
            case "\\(" ->
                evaluateExpression(nodes.get(1).getChildren(), memory);
            // TODO support all rules for "Comparable"
            default ->
                evaluateLiteral(nodes.getFirst());
        };
    }

    private V evaluateCall(Node node, Memory memory) throws StateMachineException {
        String name = node.getChild("Identifier").content();
        Collection<Transition<Event, GuardCondition, Action>> methodBody = methodProperties.getBody(name);
        if (methodBody == null) {
            throw new StateMachineException("No such procedure or function: '" + name + '\'');
        }

        List<Node> arguments = createArgumentList(node.getChildren());
        final List<Node> signatureParameters = methodProperties.getParameters(name);
        final int parameterCount = signatureParameters.size();
        if (arguments.size() != parameterCount) {
            throw new StateMachineException("Invalid numer of parameters for: '" + name + "\'. required: " + parameterCount + ", actual: " + arguments.size());
        }
        Map<String, Object> parameters = new HashMap<>();
        for (int i = 0; i < parameterCount; ++i) {
            parameters.put(identifier(signatureParameters.get(i)), evaluate(arguments.get(i), memory).value);
        }
        Collection<String> locals = new ArrayList<>(localNames(name));
        final boolean isProcedure = "Void".equals(methodProperties.getType(name));
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
            ? new V(null, "Void")
            : new V(stateMachine.getMemoryObject(name), methodProperties.getType(name));
    }

    private V evaluateIdentifier(Node node, Memory memory) throws StateMachineException {
        String name = node.content();
        Collection<Transition<Event, GuardCondition, Action>> method = methodProperties.getBody(name);
        return (method != null)
            ? evaluate(name, method, memory)
            : evaluate(node, memory);
    }

    private V evaluate(String name, Collection<Transition<Event, GuardCondition, Action>> method, Memory memory) throws StateMachineException {
        Collection<String> identifiers = new ArrayList<>(localNames(name));
        identifiers.add(name);
        StateMachine stateMachine = new StateMachine(method, memory, identifiers);
        stateMachine.start();
        return new V(stateMachine.getMemoryObject(name), methodProperties.getType(name));
    }

    private V evaluateUnaryOperation(Node operator, V operand) {
        if ("\\-".equals(operator.getSymbol())) {
            return new V(-(Integer) operand.value, "Integer");
        }
        if ("NOT\\b".equals(operator.getSymbol())) {
            return new V(!(Boolean) operand.value, "Boolean");
        }
        return operand;
    }

    private V evaluateFactor(List<Node> nodes, Memory memory) throws StateMachineException {
        if (nodes.size() == 1) {
            return evaluate(nodes.getFirst(), memory);
        }
        if (nodes.size() == 3) {
            V left = evaluate(nodes.getFirst(), memory);
            V right = evaluate(nodes.getLast(), memory);
            if (!((left.value instanceof Comparable) && (right.value instanceof Comparable))) {
                throw new StateMachineException("Comparables required");
            }
            int compare = ((Comparable) left.value).compareTo(((Comparable) right.value));
            return switch (nodes.get(1).getChildren().getFirst().getSymbol()) {
                case "\\=" ->
                    new V(compare == 0, "Boolean");
                case "\\<\\>" ->
                    new V(compare != 0, "Boolean");
                case "\\<\\=" ->
                    new V(compare <= 0, "Boolean");
                case "\\<" ->
                    new V(compare < 0, "Boolean");
                case "\\>\\=" ->
                    new V(compare >= 0, "Boolean");
                case "\\>" ->
                    new V(compare > 0, "Boolean");
                default ->
                    throw new StateMachineException("Unsupported relational operator" + (nodes.get(1).getChildren().getFirst().getSymbol()));
            };
        }
        throw new StateMachineException("Cannot evaluate Factor " + nodes);
    }

    private V evaluateLiteral(Node node) throws StateMachineException {
        return switch (node.getSymbol()) {
            case "Literal", "IntegerLiteral" ->
                evaluateLiteral(node.getChildren().getFirst());
            case "\\d+" ->
                new V((java.lang.Object) Integer.parseInt(node.content()), "Integer");
            case "\\$[0-9A-F]+" ->
                new V((java.lang.Object) Integer.parseInt(node.content().substring(1), 16), "Integer");
            case "FALSE\\b" ->
                new V(false, "Boolean");
            case "TRUE\\b" ->
                new V(true, "Boolean");
            default ->
                throw new StateMachineException("Cannot evaluate literal" + node);
        };
    }

    private record V(java.lang.Object value, String type) {

    }

    private void store(Memory memory, Object value) throws StateMachineException {
        Node target = assignable.get();
        Optional<Node> indexExpression = target.findChild("Expression");
        if (indexExpression.isPresent()) {
            loadArray(memory, target)[intValue(indexExpression.get(), memory)] = value;
        }
        else {
            memory.store(target.content(), value);
        }
    }

    private static Object[] loadArray(Memory memory, Node target) throws StateMachineException {
        return (Object[]) memory.load(identifier(target));
    }

    private int intValue(Node indexExpression, Memory memory) throws StateMachineException {
        return requireInteger(evaluate(indexExpression, memory).value);
//        return (int) createParseTreeExpression(indexExpression).evaluate(memory).evaluate();
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
        return Logger.getLogger(PascalCompiler.class.getName());
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

    private static final Object EMPTY_MULTIPLICATIVE_OPERATION = new Object() {
        @Override
        public String toString() {
            return "Empty Multiplicative Operation";
        }
    };

    private static final Object EMPTY_ADDITIVE_OPERATION = new Object() {
        @Override
        public String toString() {
            return "Empty Additive Operation";
        }
    };

    private static final String EMPTY_OPERATION_RESULT = "EmptyOperationResult";

}
