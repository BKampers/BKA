/*
** © Bart Kampers
*/

package run;

import java.util.*;
import uml.annotation.*;
import uml.structure.*;


public class UmlAttributeFactory {

    private UmlAttributeFactory() {
        // Utility class should not be instantiated
    }

    public static Attribute createPrivate(String name, Type type, Type owner) {
        return createAttribute(Optional.of(name), Optional.of(type), Member.Visibility.PRIVATE, Objects.requireNonNull(owner), false, Collections.emptySet());
    }

    private static Attribute createAttribute(Optional<String> name, Optional<Type> type, Member.Visibility visibility, Type owner, boolean isClassScoped, Set<Stereotype> stereotypes) {
        return new Attribute() {

            @Override
            public Optional<String> getName() {
                return name;
            }

            @Override
            public Optional<Type> getType() {
                return type;
            }

            @Override
            public Member.Visibility getVisibility() {
                return visibility;
            }

            @Override
            public Type getOwner() {
                return owner;
            }

            @Override
            public boolean isClassScoped() {
                return isClassScoped;
            }

            @Override
            public Set<Stereotype> getStereotypes() {
                return stereotypes;
            }

            @Override
            public String toString() {
                return "(UML-Attribute: " + displayName() + ")";
            }

            private String displayName() {
                return ((name.isPresent()) ? name.get() : "anonymous") + ":" + ((type.isPresent()) ? type.toString() : "anonymous");
            }

        };
    }
}
