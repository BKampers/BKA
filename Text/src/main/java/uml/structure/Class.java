package uml.structure;

import java.util.*;


public interface Class extends Type {

    default List<uml.structure.Class> getParents() {
        return Collections.emptyList();
    }

    List<Attribute> getAttributes();

    List<Operation> getOperations();
    
}
