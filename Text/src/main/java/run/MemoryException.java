/*
** © Bart Kampers
*/

package run;


public final class MemoryException extends Exception {

    public MemoryException(String message) {
        super(message);
    }

    public MemoryException(String message, Throwable cause) {
        super(message, cause);
    }

}
