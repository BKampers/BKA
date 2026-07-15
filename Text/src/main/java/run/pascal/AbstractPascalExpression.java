package run.pascal;

import java.util.Optional;
import run.Engine;
import run.EngineExpression;
import run.Expression;
import uml.structure.Type;


/**
 * Base class for Pascal expressions evaluated by {@link Engine}.
 */
public abstract class AbstractPascalExpression extends Expression implements EngineExpression {

    @Override
    public abstract java.lang.Object evaluate(Engine engine);

    @Override
    public abstract Optional<Type> getType();

}
