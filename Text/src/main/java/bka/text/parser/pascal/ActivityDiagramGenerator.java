/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans, humanity, the environment or the universe.
*/

package bka.text.parser.pascal;

import java.util.*;
import run.*;
import run.pascal.*;
import run.statemachine.*;
import uml.factory.*;
import uml.statechart.*;
import uml.structure.Operation;
import uml.structure.Parameter;
import uml.structure.Type;

/**
 * Builds an activity diagram from executable statements.
 *
 * <p>Control flow comes from the statement tree. Actions and guards evaluate
 * those statements against a {@link Memory}, so a {@link StateMachine} can run
 * the diagram.
 */
public final class ActivityDiagramGenerator {

    public ActivityDiagramGenerator(Map<Operation, CompoundStatement> methods) {
        this.methods = Map.copyOf(methods);
    }

    public Collection<Transition<Event, GuardCondition, Action>> generate(Operation operation) {
        return diagrams.computeIfAbsent(operation, o -> createDiagram(o));
    }

    private Collection<Transition<Event, GuardCondition, Action>> createDiagram(Operation operation) {
        ActivityDiagramBuilder diagram = new ActivityDiagramBuilder();
        CompoundStatement body = methods.get(operation);
        if (body == null) {
            throw new IllegalArgumentException("No statements for operation " + operation.getName().orElse("anonymous"));
        }
        body.getStatements().forEach(statement -> add(statement, diagram));
        diagram.addFinalState();
        return diagram.getTransitions();
    }

    private void add(Statement statement, ActivityDiagramBuilder diagram) {
        switch (statement) {
            case NoOperation _ -> {
            }
            case CompoundStatement compound ->
                compound.getStatements().forEach(child -> add(child, diagram));
            case ExpressionStatement expression ->
                diagram.add(actionState(expression));
            case BranchStatement branch ->
                addBranch(branch, diagram);
            case LoopStatement loop ->
                addLoop(loop, diagram);
        }
    }

    private void addBranch(BranchStatement branch, ActivityDiagramBuilder diagram) {
        Decision<Evaluator> decision = decision(branch.getCondition());
        diagram.add(leave -> UmlTransitionFactory.createTransition(leave, decision), decision);
        add(branch.getIfClause().orElseThrow(), diagram);
        diagram.addGuardCondition(decision, GuardConditionFactory.pass(decision), "then");
        Optional<Statement> elseClause = branch.getDefaultChoice();
        if (elseClause.isEmpty()) {
            diagram.addLeaf(decision);
        }
        else {
            diagram.fork(decision);
            add(elseClause.get(), diagram);
            diagram.addStereotype(
                transition -> decision.equals(transition.getSource()) && transition.getGuardCondition().isEmpty(),
                "else");
            diagram.join();
        }
    }

    private void addLoop(LoopStatement loop, ActivityDiagramBuilder diagram) {
        if (loop.getExitCondition().isPresent()) {
            addRepeat(loop, diagram);
            return;
        }
        if (loop.getIncrementAction().isPresent()) {
            addFor(loop, diagram);
            return;
        }
        addWhile(loop, diagram);
    }

    private void addWhile(LoopStatement loop, ActivityDiagramBuilder diagram) {
        Decision<Evaluator> decision = decision(loop.getEntryCondition().orElseThrow());
        diagram.add(leave -> UmlTransitionFactory.createTransition(leave, decision), decision);
        add(loop.getAction(), diagram);
        diagram.addGuardCondition(decision, GuardConditionFactory.pass(decision), "while");
        diagram.add(decision, UmlStereotypeFactory.createStereotypes("loop"));
    }

    private void addRepeat(LoopStatement loop, ActivityDiagramBuilder diagram) {
        TransitionSource loopRoot = diagram.anyLeaf();
        add(loop.getAction(), diagram);
        Decision<Evaluator> decision = decision(loop.getExitCondition().orElseThrow());
        diagram.add(leave -> UmlTransitionFactory.createTransition(leave, decision), decision);
        TransitionTarget loopStart = diagram.targetOf(loopRoot);
        diagram.addTransition(decision, loopStart, GuardConditionFactory.fail(decision), "repeat");
    }

    private void addFor(LoopStatement loop, ActivityDiagramBuilder diagram) {
        Decision<Evaluator> loopStartDecision = decision(loop.getEntryCondition().orElseThrow());
        diagram.add(leave -> UmlTransitionFactory.createTransition(leave, loopStartDecision), loopStartDecision);
        add(loop.getAction(), diagram);
        diagram.addGuardCondition(loopStartDecision, GuardConditionFactory.pass(loopStartDecision), "for");
        TransitionTarget loopStart = diagram.targetOf(loopStartDecision);
        Decision<Evaluator> loopEndDecision = decision(loop.getIncrementGuard().orElseThrow());
        diagram.add(loopEndDecision, actionState(requireExpression(loop.getIncrementAction().orElseThrow())), loopStart, "for");
        diagram.addLeaf(loopStartDecision);
    }

    private ActionState<Action> actionState(ExpressionStatement statement) {
        return UmlStateFactory.createActionState(new Action() {
            @Override
            public void perform(Memory memory) throws StateMachineException {
                execute(statement, memory);
            }

            @Override
            public String toString() {
                return statement.toString();
            }
        });
    }

    private Decision<Evaluator> decision(Evaluable condition) {
        return new Decision<>() {
            @Override
            public Evaluator getExpression() {
                return new Evaluator() {
                    @Override
                    public Object evaluate(Memory memory) throws StateMachineException {
                        return ActivityDiagramGenerator.this.evaluate(condition, memory);
                    }

                    @Override
                    public String toString() {
                        return condition.toString();
                    }
                };
            }

            @Override
            public Optional<Type> getType() {
                return condition.getType();
            }

            @Override
            public Optional<String> getName() {
                return Optional.of(condition.toString());
            }
        };
    }

    private void execute(ExpressionStatement statement, Memory memory) throws StateMachineException {
        Object value = evaluate(statement.getExpression(), memory);
        if (statement.getAssignable().isPresent()) {
            assign(statement.getAssignable().get(), value, memory);
        }
    }

    private void assign(Assignable target, Object value, Memory memory) throws StateMachineException {
        if (target instanceof ScopeVariableExpression variable) {
            store(memory, variable.getName(), value);
            return;
        }
        if (target instanceof IndexAccessExpression index) {
            requireArray(evaluate(index.getBase(), memory))[slot(index, memory)] = value;
            return;
        }
        if (target instanceof MemberAccessExpression member) {
            requireRecord(evaluate(member.getReceiver(), memory)).put(member.getMember().toLowerCase(), value);
            return;
        }
        throw new StateMachineException("Cannot assign " + target);
    }

    private Object evaluate(Evaluable expression, Memory memory) throws StateMachineException {
        if (expression instanceof ValueExpression value) {
            return value.getValue();
        }
        if (expression instanceof ScopeVariableExpression variable) {
            return load(memory, variable.getName());
        }
        if (expression instanceof OperatorExpression operator) {
            return evaluateOperator(operator, memory);
        }
        if (expression instanceof UnaryOperatorExpression unary) {
            return evaluateUnary(unary, memory);
        }
        if (expression instanceof MethodCallExpression call) {
            return evaluateCall(call, memory);
        }
        if (expression instanceof IndexAccessExpression index) {
            return requireArray(evaluate(index.getBase(), memory))[slot(index, memory)];
        }
        if (expression instanceof MemberAccessExpression member) {
            return requireRecord(evaluate(member.getReceiver(), memory)).get(member.getMember().toLowerCase());
        }
        if (expression.getClass().isAnonymousClass()) {
            return expression.evaluate(null, null);
        }
        throw new StateMachineException("Cannot evaluate " + expression);
    }

    private Object evaluateUnary(UnaryOperatorExpression unary, Memory memory) throws StateMachineException {
        Object value = evaluate(unary.getExpression(), memory);
        return switch (unary.getUnaryOperator()) {
            case NEGATION ->
                negate(value);
            case NOT ->
                not(value);
        };
    }

    private Object evaluateOperator(OperatorExpression operator, Memory memory) throws StateMachineException {
        Object left = evaluate(operator.getLeft(), memory);
        Object right = evaluate(operator.getRight(), memory);
        return switch (operator.getOperator()) {
            case EQUALS ->
                left.equals(right);
            case UNEQUALS ->
                !left.equals(right);
            case LESS_THAN ->
                number(left).doubleValue() < number(right).doubleValue();
            case GREATER_THAN ->
                number(left).doubleValue() > number(right).doubleValue();
            case LESS_EQUAL ->
                number(left).doubleValue() <= number(right).doubleValue();
            case GREATER_EQUAL ->
                number(left).doubleValue() >= number(right).doubleValue();
            case AND ->
                and(left, right);
            case OR ->
                or(left, right);
            case XOR ->
                xor(left, right);
            case MULTIPLICATION ->
                product(left, right);
            case REAL_DIVISION ->
                number(left).floatValue() / number(right).floatValue();
            case INTEGER_DIVISION ->
                integer(left) / integer(right);
            case MODULUS ->
                integer(left) % integer(right);
            case ADDITION ->
                sum(left, right);
            case SUBTRACTION ->
                difference(left, right);
            default ->
                throw new StateMachineException("Unsupported operator: " + operator.getOperator());
        };
    }

    private Object evaluateCall(MethodCallExpression call, Memory memory) throws StateMachineException {
        Operation operation = call.getOperation();
        Map<String, Object> parameters = new LinkedHashMap<>();
        for (Parameter parameter : operation.getParameters()) {
            parameters.put(parameter.getName().get(), evaluate(call.getArguments().get(parameter), memory));
        }
        List<String> locals = new ArrayList<>();
        methods.get(operation).getLocals().forEach(local -> locals.add(local.getName().get()));
        if (operation.getType().isPresent()) {
            locals.add(operation.getName().get());
        }
        StateMachine stateMachine = new StateMachine(generate(operation), memory, parameters, locals);
        stateMachine.start();
        for (Parameter parameter : operation.getParameters()) {
            if (parameter.getDirection() == Parameter.Direction.INOUT) {
                assign(
                    Execution.asAssignable(call.getArguments().get(parameter)),
                    stateMachine.getMemoryObject(parameter.getName().get()),
                    memory);
            }
        }
        if (operation.getType().isEmpty()) {
            return null;
        }
        return stateMachine.getMemoryObject(operation.getName().get());
    }

    private int slot(IndexAccessExpression index, Memory memory) throws StateMachineException {
        return integer(evaluate(index.getIndex(), memory)) - index.getArrayType().getLowerBound();
    }

    private static ExpressionStatement requireExpression(Statement statement) {
        if (statement instanceof ExpressionStatement expression) {
            return expression;
        }
        throw new IllegalStateException("Expression statement expected: " + statement);
    }

    private static Object[] requireArray(Object value) throws StateMachineException {
        if (value instanceof Object[] array) {
            return array;
        }
        throw new StateMachineException("Not an array: " + value);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> requireRecord(Object value) throws StateMachineException {
        if (value instanceof Map<?, ?> record) {
            return (Map<String, Object>) record;
        }
        throw new StateMachineException("Not a record: " + value);
    }

    private static Object load(Memory memory, String name) throws StateMachineException {
        try {
            return memory.load(name.toLowerCase());
        }
        catch (MemoryException ex) {
            throw new StateMachineException(ex.getMessage(), ex);
        }
    }

    private static void store(Memory memory, String name, Object value) throws StateMachineException {
        try {
            memory.store(name.toLowerCase(), value);
        }
        catch (MemoryException ex) {
            throw new StateMachineException(ex.getMessage(), ex);
        }
    }

    private static Object negate(Object value) throws StateMachineException {
        if (value instanceof Integer integer) {
            return -integer;
        }
        if (value instanceof Float real) {
            return -real;
        }
        throw new StateMachineException("Integer or Real expected: " + value);
    }

    private static Object not(Object value) throws StateMachineException {
        if (value instanceof Boolean bool) {
            return !bool;
        }
        throw new StateMachineException("Not a boolean: " + value);
    }

    private static Object and(Object left, Object right) throws StateMachineException {
        if (left instanceof Boolean leftBoolean && right instanceof Boolean rightBoolean) {
            return leftBoolean && rightBoolean;
        }
        if (left instanceof Integer leftInteger && right instanceof Integer rightInteger) {
            return leftInteger & rightInteger;
        }
        throw new StateMachineException("Cannot apply AND to " + left + " and " + right);
    }

    private static Object or(Object left, Object right) throws StateMachineException {
        if (left instanceof Boolean leftBoolean && right instanceof Boolean rightBoolean) {
            return leftBoolean || rightBoolean;
        }
        if (left instanceof Integer leftInteger && right instanceof Integer rightInteger) {
            return leftInteger | rightInteger;
        }
        throw new StateMachineException("Cannot apply OR to " + left + " and " + right);
    }

    private static Object xor(Object left, Object right) throws StateMachineException {
        if (left instanceof Boolean leftBoolean && right instanceof Boolean rightBoolean) {
            return leftBoolean ^ rightBoolean;
        }
        if (left instanceof Integer leftInteger && right instanceof Integer rightInteger) {
            return leftInteger ^ rightInteger;
        }
        throw new StateMachineException("Cannot apply XOR to " + left + " and " + right);
    }

    private static Object product(Object left, Object right) throws StateMachineException {
        if (left instanceof Integer leftInteger && right instanceof Integer rightInteger) {
            return leftInteger * rightInteger;
        }
        if (left instanceof Number leftNumber && right instanceof Number rightNumber) {
            return leftNumber.floatValue() * rightNumber.floatValue();
        }
        throw new StateMachineException("Cannot multiply " + left + " and " + right);
    }

    private static Object sum(Object left, Object right) throws StateMachineException {
        if (left instanceof Integer leftInteger && right instanceof Integer rightInteger) {
            return leftInteger + rightInteger;
        }
        if (left instanceof Number leftNumber && right instanceof Number rightNumber) {
            return leftNumber.floatValue() + rightNumber.floatValue();
        }
        throw new StateMachineException("Cannot add " + left + " and " + right);
    }

    private static Object difference(Object left, Object right) throws StateMachineException {
        if (left instanceof Integer leftInteger && right instanceof Integer rightInteger) {
            return leftInteger - rightInteger;
        }
        if (left instanceof Number leftNumber && right instanceof Number rightNumber) {
            return leftNumber.floatValue() - rightNumber.floatValue();
        }
        throw new StateMachineException("Cannot subtract " + left + " and " + right);
    }

    private static Number number(Object value) throws StateMachineException {
        if (value instanceof Number number) {
            return number;
        }
        throw new StateMachineException("Not a number: " + value);
    }

    private static Integer integer(Object value) throws StateMachineException {
        if (value instanceof Integer integer) {
            return integer;
        }
        throw new StateMachineException("Not an integer: " + value);
    }

    private final Map<Operation, CompoundStatement> methods;
    private final Map<Operation, Collection<Transition<Event, GuardCondition, Action>>> diagrams = new HashMap<>();

}
