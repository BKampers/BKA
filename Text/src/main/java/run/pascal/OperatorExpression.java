package run.pascal;

import static run.PascalTypes.BOOLEAN;
import static run.PascalTypes.INTEGER;
import static run.PascalTypes.REAL;

import java.util.Objects;
import java.util.Optional;
import run.BinaryOperatorExpression;
import run.Engine;
import run.Expression;
import run.Operator;
import uml.structure.Type;


/**
 * Binary operator expression.
 */
public final class OperatorExpression extends AbstractPascalExpression implements BinaryOperatorExpression {

    public OperatorExpression(AbstractPascalExpression left, Operator operator, AbstractPascalExpression right) {
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
    public Expression getLeft() {
        return left;
    }

    @Override
    public Expression getRight() {
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
    public java.lang.Object evaluate(Engine engine) {
        return switch (operator) {
            case EQUALS ->
                engine.evaluate(left).equals(engine.evaluate(right));
            case LESS_THAN ->
                ((Number) engine.evaluate(left)).doubleValue() < ((Number) engine.evaluate(right)).doubleValue();
            case GREATER_THAN ->
                ((Number) engine.evaluate(left)).doubleValue() > ((Number) engine.evaluate(right)).doubleValue();
            case LESS_EQUAL ->
                ((Number) engine.evaluate(left)).doubleValue() <= ((Number) engine.evaluate(right)).doubleValue();
            case GREATER_EQUAL ->
                ((Number) engine.evaluate(left)).doubleValue() >= ((Number) engine.evaluate(right)).doubleValue();
            case UNEQUALS ->
                !engine.evaluate(left).equals(engine.evaluate(right));
            case AND ->
                and(engine.evaluate(left), engine.evaluate(right));
            case OR ->
                or(engine.evaluate(left), engine.evaluate(right));
            case XOR ->
                xor(engine.evaluate(left), engine.evaluate(right));
            case MULTIPLICATION ->
                product(engine.evaluate(left), engine.evaluate(right));
            case REAL_DIVISION ->
                ((Number) engine.evaluate(left)).floatValue() / ((Number) engine.evaluate(right)).floatValue();
            case INTEGER_DIVISION ->
                (Integer) engine.evaluate(left) / (Integer) engine.evaluate(right);
            case MODULUS ->
                (Integer) engine.evaluate(left) % (Integer) engine.evaluate(right);
            case ADDITION ->
                sum(engine.evaluate(left), engine.evaluate(right));
            case SUBTRACTION ->
                difference(engine.evaluate(left), engine.evaluate(right));
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

    private final AbstractPascalExpression left;
    private final Operator operator;
    private final AbstractPascalExpression right;

}
