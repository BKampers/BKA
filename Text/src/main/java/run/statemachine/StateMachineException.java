/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans, humanity, the environment or the universe.
*/

package run.statemachine;


public class StateMachineException extends Exception {

    public StateMachineException(String message) {
        super(message);
    }

    public StateMachineException(Throwable cause) {
        super(cause);
    }

    public StateMachineException(String message, Throwable cause) {
        super(message, cause);
    }
}
