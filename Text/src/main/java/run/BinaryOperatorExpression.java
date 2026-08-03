package run;


/**
 * Binary operator expression used at runtime by {@link Execution}.
 */
public interface BinaryOperatorExpression extends Evaluable {

    Operator getOperator();

    Evaluable getLeft();

    Evaluable getRight();

}
