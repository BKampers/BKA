package run;

import java.util.*;
import java.util.stream.*;


/**
 */
public final class CompoundStatement implements Statement {

    public CompoundStatement(List<Statement> statements) {
        this(statements, List.of());
    }

    public CompoundStatement(List<Statement> statements, Collection<uml.structure.Object> locals) {
        this.statements = List.copyOf(statements);
        this.locals = List.copyOf(locals);
    }

    public List<Statement> getStatements() {
        return statements;
    }

    public List<uml.structure.Object> getLocals() {
        return locals;
    }
    
    @Override
    public String toString() {
        return statements.stream()
            .map(Objects::toString)
            .collect(Collectors.joining(";\n\t", "{\n\t", "\n}"));
    }

    private final List<Statement> statements;
    private final List<uml.structure.Object> locals;

}
