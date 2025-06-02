/*
** © Bart Kampers
*/

package run;


public class StateMachineException extends Exception {

    public StateMachineException(String message) {
        super(message);
    }

    public StateMachineException(InterruptedException cause) {
        super(cause);
    }
}
