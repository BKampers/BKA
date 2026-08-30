/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans, humanity, the environment or the universe.
*/

package run;

import java.util.*;


/**
 */
public final class LoopStatement implements Statement {

    public static LoopStatement whileLoop(Evaluable condition, Statement action) {
        return new LoopStatement(Optional.of(condition), Optional.empty(), action, Optional.empty(), Optional.empty());
    }

    public static LoopStatement untilLoop(Evaluable condition, Statement action) {
        return new LoopStatement(Optional.empty(), Optional.of(condition), action, Optional.empty(), Optional.empty());
    }

    public static LoopStatement forLoop(Evaluable condition, Evaluable incrementGuard, Statement action, Statement incrementAction) {
        return new LoopStatement(Optional.of(condition), Optional.empty(), action, Optional.of(incrementGuard), Optional.of(incrementAction));
    }

    public static LoopStatement foreverLoop(Statement action) {
        return new LoopStatement(Optional.empty(), Optional.empty(), action, Optional.empty(), Optional.empty());
    }

    private LoopStatement(Optional<Evaluable> entryCondition, Optional<Evaluable> exitCondition, Statement action, Optional<Evaluable> incrementGuard, Optional<Statement> incrementAction) {
        this.entryCondition = entryCondition;
        this.exitCondition = exitCondition;
        this.action = Objects.requireNonNull(action);
        this.incrementGuard = incrementGuard;
        this.incrementAction = incrementAction;
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder("@Loop\n");
        entryCondition.ifPresent(expression -> string.append("@While ").append(expression).append('\n'));
        string.append(action);
        incrementAction.ifPresent(increment -> string.append(increment).append('\n'));
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

    public Optional<Evaluable> getIncrementGuard() {
        return incrementGuard;
    }

    public Optional<Statement> getIncrementAction() {
        return incrementAction;
    }

    @Override
    public void execute(Execution execution, ObjectScope scope) {
        if (exitCondition.isPresent()) {
            executeUntilLoop(execution, scope);
        }
        else if (incrementAction.isPresent()) {
            executeForLoop(execution, scope);
        }
        else {
            executeWhileLoop(execution, scope);
        }
    }

    private void executeWhileLoop(Execution execution, ObjectScope scope) {
        while (execution.evaluateBoolean(entryCondition.get(), scope)) {
            execution.execute(action, scope);
        }
    }

    private void executeUntilLoop(Execution execution, ObjectScope scope) {
        do {
            execution.execute(action, scope);
        } while (!execution.evaluateBoolean(exitCondition.get(), scope));
    }

    private void executeForLoop(Execution execution, ObjectScope scope) {
        boolean doLoop = execution.evaluateBoolean(entryCondition.get(), scope);
        while (doLoop) {
            execution.execute(action, scope);
            if (execution.evaluateBoolean(incrementGuard.get(), scope)) {
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
    private final Optional<Evaluable> incrementGuard;
    private final Optional<Statement> incrementAction;

}
