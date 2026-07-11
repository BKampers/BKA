package run;

import java.util.*;

/**
 */
public final class LoopStatement implements Statement {

    public static LoopStatement whileLoop(Expression condition, Statement action) {
        return new LoopStatement(Optional.of(condition), Optional.empty(), action, Optional.empty());
    }
    
    public static LoopStatement untilLoop(Expression condition, Statement action) {
        return new LoopStatement(Optional.empty(), Optional.of(condition), action, Optional.empty());
    }
    
    public static LoopStatement forLoop(Expression condition, Statement action, Statement incrementAction) {
        return new LoopStatement(Optional.of(condition), Optional.empty(), action, Optional.of(incrementAction));
    }
    
    public static LoopStatement foreverLoop(Statement action) {
        return new LoopStatement(Optional.empty(), Optional.empty(), action, Optional.empty());
    }
    
    private LoopStatement(Optional<Expression> entryCondition, Optional<Expression> exitCondition, Statement action, Optional<Statement> incrementAction) {
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
    
    public Optional<Expression> getEntryCondition() {
        return entryCondition;
    }

    public Optional<Expression> getExitCondition() {
        return exitCondition;
    }

    public Statement getAction() {
        return action;
    }

    public Optional<Statement> getIncrementAction() {
        return incrementAction;
    }

    private final Optional<Expression> entryCondition;
    private final Optional<Expression> exitCondition;
    private final Statement action;
    private final Optional<Statement> incrementAction;
    
}
