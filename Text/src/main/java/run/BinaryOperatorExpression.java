package run;


/**
 * Binary operator expression used at runtime by {@link Engine}.
 */
public interface BinaryOperatorExpression extends Expression {

    Operator getOperator();

    Expression getLeft();

    Expression getRight();

}
