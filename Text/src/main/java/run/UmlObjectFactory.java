package run;

import java.util.*;
import uml.structure.*;


/**
 */
public class UmlObjectFactory {

    private UmlObjectFactory() {
        // Utility class should not be instantiated
    }

    public static uml.structure.Object create(String name, Type type) {
        return createObject(Optional.of(name), Optional.of(type), Collections.emptyMap());
    }

    private static uml.structure.Object createObject(Optional<String> name, Optional<Type> type, Map<Attribute, ValueSpecification> attributeValues) {
        final Map<Attribute, ValueSpecification> values = Map.copyOf(attributeValues);
        return new uml.structure.Object() {
            @Override
            public Optional<String> getName() {
                return name;
            }

            @Override
            public Optional<Type> getType() {
                return type;
            }

            @Override
            public Map<Attribute, ValueSpecification> getAttributeValues() {
                return values;
            }

        };
    }

}
