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
    
    private final Optional<Expression> entryCondition;
    private final Optional<Expression> exitCondition;
    private final Statement action;
    private final Optional<Statement> incrementAction;
    
}
