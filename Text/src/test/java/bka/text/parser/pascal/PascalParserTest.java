package bka.text.parser.pascal;

import com.fasterxml.jackson.databind.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.*;

public class PascalParserTest {

    @Before
    public void init() {
        parser = new PascalParser(getPascalGrammar());
    }

    @Test
    public void testEmptyProgram() {
        List<PascalParser.Node> tree = parser.buildTree("PROGRAM program_name; BEGIN END.");
        assertParseTree(List.of(
            ExpectedNode.ofSymbolAndContent("PROGRAM"),
            ExpectedNode.ofSymbol("Identifier", "program_name",
                ExpectedNode.ofContent("program_name")),
            ExpectedNode.ofSymbolAndContent(";"),
            ExpectedNode.ofSymbol("Declarations", ""),
            ExpectedNode.ofSymbol("CompoundStatement", "BEGIN END",
                ExpectedNode.ofSymbolAndContent("BEGIN"),
                ExpectedNode.ofSymbol("Statements", "",
                    ExpectedNode.ofSymbol("Statement", "")),
                ExpectedNode.ofSymbolAndContent("END")),
            ExpectedNode.ofSymbol("\\.", ".")), tree);
    }

    @Test
    public void testVariableDeclarations() {
        List<PascalParser.Node> tree = parser.buildTree("""
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
        assertParseTree(List.of(
            ExpectedNode.ofSymbolAndContent("PROGRAM"),
            ExpectedNode.ofSymbol("Identifier", "program_name",
                ExpectedNode.ofContent("program_name")),
            ExpectedNode.ofSymbolAndContent(";"),
            ExpectedNode.ofSymbol("Declarations",
                ExpectedNode.ofSymbol("VariableDeclaration", "VAR bool: BOOLEAN;",
                    ExpectedNode.ofSymbolAndContent("VAR"),
                    ExpectedNode.ofSymbol("VariableDeclarationList", "bool: BOOLEAN;",
                        ExpectedNode.ofSymbol("VariableDeclarationExpression", "bool: BOOLEAN;",
                            ExpectedNode.ofSymbol("IdentifierList", "bool",
                                ExpectedNode.ofSymbol("Identifier", "bool",
                                    ExpectedNode.ofContent("bool"))),
                            ExpectedNode.ofSymbol("\\:", ":"),
                            ExpectedNode.ofSymbol("TypeDeclarationExpression", "BOOLEAN",
                                ExpectedNode.ofSymbol("TypeExpression", "BOOLEAN",
                                    ExpectedNode.ofSymbolAndContent("BOOLEAN"))),
                            ExpectedNode.ofSymbolAndContent(";")))),
                ExpectedNode.ofSymbol("Declarations",
                    ExpectedNode.ofSymbol("VariableDeclaration", "VAR byte, word: 0 .. 255;",
                        ExpectedNode.ofSymbolAndContent("VAR"),
                        ExpectedNode.ofSymbol("VariableDeclarationList", "byte, word: 0 .. 255;",
                            ExpectedNode.ofSymbol("VariableDeclarationExpression", "byte, word: 0 .. 255;",
                                ExpectedNode.ofSymbol("IdentifierList", "byte, word",
                                    ExpectedNode.ofSymbol("Identifier", "byte",
                                        ExpectedNode.ofContent("byte")),
                                    ExpectedNode.ofSymbol("\\,", ","),
                                    ExpectedNode.ofSymbol("IdentifierList",
                                        ExpectedNode.ofSymbol("Identifier", "word",
                                            ExpectedNode.ofContent("word")))),
                                ExpectedNode.ofSymbol("\\:", ":"),
                                ExpectedNode.ofSymbol("TypeDeclarationExpression", "0 .. 255",
                                    ExpectedNode.ofSymbol("RangeExpression", "0 .. 255",
                                        ExpectedNode.ofSymbol("IntegerLiteral", "0",
                                            ExpectedNode.ofContent("0")),
                                        ExpectedNode.ofSymbol("\\.\\.", ".."),
                                        ExpectedNode.ofSymbol("IntegerLiteral", "255",
                                            ExpectedNode.ofContent("255")))),
                                ExpectedNode.ofSymbolAndContent(";")))),
                    ExpectedNode.ofSymbol("Declarations",
                        ExpectedNode.ofSymbol("VariableDeclaration", "VAR point: RECORD\n    x: REAL;\n    y: REAL;\n    END;",
                            ExpectedNode.ofSymbolAndContent("VAR"),
                            ExpectedNode.ofSymbol("VariableDeclarationList", "point: RECORD\n    x: REAL;\n    y: REAL;\n    END;",
                                ExpectedNode.ofSymbol("VariableDeclarationExpression", "point: RECORD\n    x: REAL;\n    y: REAL;\n    END;",
                                    ExpectedNode.ofSymbol("IdentifierList", "point",
                                        ExpectedNode.ofSymbol("Identifier", "point",
                                            ExpectedNode.ofContent("point"))),
                                    ExpectedNode.ofSymbol("\\:", ":"),
                                    ExpectedNode.ofSymbol("TypeDeclarationExpression", "RECORD\n    x: REAL;\n    y: REAL;\n    END",
                                        ExpectedNode.ofSymbolAndContent("RECORD"),
                                        ExpectedNode.ofSymbol("VariableDeclarationList", "x: REAL;\n    y: REAL;",
                                            ExpectedNode.ofSymbol("VariableDeclarationExpression", "x: REAL;",
                                                ExpectedNode.ofSymbol("IdentifierList", "x",
                                                    ExpectedNode.ofSymbol("Identifier", "x",
                                                        ExpectedNode.ofContent("x"))),
                                                ExpectedNode.ofSymbol("\\:", ":"),
                                                ExpectedNode.ofSymbol("TypeDeclarationExpression", "REAL",
                                                    ExpectedNode.ofSymbol("TypeExpression", "REAL",
                                                        ExpectedNode.ofSymbolAndContent("REAL"))),
                                                ExpectedNode.ofSymbolAndContent(";")),
                                            ExpectedNode.ofSymbol("VariableDeclarationList", "y: REAL;",
                                                ExpectedNode.ofSymbol("VariableDeclarationExpression", "y: REAL;",
                                                    ExpectedNode.ofSymbol("IdentifierList", "y",
                                                        ExpectedNode.ofSymbol("Identifier", "y",
                                                            ExpectedNode.ofContent("y"))),
                                                    ExpectedNode.ofSymbol("\\:", ":"),
                                                    ExpectedNode.ofSymbol("TypeDeclarationExpression", "REAL",
                                                        ExpectedNode.ofSymbol("TypeExpression", "REAL",
                                                            ExpectedNode.ofSymbolAndContent("REAL"))),
                                                    ExpectedNode.ofSymbolAndContent(";")))),
                                        ExpectedNode.ofSymbolAndContent("END")),
                                    ExpectedNode.ofSymbolAndContent(";")))),
                        ExpectedNode.ofSymbol("Declarations",
                            ExpectedNode.ofSymbol("VariableDeclaration", "VAR fruit: ( apple, banana, cherry );",
                                ExpectedNode.ofSymbolAndContent("VAR"),
                                ExpectedNode.ofSymbol("VariableDeclarationList", "fruit: ( apple, banana, cherry );",
                                    ExpectedNode.ofSymbol("VariableDeclarationExpression", "fruit: ( apple, banana, cherry );",
                                        ExpectedNode.ofSymbol("IdentifierList", "fruit",
                                            ExpectedNode.ofSymbol("Identifier", "fruit",
                                                ExpectedNode.ofContent("fruit"))),
                                        ExpectedNode.ofSymbol("\\:", ":"),
                                        ExpectedNode.ofSymbol("TypeDeclarationExpression", "( apple, banana, cherry )",
                                            ExpectedNode.ofSymbol("\\(", "("),
                                            ExpectedNode.ofSymbol("IdentifierList", "apple, banana, cherry",
                                                ExpectedNode.ofSymbol("Identifier", "apple",
                                                    ExpectedNode.ofContent("apple")),
                                                ExpectedNode.ofSymbol("\\,", ","),
                                                ExpectedNode.ofSymbol("IdentifierList", "banana, cherry",
                                                    ExpectedNode.ofSymbol("Identifier", "banana",
                                                        ExpectedNode.ofContent("banana")),
                                                    ExpectedNode.ofSymbol("\\,", ","),
                                                    ExpectedNode.ofSymbol("IdentifierList", "cherry",
                                                        ExpectedNode.ofSymbol("Identifier", "cherry",
                                                            ExpectedNode.ofContent("cherry"))))),
                                            ExpectedNode.ofSymbol("\\)", ")")),
                                        ExpectedNode.ofSymbolAndContent(";")))),
                            ExpectedNode.ofSymbol("Declarations", ""))))),
            ExpectedNode.ofSymbol("CompoundStatement", "BEGIN\nEND",
                ExpectedNode.ofSymbolAndContent("BEGIN"),
                ExpectedNode.ofSymbol("Statements", "",
                    ExpectedNode.ofSymbol("Statement", "")),
                ExpectedNode.ofSymbolAndContent("END")),
            ExpectedNode.ofSymbol("\\.", ".")), tree);
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


    private static void assertParseTree(List<ExpectedNode> expected, List<PascalParser.Node> actual) {
        assertParseTree(null, expected, actual);
    }

    private static void assertParseTree(PascalParser.Node parent, List<ExpectedNode> expected, List<PascalParser.Node> actual) {
        int line = (parent == null) ? 0 : parent.startLine();
        assertEquals("Line " + line + ": Child count of '" + ((parent == null) ? "root" : parent.getSymbol()) + '\'', expected.size(), actual.size());
        for (int i = 0; i < expected.size(); ++i) {
            expected.get(i).assertParserNode(actual.get(i));
        }
    }


    private static class ExpectedNode {

        public static ExpectedNode ofSymbol(String symbol) {
            return new ExpectedNode(symbol, null, Collections.emptyList());
        }

        public static ExpectedNode ofSymbol(String symbol, String content) {
            return new ExpectedNode(symbol, content, Collections.emptyList());
        }

        public static ExpectedNode ofSymbolAndContent(String symbol) {
            return new ExpectedNode(symbol, symbol, Collections.emptyList());
        }

        public static ExpectedNode ofSymbol(String symbol, ExpectedNode... children) {
            return new ExpectedNode(symbol, null, Arrays.asList(children));
        }

        public static ExpectedNode ofSymbol(String symbol, String content, ExpectedNode... children) {
            return new ExpectedNode(symbol, content, Arrays.asList(children));
        }

        public static ExpectedNode ofContent(String content) {
            return new ExpectedNode(null, content, Collections.emptyList());
        }

        public ExpectedNode(String symbol, String content, List<ExpectedNode> children) {
            this.symbol = symbol;
            this.content = content;
            this.children = children;
        }

        public String getSymbol() {
            return symbol;
        }

        public String getContent() {
            return content;
        }

        public List<ExpectedNode> getChildren() {
            return children;
        }

        public void assertParserNode(PascalParser.Node node) {
            if (symbol != null) {
                assertEquals("Line " + node.startLine() + ": Symbol", symbol, node.getSymbol());
            }
            if (content != null) {
                assertEquals("Line " + node.startLine() + ": Content", content, node.content());
            }
            assertParseTree(node, children, node.getChildren());
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            if (symbol != null) {
                builder.append(symbol);
            }
            builder.append(" (").append(children.size()).append(')');
            if (content != null) {
                builder.append(" '").append(content).append('\'');
            }
            return builder.toString();
        }

        private final String symbol;
        private final String content;
        private final List<ExpectedNode> children;
    }


    private PascalParser parser;

}
