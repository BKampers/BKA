/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans, humanity, the environment or the universe.
*/

package run.pascal;

import java.util.*;
import run.*;
import uml.structure.*;


/**
 * Indexed access to an array element.
 */
public final class IndexAccessExpression implements Assignable {

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

    private static int arraySlot(ArrayType arrayType, int index) {
        return index - arrayType.getLowerBound();
    }

    @Override
    public Optional<Type> getType() {
        return Optional.of(getArrayType().getElementType());
    }

    @Override
    public java.lang.Object evaluate(Execution execution, ObjectScope scope) {
        return array(execution, scope)[slot(execution, scope)];
    }

    @Override
    public void assign(Execution execution, java.lang.Object value, ObjectScope scope) {
        array(execution, scope)[slot(execution, scope)] = value;
    }

    private java.lang.Object[] array(Execution execution, ObjectScope scope) {
        java.lang.Object value = execution.evaluate(base, scope);
        if (value instanceof java.lang.Object[] container) {
            return container;
        }
        throw new IllegalStateException("Not an array: " + base);
    }

    private int slot(Execution execution, ObjectScope scope) {
        return arraySlot(getArrayType(), (Integer) execution.evaluate(index, scope));
    }

    @Override
    public String toString() {
        return base + "[" + index + "]";
    }

    private final Evaluable base;
    private final Evaluable index;

}
