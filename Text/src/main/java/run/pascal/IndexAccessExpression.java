package run.pascal;

import java.util.Objects;
import java.util.Optional;
import run.ArrayType;
import run.Execution;
import run.ObjectScope;
import uml.structure.Type;


/**
 * Indexed access to an array element.
 */
public final class IndexAccessExpression extends AbstractPascalExpression {

    public IndexAccessExpression(AbstractPascalExpression base, AbstractPascalExpression index) {
        this.base = Objects.requireNonNull(base);
        this.index = Objects.requireNonNull(index);
    }

    public AbstractPascalExpression getBase() {
        return base;
    }

    public AbstractPascalExpression getIndex() {
        return index;
    }

    public ArrayType getArrayType() {
        return (ArrayType) base.getType().get();
    }

    public static int arraySlot(ArrayType arrayType, int index) {
        return index - arrayType.getLowerBound();
    }

    @Override
    public Optional<Type> getType() {
        return Optional.of(getArrayType().getElementType());
    }

    @Override
    public java.lang.Object evaluate(Execution execution, ObjectScope scope) {
        java.lang.Object[] value = (java.lang.Object[]) execution.evaluate(base, scope);
        return value[arraySlot(getArrayType(), (Integer) execution.evaluate(index, scope))];
    }

    @Override
    public String toString() {
        return base + "[" + index + "]";
    }

    private final AbstractPascalExpression base;
    private final AbstractPascalExpression index;

}
