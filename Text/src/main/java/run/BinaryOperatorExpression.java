/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans, humanity, the environment or the universe.
*/

package run;


/**
 * Binary operator expression used at runtime by {@link Execution}.
 */
public interface BinaryOperatorExpression extends Evaluable {

    Operator getOperator();

    Evaluable getLeft();

    Evaluable getRight();

}
