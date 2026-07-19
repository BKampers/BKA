package run.pascal;

import java.util.Optional;
import run.Engine;
import run.Expression;
import uml.structure.Type;


/**
 * Base class for Pascal expressions evaluated by {@link Engine}.
 */
public abstract class AbstractPascalExpression implements Expression {

    @Override
    public abstract java.lang.Object evaluate(Engine engine);

    @Override
    public abstract Optional<Type> getType();

}
