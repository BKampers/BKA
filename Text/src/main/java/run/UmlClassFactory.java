/*
** © Bart Kampers
*/

package run;

import java.util.*;
import uml.structure.*;


public class UmlClassFactory {

    private UmlClassFactory() {
        // Utility class should not be instantiated
    }

    public static uml.structure.Class create(String name, List<Attribute> attributes, List<Operation> operations) {
        return new uml.structure.Class() {

            @Override
            public Optional<String> getName() {
                return Optional.of(name);
            }

            @Override
            public List<Attribute> getAttributes() {
                return attributes;
            }

            @Override
            public List<Operation> getOperations() {
                return operations;
            }

            @Override
            public boolean isAbstract() {
                return false;
            }

            @Override
            public List<uml.structure.Class> getParents() {
                return Collections.emptyList();
            }

        };
    }

}
