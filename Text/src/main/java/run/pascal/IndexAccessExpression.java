package run.pascal;

import java.util.*;
import run.*;
import uml.structure.*;


/**
 * Indexed access to an array element.
 */
public final class IndexAccessExpression implements Evaluable {

    public IndexAccessExpression(Evaluable base, Evaluable index) {
        this.base = Objects.requireNonNull(base);
        this.index = Objects.requireNonNull(index);
    }

    public Evaluable getBase() {
        return base;
    }

    public Evaluable getIndex() {
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

    private final Evaluable base;
    private final Evaluable index;

}
