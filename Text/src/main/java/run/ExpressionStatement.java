package run;

import java.util.*;
import uml.structure.*;


/**
 * Statement with an evaluable expression and an optional assignable
 */
public class ExpressionStatement implements Statement {

    public ExpressionStatement(Expression assignable, Expression expression) {
        this(Optional.of(assignable), expression);
    }

    public ExpressionStatement(Expression expression) {
        this(Optional.empty(), expression);
    }

    private ExpressionStatement(Optional<Expression> assignable, Expression expression) {
        if (assignable.isPresent()) {
            if (!compatible(assignable.get().getType().get(), expression.getType().get())) {
                throw new IllegalArgumentException("Type mismatch " + assignable.get().getType() + " != " + expression.getType());
            }
        }
        this.assignable = assignable;
        this.expression = Objects.requireNonNull(expression);
    }

    private static boolean compatible(Type left, Type right) {
        if (left.equals(right)) {
            return true;
        }
        if (left instanceof ArrayType leftArray && right instanceof ArrayType rightArray) {
            return leftArray.getElementType().equals(rightArray.getElementType()) && leftArray.getLowerBound() == rightArray.getLowerBound() && leftArray.getUpperBound() == rightArray.getUpperBound();
        }
        return false;
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
