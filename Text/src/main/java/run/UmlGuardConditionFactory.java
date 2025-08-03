/*
** © Bart Kampers
*/

package run;

import uml.statechart.*;


public class UmlGuardConditionFactory {

    private UmlGuardConditionFactory() {
        // Utility class should not be instantiated
    }


    public static GuardCondition pass(Decision decision) {
        return new GuardCondition() {
            @Override
            public boolean applies(Memory memory) throws StateMachineException {
                return Boolean.TRUE.equals(((ParseTreeExpression) decision.getExpression()).evaluate(memory).get());
            }

            @Override
            public String toString() {
                return "(UML-Guard Condition " + decision.getExpression().toString() + ")";
            }
        };
    }

    public static GuardCondition fail(Decision decision) {
        return new GuardCondition() {
            @Override
            public boolean applies(Memory memory) throws StateMachineException {
                return Boolean.FALSE.equals(((ParseTreeExpression) decision.getExpression()).evaluate(memory).get());
            }

            @Override
            public String toString() {
                return "(UML-Guard Condition \u00AC (" + decision.getExpression().toString() + "))";
            }
        };
    }

}
