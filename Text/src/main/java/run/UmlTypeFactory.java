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
                return "(UML-Type: " + displayName() + ")";
            }

            private String displayName() {
                return (name.isPresent()) ? "'" + name.get() + "'" : "anonymous";
            }

        };
    }
}
