/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans, humanity, the environment or the universe.
*/

package run;


/**
 * {@link Evaluable} that can appear on the left-hand side of an assignment.
 */
public interface Assignable extends Evaluable {

    void assign(Execution execution, java.lang.Object value, ObjectScope scope);

}
