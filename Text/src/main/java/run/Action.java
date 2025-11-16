/*
** © Bart Kampers
*/

package run;

import java.util.*;


public interface Action {

    void perform(Memory memory) throws StateMachineException;

    public static Action of(Statement statement) {
        return create(Objects.requireNonNull(statement));
    }

    private static Action create(Statement statement) {
        return new Action() {

            @Override
            public void perform(Memory memory) throws StateMachineException {
                statement.execute(memory);
            }

            @Override
            public String toString() {
                return String.format("Action (%s)", statement);
            }

        };
    }

}
