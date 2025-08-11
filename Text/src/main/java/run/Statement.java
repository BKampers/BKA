/*
** © Bart Kampers
*/
package run;

import bka.text.parser.pascal.*;
import java.util.*;
import java.util.function.*;
import java.util.logging.*;
import uml.statechart.*;


public class Statement {

    public Statement(PascalParser.Node node, Function<String, Optional<Collection<Transition<Event, GuardCondition, Action>>>> methods) {
        this(Optional.empty(), Objects.requireNonNull(node), Objects.requireNonNull(methods));
    }

    private Statement(PascalParser.Node assignable, PascalParser.Node expression, Function<String, Optional<Collection<Transition<Event, GuardCondition, Action>>>> methods) {
        this(Optional.of(assignable), expression, methods);
    }

    private Statement(Optional<PascalParser.Node> assignable, PascalParser.Node expression, Function<String, Optional<Collection<Transition<Event, GuardCondition, Action>>>> methods) {
        this.assignable = assignable;
        this.expression = expression;
        this.methodSupplier = methods;
    }

    public void getTransitions(Collection<Transition<Event, GuardCondition, Action>> transitions, Collection<TransitionSource> leaves) {
        if (!expression.getChildren().isEmpty()) {
            switch (expression.getChildren().getFirst().getSymbol()) {
                case "IF\\b" ->
                    addIfThenElseTransitions(transitions, leaves);
                case "FOR\\b" ->
                    addForLoopTransitions(transitions, leaves);
                case "WHILE\\b" ->
                    addWhileLoopTransitions(transitions, leaves);
                case "REPEAT\\b" ->
                    addRepeatLoopTransitions(transitions, leaves);
                case "Assignable" ->
                    addAssignementTransitions(transitions, leaves);
                default ->
                    throw new IllegalStateException("Unexpected symbol " + expression.getChildren().getFirst().getSymbol());
            }
        }
        else {
            Logger.getLogger(PascalCompiler.class.getName()).log(Level.WARNING, "Empty statenemt: {0}", toString());
        }
    }

    private void addIfThenElseTransitions(Collection<Transition<Event, GuardCondition, Action>> transitions, Collection<TransitionSource> leaves) {
        Decision decision = UmlStateFactory.createDecision(createParseTreeExpression(expression.getChildren().get(1).getSymbol(), expression.getChildren().get(1).getChildren()));
        leaves.forEach(leave -> transitions.add(UmlTransitionFactory.createTransition(leave, decision)));
        leaves.clear();
        leaves.add(decision);
        createTransitions(expression.getChildren().get(3), transitions, leaves);
        addGuardCondition(transitions, transition -> decision.equals(transition.getSource()), UmlGuardConditionFactory.pass(decision), "then");
        if (expression.getChildren().get(4).getChildren().isEmpty()) {
            leaves.add(decision);
        }
        else {
            Collection<TransitionSource> elseLeaves = new ArrayList<>(List.of(decision));
            createTransitions(expression.getChildren().get(4).getChildren().get(1), transitions, elseLeaves);
            addStereotype(transitions, transition -> decision.equals(transition.getSource()) && transition.getGuardCondition().isEmpty(), "else");
            leaves.addAll(elseLeaves);
        }
    }

    private void addForLoopTransitions(Collection<Transition<Event, GuardCondition, Action>> transitions, Collection<TransitionSource> leaves) {
        PascalParser.Node identifier = expression.getChildren().get(1);
        ActionState<Action> loopInitialization = createActionState(new Statement(identifier, expression.getChildren().get(3), methodSupplier));
        leaves.forEach(leave -> transitions.add(UmlTransitionFactory.createTransition(leave, loopInitialization)));
        leaves.clear();
        Decision decision = UmlStateFactory.createDecision(createLessEqualExpression(identifier, expression.getChildren().get(5)));
        transitions.add(UmlTransitionFactory.createTransition(loopInitialization, decision));
        leaves.add(decision);
        createTransitions(expression.getChildren().get(7), transitions, leaves);
        Action incrementAction = new Action() {
            @Override
            public void perform(Memory memory) throws StateMachineException {
                memory.store(identifier.content(), ((Integer) memory.load(identifier.content())) + 1);
            }

            @Override
            public String toString() {
                return ".INC. " + identifier.content();
            }
        };
        ActionState<Action> incrementActionState = UmlStateFactory.createActionState(incrementAction);
        leaves.forEach(leave -> transitions.add(UmlTransitionFactory.createTransition(leave, incrementActionState)));
        leaves.clear();
        addGuardCondition(transitions, transition -> decision.equals(transition.getSource()), UmlGuardConditionFactory.pass(decision), "for");
        transitions.add(UmlTransitionFactory.createTransition(incrementActionState, decision));
        leaves.add(decision);
    }

    private void addWhileLoopTransitions(Collection<Transition<Event, GuardCondition, Action>> transitions, Collection<TransitionSource> leaves) {
        Decision decision = UmlStateFactory.createDecision(createParseTreeExpression(expression.getChildren().get(1).getSymbol(), expression.getChildren().get(1).getChildren()));
        leaves.forEach(leave -> transitions.add(UmlTransitionFactory.createTransition(leave, decision)));
        leaves.clear();
        leaves.add(decision);
        createTransitions(expression.getChildren().get(3), transitions, leaves);
        addGuardCondition(transitions, transition -> decision.equals(transition.getSource()), UmlGuardConditionFactory.pass(decision), "while");
        leaves.forEach(leave -> transitions.add(UmlTransitionFactory.createTransition(leave, decision, UmlStereotypeFactory.createStereotypes("loop"))));
        leaves.clear();
        leaves.add(decision);
    }

    private void addRepeatLoopTransitions(Collection<Transition<Event, GuardCondition, Action>> transitions, Collection<TransitionSource> leaves) {
        TransitionSource loopRoot = leaves.stream().findAny().get();
        createTransitions(expression.getChildren().get(1), transitions, leaves);
        ParseTreeExpression condition = createParseTreeExpression(expression.getChildren().get(3).getSymbol(), expression.getChildren().get(3).getChildren());
        Decision decision = UmlStateFactory.createDecision(condition);
        leaves.forEach(leave -> transitions.add(UmlTransitionFactory.createTransition(leave, decision)));
        TransitionTarget loopStart = transitions.stream().filter(transition -> loopRoot.equals(transition.getSource())).findAny().get().getTarget();
        transitions.add(UmlTransitionFactory.createTransition(decision, loopStart, UmlGuardConditionFactory.fail(decision), UmlStereotypeFactory.createStereotypes("repeat")));
        leaves.clear();
        leaves.add(decision);
    }

    private void addAssignementTransitions(Collection<Transition<Event, GuardCondition, Action>> transitions, Collection<TransitionSource> leaves) {
        ActionState<Action> assignment = createActionState(new Statement(expression.getChildren().getFirst(), expression.getChildren().get(2), methodSupplier));
        leaves.forEach(leave -> transitions.add(UmlTransitionFactory.createTransition(leave, assignment)));
        leaves.clear();
        leaves.add(assignment);
    }

    private static ActionState<Action> createActionState(Statement statement) {
        return UmlStateFactory.createActionState(Action.of(statement));
    }

    private void addGuardCondition(Collection<Transition<Event, GuardCondition, Action>> transitions, Predicate<Transition<Event, GuardCondition, Action>> predicate, GuardCondition guardCondition, String stereotype) {
        Transition<Event, GuardCondition, Action> transition = transitions.stream().filter(predicate).findAny().get();
        transitions.remove(transition);
        transitions.add(UmlTransitionFactory.copyTransition(transition, Optional.of(guardCondition), UmlStereotypeFactory.createStereotypes(stereotype)));
    }

    private void addStereotype(Collection<Transition<Event, GuardCondition, Action>> transitions, Predicate<Transition<Event, GuardCondition, Action>> predicate, String stereotype) {
        Transition<Event, GuardCondition, Action> transition = transitions.stream().filter(predicate).findAny().get();
        transitions.remove(transition);
        transitions.add(UmlTransitionFactory.copyTransition(transition, transition.getGuardCondition(), UmlStereotypeFactory.createStereotypes(stereotype)));
    }

    private void createTransitions(PascalParser.Node statements, Collection<Transition<Event, GuardCondition, Action>> transitions, Collection<TransitionSource> leaves) {
        if ("CompoundStatement".equals(statements.getChildren().getFirst().getSymbol())) {
            createStatementSequence(statements.getChildren().getFirst().getChildren().get(1), transitions, leaves);
        }
        else if ("Statements".equals(statements.getSymbol())) {
            createStatementSequence(statements, transitions, leaves);
        }
        else {
            new Statement(statements, methodSupplier).getTransitions(transitions, leaves);
        }
    }

    private void createStatementSequence(PascalParser.Node statements, Collection<Transition<Event, GuardCondition, Action>> transitions, Collection<TransitionSource> leaves) {
        while (statements != null) {
            Statement statement = new Statement(statements.getChildren().getFirst(), methodSupplier);
            statement.getTransitions(transitions, leaves);
            statements = (statements.getChildren().size() > 1) ? statements.getChildren().getLast() : null;
        }
    }

    private String typeOf(List<PascalParser.Node> expression) {
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
        throw new IllegalStateException("Not an expression: " + expression);
    }

    private ParseTreeExpression getExpressionTree(PascalParser.Node expression) {
        if ("Expression".equals(expression.getSymbol())) {
            return createParseTreeExpression(expression.getSymbol(), expression.getChildren());
        }
        if ("Identifier".equals(expression.getSymbol())) {
            return createParseTreeExpression(expression.getSymbol(), expression.getChildren());
        }
        throw new IllegalStateException("Not an expression: " + expression);
    }

    private ParseTreeExpression createLessEqualExpression(PascalParser.Node leftOperand, PascalParser.Node rightOperand) {
        Evaluator evaluator = memory -> {
            Value left = createParseTreeExpression(leftOperand.getSymbol(), leftOperand.getChildren()).evaluate(memory);
            Value right = createParseTreeExpression(rightOperand.getSymbol(), rightOperand.getChildren()).evaluate(memory);
            return ((Comparable) left.get()).compareTo((Comparable) right.get()) <= 0;
        };
        return ParseTreeExpression.of("Boolean", leftOperand.content() + " .LE. " + rightOperand.content(), evaluator);
    }

    private ParseTreeExpression createParseTreeExpression(String symbol, List<PascalParser.Node> expression) {
        if (expression.size() == 1) {
            if ("Literal".equals(expression.getFirst().getSymbol())) {
                String type = switch (expression.getFirst().getChildren().getFirst().getSymbol()) {
                    case "IntegerLiteral" ->
                        "Integer";
                    case "'" ->
                        "String";
                    default ->
                        throw new IllegalArgumentException("Unexpected literal: " + expression.getFirst().getChildren().getFirst().getSymbol());
                };
                String value = expression.getFirst().content();
                Evaluator evaluator = memory -> {
                    return switch (expression.getFirst().getChildren().getFirst().getSymbol()) {
                        case "IntegerLiteral" ->
                            parseInteger(value);
                        case "'" ->
                            value;
                        default ->
                            throw new IllegalArgumentException("Cannot evaluate literal: " + expression.getFirst().getChildren().getFirst().getSymbol());
                    };
                };
                return ParseTreeExpression.of(type, value, evaluator);
            }
            if ("Identifier".equals(expression.getFirst().getSymbol())) {
                String value = expression.getFirst().getChildren().getFirst().content();
                Evaluator evaluator = memory -> {
                    String functionName = value;
                    Optional<Collection<Transition<Event, GuardCondition, Action>>> method = methodSupplier.apply(functionName);
                    if (method.isPresent()) {
                        StateMachine stateMachine = new StateMachine(method.get());
                        stateMachine.start();
                        return stateMachine.getMemoryObject(functionName);
                    }
                    return memory.load(value);
                };
                return ParseTreeExpression.of("*", value, evaluator);
            }
            if ("Identifier".equals(symbol)) {
                String value = expression.getFirst().content();
                return ParseTreeExpression.of("*", value, memory -> memory.load(value));
            }
            throw new IllegalStateException("Cannot create parse tree expression");
        }
        String type = typeOf(expression);
        if (expression.size() == 3 && "BinaryOperator".equals(expression.get(1).getSymbol())) {
            String value
                = createParseTreeExpression(expression.getFirst().getSymbol(), expression.getFirst().getChildren()).value()
                + " (Operator)" + expression.get(1).content() + " "
                + createParseTreeExpression(expression.getLast().getSymbol(), expression.getLast().getChildren()).value() + ")";
            Evaluator evaluator = memory -> {
                Value v = getDyadicOperator(expression.get(1)).evaluate(
                    createParseTreeExpression(expression.getFirst().getSymbol(), expression.getFirst().getChildren()),
                    createParseTreeExpression(expression.getLast().getSymbol(), expression.getLast().getChildren()),
                    memory);
                return v.get();
            };
            return ParseTreeExpression.of(type, value, evaluator);
        }
        throw new IllegalStateException("Invalid expression");
    }

    private static java.lang.Object parseInteger(String literal) {
        return (literal.startsWith("$"))
            ? Integer.parseInt(literal, 1, literal.length(), 0x10)
            : Integer.valueOf(literal);
    }

    private static DyadicOperator getDyadicOperator(PascalParser.Node node) {
        switch (node.content().toLowerCase()) {
            case "^":
                return createArithmicOperator((left, right) -> power(left, right));
            case "*":
                return createArithmicOperator((left, right) -> product(left, right));
            case "/":
                return createArithmicOperator((left, right) -> quotient(left, right));
            case "div":
                return createArithmicOperator((left, right) -> division(left, right));
            case "mod":
                return createArithmicOperator((left, right) -> modulus(left, right));
            case "+":
                return createArithmicOperator((left, right) -> sum(left, right));
            case "-":
                return createArithmicOperator((left, right) -> difference(left, right));
            case "=":
                return createRelationalOperator((left, right) -> left.compareTo(right) == 0);
            case "<>":
                return createRelationalOperator((left, right) -> left.compareTo(right) != 0);
            case "<":
                return createRelationalOperator((left, right) -> left.compareTo(right) < 0);
            case "<=":
                return createRelationalOperator((left, right) -> left.compareTo(right) <= 0);
            case ">":
                return createRelationalOperator((left, right) -> left.compareTo(right) > 0);
            case ">=":
                return createRelationalOperator((left, right) -> left.compareTo(right) > 0);
            case "and":
                return createLogicalOperator((left, right) -> left && right);
            case "or":
                return createLogicalOperator((left, right) -> left || right);
            case "xor":
                return createLogicalOperator((left, right) -> !left.equals(right));
            default:
                throw new IllegalStateException("Unsupported binary operator: '" + node.content() + "'");
        }
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

    public Optional<PascalParser.Node> getAssignable() {
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

    private final Optional<PascalParser.Node> assignable;
    private final PascalParser.Node expression;

    private final Function<String, Optional<Collection<Transition<Event, GuardCondition, Action>>>> methodSupplier;

    private static final List<String> RELATIONAL_OPERATORS = List.of("<", "<=", "=", ">", ">=", "<>");

}
