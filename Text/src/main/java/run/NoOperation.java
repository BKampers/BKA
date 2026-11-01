/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans, humanity, the environment or the universe.
 */
package run;


/**
 */
public final class NoOperation implements Statement {

    private NoOperation() {
        // One single INSTANCE available
    }

    public static NoOperation get() {
        return INSTANCE;
    }

    @Override
    public void execute(Execution execution, ObjectScope scope) {
        // No operation
    }

    @Override
    public String toString() {
        return "@No operation";
    }

    private static final NoOperation INSTANCE = new NoOperation();

}
