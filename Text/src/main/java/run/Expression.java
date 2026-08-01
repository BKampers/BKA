package run;

import java.util.*;
import uml.structure.*;


/**
 * Executable runtime expression.
 */
public interface Expression extends ValueSpecification {

    Optional<Type> getType();

    java.lang.Object evaluate(Execution execution);

}
