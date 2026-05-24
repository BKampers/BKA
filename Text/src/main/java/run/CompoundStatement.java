package run;

import java.util.*;


/**
 */
public class CompoundStatement implements Statement {

    public CompoundStatement(List<Statement> statements) {
        this.statements = List.copyOf(statements);
    }

    public List<Statement> getStatements() {
        return statements;
    }

    private final List<Statement> statements;

}
