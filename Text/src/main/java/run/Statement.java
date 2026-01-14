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
                    V left = evaluate(leftOperand, memory);
                    V right = evaluate(rightOperand, memory);
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

    private static Comparable requireComparable(Object object) throws StateMachineException {
        if (object instanceof Comparable comparable) {
            return comparable;
        }
        throw new StateMachineException("Not a comparable" + object);
    }

    private Collection<String> localNames(String methodName) {
        return methodProperties.getLocals(methodName).stream()
            .map(uml.structure.Object::getName)
            .map(Optional::get)
            .collect(Collectors.toList());
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
        String operator = nodes.getFirst().getChildren().getFirst().getSymbol();
        V right = evaluateTerm(nodes.get(1).getChildren(), memory);
        Object sum = (left.value instanceof Boolean leftbool && right.value instanceof Boolean rightbool)
            ? evaluateLogicalAdditiveOperation(leftbool, operator, rightbool)
            : evaluateNumericAdditiveOperation(left.value, operator, right.value);
        return evaluateAdditiveOperation(
            new V(sum, (sum instanceof Boolean) ? "Boolean" : (sum instanceof Integer) ? "Integer" : "Real"),
            nodes.getLast().getChildren(),
            memory);
    }

    private boolean evaluateLogicalAdditiveOperation(boolean left, String operator, boolean right) throws StateMachineException {
        return switch (operator) {
            case "OR\\b" ->
                left || right;
            case "XOR\\b" ->
                left ^ right;
            default ->
                throw new StateMachineException("Unsupported logical additive operator: " + operator);
        };
    }

    private Number evaluateNumericAdditiveOperation(Object left, String operator, Object right) throws StateMachineException {
        return switch (operator) {
            case "\\+" ->
                sum(requireNumber(left), requireNumber(right));
            case "\\-" ->
                difference(requireNumber(left), requireNumber(right));
            case "OR\\b" ->
                requireInteger(left) | requireInteger(right);
            case "XOR\\b" ->
                requireInteger(left) ^ requireInteger(right);
            default ->
                throw new StateMachineException("Unsupported additive operator: " + operator);
        };
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
        String operator = nodes.getFirst().getChildren().getFirst().getSymbol();
        V right = evaluateFactor(nodes.get(1).getChildren(), memory);
        Object product = (left.value instanceof Boolean leftbool && right.value instanceof Boolean rightbool)
            ? evaluateLogicalMultiplicativeOperation(leftbool, operator, rightbool)
            : evaluateNumericMultiplcativeOperation(left.value, operator, right.value);
        return evaluateMultiplicativeOperation(
            new V(product, (product instanceof Boolean) ? "Boolean" : (product instanceof Integer) ? "Integer" : "Real"),
            nodes.getLast().getChildren(),
            memory);
    }

    private boolean evaluateLogicalMultiplicativeOperation(boolean left, String operator, boolean right) throws StateMachineException {
        return switch (operator) {
            case "AND\\b" ->
                left && right;
            default ->
                throw new StateMachineException("Unsupported logical multiplicatieve operator: " + operator);
        };
    }

    private Number evaluateNumericMultiplcativeOperation(Object left, String operator, Object right) throws StateMachineException {
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
                requireInteger(left) & requireInteger(right);
            default ->
                throw new StateMachineException("Unsupported multiplicative operator: " + operator);
        };
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

    private static Boolean requireBoolean(Object object) throws StateMachineException {
        if (object instanceof Boolean bool) {
            return bool;
        }
        throw new StateMachineException("Not a boolean: " + object);
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

    private V evaluateUnaryOperation(Node operator, V operand) throws StateMachineException {
        if ("\\-".equals(operator.getSymbol())) {
            return new V(-requireInteger(operand.value), "Integer");
        }
        if ("NOT\\b".equals(operator.getSymbol())) {
            return new V(!requireBoolean(operand.value), "Boolean");
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
                new V((java.lang.Object) Integer.parseInt(node.content().substring(1), 0x10), "Integer");
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

}
