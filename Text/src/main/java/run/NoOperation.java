/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans, humanity, the environment or the universe.
 */
package run;


/**
 */
final class NoOperation implements Statement {

    public static final NoOperation INSTANCE = new NoOperation();

    private NoOperation() {
        // One single INSTANCE available
    }

    @Override
    public void execute(Execution execution, ObjectScope scope) {
        // No operation
    }

    @Override
    public String toString() {
        return "@No operation";
    }
}
