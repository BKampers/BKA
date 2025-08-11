/*
** © Bart Kampers
*/
package run;

import java.util.*;


public interface ParseTreeExpression extends uml.structure.Expression {

    String type();

    String value();

    Value evaluate(Memory memory) throws StateMachineException;

    public static ParseTreeExpression of(String type, String value, Evaluator evaluator) {
        return create(Objects.requireNonNull(type), Objects.requireNonNull(value), Objects.requireNonNull(evaluator));
    }

    private static ParseTreeExpression create(String type, String value, Evaluator evaluator) {
        return new ParseTreeExpression() {

            @Override
            public String type() {
                return type;
            }

            @Override
            public String value() {
                return value;
            }

            @Override
            public Value evaluate(Memory memory) throws StateMachineException {
                return Value.of(() -> evaluator.evaluate(memory), type);
            }

            @Override
            public String toString() {
                return value();
            }
        };
    }

}
