/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans, humanity, the environment or the universe.
*/

package run;

/**
 */
public interface Statement {

    void execute(Execution execution, ObjectScope scope);

    Statement NO_OPERATION = new Statement() {
        @Override
        public void execute(Execution execution, ObjectScope scope) {
            // No operation
        }

        @Override
        public String toString() {
            return "@No operation";
        }
    };

}
