/*
** © Bart Kampers
*/

package bka.text.parser.pascal;

import com.fasterxml.jackson.databind.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.*;
import run.*;
import uml.statechart.*;
import uml.structure.*;

public class PascalCompilerTest {

    @BeforeEach
    public void init() {
        parser = new PascalParser(getPascalGrammar());
        compiler = new PascalCompiler();
    }

    private static Map<String, List<List<String>>> getPascalGrammar() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(Paths.get("resources/grammars/pascal.json").toFile(), Map.class);
        }
        catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Test
    public void testWhileLoop() throws StateMachineException {
        List<PascalParser.Node> tree = parser.buildTree("""
            PROGRAM while_loop;
            VAR i: INTEGER;
            BEGIN
                i := 8;
                WHILE i > 0 DO
                    i := i - 1;
            END.
            """);
        uml.structure.Class program = (uml.structure.Class) compiler.createObject(tree);
        List<Operation> operations = program.getOperations();
        Operation main = operations.getFirst();
        Collection<Transition<Event, GuardCondition, Action>> transitions = compiler.getMethod(main);
        StateMachine stateMachine = new StateMachine(transitions);
        stateMachine.start();
        assertEquals(0, stateMachine.getMemoryObject("i"));
    }

    @Test
    public void testThenBranch() throws StateMachineException {
        List<PascalParser.Node> tree = parser.buildTree("""
            PROGRAM then_branch;
            VAR i: INTEGER;
            BEGIN
                i := 8;
                IF i = 8 THEN
                    i := i DIV 2
            END.
            """);
        uml.structure.Class program = (uml.structure.Class) compiler.createObject(tree);
        List<Operation> operations = program.getOperations();
        Operation main = operations.getFirst();
        Collection<Transition<Event, GuardCondition, Action>> transitions = compiler.getMethod(main);
        StateMachine stateMachine = new StateMachine(transitions);
        stateMachine.start();
        assertEquals(4, stateMachine.getMemoryObject("i"));
    }

    @Test
    public void testSkipThenBranch() throws StateMachineException {
        List<PascalParser.Node> tree = parser.buildTree("""
            PROGRAM skip_then;
            VAR i: INTEGER;
            BEGIN
                i := 8;
                IF i <> 8 THEN
                    i := i DIV 2
            END.
            """);
        uml.structure.Class program = (uml.structure.Class) compiler.createObject(tree);
        List<Operation> operations = program.getOperations();
        Operation main = operations.getFirst();
        Collection<Transition<Event, GuardCondition, Action>> transitions = compiler.getMethod(main);
        StateMachine stateMachine = new StateMachine(transitions);
        stateMachine.start();
        assertEquals(8, stateMachine.getMemoryObject("i"));
    }

    @Test
    public void testElseBranch() throws StateMachineException {
        List<PascalParser.Node> tree = parser.buildTree("""
            PROGRAM else_branch;
            VAR i: INTEGER;
            BEGIN
                i := 8;
                IF i = 0 THEN
                    i := i DIV 2
                ELSE
                    i := i * 2
            END.
            """);
        uml.structure.Class program = (uml.structure.Class) compiler.createObject(tree);
        List<Operation> operations = program.getOperations();
        Operation main = operations.getFirst();
        Collection<Transition<Event, GuardCondition, Action>> transitions = compiler.getMethod(main);
        StateMachine stateMachine = new StateMachine(transitions);
        stateMachine.start();
        assertEquals(16, stateMachine.getMemoryObject("i"));
    }

    private PascalParser parser;
    private PascalCompiler compiler;

}
