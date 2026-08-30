/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans, humanity, the environment or the universe.
*/

package run;


/**
 * Executable {@link Expression}.
 */
public interface Evaluable extends Expression {

    java.lang.Object evaluate(Execution execution, ObjectScope scope);

}
