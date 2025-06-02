package uml.structure;

import java.util.*;


public interface Class extends Type {

    List<Attribute> getAttributes();

    List<Operation> getOperations();
    
}
