/*
** © Bart Kampers
*/

package bka.demo.graphs.history;

import java.util.*;
import java.util.function.*;


public class PropertyMutation<T> extends Mutation.Symmetrical {

    public PropertyMutation(Type type, Supplier<T> oldValue, Consumer<T> newValue, T historicValue) {
        this.type = Objects.requireNonNull(type);
        this.oldValue = Objects.requireNonNull(oldValue);
        this.newValue = Objects.requireNonNull(newValue);
        this.historicValue = historicValue;
    }

    public PropertyMutation(Type type, Supplier<T> oldValue, Consumer<T> newValue) {
        this(type, oldValue, newValue, oldValue.get());
    }

    @Override
    protected void revert() {
        T swapValue = oldValue.get();
        newValue.accept(historicValue);
        historicValue = swapValue;
    }

    @Override
    public Type getType() {
        return type;
    }

    private final Type type;
    private final Supplier<T> oldValue;
    private final Consumer<T> newValue;

    private T historicValue;

}
