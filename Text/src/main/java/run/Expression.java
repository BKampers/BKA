package run;

import java.util.*;
import uml.structure.*;


/**
 * Typed runtime expression (structural). Not necessarily executable.
 */
public interface Expression extends ValueSpecification {

    Optional<Type> getType();

}
