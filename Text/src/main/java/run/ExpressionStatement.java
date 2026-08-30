/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans, humanity, the environment or the universe.
*/

package run;

import java.util.*;
import java.util.logging.*;


/**
 * Statement with an evaluable expression and an optional assignable
 */
public final class ExpressionStatement implements Statement {

    public ExpressionStatement(Assignable assignable, Evaluable expression) {
        this(Optional.of(assignable), expression);
    }

    public ExpressionStatement(Evaluable expression) {
        this(Optional.empty(), expression);
    }

    private ExpressionStatement(Optional<Assignable> assignable, Evaluable expression) {
        this.assignable = assignable;
        this.expression = Objects.requireNonNull(expression);
    }

    public Optional<Assignable> getAssignable() {
        return assignable;
    }

    public Evaluable getExpression() {
        return expression;
    }

    @Override
    public void execute(Execution execution, ObjectScope scope) {
        java.lang.Object result = execution.evaluate(expression, scope);
        assignable.ifPresentOrElse(
            target -> target.assign(execution, result, scope),
            () -> Logger.getLogger(getClass().getName()).log(Level.INFO, "Return value of {0} ignored", this));
    }

    @Override
    public String toString() {
        if (assignable.isEmpty()) {
            return expression.toString();
        }
        return assignable.get().toString() + " <- " + expression.toString();
    }

    private final Optional<Assignable> assignable;
    private final Evaluable expression;
}
