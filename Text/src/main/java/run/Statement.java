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
import uml.statechart.*;


public final class Statement {

    public Statement(Node expression, Function<String, Optional<Collection<Transition<Event, GuardCondition, Action>>>> methods) {
        this(Optional.empty(), expression, methods);
    }

    private Statement(Node assignable, Node expression, Function<String, Optional<Collection<Transition<Event, GuardCondition, Action>>>> methods) {
        this(Optional.of(assignable), expression, methods);
    }

    private Statement(Optional<Node> assignable, Node expression, Function<String, Optional<Collection<Transition<Event, GuardCondition, Action>>>> methods) {
        this.assignable = assignable;
        this.expression = Objects.requireNonNull(expression);
        this.methodSupplier = Objects.requireNonNull(methods);
    }

    public void getTransitions(ActivityDiagramBuilder diagram) {
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
                default ->
                    throw new IllegalStateException("Unexpected symbol " + expression.getChildren().getFirst().getSymbol());
            }
        }
        else {
            Logger.getLogger(PascalCompiler.class.getName()).log(Level.WARNING, "Empty statenemt: {0}", toString());
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
            Collection<TransitionSource> ifBranchLeaves = diagram.replaceLeaves(List.of(decision));
            createTransitions(elseClause.getChild("Statement"), diagram);
            diagram.addStereotype(transition -> decision.equals(transition.getSource()) && transition.getGuardCondition().isEmpty(), "else");
            diagram.addLeaves(ifBranchLeaves);
        }
    }

    private void addForLoopTransitions(ActivityDiagramBuilder diagram) {
        Node identifier = expression.getChild("Identifier");
        List<Node> expressions = expression.findChildren("Expression");
        ActionState<Action> loopInitialization = createActionState(new Statement(identifier, expressions.getFirst(), methodSupplier));
        Decision decision = UmlStateFactory.createDecision(createLessEqualExpression(identifier, expressions.getLast()));
        diagram.add(loopInitialization, decision);
        createTransitions(expression.getChild("Statement"), diagram);
        Action incrementAction = createAction(
            memory -> memory.store(identifier.content(), ((Integer) memory.load(identifier.content())) + 1),
            () -> ".INC. " + identifier.content());
        ActionState<Action> incrementActionState = UmlStateFactory.createActionState(incrementAction);
        diagram.add(incrementActionState, decision, "for");
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
        TransitionSource loopRoot = diagram.anyLeaf();//leaves.stream().findAny().get();
        createTransitions(expression.getChild("Statements"), diagram);
        ParseTreeExpression condition = createParseTreeExpression();
        Decision decision = UmlStateFactory.createDecision(condition);
        diagram.add(leave -> UmlTransitionFactory.createTransition(leave, decision), decision);
        TransitionTarget loopStart = diagram.targetOf(loopRoot);//transitions.stream().filter(transition -> loopRoot.equals(transition.getSource())).findAny().get().getTarget();
        diagram.addTransition(decision, loopStart, UmlGuardConditionFactory.fail(decision), "repeat");
    }

    private void addAssignementTransitions(ActivityDiagramBuilder diagram) {
        diagram.add(createActionState(new Statement(expression.getChild("Assignable"), expression.getChild("Expression"), methodSupplier)));
    }

    private static ActionState<Action> createActionState(Statement statement) {
        return UmlStateFactory.createActionState(Action.of(statement));
    }

    private void createTransitions(Node statements, ActivityDiagramBuilder diagram) {
        if (statements.startsWith("CompoundStatement")) {
            createStatementSequence(statements.getChild("CompoundStatement").getChild("Statements"), diagram);
        }
        else if ("Statements".equals(statements.getSymbol())) {
            createStatementSequence(statements, diagram);
        }
        else {
            new Statement(statements, methodSupplier).getTransitions(diagram);
        }
    }

    private void createStatementSequence(Node statements, ActivityDiagramBuilder diagram) {
        Optional<Node> next = Optional.of(statements);
        while (next.isPresent()) {
            Statement statement = new Statement(next.get().getChild("Statement"), methodSupplier);
            statement.getTransitions(diagram);
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
        throw new IllegalStateException("Not an expression: " + expression);
    }

    private ParseTreeExpression getExpressionTree(Node expression) {
        if ("Expression".equals(expression.getSymbol())) {
            return createParseTreeExpression(expression.getSymbol(), expression.getChildren());
        }
        if ("Identifier".equals(expression.getSymbol())) {
            return createParseTreeExpression(expression.getSymbol(), expression.getChildren());
        }
        throw new IllegalStateException("Not an expression: " + expression);
    }

    private ParseTreeExpression createLessEqualExpression(Node leftOperand, Node rightOperand) {
        Evaluator evaluator = memory -> {
            Value left = createParseTreeExpression(leftOperand.getSymbol(), leftOperand.getChildren()).evaluate(memory);
            Value right = createParseTreeExpression(rightOperand.getSymbol(), rightOperand.getChildren()).evaluate(memory);
            return ((Comparable) left.get()).compareTo((Comparable) right.get()) <= 0;
        };
        return ParseTreeExpression.of("Boolean", leftOperand.content() + " .LE. " + rightOperand.content(), evaluator);
    }

    private ParseTreeExpression createParseTreeExpression() {
        return createParseTreeExpression("Expression", expression.getChild("Expression").getChildren());
    }
    
    private ParseTreeExpression createParseTreeExpression(Node node) {
        return createParseTreeExpression(node.getSymbol(), node.getChildren());
    }

    private ParseTreeExpression createParseTreeExpression(String symbol, List<Node> expression) {
        if (expression.size() == 1) {
            Node first = expression.getFirst();
            if ("Literal".equals(first.getSymbol())) {
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
            if ("Identifier".equals(first.getSymbol())) {
                String value = first.getChildren().getFirst().content();
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
                String value = first.content();
                return ParseTreeExpression.of("*", value, memory -> memory.load(value));
            }
            throw new IllegalStateException("Cannot create parse tree expression");
        }
        String type = typeOf(expression);
        if (expression.size() == 3 && "BinaryOperator".equals(expression.get(1).getSymbol())) {
            Node first = expression.getFirst();
            String value
                = createParseTreeExpression(first).value()
                + " (Operator)" + expression.get(1).content() + " "
                + createParseTreeExpression(expression.getLast()).value() + ")";
            Evaluator evaluator = memory -> {
                Value v = getDyadicOperator(expression.get(1)).evaluate(
                    createParseTreeExpression(first),
                    createParseTreeExpression(expression.getLast()),
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

    private static DyadicOperator getDyadicOperator(Node node) {
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

    private final Function<String, Optional<Collection<Transition<Event, GuardCondition, Action>>>> methodSupplier;

    private static final List<String> RELATIONAL_OPERATORS = List.of("<", "<=", "=", ">", ">=", "<>");

}
