/*
** © Bart Kampers
*/

package run;

import java.util.*;
import java.util.stream.*;
import uml.annotation.*;
import uml.structure.*;


public class UmlOperationFactory extends Factory {

    private UmlOperationFactory() {
        // Utility class should not be instantiated
    }

    public static Operation createPublicProcedure(String name, Type owner, Set<Stereotype> stereotypes) {
        return createOperation(Optional.of(name), Optional.empty(), Collections.emptyList(), Member.Visibility.PUBLIC, owner, false, false, unmodifiable(stereotypes));
    }

    public static Operation createPrivateFunction(String name, Type type, Type owner) {
        return createOperation(Optional.of(name), Optional.of(type), Collections.emptyList(), Member.Visibility.PRIVATE, owner, false, false, Collections.emptySet());
    }

    private static Operation createOperation(Optional<String> name, Optional<Type> type, List<Parameter> parameters, Member.Visibility visibility, Type owner, boolean isAbstract, boolean isClassScoped, Set<Stereotype> stereotypes) {
        return new Operation() {

            @Override
            public Optional<String> getName() {
                return name;
            }

            @Override
            public Optional<Type> getType() {
                return type;
            }

            @Override
            public List<Parameter> getParameters() {
                return parameters;
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
            public boolean isAbstract() {
                return isAbstract;
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
                return "(UML-Operation: " + displayName() + ")";
            }

            private String displayName() {
                return ((name.isPresent()) ? name.get() : "anonymous")
                    + parameters.stream().map(java.lang.Object::toString).collect(Collectors.joining(", ", "(", ")"))
                    + ":"
                    + ((type.isPresent()) ? type.toString() : "void");
            }

        };
    }
}
