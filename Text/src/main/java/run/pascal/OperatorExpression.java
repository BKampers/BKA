package run.pascal;

import static run.PascalTypes.BOOLEAN;
import static run.PascalTypes.INTEGER;
import static run.PascalTypes.REAL;

import java.util.Objects;
import java.util.Optional;
import run.*;
import uml.structure.*;


/**
 * Binary operator expression.
 */
public final class OperatorExpression implements BinaryOperatorExpression {

    public OperatorExpression(Evaluable left, Operator operator, Evaluable right) {
        if (left.getType().isEmpty() || right.getType().isEmpty()) {
            throw new IllegalArgumentException();
        }
        this.left = Objects.requireNonNull(left);
        this.operator = Objects.requireNonNull(operator);
        this.right = Objects.requireNonNull(right);
    }

    @Override
    public Operator getOperator() {
        return operator;
    }

    @Override
    public Evaluable getLeft() {
        return left;
    }

    @Override
    public Evaluable getRight() {
        return right;
    }

    @Override
    public Optional<Type> getType() {
        return switch (operator) {
            case EQUALS, LESS_THAN, GREATER_THAN, LESS_EQUAL, GREATER_EQUAL, UNEQUALS ->
                Optional.of(BOOLEAN);
            case AND, OR, XOR ->
                left.getType();
            case MULTIPLICATION, ADDITION, SUBTRACTION ->
                (INTEGER.equals(left.getType().get()) && INTEGER.equals(right.getType().get())) ? Optional.of(INTEGER) : Optional.of(REAL);
            case REAL_DIVISION ->
                Optional.of(REAL);
            case INTEGER_DIVISION, MODULUS ->
                Optional.of(INTEGER);
            default ->
                throw new IllegalStateException(String.format("Cannot determine type of %s %s %s", left.getType().get(), operator, right.getType().get()));
        };
    }

    @Override
    public java.lang.Object evaluate(Execution execution, ObjectScope scope) {
        return switch (operator) {
            case EQUALS ->
                execution.evaluate(left, scope).equals(execution.evaluate(right, scope));
            case LESS_THAN ->
                ((Number) execution.evaluate(left, scope)).doubleValue() < ((Number) execution.evaluate(right, scope)).doubleValue();
            case GREATER_THAN ->
                ((Number) execution.evaluate(left, scope)).doubleValue() > ((Number) execution.evaluate(right, scope)).doubleValue();
            case LESS_EQUAL ->
                ((Number) execution.evaluate(left, scope)).doubleValue() <= ((Number) execution.evaluate(right, scope)).doubleValue();
            case GREATER_EQUAL ->
                ((Number) execution.evaluate(left, scope)).doubleValue() >= ((Number) execution.evaluate(right, scope)).doubleValue();
            case UNEQUALS ->
                !execution.evaluate(left, scope).equals(execution.evaluate(right, scope));
            case AND ->
                and(execution.evaluate(left, scope), execution.evaluate(right, scope));
            case OR ->
                or(execution.evaluate(left, scope), execution.evaluate(right, scope));
            case XOR ->
                xor(execution.evaluate(left, scope), execution.evaluate(right, scope));
            case MULTIPLICATION ->
                product(execution.evaluate(left, scope), execution.evaluate(right, scope));
            case REAL_DIVISION ->
                ((Number) execution.evaluate(left, scope)).floatValue() / ((Number) execution.evaluate(right, scope)).floatValue();
            case INTEGER_DIVISION ->
                (Integer) execution.evaluate(left, scope) / (Integer) execution.evaluate(right, scope);
            case MODULUS ->
                (Integer) execution.evaluate(left, scope) % (Integer) execution.evaluate(right, scope);
            case ADDITION ->
                sum(execution.evaluate(left, scope), execution.evaluate(right, scope));
            case SUBTRACTION ->
                difference(execution.evaluate(left, scope), execution.evaluate(right, scope));
            default ->
                throw new IllegalStateException();
        };
    }

    private static java.lang.Object and(java.lang.Object left, java.lang.Object right) {
        if (left instanceof Boolean leftBoolean && right instanceof Boolean rightBoolean) {
            return leftBoolean && rightBoolean;
        }
        if (left instanceof Integer leftInteger && right instanceof Integer rightInteger) {
            return leftInteger & rightInteger;
        }
        throw new IllegalStateException();
    }

    private static java.lang.Object or(java.lang.Object left, java.lang.Object right) {
        if (left instanceof Boolean leftBoolean && right instanceof Boolean rightBoolean) {
            return leftBoolean || rightBoolean;
        }
        if (left instanceof Integer leftInteger && right instanceof Integer rightInteger) {
            return leftInteger | rightInteger;
        }
        throw new IllegalStateException();
    }

    private static java.lang.Object xor(java.lang.Object left, java.lang.Object right) {
        if (left instanceof Boolean leftBoolean && right instanceof Boolean rightBoolean) {
            return leftBoolean ^ rightBoolean;
        }
        if (left instanceof Integer leftInteger && right instanceof Integer rightInteger) {
            return leftInteger ^ rightInteger;
        }
        throw new IllegalStateException();
    }

    private static java.lang.Object product(java.lang.Object left, java.lang.Object right) {
        if (left instanceof Integer leftInteger && right instanceof Integer rightInteger) {
            return leftInteger * rightInteger;
        }
        if (left instanceof Number leftNumber && right instanceof Number rightNumber) {
            return leftNumber.floatValue() * rightNumber.floatValue();
        }
        throw new IllegalStateException();
    }

    private static java.lang.Object sum(java.lang.Object left, java.lang.Object right) {
        if (left instanceof Integer leftInteger && right instanceof Integer rightInteger) {
            return leftInteger + rightInteger;
        }
        if (left instanceof Number leftNumber && right instanceof Number rightNumber) {
            return leftNumber.floatValue() + rightNumber.floatValue();
        }
        throw new IllegalStateException();
    }

    private static java.lang.Object difference(java.lang.Object left, java.lang.Object right) {
        if (left instanceof Integer leftInteger && right instanceof Integer rightInteger) {
            return leftInteger - rightInteger;
        }
        if (left instanceof Number leftNumber && right instanceof Number rightNumber) {
            return leftNumber.floatValue() - rightNumber.floatValue();
        }
        throw new IllegalStateException();
    }

    @Override
    public String toString() {
        return "{" + left + " " + operator + " " + right + "}";
    }

    private final Evaluable left;
    private final Operator operator;
    private final Evaluable right;

}
