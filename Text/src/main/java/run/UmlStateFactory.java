/*
** © Bart Kampers
*/

package run;

import java.util.*;
import uml.statechart.*;
import uml.structure.*;


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

    public static Decision<Expression> createDecision(ParseTreeExpression expression) {
        Objects.requireNonNull(expression);
        return new Decision() {

            @Override
            public Expression getExpression() {
                return expression;
            }

            @Override
            public Optional<Type> getType() {
                return Optional.of(UmlTypeFactory.create(expression.type()));
            }

            @Override
            public Optional<String> getName() {
                String name = expression.toString();
                return Optional.of((name != null) ? name : expression.getClass().getName());
            }

            @Override
            public String toString() {
                return "(UML-Decision (" + typeString(getType()) + ") " + expression.value() + ")";
            }

            private String typeString(Optional<Type> type) {
                if (type.isEmpty()) {
                    return "Void";
                }
                if (type.get().getName().isEmpty()) {
                    return "Anonimous";
                }
                return type.get().getName().get();
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
