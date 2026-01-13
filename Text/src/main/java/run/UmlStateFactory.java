/*
** © Bart Kampers
*/

package run;

import java.util.*;
import uml.statechart.*;


public class UmlStateFactory {

    private UmlStateFactory() {
        // Utility class should not be instantiated
    }

    public static ActionState<Action> createActionState(Action action) {
        return createActionState(Optional.of(action));
    }

    private static ActionState<Action> createActionState(Optional<Action> action) {
        return new ActionState() {
            @Override
            public Optional<Action> getAction() {
                return action;
            }
            @Override
            public String toString() {
                return String.format("(UML-Action state %s)", action);
            }
        };
    }

    public static InitialState getInitialState() {
        return INITIAL_STATE;
    }

    public static FinalState getFinalState() {
        return FINAL_STATE;
    }

    private static final InitialState INITIAL_STATE = new InitialState() {
        @Override
        public String toString() {
            return "(UML-Initial State)";
        }
    };

    private static final FinalState FINAL_STATE = new FinalState() {
        @Override
        public String toString() {
            return "(UML-Final State)";
        }
    };

}
