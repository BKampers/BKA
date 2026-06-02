package run;

import java.util.*;
import uml.structure.*;

/**
 */
public final class BranchStatement implements Statement {

    public static BranchStatement ifStatement(Expression condition, Statement ifClause) {
        return new BranchStatement(condition, Map.of(TRUE, ifClause), Optional.empty());
    }
    
    public static BranchStatement ifStatement(Expression condition, Statement ifClause, Statement elseClause) {
        return new BranchStatement(condition, Map.of(TRUE, ifClause), Optional.of(elseClause));
    }
    
    public static BranchStatement caseStatement(Expression condition, Map<Expression, Statement> choices) {
        return new BranchStatement(condition, choices, Optional.empty());
    }
    
    public static BranchStatement caseStatement(Expression condition, Map<Expression, Statement> choices, Statement defaultChoice) {
        return new BranchStatement(condition, choices, Optional.of(defaultChoice));
    }
    
    private BranchStatement(Expression condition, Map<Expression, Statement> choices, Optional<Statement> defaultChoice) {
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
    
    private final Expression condition;
    private final Map<Expression, Statement> choices;
    private final Optional<Statement> defaultChoice;
    
    private static final Expression TRUE = new Expression() {
        @Override
        public Optional<Type> getType() {
            return Optional.of(UmlTypeFactory.create("Boolean"));
        }
        @Override
        public String toString() {
            return "@TRUE";
        } 
    };
}
