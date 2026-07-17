package run.pascal;

import static run.PascalTypes.BOOLEAN;
import static run.PascalTypes.INTEGER;
import static run.PascalTypes.REAL;
import static run.PascalTypes.STRING;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import run.ArrayType;
import run.Engine;
import run.MutableObject;
import run.StateMachine;
import uml.structure.Attribute;
import uml.structure.Type;


/**
 * Factory methods for Pascal literal and value expressions.
 */
public final class PascalValues {

    public static AbstractPascalExpression valueOf(Type type, java.lang.Object value) {
        return new AbstractPascalExpression() {
            @Override
            public Optional<Type> getType() {
                return Optional.of(type);
            }

            @Override
            public java.lang.Object evaluate(Engine engine) {
                if (REAL.equals(type)) {
                    return ((Number) value).floatValue();
                }
                return value;
            }
        };
    }

    public static AbstractPascalExpression uninitialized(Type type) {
        return valueOf(type, initialValue(type));
    }

    public static java.lang.Object initialValue(Type type) {
        if (type instanceof ArrayType arrayType) {
            return createArrayValue(arrayType);
        }
        if (type instanceof uml.structure.Class recordType) {
            return createRecordValue(recordType);
        }
        return StateMachine.UNINITIALIZED;
    }

    public static AbstractPascalExpression intLiteral(int value) {
        return new AbstractPascalExpression() {
            @Override
            public Optional<Type> getType() {
                return Optional.of(INTEGER);
            }

            @Override
            public java.lang.Object evaluate(Engine engine) {
                return value;
            }

            @Override
            public String toString() {
                return Integer.toString(value);
            }
        };
    }

    public static AbstractPascalExpression realLiteral(float value) {
        return new AbstractPascalExpression() {
            @Override
            public Optional<Type> getType() {
                return Optional.of(REAL);
            }

            @Override
            public java.lang.Object evaluate(Engine engine) {
                return value;
            }

            @Override
            public String toString() {
                return Float.toString(value);
            }
        };
    }

    public static AbstractPascalExpression stringLiteral(String value) {
        return new AbstractPascalExpression() {
            @Override
            public Optional<Type> getType() {
                return Optional.of(STRING);
            }

            @Override
            public java.lang.Object evaluate(Engine engine) {
                return value;
            }

            @Override
            public String toString() {
                return "'" + value + "'";
            }
        };
    }

    public static AbstractPascalExpression booleanLiteral(boolean value) {
        return new AbstractPascalExpression() {
            @Override
            public Optional<Type> getType() {
                return Optional.of(BOOLEAN);
            }

            @Override
            public java.lang.Object evaluate(Engine engine) {
                return value;
            }

            @Override
            public String toString() {
                return Boolean.toString(value).toUpperCase();
            }
        };
    }

    private static MutableObject createRecordValue(uml.structure.Class recordType) {
        Map<Attribute, run.Expression> attributeValues = recordType.getAttributes().stream()
            .collect(Collectors.toMap(Function.identity(), attribute -> uninitialized(attribute.getType().get())));
        return MutableObject.constructAnonymous(recordType, attributeValues);
    }

    private static java.lang.Object[] createArrayValue(ArrayType arrayType) {
        return IntStream.range(0, arraySize(arrayType))
            .mapToObj(i -> initialValue(arrayType.getElementType()))
            .toArray();
    }

    private static int arraySize(ArrayType arrayType) {
        return arrayType.getUpperBound() - arrayType.getLowerBound() + 1;
    }

    private PascalValues() {
    }

}
