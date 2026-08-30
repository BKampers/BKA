/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans, humanity, the environment or the universe.
*/

package run;

import java.util.*;
import uml.factory.UmlTypeFactory;


/**
 */
public final class BranchStatement implements Statement {

    public static final Evaluable TRUE = new ValueExpression(true, UmlTypeFactory.create("Boolean"));

    public static BranchStatement ifStatement(Evaluable condition, Statement ifClause) {
        return new BranchStatement(condition, Map.of(TRUE, ifClause), Optional.empty());
    }

    public static BranchStatement ifStatement(Evaluable condition, Statement ifClause, Statement elseClause) {
        return new BranchStatement(condition, Map.of(TRUE, ifClause), Optional.of(elseClause));
    }

    public static BranchStatement caseStatement(Evaluable condition, Map<Evaluable, Statement> choices) {
        return new BranchStatement(condition, choices, Optional.empty());
    }

    public static BranchStatement caseStatement(Evaluable condition, Map<Evaluable, Statement> choices, Statement defaultChoice) {
        return new BranchStatement(condition, choices, Optional.of(defaultChoice));
    }

    private BranchStatement(Evaluable condition, Map<Evaluable, Statement> choices, Optional<Statement> defaultChoice) {
        this.condition = Objects.requireNonNull(condition);
        this.choices = Map.copyOf(choices);
        this.defaultChoice = defaultChoice;
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder("@On").append(condition);
        choices.forEach((expression, statement) ->
            string.append("\n  @If ").append(expression).append(": ").append(statement)
        );
        defaultChoice.ifPresent(statement -> string.append("\n  @Otherwise: ").append(statement));
        return string.toString();
    }

    public Evaluable getCondition() {
        return condition;
    }

    public Map<Evaluable, Statement> getChoices() {
        return choices;
    }

    public Optional<Statement> getDefaultChoice() {
        return defaultChoice;
    }

    public Optional<Statement> getIfClause() {
        return Optional.ofNullable(choices.get(TRUE));
    }

    @Override
    public void execute(Execution execution, ObjectScope scope) {
        java.lang.Object value = execution.evaluate(condition, scope);
        choices.entrySet().stream()
            .filter(choice -> Objects.equals(value, execution.evaluate(choice.getKey(), scope)))
            .map(Map.Entry::getValue)
            .findAny()
            .ifPresentOrElse(
                statement -> execution.execute(statement, scope),
                () -> defaultChoice.ifPresent(statement -> execution.execute(statement, scope)));
    }

    private final Evaluable condition;
    private final Map<Evaluable, Statement> choices;
    private final Optional<Statement> defaultChoice;

}
