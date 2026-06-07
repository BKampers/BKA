package run;

import java.util.*;
import java.util.stream.*;
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

    private static uml.structure.Object createObject(Optional<String> name, Optional<Type> type, Map<Attribute, Expression> attributeValues) {
        final Map<Attribute, uml.structure.Expression> values = attributeValues.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
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
            public Map<Attribute, uml.structure.Expression> getAttributeValues() {
                return values;
            }

        };
    }

}
