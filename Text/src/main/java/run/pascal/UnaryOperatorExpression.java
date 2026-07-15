package run.pascal;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import run.Engine;
import uml.structure.Type;


/**
 * Unary operator expression.
 */
public final class UnaryOperatorExpression extends AbstractPascalExpression {

    public UnaryOperatorExpression(UnaryOperator operator, AbstractPascalExpression expression) {
        this.operator = Objects.requireNonNull(operator);
        this.expression = Objects.requireNonNull(expression);
    }

    public UnaryOperator getUnaryOperator() {
        return operator;
    }

    public AbstractPascalExpression getExpression() {
        return expression;
    }

    @Override
    public Optional<Type> getType() {
        return expression.getType();
    }

    @Override
    public java.lang.Object evaluate(Engine engine) {
        return switch (operator) {
            case NEGATION ->
                minus((Number) engine.evaluate(expression));
            case NOT ->
                !(Boolean) engine.evaluate(expression);
        };
    }

    private static Number minus(Number value) throws IllegalStateException {
        if (value instanceof Integer) {
            return -value.intValue();
        }
        if (value instanceof Float) {
            return -value.floatValue();
        }
        throw new IllegalStateException();
    }

    @Override
    public String toString() {
        return String.format("%s %s", operator, expression);
    }

    public enum UnaryOperator {

        NEGATION("\\-"),
        NOT("NOT\\b");

        UnaryOperator(String symbol) {
            this.symbol = symbol;
        }

        public static UnaryOperator lookup(String symbol) {
            return Arrays.stream(values())
                .filter(operator -> symbol.equals(operator.symbol))
                .findAny()
                .orElseThrow(() -> new NoSuchElementException(symbol));
        }

        private final String symbol;
    }

    private final UnaryOperator operator;
    private final AbstractPascalExpression expression;

}
