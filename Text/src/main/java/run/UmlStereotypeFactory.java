/*
** © Bart Kampers
*/

package run;

import java.util.*;
import java.util.stream.*;
import uml.annotation.*;


public class UmlStereotypeFactory {

    private UmlStereotypeFactory() {
        // Utility class should not be instantiated
    }

    public static Set<Stereotype> createStereotypes(String... names) {
        return Arrays.stream(names).map(UmlStereotypeFactory::createStereotype).collect(Collectors.toSet());
    }

    public static Stereotype createStereotype(String name) {
        return new Stereotype() {

            @Override
            public String getName() {
                return name;
            }

            @Override
            public boolean equals(Object object) {
                if (object instanceof Stereotype stereotype) {
                    return name.equals(stereotype.getName());
                }
                return false;
            }

            @Override
            public int hashCode() {
                return name.hashCode();
            }

            @Override
            public String toString() {
                return "UML-Stereotype: " + name;
            }

        };
    }

}
