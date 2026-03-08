package run;

import java.util.*;
import uml.structure.Type;


/**
 * Type definition of an array of an element Type with lower bound and upper bound
 */
public class ArrayType implements Type {

    public ArrayType(String name, Type elementType, int lowerBound, int upperBound) {
        this(Optional.of(name), elementType, lowerBound, upperBound);
    }

    public ArrayType(Type elementType, int lowerBound, int upperBound) {
        this(Optional.empty(), elementType, lowerBound, upperBound);
    }

    private ArrayType(Optional<String> name, Type elementType, int lowerBound, int upperBound) {
        if (upperBound < lowerBound) {
            throw new IllegalArgumentException(String.format("Invalid bounds: [%d..%d]", lowerBound, upperBound));
        }
        this.name = name;
        this.elementType = Objects.requireNonNull(elementType);
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    @Override
    public Optional<String> getName() {
        if (name.isPresent()) {
            return name;
        }
        StringBuilder builder = new StringBuilder();
        elementType.getName().ifPresent(builder::append);
        builder.append('[').append(lowerBound).append("..").append(upperBound).append(']');
        return Optional.of(builder.toString());
    }

    @Override
    public boolean isAbstract() {
        return false;
    }

    public Type getElementType() {
        return elementType;
    }

    public int getLowerBound() {
        return lowerBound;
    }

    public int getUpperBound() {
        return upperBound;
    }

    private final Optional<String> name;
    private final Type elementType;
    private final int lowerBound;
    private final int upperBound;

}
