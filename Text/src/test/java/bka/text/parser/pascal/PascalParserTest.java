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
        System.out.println(output);
        assertSuccess(output);
    }

    @Test
    public void testProcedure() {
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
        System.out.println(output);
        assertSuccess(output);
    }

    @Test
    public void testFunction() {
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
        System.out.println(output);
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
                    a [i] := i + 1;
            END.
            """);
        System.out.println(output);
        assertSuccess(output);
    }

    @Test
    public void testFor() {
        String output = parser.parse("""
            PROGRAM program_name;
            VAR a: ARRAY [1 .. 10] OF INTEGER;
            VAR i: INTEGER;
            BEGIN
                FOR i := 1 TO 10 DO
                    BEGIN
                    a[i] := i
                    END
            END.
            """);
        System.out.println(output);
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
        System.out.println(output);
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
        System.out.println(output);
        assertSuccess(output);
    }

    @Test
    public void testComment() {
        String output = parser.parse("""
            (*
            ** A Pascal Program
            *)
            PROGRAM program_name;
            (* Main *)
            BEGIN
            END. (**)
            """);
        System.out.println(output);
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
        System.out.println(output);
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
        System.out.println(output);
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
        System.out.println(output);
        assertError(output);
    }

    @Test
    public void testMissingEndDot() {
        String output = parser.parse("""
            PROGRAM program_name ;
            BEGIN
            END(*.*)
            """);
        System.out.println(output);
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
        System.out.println(output);
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
        assertTrue(output.startsWith("Program parsed successfully"));
    }

    private static void assertError(String output) {
        assertTrue(output.startsWith("Error"));
    }

    private PascalParser parser;

    private static final String A_PROGRAM = """
        PROGRAM program_name;
            var a,b,c: integer;

            procedure procedure1
                var _1_: real;
                procedure nested
                begin
                end;
            begin
                _1_ := 1e+1 + 2.2
            end

           function function1 : integer
           begin

           end;

        BEGIN (* Main *)
        a:=3+3;
        b:=a-1;
        c:= b mod a;
        END. (**)
        """;

}
