/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans, humanity, the environment or the universe.
*/

package uml.factory;

import java.util.*;
import uml.statechart.*;


public class UmlStateFactory {

    private UmlStateFactory() {
        // Utility class should not be instantiated
    }

    public static <A> ActionState<A> createActionState(A action) {
        return createActionState(Optional.of(action));
    }

    private static <A> ActionState<A> createActionState(Optional<A> action) {
        return new ActionState() {
            @Override
            public Optional<A> getAction() {
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
