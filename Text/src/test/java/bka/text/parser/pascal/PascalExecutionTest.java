/*
** © Bart Kampers
*/

package bka.text.parser.pascal;

import bka.text.parser.*;
import java.io.IOException;
import java.util.*;
import org.junit.jupiter.api.*;
import run.Engine;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PascalExecutionTest {

    @BeforeEach
    public void init() throws IOException {
        parser = new Parser(GrammarLoader.loadJsonFile("resources/grammars/pascal-grammar.json"));
        compiler = new PascalCompiler();
    }

    @Test
    public void testSimpleExpression() {
        Node tree = parser.parse("""
            PROGRAM simple_expression;
            VAR ONE, sum, product, expression, braces: INTEGER;
            VAR T0, F0, both, at_least_one, one_and_only_one: BOOLEAN;
            VAR equals, less_than, less_equal, greater_than, greater_equal, unequal: BOOLEAN;
            VAR r1, r2: REAL;
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
                r1 := 10.0;
                r2 := ONE;
                s := 'A string';
            END.
            """);
        execute(tree);
        assertEquals(1, engine.getVariableValue("one"));
        assertEquals(3, engine.getVariableValue("sum"));
        assertEquals(12, engine.getVariableValue("product"));
        assertEquals(26, engine.getVariableValue("expression"));
        assertEquals(70, engine.getVariableValue("braces"));
        assertEquals(false, engine.getVariableValue("f0"));
        assertEquals(true, engine.getVariableValue("t0"));
        assertEquals(false, engine.getVariableValue("both"));
        assertEquals(true, engine.getVariableValue("at_least_one"));
        assertEquals(false, engine.getVariableValue("one_and_only_one"));
        assertEquals(true, engine.getVariableValue("equals"));
        assertEquals(true, engine.getVariableValue("less_than"));
        assertEquals(true, engine.getVariableValue("less_equal"));
        assertEquals(false, engine.getVariableValue("greater_than"));
        assertEquals(false, engine.getVariableValue("greater_equal"));
        assertEquals(true, engine.getVariableValue("unequal"));
        assertEquals(10.0f, engine.getVariableValue("r1"));
        //assertEquals(1.0f, engine.getVariableValue("r2")); FIXME r2 evaluates to Integer 1
        assertEquals("A string", engine.getVariableValue("s"));
    }

    @Test
    public void testWhileLoop() {
        Node tree = parser.parse("""
            PROGRAM while_loop;
            VAR i: INTEGER;
            BEGIN
                i := 8;
                WHILE i > 0 DO
                    i := i - 1;
            END.
            """);
        execute(tree);
        assertEquals(0, engine.getVariableValue("i"));
    }

    @Test
    public void testThenBranch() {
        Node tree = parser.parse("""
            PROGRAM then_branch;
            VAR i: INTEGER;
            BEGIN
                i := 8;
                IF i = 8 THEN
                    i := i DIV 2
            END.
            """);
        execute(tree);
        assertEquals(4, engine.getVariableValue("i"));
    }

    @Test
    public void testSkipThenBranch() {
        Node tree = parser.parse("""
            PROGRAM skip_then;
            VAR i: INTEGER;
            BEGIN
                i := 8;
                IF i <> 8 THEN
                    i := i DIV 2
            END.
            """);
        execute(tree);
        assertEquals(8, engine.getVariableValue("i"));
    }

    @Test
    public void testElseBranch() {
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
        execute(tree);
        assertEquals(16, engine.getVariableValue("i"));
    }

    @Test
    public void testForLoop() {
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
        execute(tree);
        assertEquals(6, engine.getVariableValue("sum"));
        assertEquals(3, engine.getVariableValue("i"));
    }

    @Test
    public void testForLoopOneIteration() {
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
        execute(tree);
        assertEquals(1, engine.getVariableValue("sum"));
        assertEquals(1, engine.getVariableValue("i"));
    }

    @Test
    public void testForLoopNoIterations() {
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
        execute(tree);
        assertEquals(0, engine.getVariableValue("sum"));
        assertEquals(1, engine.getVariableValue("i"));
    }

    @Test
    public void testForLoopWithStep() {
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
        execute(tree);
        assertEquals(4, engine.getVariableValue("sum"));
        assertEquals(4, engine.getVariableValue("i"));
    }

    @Test
    public void testRepeatLoop() {
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
        execute(tree);
        assertEquals(7, engine.getVariableValue("sum"));
    }

    @Test
    public void testFunctionCallWithoutParameters() {
        Node tree = parser.parse("""
            PROGRAM function_call_without_parameters;
            VAR result : INTEGER;

            FUNCTION get_result: INTEGER;
                BEGIN
                get_result := $F;
                END;

            BEGIN
            result := get_result;
            get_result (* ignore return value *)
            END.
            """);
        execute(tree);
        assertEquals(0xF, engine.getVariableValue("result"));
    }

    @Test
    public void testFunctionCallWithParameters() {
        Node tree = parser.parse("""
            PROGRAM function_call_with_parameters;
            VAR result : REAL;

            FUNCTION sum(left: REAL; right: REAL): REAL;
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
        execute(tree);
        assertEquals(1001.0f, engine.getVariableValue("result"));
    }

    @Test
    public void testProcedureCallWithInputParameter() {
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
        execute(tree);
        assertEquals(20, engine.getVariableValue("result"));
    }

    @Test
    public void testProcedureCallWithInOutParameter() {
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
        execute(tree);
        assertEquals(11, engine.getVariableValue("result"));
    }

    @Test
    public void testLocalVariable() {
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
        execute(tree);
        assertEquals(144, engine.getVariableValue("result"));
    }

    @Test
    public void testArray() {
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
        execute(tree);
        assertArrayEquals(new java.lang.Object[]{1, 2}, (java.lang.Object[]) engine.getVariableValue("integers"));
        assertEquals(2, engine.getVariableValue("i"));
        assertEquals(1, engine.getVariableValue("p0"));
        assertEquals(2, engine.getVariableValue("p1"));
        assertArrayEquals(new java.lang.Object[]{1, 2}, (java.lang.Object[]) engine.getVariableValue("p"));
    }

    @Test
    public void testRecord() {
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
        execute(tree);
        assertEquals(Map.of("x", 0.1f, "y", 0.2f), engine.getRecordValue("p1"));
        assertEquals(Map.of("x", -0.3f, "y", -0.4f), engine.getRecordValue("p2"));
        assertEquals(0.1f, engine.getVariableValue("x"));
    }

    @Test
    public void testRecordArray() {
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
        execute(tree);
        assertArrayEquals(
            new java.lang.Object[]{Map.of("x", 1.0f, "y", 2.0f), Map.of("x", 3.0f, "y", 4.0f)},
            recordArray(engine.getVariableValue("line")));
        assertEquals(1.0f, engine.getVariableValue("x0"));
        assertEquals(2.0f, engine.getVariableValue("y0"));
        assertEquals(3.0f, engine.getVariableValue("x1"));
        assertEquals(4.0f, engine.getVariableValue("y1"));
    }

    @Test
    public void testRecordOfRecord() {
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
        execute(tree);
        assertEquals(
            Map.of("s", Map.of("x", 0.1f, "y", 0.2f), "e", Map.of("x", 0.3f, "y", 0.4f)),
            engine.getRecordValue("line"));
        assertEquals(0.1f, engine.getVariableValue("sx"));
        assertEquals(0.2f, engine.getVariableValue("sy"));
        assertEquals(0.3f, engine.getVariableValue("ex"));
        assertEquals(0.4f, engine.getVariableValue("ey"));
    }

    @Test
    public void testMatrix() {
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
        execute(tree);
        assertArrayEquals(new java.lang.Object[]{
            new java.lang.Object[]{0, 1, 2},
            new java.lang.Object[]{10, 11, 12},
            new java.lang.Object[]{20, 21, 22}
        }, (java.lang.Object[]) engine.getVariableValue("matrix"));
        assertEquals(0, engine.getVariableValue("m00"));
        assertEquals(1, engine.getVariableValue("m01"));
        assertEquals(2, engine.getVariableValue("m02"));
        assertEquals(10, engine.getVariableValue("m10"));
        assertEquals(11, engine.getVariableValue("m11"));
        assertEquals(12, engine.getVariableValue("m12"));
        assertEquals(20, engine.getVariableValue("m20"));
        assertEquals(21, engine.getVariableValue("m21"));
        assertEquals(22, engine.getVariableValue("m22"));
    }

    private void execute(Node tree) {
        uml.structure.Class program = compiler.createProgramClass(tree);
        engine = new Engine(program, compiler.getMethods());
        engine.execute();
    }

    private java.lang.Object[] recordArray(java.lang.Object value) {
        java.lang.Object[] array = (java.lang.Object[]) value;
        return Arrays.stream(array)
            .map(engine::toRecordMap)
            .toArray();
    }

    private Parser parser;
    private PascalCompiler compiler;
    private Engine engine;

}
