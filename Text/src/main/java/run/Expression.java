/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans, humanity, the environment or the universe.
*/

package run;

import java.util.*;
import uml.structure.*;


/**
 * Typed runtime expression (structural). Not necessarily executable.
 */
public interface Expression extends ValueSpecification {

    Optional<Type> getType();

}
