package run;

import java.util.*;
import uml.structure.Object;


/**
 * Statement with an evaluable expression and an optional assignable
 */
public class Statement {

    public Statement(uml.structure.Object assignable, Expression expression) {
        this(Optional.of(assignable), expression);
    }

    public Statement(Expression expression) {
        this(Optional.empty(), expression);
    }

    private Statement(java.util.Optional<uml.structure.Object> assignable, Expression expression) {
        this.assignable = assignable;
        this.expression = Objects.requireNonNull(expression);
    }

    public Optional<Object> getAssignable() {
        return assignable;
    }

    public Expression getExpression() {
        return expression;
    }

    private final Optional<uml.structure.Object> assignable;
    private final Expression expression;
}
