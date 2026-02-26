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
        return createType(Optional.ofNullable(name), false, Optional.empty(), Collections.emptySet());
    }

    public static Type create(Multiplicity multiplicity) {
        return createType(Optional.empty(), false, Optional.of(multiplicity), Collections.emptySet());
    }

    public static Type create(String name, Multiplicity multiplicity) {
        return createType(Optional.ofNullable(name), false, Optional.of(multiplicity), Collections.emptySet());
    }

    private static Type createType(Optional<String> name, boolean isAbstract, Optional<Multiplicity> multiplicity, Set<Stereotype> stereotypes) {
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
            public Optional<Multiplicity> getMultiplicity() {
                return multiplicity;
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
                StringBuilder builder = new StringBuilder();
                if (name.isPresent()) {
                    builder.append('\'').append(name.get()).append('\'');
                }
                else {
                    builder.append("anonymous");
                }
                if (getMultiplicity().isPresent()) {
                    builder.append(getMultiplicity().get());
                }
                return builder.toString();
            }

        };
    }
}
