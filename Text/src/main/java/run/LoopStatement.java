package run;

import java.util.*;
import run.pascal.*;


/**
 */
public final class LoopStatement implements Statement {

    public static LoopStatement whileLoop(Evaluable condition, Statement action) {
        return new LoopStatement(Optional.of(condition), Optional.empty(), action, Optional.empty());
    }

    public static LoopStatement untilLoop(Evaluable condition, Statement action) {
        return new LoopStatement(Optional.empty(), Optional.of(condition), action, Optional.empty());
    }

    public static LoopStatement forLoop(Evaluable condition, Statement action, Statement incrementAction) {
        return new LoopStatement(Optional.of(condition), Optional.empty(), action, Optional.of(incrementAction));
    }

    public static LoopStatement foreverLoop(Statement action) {
        return new LoopStatement(Optional.empty(), Optional.empty(), action, Optional.empty());
    }

    private LoopStatement(Optional<Evaluable> entryCondition, Optional<Evaluable> exitCondition, Statement action, Optional<Statement> incrementAction) {
        this.entryCondition = entryCondition;
        this.exitCondition = exitCondition;
        this.action = Objects.requireNonNull(action);
        this.incrementAction = incrementAction;
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder("@Loop\n");
        entryCondition.ifPresent(expression -> string.append("@While ").append(expression).append('\n'));
        string.append(action);
        incrementAction.ifPresent(action -> string.append(action).append('\n'));
        exitCondition.ifPresent(expression -> string.append("@Until ").append(expression));
        return string.toString();
    }

    public Optional<Evaluable> getEntryCondition() {
        return entryCondition;
    }

    public Optional<Evaluable> getExitCondition() {
        return exitCondition;
    }

    public Statement getAction() {
        return action;
    }

    public Optional<Statement> getIncrementAction() {
        return incrementAction;
    }

    @Override
    public void execute(Execution execution, ObjectScope scope) {
        if (exitCondition.isPresent()) {
            do {
                execution.execute(action, scope);
            } while (!execution.evaluateBoolean(exitCondition.get(), scope));
        }
        else if (incrementAction.isPresent()) {
            executeForLoop(execution, scope);
        }
        else {
            while (execution.evaluateBoolean(entryCondition.get(), scope)) {
                execution.execute(action, scope);
            }
        }
    }

    private void executeForLoop(Execution execution, ObjectScope scope) {
        Evaluable condition = entryCondition.orElseThrow(() -> new IllegalStateException("No loop condition"));
        if (!(condition instanceof BinaryOperatorExpression loopCondition && loopCondition.getOperator() == Operator.LESS_EQUAL)) {
            throw new IllegalStateException("Unsupported for loop condition: " + condition);
        }
        Evaluable incrementCondition = new OperatorExpression(
            loopCondition.getLeft(),
            Operator.LESS_THAN,
            loopCondition.getRight());
        boolean doLoop = execution.evaluateBoolean(condition, scope);
        while (doLoop) {
            execution.execute(action, scope);
            if (execution.evaluateBoolean(incrementCondition, scope)) {
                execution.execute(incrementAction.get(), scope);
            }
            else {
                doLoop = false;
            }
        }
    }

    private final Optional<Evaluable> entryCondition;
    private final Optional<Evaluable> exitCondition;
    private final Statement action;
    private final Optional<Statement> incrementAction;

}
