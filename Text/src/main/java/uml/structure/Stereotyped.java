package uml.structure;

import java.util.*;
import uml.annotation.*;


public interface Stereotyped {
    
    default Set<Stereotype> getStereotypes() {
        return Collections.emptySet();
    }
    
}
