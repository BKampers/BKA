package run;

import java.util.*;


/**
 * Statement with an evaluable expression and an optional assignable
 */
public final class ExpressionStatement implements Statement {

    public ExpressionStatement(Evaluable assignable, Evaluable expression) {
        this(Optional.of(assignable), expression);
    }

    public ExpressionStatement(Evaluable expression) {
        this(Optional.empty(), expression);
    }

    private ExpressionStatement(Optional<Evaluable> assignable, Evaluable expression) {
        this.assignable = assignable;
        this.expression = Objects.requireNonNull(expression);
    }

    public Optional<Evaluable> getAssignable() {
        return assignable;
    }

    public Evaluable getExpression() {
        return expression;
    }

    @Override
    public String toString() {
        if (assignable.isEmpty()) {
            return expression.toString();
        }
        return assignable.get().toString() + " <- " + expression.toString();
    }

    private final Optional<Evaluable> assignable;
    private final Evaluable expression;
}
