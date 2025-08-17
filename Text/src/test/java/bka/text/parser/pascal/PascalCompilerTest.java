/*
** © Bart Kampers
*/

package bka.text.parser.pascal;

import bka.text.parser.*;
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
        List<Node> tree = parser.buildTree("""
            PROGRAM while_loop;
            VAR i: INTEGER;
            BEGIN
                i := 8;
                WHILE i > 0 DO
                    i := i - 1;
            END.
            """);
        uml.structure.Class program = (uml.structure.Class) compiler.createObject(tree);
        Collection<Transition<Event, GuardCondition, Action>> transitions = compiler.getMethod(getMain(program));
        StateMachine stateMachine = new StateMachine(transitions);
        stateMachine.start();
        assertEquals(0, stateMachine.getMemoryObject("i"));
    }

    @Test
    public void testThenBranch() throws StateMachineException {
        List<Node> tree = parser.buildTree("""
            PROGRAM then_branch;
            VAR i: INTEGER;
            BEGIN
                i := 8;
                IF i = 8 THEN
                    i := i DIV 2
            END.
            """);
        uml.structure.Class program = (uml.structure.Class) compiler.createObject(tree);
        Collection<Transition<Event, GuardCondition, Action>> transitions = compiler.getMethod(getMain(program));
        StateMachine stateMachine = new StateMachine(transitions);
        stateMachine.start();
        assertEquals(4, stateMachine.getMemoryObject("i"));
    }

    @Test
    public void testSkipThenBranch() throws StateMachineException {
        List<Node> tree = parser.buildTree("""
            PROGRAM skip_then;
            VAR i: INTEGER;
            BEGIN
                i := 8;
                IF i <> 8 THEN
                    i := i DIV 2
            END.
            """);
        uml.structure.Class program = (uml.structure.Class) compiler.createObject(tree);
        Collection<Transition<Event, GuardCondition, Action>> transitions = compiler.getMethod(getMain(program));
        StateMachine stateMachine = new StateMachine(transitions);
        stateMachine.start();
        assertEquals(8, stateMachine.getMemoryObject("i"));
    }

    @Test
    public void testElseBranch() throws StateMachineException {
        List<Node> tree = parser.buildTree("""
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
        Collection<Transition<Event, GuardCondition, Action>> transitions = compiler.getMethod(getMain(program));
        StateMachine stateMachine = new StateMachine(transitions);
        stateMachine.start();
        assertEquals(16, stateMachine.getMemoryObject("i"));
    }

    @Test
    public void testForLoop() throws StateMachineException {
        List<Node> tree = parser.buildTree("""
            PROGRAM for_loop;
            VAR i, sum: INTEGER;
            BEGIN
                sum := 0;
                FOR i := 1 TO 3 DO
                    BEGIN
                    sum := sum + i
                    END
            END.
            """);
        uml.structure.Class program = (uml.structure.Class) compiler.createObject(tree);
        Collection<Transition<Event, GuardCondition, Action>> transitions = compiler.getMethod(getMain(program));
        StateMachine stateMachine = new StateMachine(transitions);
        stateMachine.start();
        assertEquals(6, stateMachine.getMemoryObject("sum"));
        assertEquals(4, stateMachine.getMemoryObject("i"));
    }

    @Test
    public void testRepeatLoop() throws StateMachineException {
        List<Node> tree = parser.buildTree("""
            PROGRAM for_loop;
            VAR sum: INTEGER;
            BEGIN
                sum := 0;
                REPEAT
                    sum := sum + 1
                    UNTIL sum = 7
            END.
            """);
        uml.structure.Class program = (uml.structure.Class) compiler.createObject(tree);
        Collection<Transition<Event, GuardCondition, Action>> transitions = compiler.getMethod(getMain(program));
        StateMachine stateMachine = new StateMachine(transitions);
        stateMachine.start();
        assertEquals(7, stateMachine.getMemoryObject("sum"));
    }

    @Test
    public void testFunctionCall() throws StateMachineException {
        List<Node> tree = parser.buildTree("""
            PROGRAM function_call;
            VAR result : INTEGER;

            FUNCTION get_result: INTEGER;
                BEGIN
                get_result := $F;
                END;

            BEGIN
            result := get_result;
            END.
            """);
        uml.structure.Class program = (uml.structure.Class) compiler.createObject(tree);
        Collection<Transition<Event, GuardCondition, Action>> transitions = compiler.getMethod(getMain(program));
        StateMachine stateMachine = new StateMachine(transitions);
        stateMachine.start();
        assertEquals(0xF, stateMachine.getMemoryObject("result"));
    }

    private static Operation getMain(uml.structure.Class program) {
        return program.getOperations().stream()
            .filter(operation -> operation.getStereotypes().stream().anyMatch(stereotype -> "Main".equals(stereotype.getName())))
            .findAny().get();
    }

    private PascalParser parser;
    private PascalCompiler compiler;

}
