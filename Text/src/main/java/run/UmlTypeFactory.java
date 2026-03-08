/*
** © Bart Kampers
*/

package run;

import java.util.*;
import uml.annotation.*;
import uml.structure.*;


public class UmlTypeFactory {

    private UmlTypeFactory() {
        // Utility class should not be instantiated
    }

    public static Type create(String name) {
        return createType(Optional.ofNullable(name), false, Collections.emptySet());
    }

    public static String displayName(Type type) {
        if (type.getName().isEmpty()) {
            return "anonymous";
        }
        return "'" + type.getName().get() + "'";
    }

    private static Type createType(Optional<String> name, boolean isAbstract, Set<Stereotype> stereotypes) {
        return new Type() {
            @Override
            public Optional<String> getName() {
                return name;
            }

            @Override
            public boolean isAbstract() {
                return isAbstract;
            }

            @Override
            public Set<Stereotype> getStereotypes() {
                return stereotypes;
            }

            @Override
            public String toString() {
                return "(UML-Type: " + displayName(this) + ")";
            }

        };
    }
}
