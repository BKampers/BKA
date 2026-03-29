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
            VAR ONE, sum, product, expression, braces: INTEGER;
            VAR T0, F0, both, at_least_one, one_and_only_one: BOOLEAN;
            VAR equals, less_than, less_equal, greater_than, greater_equal, unequal: BOOLEAN;
            VAR r1: REAL;
            VAR s: STRING;
            BEGIN
                ONE := 1;
                sum := one + 2;
                product := sum * 4;
                expression := 2 * sum + 4 * 5;
                braces := 2 * (sum + 4) * 5;
                f0 := FALSE;
                t0 := NOT F0;
                both := t0 AND FALSE;
                at_least_one := t0 OR TRUE;
                one_and_only_one := FALSE OR t0 XOR TRUE;
                equals := one = sum - 2;
                less_than := 0 < -1 + 2;
                less_equal := one <= braces;
                greater_than := one > braces;
                greater_equal := one >= braces;
                unequal := one <> 2;
                r1:= 10.0;
                s := 'A string';
            END.
            """);
        StateMachine stateMachine = createStateMachine(tree);
        stateMachine.start();
        assertEquals(1, stateMachine.getMemoryObject("one"));
        assertEquals(3, stateMachine.getMemoryObject("sum"));
        assertEquals(12, stateMachine.getMemoryObject("product"));
        assertEquals(26, stateMachine.getMemoryObject("expression"));
        assertEquals(70, stateMachine.getMemoryObject("braces"));
        assertEquals(false, stateMachine.getMemoryObject("f0"));
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
        assertEquals(10.0f, stateMachine.getMemoryObject("r1"));
        assertEquals("A string", stateMachine.getMemoryObject("s"));
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
            TYPE Pair = ARRAY[0..1] OF INTEGER;
            VAR integers : ARRAY [0..1] OF INTEGER;
                p : Pair;
                i, p0, p1 : INTEGER;

            BEGIN
            i := 1;
            integers[0] := i;
            integers[I] := 2;
            i := integers[1];
            p := integers;
            p0 := integers[0];
            p1 := integers[1];
            END.
            """);
        uml.structure.Class program = (uml.structure.Class) compiler.createProgramClass(tree);
        Collection<Transition<Event, GuardCondition, Action>> transitions = compiler.getMethod(getMain(program));
        StateMachine stateMachine = new StateMachine(transitions, null, getVariableInitializations(program));
        stateMachine.start();
        assertArrayEquals(new java.lang.Object[]{1, 2}, (java.lang.Object[]) stateMachine.getMemoryObject("integers"));
        assertEquals(2, stateMachine.getMemoryObject("i"));
        assertEquals(1, stateMachine.getMemoryObject("p0"));
        assertEquals(2, stateMachine.getMemoryObject("p1"));
    }

    @Test
    public void testRecord() throws StateMachineException {
        Node tree = parser.parse("""
            PROGRAM record_var;

            TYPE point = RECORD
                x,y: REAL
                END;
            VAR p1: RECORD
                x,y: REAL
                END;
            var p2: Point;
            var x: real;

            BEGIN
            P1.x := 0.1;
            P1.y := 0.2;
            p2.X := -0.3;
            p2.Y := -0.4;
            X := p1.X;
            END.
            """);
        uml.structure.Class program = (uml.structure.Class) compiler.createProgramClass(tree);
        Collection<Transition<Event, GuardCondition, Action>> transitions = compiler.getMethod(getMain(program));
        StateMachine stateMachine = new StateMachine(transitions, null, getVariableInitializations(program));
        stateMachine.start();
        assertEquals(Map.of("x", 0.1f, "y", 0.2f), stateMachine.getMemoryObject("p1"));
        assertEquals(Map.of("x", -0.3f, "y", -0.4f), stateMachine.getMemoryObject("p2"));
        assertEquals(0.1f, stateMachine.getMemoryObject("x"));
    }

    @Test
    public void testRecordArray() throws StateMachineException {
        Node tree = parser.parse("""
            PROGRAM record_array;

            VAR line: ARRAY[0..1] OF RECORD
                x,y: REAL
                END;
            VAR x0,x1,y0,y1: REAL;

            BEGIN
            line[0].x := 1.0;
            line[0].y := 2.0;
            line[1].x := 3.0;
            line[1].y := 4.0;
            x0 := line[0].x;
            x1 := line[1].x;
            y0 := line[0].y;
            y1 := line[1].y;
            END.
            """);
        uml.structure.Class program = (uml.structure.Class) compiler.createProgramClass(tree);
        Collection<Transition<Event, GuardCondition, Action>> transitions = compiler.getMethod(getMain(program));
        StateMachine stateMachine = new StateMachine(transitions, null, getVariableInitializations(program));
        stateMachine.start();
        assertArrayEquals(new java.lang.Object[]{Map.of("x", 1.0f, "y", 2.0f), Map.of("x", 3.0f, "y", 4.0f)}, (java.lang.Object[]) stateMachine.getMemoryObject("line"));
        assertEquals(1.0f, stateMachine.getMemoryObject("x0"));
        assertEquals(2.0f, stateMachine.getMemoryObject("y0"));
        assertEquals(3.0f, stateMachine.getMemoryObject("x1"));
        assertEquals(4.0f, stateMachine.getMemoryObject("y1"));
    }

    @Test
    public void testRecordOfRecord() throws StateMachineException {
        Node tree = parser.parse("""
            PROGRAM record_record;

            TYPE point = RECORD
                x,y: REAL
                END;

            VAR line: RECORD
                s, e: point
                END;
             VAR sx, sy, ex, ey : REAL;

            BEGIN
            line.s.x := 0.1;
            line.s.y := 0.2;
            line.e.x := 0.3;
            line.e.y := 0.4;
            sx := line.s.x;
            sy := line.s.y;
            ex := line.e.x;
            ey:= line.e.y;
            END.
            """);
        uml.structure.Class program = (uml.structure.Class) compiler.createProgramClass(tree);
        Collection<Transition<Event, GuardCondition, Action>> transitions = compiler.getMethod(getMain(program));
        StateMachine stateMachine = new StateMachine(transitions, null, getVariableInitializations(program));
        stateMachine.start();
        assertEquals(Map.of("s", Map.of("x", 0.1f, "y", 0.2f), "e", Map.of("x", 0.3f, "y", 0.4f)), stateMachine.getMemoryObject("line"));
        assertEquals(0.1f, stateMachine.getMemoryObject("sx"));
        assertEquals(0.2f, stateMachine.getMemoryObject("sy"));
        assertEquals(0.3f, stateMachine.getMemoryObject("ex"));
        assertEquals(0.4f, stateMachine.getMemoryObject("ey"));
    }

    @Test
    public void testMatrix() throws StateMachineException {
        Node tree = parser.parse("""
            PROGRAM matrix_var;

            VAR matrix: ARRAY[0..2] OF ARRAY[0..2] OF INTEGER;
            VAR m00, m01, m02, m10, m11, m12, m20, m21, m22: INTEGER;

            BEGIN
            matrix[0][0] := 0;
            matrix[0][1] := 1;
            matrix[0][2] := 2;
            matrix[1][0] := 10;
            matrix[1][1] := 11;
            matrix[1][2] := 12;
            matrix[2][0] := 20;
            matrix[2][1] := 21;
            matrix[2][2] := 22;
            m00 := matrix[0][0];
            m01 := matrix[0][1];
            m02 := matrix[0][2];
            m10 := matrix[1][0];
            m11 := matrix[1][1];
            m12 := matrix[1][2];
            m20 := matrix[2][0];
            m21 := matrix[2][1];
            m22 := matrix[2][2];
            END.
            """);
        uml.structure.Class program = (uml.structure.Class) compiler.createProgramClass(tree);
        Collection<Transition<Event, GuardCondition, Action>> transitions = compiler.getMethod(getMain(program));
        StateMachine stateMachine = new StateMachine(transitions, null, getVariableInitializations(program));
        stateMachine.start();
        assertArrayEquals(new java.lang.Object[]{
            new java.lang.Object[]{0, 1, 2},
            new java.lang.Object[]{10, 11, 12},
            new java.lang.Object[]{20, 21, 22}
        }, (java.lang.Object[]) stateMachine.getMemoryObject("matrix"));
        assertEquals(0, stateMachine.getMemoryObject("m00"));
        assertEquals(1, stateMachine.getMemoryObject("m01"));
        assertEquals(2, stateMachine.getMemoryObject("m02"));
        assertEquals(10, stateMachine.getMemoryObject("m10"));
        assertEquals(11, stateMachine.getMemoryObject("m11"));
        assertEquals(12, stateMachine.getMemoryObject("m12"));
        assertEquals(20, stateMachine.getMemoryObject("m20"));
        assertEquals(21, stateMachine.getMemoryObject("m21"));
        assertEquals(22, stateMachine.getMemoryObject("m22"));
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
            attribute -> getObjectValue(attribute.getType().get())));
    }

    private static java.lang.Object getObjectValue(Type type) {
        if (type instanceof ArrayType arrayType) {
            return getArrayValue(arrayType);
        }
        if (type instanceof uml.structure.Class recordType) {
            return getRecordValue(recordType);
        }
        return EMPTY_VALUE;
    }

    private static java.lang.Object getArrayValue(ArrayType arrayType) {
        return IntStream.range(0, sizeOf(arrayType)).boxed()
            .map(i -> getObjectValue(arrayType.getElementType()))
            .toArray(java.lang.Object[]::new);
    }

    private static int sizeOf(ArrayType arrayType) {
        return arrayType.getUpperBound() - arrayType.getLowerBound() + 1;
    }

    private static Map<String, java.lang.Object> getRecordValue(uml.structure.Class recordType) {
        return recordType.getAttributes().stream().collect(Collectors.toMap(
            field -> field.getName().get(),
            field -> getObjectValue(field.getType().get())));
    }

    private Parser parser;
    private PascalCompiler compiler;

    private static final java.lang.Object EMPTY_VALUE = new java.lang.Object() {
        @Override
        public String toString() {
            return "@EmptyValue";
        }
    };

}
