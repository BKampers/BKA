/*
** © Bart Kampers
*/

package bka.text.parser.pascal;

import bka.text.parser.*;
import com.fasterxml.jackson.databind.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
        Node tree = parser.buildTree("""
            PROGRAM while_loop;
            VAR i: INTEGER;
            BEGIN
                i := 8;
                WHILE i > 0 DO
                    i := i - 1;
            END.
            """);
        StateMachine stateMachine = createStateMachine(tree);
        stateMachine.start();
        assertEquals(0, stateMachine.getMemoryObject("i"));
    }

    @Test
    public void testThenBranch() throws StateMachineException {
        Node tree = parser.buildTree("""
            PROGRAM then_branch;
            VAR i: INTEGER;
            BEGIN
                i := 8;
                IF i = 8 THEN
                    i := i DIV 2
            END.
            """);
        StateMachine stateMachine = createStateMachine(tree);
        stateMachine.start();
        assertEquals(4, stateMachine.getMemoryObject("i"));
    }

    @Test
    public void testSkipThenBranch() throws StateMachineException {
        Node tree = parser.buildTree("""
            PROGRAM skip_then;
            VAR i: INTEGER;
            BEGIN
                i := 8;
                IF i <> 8 THEN
                    i := i DIV 2
            END.
            """);
        StateMachine stateMachine = createStateMachine(tree);
        stateMachine.start();
        assertEquals(8, stateMachine.getMemoryObject("i"));
    }

    @Test
    public void testElseBranch() throws StateMachineException {
        Node tree = parser.buildTree("""
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
        StateMachine stateMachine = createStateMachine(tree);
        stateMachine.start();
        assertEquals(16, stateMachine.getMemoryObject("i"));
    }

    @Test
    public void testForLoop() throws StateMachineException {
        Node tree = parser.buildTree("""
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
        StateMachine stateMachine = createStateMachine(tree);
        stateMachine.start();
        assertEquals(6, stateMachine.getMemoryObject("sum"));
        assertEquals(4, stateMachine.getMemoryObject("i"));
    }

    @Test
    public void testRepeatLoop() throws StateMachineException {
        Node tree = parser.buildTree("""
            PROGRAM for_loop;
            VAR sum: INTEGER;
            BEGIN
                sum := 0;
                REPEAT
                    sum := sum + 1
                    UNTIL sum = 7
            END.
            """);
        StateMachine stateMachine = createStateMachine(tree);
        stateMachine.start();
        assertEquals(7, stateMachine.getMemoryObject("sum"));
    }

    @Test
    public void testFunctionCall() throws StateMachineException {
        Node tree = parser.buildTree("""
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
        StateMachine stateMachine = createStateMachine(tree);
        stateMachine.start();
        assertEquals(0xF, stateMachine.getMemoryObject("result"));
    }

//        @Test
    public void testProcedureCall() throws StateMachineException {
        Node tree = parser.buildTree("""
            PROGRAM procedure_call;
            VAR result : INTEGER;

            PROCEDURE compute(input: INTEGER);
                BEGIN
                result := input * 2;
                END;

            BEGIN
            compute(10);
            END.
            """);
        StateMachine stateMachine = createStateMachine(tree);
        stateMachine.start();
        assertEquals(20, stateMachine.getMemoryObject("result"));
    }

    private StateMachine createStateMachine(Node tree) throws StateMachineException {
        uml.structure.Class program = (uml.structure.Class) compiler.createProgramClass(tree);
        Collection<Transition<Event, GuardCondition, Action>> transitions = compiler.getMethod(getMain(program));
        return new StateMachine(transitions, getVariables(program));
    }

    private static Operation getMain(uml.structure.Class program) {
        return program.getOperations().stream()
            .filter(operation -> operation.getStereotypes().stream().anyMatch(stereotype -> "Main".equals(stereotype.getName())))
            .findAny().get();
    }

    private static Collection<String> getVariables(uml.structure.Class program) {
        return program.getAttributes().stream().map(attribute -> attribute.getName().get()).collect(Collectors.toList());
    }

    private PascalParser parser;
    private PascalCompiler compiler;

}
