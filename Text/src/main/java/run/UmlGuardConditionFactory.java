/*
** © Bart Kampers
*/

package run;

import uml.statechart.*;


public class UmlGuardConditionFactory {

    private UmlGuardConditionFactory() {
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
