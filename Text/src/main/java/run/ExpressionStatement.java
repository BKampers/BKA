package run;

import java.util.*;


/**
 * Statement with an evaluable expression and an optional assignable
 */
public final class ExpressionStatement implements Statement {

    public ExpressionStatement(Expression assignable, Expression expression) {
        this(Optional.of(assignable), expression);
    }

    public ExpressionStatement(Expression expression) {
        this(Optional.empty(), expression);
    }

    private ExpressionStatement(Optional<Expression> assignable, Expression expression) {
        this.assignable = assignable;
        this.expression = Objects.requireNonNull(expression);
    }

    public Optional<Expression> getAssignable() {
        return assignable;
    }

    public Expression getExpression() {
        return expression;
    }
    
    @Override 
    public String toString() {
        if (assignable.isEmpty()) {
            return expression.toString();
        }
        return assignable.get().toString() + " <- " + expression.toString();
    }

    private final Optional<Expression> assignable;
    private final Expression expression;
}
