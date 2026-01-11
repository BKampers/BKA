/*
** © Bart Kampers
*/

package bka.text.parser.pascal;

import bka.text.parser.*;
import java.io.IOException;
import java.util.*;
import java.util.stream.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import run.*;
import uml.statechart.*;
import uml.structure.*;

public class PascalCompilerTest {

    @BeforeEach
    public void init() throws IOException {
        parser = new Parser(GrammarLoader.loadJsonFile("resources/grammars/pascal-grammar.json"));
        compiler = new PascalCompiler();
    }

    @Test
    public void testSimpleExpression() throws StateMachineException {
        Node tree = parser.parse("""
            PROGRAM simple_expression;
            VAR one, sum, product, expression, braces: INTEGER;
            VAR t0, both, at_least_one, one_and_only_one: BOOLEAN;
            VAR equals, less_than, less_equal, greater_than, greater_equal, unequal: BOOLEAN;
            BEGIN
                one := 1;
                sum := one + 2;
                product := sum * 4;
                expression := 2 * sum + 4 * 5;
                braces := 2 * (sum + 4) * 5;
                t0 := TRUE;
                both := t0 AND FALSE;
                at_least_one := t0 OR TRUE;
                one_and_only_one := t0 XOR TRUE;
                equals := one = (sum - 2);
                less_than := 0 < (-1 + 2);
                less_equal := one <= braces;
                greater_than := one > braces;
                greater_equal := one >= braces;
                unequal := one <> 2
                                                                                                                                                                                    END.
            """);
        StateMachine stateMachine = createStateMachine(tree);
        stateMachine.start();
        assertEquals(1, stateMachine.getMemoryObject("one"));
        assertEquals(3, stateMachine.getMemoryObject("sum"));
        assertEquals(12, stateMachine.getMemoryObject("product"));
        assertEquals(26, stateMachine.getMemoryObject("expression"));
        assertEquals(70, stateMachine.getMemoryObject("braces"));
        assertEquals(true, stateMachine.getMemoryObject("t0"));
        assertEquals(false, stateMachine.getMemoryObject("both"));
        assertEquals(true, stateMachine.getMemoryObject("at_least_one"));
        assertEquals(false, stateMachine.getMemoryObject("one_and_only_one"));
        assertEquals(true, stateMachine.getMemoryObject("equals"));
        assertEquals(true, stateMachine.getMemoryObject("less_than"));
        assertEquals(true, stateMachine.getMemoryObject("less_equal"));
        assertEquals(false, stateMachine.getMemoryObject("greater_than"));
        assertEquals(false, stateMachine.getMemoryObject("greater_equal"));
        assertEquals(true, stateMachine.getMemoryObject("unequal"));
    }

    @Test
    public void testWhileLoop() throws StateMachineException {
        Node tree = parser.parse("""
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
        Node tree = parser.parse("""
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
        Node tree = parser.parse("""
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
        Node tree = parser.parse("""
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
        Node tree = parser.parse("""
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
        assertEquals(3, stateMachine.getMemoryObject("i"));
    }

    @Test
    public void testForLoopOneIteration() throws StateMachineException {
        Node tree = parser.parse("""
            PROGRAM for_loop_one_iteration;
            VAR i, sum: INTEGER;
            BEGIN
                sum := 0;
                FOR i := 1 TO 1 DO
                    BEGIN
                    sum := sum + i
                    END
            END.
            """);
        StateMachine stateMachine = createStateMachine(tree);
        stateMachine.start();
        assertEquals(1, stateMachine.getMemoryObject("sum"));
        assertEquals(1, stateMachine.getMemoryObject("i"));
    }

    @Test
    public void testForLoopNoIterations() throws StateMachineException {
        Node tree = parser.parse("""
            PROGRAM for_loop_no_iterations;
            VAR i, sum: INTEGER;
            BEGIN
                sum := 0;
                FOR i := 1 TO 0 DO
                    BEGIN
                    sum := sum + i
                    END
            END.
            """);
        StateMachine stateMachine = createStateMachine(tree);
        stateMachine.start();
        assertEquals(0, stateMachine.getMemoryObject("sum"));
        assertEquals(1, stateMachine.getMemoryObject("i"));
    }

    @Test
    public void testForLoopWithStep() throws StateMachineException {
        Node tree = parser.parse("""
            PROGRAM for_loop_with_step;
            VAR i, sum: INTEGER;
            BEGIN
                sum := 0;
                FOR i := 1 TO 3 DO
                    BEGIN
                    sum := sum + i;
                    i := i + 1
                    END
            END.
            """);
        StateMachine stateMachine = createStateMachine(tree);
        stateMachine.start();
        assertEquals(4, stateMachine.getMemoryObject("sum"));
        assertEquals(4, stateMachine.getMemoryObject("i"));
    }

    @Test
    public void testRepeatLoop() throws StateMachineException {
        Node tree = parser.parse("""
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
    public void testFunctionCallWithoutParameters() throws StateMachineException {
        Node tree = parser.parse("""
            PROGRAM function_call_without_parameters;
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

    @Test
    public void testFunctionCallWithParameters() throws StateMachineException {
        Node tree = parser.parse("""
            PROGRAM function_call_with_parameters;
            VAR result : INTEGER;

            FUNCTION sum(left: INTEGER; right: INTEGER): INTEGER;
                BEGIN
                sum := left + right
                END;

            FUNCTION one : INTEGER;
                BEGIN
                one := 1
                END;

            BEGIN
            result := sum(1000, one);
            END.
            """);
        StateMachine stateMachine = createStateMachine(tree);
        stateMachine.start();
        assertEquals(1001, stateMachine.getMemoryObject("result"));
    }

    @Test
    public void testProcedureCallWithInputParameter() throws StateMachineException {
        Node tree = parser.parse("""
            PROGRAM procedure_call_with_input;
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

    @Test
    public void testProcedureCallWithInOutParameter() throws StateMachineException {
        Node tree = parser.parse("""
            PROGRAM procedure_call_with_inout;
            VAR result : INTEGER;

            PROCEDURE increase(VAR inout: INTEGER);
                BEGIN
                inout := inout + 1;
                END;

            BEGIN
            result := 10;
            increase(result);
            END.
            """);
        StateMachine stateMachine = createStateMachine(tree);
        stateMachine.start();
        assertEquals(11, stateMachine.getMemoryObject("result"));
    }

    @Test
    public void testLocalVariable() throws StateMachineException {
        Node tree = parser.parse("""
            PROGRAM procedure_call_local;
            VAR result : INTEGER;

            PROCEDURE task;
                VAR local: INTEGER;
                BEGIN
                local := result;
                result := local * local
                END;

            BEGIN
            result := 12;
            task
            END.
            """);
        StateMachine stateMachine = createStateMachine(tree);
        stateMachine.start();
        assertEquals(144, stateMachine.getMemoryObject("result"));
    }

    @Test
    public void testArray() throws StateMachineException {
        Node tree = parser.parse("""
            PROGRAM array_var;
            VAR integers : ARRAY [0..1] OF INTEGER;
                i : INTEGER;

            BEGIN
            i := 1;
            integers[0] := i;
            integers[i] := 2;
            END.
            """);
        uml.structure.Class program = (uml.structure.Class) compiler.createProgramClass(tree);
        Collection<Transition<Event, GuardCondition, Action>> transitions = compiler.getMethod(getMain(program));
        StateMachine stateMachine = new StateMachine(transitions, null, getVariableInitializations(program));
        stateMachine.start();
        assertArrayEquals(new java.lang.Object[]{1, 2}, (java.lang.Object[]) stateMachine.getMemoryObject("integers"));
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

    private static Map<String, java.lang.Object> getVariableInitializations(uml.structure.Class program) {
        return program.getAttributes().stream().collect(Collectors.toMap(
            attribute -> attribute.getName().get(),
            attribute -> getValueObject(attribute.getType().get().getName().get())));
    }

    private static java.lang.Object getValueObject(String declaration) {
        if (declaration.contains("ARRAY")) {
            int openIndex = declaration.indexOf('[');
            int separatorIndex = declaration.indexOf("..", openIndex);
            int closeIndex = declaration.indexOf(']', separatorIndex);
            int lowerBound = Integer.parseInt(declaration.substring(openIndex + 1, separatorIndex).trim());
            int upperBound = Integer.parseInt(declaration.substring(separatorIndex + 2, closeIndex).trim());
            return new java.lang.Object[upperBound - lowerBound + 1];
        }
        return new java.lang.Object();
    }

    private Parser parser;
    private PascalCompiler compiler;

}
