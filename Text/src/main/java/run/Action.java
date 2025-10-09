/*
** © Bart Kampers
*/

package run;

import bka.text.parser.*;
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
                Optional<ParseTreeExpression> expression = statement.getExpressionTree();
                if (expression.isPresent()) {
                    Value value = expression.get().evaluate(memory);
                    Object result = value.get();
                    Optional<Node> assignable = statement.getAssignable();
                    if (assignable.isPresent()) {
                        memory.store(assignable.get().content(), result);
                    }
                }
            }

            @Override
            public String toString() {
                return String.format("Action (%s)", statement);
            }

        };
    }

}
