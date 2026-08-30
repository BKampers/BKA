/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans, humanity, the environment or the universe.
*/

package uml.factory;

import java.util.*;
import uml.structure.*;


/**
 */
public class UmlMultiplicityFactory {

    public static Multiplicity createMultiplicity(int minimum, int maximum) {
        return create(minimum, OptionalInt.of(maximum));
    }

    public static Multiplicity createMultiplicity(int minimum) {
        return create(minimum, OptionalInt.empty());
    }

    private static Multiplicity create(int minimum, OptionalInt maximum) {
        return new Multiplicity() {
            @Override
            public int minimum() {
                return minimum;
            }

            @Override
            public OptionalInt maximum() {
                return maximum;
            }

            @Override
            public String toString() {
                StringBuilder builder = new StringBuilder();
                builder.append('[').append(minimum).append("..");
                if (maximum.isPresent()) {
                    builder.append(maximum);
                }
                else {
                    builder.append('*');
                }
                builder.append(']');
                return builder.toString();
            }

        };
    }

}
