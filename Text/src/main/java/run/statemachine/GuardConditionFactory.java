/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans, humanity, the environment or the universe.
*/

package run.statemachine;

import uml.statechart.*;


public class GuardConditionFactory {

    private GuardConditionFactory() {
        // Utility class should not be instantiated
    }


    public static GuardCondition pass(Decision<Evaluator> decision) {
        return new GuardCondition() {
            @Override
            public boolean applies(Memory memory) throws StateMachineException {
                return requireBoolean(decision.getExpression(), memory);
            }

            @Override
            public String toString() {
                return "(UML-Guard Condition " + decision.getExpression().toString() + ")";
            }
        };
    }

    public static GuardCondition fail(Decision<Evaluator> decision) {
        return new GuardCondition() {
            @Override
            public boolean applies(Memory memory) throws StateMachineException {
                return !requireBoolean(decision.getExpression(), memory);

            }

            @Override
            public String toString() {
                return "(UML-Guard Condition \u00AC (" + decision.getExpression().toString() + "))";
            }
        };
    }

    private static boolean requireBoolean(Evaluator evaluator, Memory memory) throws StateMachineException {
        Object value = evaluator.evaluate(memory);
        if (evaluator.evaluate(memory) instanceof Boolean booleanValue) {
            return booleanValue;
        }
        throw new IllegalStateException("Not a boolean: " + value);
    }

}
