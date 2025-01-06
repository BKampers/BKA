package bka.text.parser.pascal;

import com.fasterxml.jackson.databind.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import static org.junit.Assert.assertTrue;
import org.junit.*;

public class PascalParserTest {

    @Before
    public void init() {
        parser = new PascalParser(getPascalGrammar());
    }

    @Test
    public void testEmptyProgram() {
        String output = parser.parse("PROGRAM program_name; BEGIN END.");
        assertSuccess(output);
    }

    @Test
    public void testVariableDeclarations() {
        String output = parser.parse("""
            PROGRAM program_name;
            VAR bool: BOOLEAN;
            VAR byte, word: 0 .. 255;
            VAR point: RECORD
                x: REAL;
                y: REAL;
                END;
            VAR fruit: ( apple, banana, cherry );
            BEGIN
            END.
            """);
        assertSuccess(output);
    }

    @Test
    public void testTypeDeclarations() {
        String output = parser.parse("""
            PROGRAM program_name;
            TYPE bool = BOOLEAN;
            TYPE byte = 0 .. 255;
            TYPE point = RECORD
                x: REAL;
                y: REAL;
                END;
            TYPE fruit = ( apple, banana, cherry );
            BEGIN
            END.
            """);
        System.out.println(output);
        assertSuccess(output);
    }

    @Test
    public void testProcedureDeclaration() {
        String output = parser.parse("""
            PROGRAM program_name;

            VAR result: INTEGER;

            PROCEDURE p1(VAR out: INTEGER; in: INTEGER);
                BEGIN
                out := in
                END;

            BEGIN
            p1(result, 0);
            END.
            """);
        assertSuccess(output);
    }

    @Test
    public void testFunctionDeclaration() {
        String output = parser.parse("""
            PROGRAM program_name;

            CONST pi = 3.14;

            VAR result : REAL;

            FUNCTION getPi : REAL;
                BEGIN
                getPi := pi
                END;

            BEGIN
            result := getPi;
            END.
            """);
        assertSuccess(output);
    }

    @Test
    public void testWhile() {
        String output = parser.parse("""
            PROGRAM program_name;
            VAR a: ARRAY [0 .. 9] OF INTEGER;
                i: INTEGER;
            BEGIN
                i := 0;
                WHILE i < 10 DO
                BEGIN
                    a[i] := i;
                    i := i + 1
                END
            END.
            """);
        assertSuccess(output);
    }

    @Test
    public void testFor() {
        String output = parser.parse("""
            PROGRAM program_name;
            VAR a: ARRAY [1 .. 10] OF INTEGER;
            VAR i: INTEGER;
            BEGIN
                FOR i := 1 TO 10 DO a[i] := i
            END.
            """);
        assertSuccess(output);
    }

    @Test
    public void testIfThenElse() {
        String output = parser.parse("""
            PROGRAM program_name;
            VAR i,j,s: INTEGER;
            BEGIN
                IF i = 0 THEN
                    BEGIN
                    s := 0;
                    END;
                IF i < 0 THEN
                    BEGIN
                    s := -1
                    END
                ELSE
                    BEGIN
                    s := 1;
                    END;
                IF s < 0 THEN
                    j := -i
                ELSE IF s = 0 THEN
                    j := 0
                ELSE
                    j := i;
            END.
            """);
        assertSuccess(output);
    }

    @Test
    public void testLiterals() {
        String output = parser.parse("""
            PROGRAM program_name;
            CONST float = 1.4e9;
            CONST greeting = 'Hello World!';
            CONST int = 1234567890;
            CONST yes = TRUE;
            CONST no = FALSE;
            BEGIN
            END.
            """);
        assertSuccess(output);
    }

    @Test
    public void testExpressions() {
        String output = parser.parse("""
            PROGRAM program_name;
            CONST c1 = 1 + 2;
            CONST c2 = -c1;
            CONST c3 = TRUE OR FALSE;
            CONST c4 = NOT c3;
            CONST c5 = (c1 - c2 < 0) OR c3 AND NOT c4;
            BEGIN
            END.
            """);
        assertSuccess(output);
    }

    @Test
    public void testComment() {
        String output = parser.parse("""
            (*
            ** A Pascal Program
            *)
            PROGRAM program_name;
            (* Main *)(* *)
            BEGIN
            END.
            (**)""");
        assertSuccess(output);
    }

    @Test
    public void testRangeExpressions() {
        String output = parser.parse("""
            PROGRAM program_name;
            CONST s = 0;
            CONST e = 9;
            VAR r1: 0 .. 7;
            VAR r2: 25.. 74;
            VAR r3: 1 ..12;
            VAR r4: 1024..2047;
            VAR r5: s .. e;
            VAR r6: s.. e;
            VAR r7: s ..e;
            VAR r8: s..e;
            BEGIN
            END.
            """);
        assertSuccess(output);
    }

    @Test
    public void testMissingProgramKeyword() {
        String output = parser.parse("""
            (*PROGRAM*)program_name;
            BEGIN
            END.
            """);
        System.out.println(output);
        assertError(output);
    }

    @Test
    public void testMissingProgramName() {
        String output = parser.parse("""
            PROGRAM (*program_name*);
            BEGIN
            END.
            """);
        assertError(output);
    }

    @Test
    public void testKeywordForProgramName() {
        String output = parser.parse("""
            PROGRAM case;
            BEGIN
            END.
            """);
        System.out.println(output);
        assertError(output);
    }

    @Test
    public void testMissingSemicolon() {
        String output = parser.parse("""
            PROGRAM program_name(*;*)
            BEGIN
            END.
            """);
        assertError(output);
    }

    @Test
    public void testMissingBegin() {
        String output = parser.parse("""
            PROGRAM program_name;
            (*BEGIN*)
            END.
            """);
        System.out.println(output);
        assertError(output);
    }

    @Test
    public void testMissingEnd() {
        String output = parser.parse("""
            PROGRAM program_name;
            BEGIN
            (*END*).
            """);
        assertError(output);
    }

    @Test
    public void testMissingEndDot() {
        String output = parser.parse("""
            PROGRAM program_name ;
            BEGIN
            END(*.*)
            """);
        assertError(output);
    }

    @Test
    public void testMissingBracket() {
        String output = parser.parse("""
            PROGRAM program_name;

            (* p1 *)
            PROCEDURE p1(VAR out: INTEGER; in: INTEGER(* ) *);
                BEGIN
                out := in
                END;

            BEGIN
            p1(result, 0);
            END.
            """);
        assertError(output);
    }

    private Map<String, List<List<String>>> getPascalGrammar() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(Paths.get("resources/grammars/pascal.json").toFile(), Map.class);
        }
        catch (IOException ex) {
            throw new IllegalStateException(ex);
        }

    }

    private static void assertSuccess(String output) {
        assertTrue("Parse error: " + output, output.startsWith("Program parsed successfully"));
    }

    private static void assertError(String output) {
        assertTrue(output.startsWith("Error"));
    }

    private PascalParser parser;

}
