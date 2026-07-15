package run;

import java.util.*;
import uml.structure.*;


/**
 * A binary operator expression used at runtime by {@link Engine}.
 */
public interface BinaryOperatorExpression {

    Operator getOperator();

    Expression getLeft();

    Expression getRight();

    Optional<Type> getType();

}
