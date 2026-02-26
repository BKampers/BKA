package uml.structure;

import java.util.*;


public interface Type extends Structural {
    
    boolean isAbstract();

    default Optional<Multiplicity> getMultiplicity() {
        return Optional.empty();
    }
    
}
