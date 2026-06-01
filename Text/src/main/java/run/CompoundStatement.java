package run;

import java.util.*;
import java.util.stream.*;


/**
 */
public class CompoundStatement implements Statement {

    public CompoundStatement(List<Statement> statements) {
        this.statements = List.copyOf(statements);
    }

    public List<Statement> getStatements() {
        return statements;
    }
    
    @Override
    public String toString() {
        return statements.stream().map(Objects::toString).collect(Collectors.joining(";\n\t", "{\n\t", "\n}"));
    }

    private final List<Statement> statements;

}
