/*
** © Bart Kampers
*/
package run;

import java.util.*;

public interface Value {

    Object evaluate() throws StateMachineException;

    String type();

    public static Value of(Executable executable, String type) {
        return createValue(Objects.requireNonNull(executable), Objects.requireNonNull(type));
    }

    private static Value createValue(Executable executable, String type) {
        return new Value() {

            @Override
            public Object evaluate() throws StateMachineException {
                return executable.perform();
            }

            @Override
            public String type() {
                return type;
            }

        };
    }

}
