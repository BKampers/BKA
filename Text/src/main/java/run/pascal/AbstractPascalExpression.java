package run.pascal;

import java.util.Optional;
import run.Execution;
import run.Expression;
import run.ObjectScope;
import uml.structure.Type;


/**
 * Base class for Pascal expressions evaluated by {@link Execution}.
 */
public abstract class AbstractPascalExpression implements Expression {

    @Override
    public abstract java.lang.Object evaluate(Execution execution, ObjectScope scope);

    @Override
    public abstract Optional<Type> getType();

}
