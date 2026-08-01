package run;


import java.util.*;
import uml.structure.*;


/**
 * Stateless executor for a compiled Pascal program.
 * Each {@link #execute()} call produces a new {@link Execution} with its own runtime state.
 */
public final class Engine {

    public Engine(uml.structure.Class programClass, Map<Operation, CompoundStatement> methods) {
        this.programClass = Objects.requireNonNull(programClass);
        this.methods = Map.copyOf(methods);
    }

    /**
     * Runs the main operation and returns the resulting execution state.
     */
    public Execution execute() {
        Execution execution = new Execution(this);
        execution.run();
        return execution;
    }

    uml.structure.Class getProgramClass() {
        return programClass;
    }

    Map<Operation, CompoundStatement> getMethods() {
        return methods;
    }

    private final uml.structure.Class programClass;
    private final Map<Operation, CompoundStatement> methods;

}
