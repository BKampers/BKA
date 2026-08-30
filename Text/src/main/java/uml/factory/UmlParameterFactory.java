/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans, humanity, the environment or the universe.
*/

package uml.factory;

import java.util.*;
import uml.structure.*;


/**
 */
public class UmlParameterFactory {

    private UmlParameterFactory() {
        // Utility class should not be instantiated
    }

    public static Parameter create(Parameter.Direction direction, Type type, String name) {
        return createParameter(Objects.requireNonNull(direction), Optional.of(type), Optional.of(name));
    }

    private static Parameter createParameter(Parameter.Direction direction, Optional<Type> type, Optional<String> name) {
        return new Parameter() {
            @Override
            public Parameter.Direction getDirection() {
                return direction;
            }

            @Override
            public Optional<Type> getType() {
                return type;
            }

            @Override
            public Optional<String> getName() {
                return name;
            }
        };
    }
}
