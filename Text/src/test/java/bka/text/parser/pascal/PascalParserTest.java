package bka.text.parser.pascal;

import bka.text.parser.*;
import java.io.IOException;
import java.util.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


public class PascalParserTest {

    @BeforeEach
    public void init() throws IOException {
        parser = new Parser(GrammarLoader.loadJsonFile("resources/grammars/pascal-grammar.json"));
    }

    @Test
    public void testEmptyProgram() {
        assertParseTree(
            List.of(
                keyword("PROGRAM"),
                identifier("empty"),
                separator(),
                emptyNode("Declarations"),
                emptyBody(" "),
                endOfProgram()),
            parser.parse("PROGRAM empty; BEGIN END.").getChildren());
    }

    @Test
    public void testEnumerationTypeDefinition() {
        assertParseTree(
            List.of(
                keyword("PROGRAM"),
                identifier("program_name"),
                separator(),
                ExpectedNode.ofSymbol("Declarations", "TYPE fruit = ( apple, banana, cherry );\n",
                    enumerationTypeDefinition("fruit", List.of("apple", "banana", "cherry"), "TYPE fruit = ( apple, banana, cherry );"),
                    emptyNode("Declarations")),
                emptyBody(),
                endOfProgram()),
            parser.parse("""
                PROGRAM program_name;
                TYPE fruit = ( apple, banana, cherry );
                BEGIN
                END.
                """).getChildren());
    }

    @Test
    public void testRecordTypeDefinition() {
        assertParseTree(
            List.of(
                keyword("PROGRAM"),
                identifier("program_name"),
                separator(),
                ExpectedNode.ofSymbol("Declarations", "TYPE Point = RECORD\n    x: REAL;\n    y: REAL\n    END;\n\n",
                    recordTypeDefinition("Point", sequencedMapOf("x", "REAL", "y", "REAL"), "TYPE Point = RECORD\n    x: REAL;\n    y: REAL\n    END;"),
                    emptyNode("Declarations")),
                emptyBody(),
                endOfProgram()),
            parser.parse("""
                PROGRAM program_name;
                
                TYPE Point = RECORD
                    x: REAL;
                    y: REAL
                    END;

                BEGIN
                END.
                """).getChildren());
    }

    @Test
    public void testRangeVarDeclaration() {
        assertParseTree(List.of(keyword("PROGRAM"),
            identifier("byte_definition"),
            separator(),
            ExpectedNode.ofSymbol("Declarations", "VAR byte : 0..255;\n",
                ExpectedNode.ofSymbol("VariableDeclaration",
                    keyword("VAR"),
                    ExpectedNode.ofSymbol("VariableDeclarationList",
                        ExpectedNode.ofSymbol("VariableDeclarationExpression",
                            identifierList(List.of("byte")),
                            sign("\\:", ":"),
                            ExpectedNode.ofSymbol("TypeDeclarationExpression",
                                rangeExpression("0", "255")))),
                    separator()),
                emptyNode("Declarations")),
            emptyBody(),
            endOfProgram()),
            parser.parse("""
                PROGRAM byte_definition;
                VAR byte : 0..255;
                BEGIN
                END.
                """).getChildren());
    }

    private static ExpectedNode separator() {
        return ExpectedNode.ofSymbolAndContent(";");
    }

    private static ExpectedNode rangeExpression(String start, String end) {
        return ExpectedNode.ofSymbol("RangeExpression",
            integerLiteral(start),
            sign("\\.\\.", ".."),
            integerLiteral(end));
    }

    private <K, V> SequencedMap<K, V> sequencedMapOf(K k1, V v1, K k2, V v2) {
        SequencedMap<K, V> fields = new LinkedHashMap<>();
        fields.put(k1, v1);
        fields.put(k2, v2);
        return fields;
    }

    private static ExpectedNode emptyNode(String symbol) {
        return ExpectedNode.ofSymbol(symbol, "");
    }

    private static ExpectedNode keyword(String keyword) {
        return ExpectedNode.ofSymbol(keyword + "\\b", keyword);
    }

    private static ExpectedNode recordTypeDefinition(String typeName, SequencedMap<String, String> fields, String content) {
        return ExpectedNode.ofSymbol("TypeDeclaration", content,
            recordDefinition(typeName, fields, content.substring(content.indexOf("=") + 1, content.lastIndexOf(";")).trim()));
    }

    private static List<ExpectedNode> recordDefinition(String typeName, SequencedMap<String, String> fields, String content) {
        return List.of(
            keyword("TYPE"),
            identifier(typeName),
            sign("\\=", "="),
            ExpectedNode.ofSymbol("TypeDeclarationExpression", content,
                keyword("RECORD"),
                variableDeclarationList(fields),
                keyword("END")),
            separator());
    }

    private static ExpectedNode enumerationTypeDefinition(String typeName, List<String> values, String content) {
        return ExpectedNode.ofSymbol("TypeDeclaration", content,
            enumerationDefinition(typeName, values, content.substring(content.indexOf("=") + 1, content.lastIndexOf(";")).trim()));
    }

    private static List<ExpectedNode> enumerationDefinition(String typeName, List<String> values, String content) {
        return List.of(
            keyword("TYPE"),
            identifier(typeName),
            sign("\\=", "="),
            ExpectedNode.ofSymbol("TypeDeclarationExpression", content,
                sign("\\(", "("),
                identifierList(values),
                sign("\\)", ")")),
            separator());
    }

    private static ExpectedNode identifierList(List<String> identifiers) {
        if (identifiers.size() == 1) {
            return ExpectedNode.ofSymbol("IdentifierList", identifier(identifiers.getFirst()));
        }
        return ExpectedNode.ofSymbol("IdentifierList", identifier(identifiers.getFirst()), sign("\\,", ","), identifierList(identifiers.subList(1, identifiers.size())));
    }

    private static ExpectedNode variableDeclarationList(SequencedMap<String, String> variables) {
        List<ExpectedNode> declarations = new ArrayList<>();
        declarations.add(variableDeclaration(variables.firstEntry().getKey(), variables.firstEntry().getValue()));
        if (variables.size() > 1) {
            declarations.add(separator());
            LinkedHashMap<String, String> remainder = new LinkedHashMap<>(variables);
            remainder.remove(variables.firstEntry().getKey());
            declarations.add(variableDeclarationList(remainder));
        }
        return ExpectedNode.ofSymbol("VariableDeclarationList", declarations);
    }

    private static ExpectedNode variableDeclaration(String variableName, String variableType) {
        return variableDeclaration(variableName, variableType, null);
    }

    private static ExpectedNode variableDeclaration(String variableName, String variableType, String content) {
        return ExpectedNode.ofSymbol("VariableDeclarationExpression", content,
            ExpectedNode.ofSymbol("IdentifierList", variableName,
                identifier(variableName)),
            sign("\\:", ":"),
            ExpectedNode.ofSymbol("TypeDeclarationExpression", variableType,
                ExpectedNode.ofSymbol("TypeExpression", variableType,
                    keyword(variableType))));
    }

    private static ExpectedNode sign(String regex, String sign) {
        return ExpectedNode.ofSymbol(regex, sign);
    }

    private static ExpectedNode emptyBody() {
        return emptyBody("\n");
    }

    private static ExpectedNode emptyBody(String body) {
        return ExpectedNode.ofSymbol("CompoundStatement", "BEGIN" + body + "END",
            keyword("BEGIN"),
            ExpectedNode.ofSymbol("Statements", "",
                ExpectedNode.ofSymbol("Statement", "")),
            keyword("END"));
    }

    private static ExpectedNode identifier(String identifierName) {
        return ExpectedNode.ofSymbol("Identifier", identifierName,
            ExpectedNode.ofContent(identifierName));
    }

    private static ExpectedNode integerLiteral(String literal) {
        return ExpectedNode.ofSymbol("IntegerLiteral", literal,
            ExpectedNode.ofContent(literal));
    }

    private static ExpectedNode endOfProgram() {
        return ExpectedNode.ofSymbol("\\.", ".");
    }

    @Test
    public void testVariableDeclarations() {
        Node tree = parser.parse("""
            PROGRAM program_name;
            VAR bool: BOOLEAN;
            VAR byte, word: 0 .. 255;
            VAR point: RECORD
                x: REAL;
                y: REAL
                END;
            VAR fruit: ( apple, banana, cherry );
            BEGIN
            END.
            """);
        assertParseTree(
            List.of(
                keyword("PROGRAM"),
                ExpectedNode.ofSymbol("Identifier", "program_name",
                    ExpectedNode.ofContent("program_name")),
                separator(),
                ExpectedNode.ofSymbol("Declarations",
                    ExpectedNode.ofSymbol("VariableDeclaration", "VAR bool: BOOLEAN;",
                        keyword("VAR"),
                        ExpectedNode.ofSymbol("VariableDeclarationList", "bool: BOOLEAN",
                            ExpectedNode.ofSymbol("VariableDeclarationExpression", "bool: BOOLEAN",
                                ExpectedNode.ofSymbol("IdentifierList", "bool",
                                    ExpectedNode.ofSymbol("Identifier", "bool",
                                        ExpectedNode.ofContent("bool"))),
                                ExpectedNode.ofSymbol("\\:", ":"),
                                ExpectedNode.ofSymbol("TypeDeclarationExpression", "BOOLEAN",
                                    ExpectedNode.ofSymbol("TypeExpression", "BOOLEAN",
                                        keyword("BOOLEAN"))))),
                        separator()),
                    ExpectedNode.ofSymbol("Declarations",
                        ExpectedNode.ofSymbol("VariableDeclaration", "VAR byte, word: 0 .. 255;",
                            keyword("VAR"),
                            ExpectedNode.ofSymbol("VariableDeclarationList", "byte, word: 0 .. 255",
                                ExpectedNode.ofSymbol("VariableDeclarationExpression", "byte, word: 0 .. 255",
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
                                                ExpectedNode.ofContent("255")))))),
                            separator()),
                        ExpectedNode.ofSymbol("Declarations",
                            ExpectedNode.ofSymbol("VariableDeclaration", "VAR point: RECORD\n    x: REAL;\n    y: REAL\n    END;",
                                keyword("VAR"),
                                ExpectedNode.ofSymbol("VariableDeclarationList", "point: RECORD\n    x: REAL;\n    y: REAL\n    END",
                                    ExpectedNode.ofSymbol("VariableDeclarationExpression", "point: RECORD\n    x: REAL;\n    y: REAL\n    END",
                                        ExpectedNode.ofSymbol("IdentifierList", "point",
                                            ExpectedNode.ofSymbol("Identifier", "point",
                                                ExpectedNode.ofContent("point"))),
                                        ExpectedNode.ofSymbol("\\:", ":"),
                                        ExpectedNode.ofSymbol("TypeDeclarationExpression", "RECORD\n    x: REAL;\n    y: REAL\n    END",
                                            keyword("RECORD"),
                                            ExpectedNode.ofSymbol("VariableDeclarationList", "x: REAL;\n    y: REAL",
                                                ExpectedNode.ofSymbol("VariableDeclarationExpression", "x: REAL",
                                                    ExpectedNode.ofSymbol("IdentifierList", "x",
                                                        ExpectedNode.ofSymbol("Identifier", "x",
                                                            ExpectedNode.ofContent("x"))),
                                                    ExpectedNode.ofSymbol("\\:", ":"),
                                                    ExpectedNode.ofSymbol("TypeDeclarationExpression", "REAL",
                                                        ExpectedNode.ofSymbol("TypeExpression", "REAL",
                                                            keyword("REAL")))
                                                ),
                                                separator(),
                                                ExpectedNode.ofSymbol("VariableDeclarationList", "y: REAL",
                                                    ExpectedNode.ofSymbol("VariableDeclarationExpression", "y: REAL",
                                                        ExpectedNode.ofSymbol("IdentifierList", "y",
                                                            ExpectedNode.ofSymbol("Identifier", "y",
                                                                ExpectedNode.ofContent("y"))),
                                                        ExpectedNode.ofSymbol("\\:", ":"),
                                                        ExpectedNode.ofSymbol("TypeDeclarationExpression", "REAL",
                                                            ExpectedNode.ofSymbol("TypeExpression", "REAL",
                                                                keyword("REAL")))))),
                                            keyword("END")))),
                                separator()),
                            ExpectedNode.ofSymbol("Declarations",
                                ExpectedNode.ofSymbol("VariableDeclaration", "VAR fruit: ( apple, banana, cherry );",
                                    keyword("VAR"),
                                    ExpectedNode.ofSymbol("VariableDeclarationList", "fruit: ( apple, banana, cherry )",
                                        ExpectedNode.ofSymbol("VariableDeclarationExpression", "fruit: ( apple, banana, cherry )",
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
                                                ExpectedNode.ofSymbol("\\)", ")"))
                                        )), separator()),
                                ExpectedNode.ofSymbol("Declarations", ""))))),
                ExpectedNode.ofSymbol("CompoundStatement", "BEGIN\nEND",
                    keyword("BEGIN"),
                    ExpectedNode.ofSymbol("Statements", "",
                        ExpectedNode.ofSymbol("Statement", "")),
                    keyword("END")),
                ExpectedNode.ofSymbol("\\.", ".")),
            tree.getChildren());
    }

    @Test
    public void testTypeDeclarations() {
        assertSuccess(parser.parse("""
            PROGRAM program_name;
            TYPE bool = BOOLEAN;
            TYPE byte = 0 .. 255;
            TYPE point = RECORD
                x: REAL;
                y: REAL
                END;
            TYPE fruit = ( apple, banana, cherry );
            BEGIN
            END.
            """));
    }

    @Test
    public void testProcedureDeclaration() {
        assertSuccess(parser.parse("""
            PROGRAM program_name;

            VAR result: INTEGER;

            PROCEDURE p1(VAR out: INTEGER; in: INTEGER);
                BEGIN
                out := in
                END;

            BEGIN
            p1(result, 0);
            END.
            """));
    }

    @Test
    public void testFunctionDeclaration() {
        assertSuccess(parser.parse("""
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
            """));
    }

    @Test
    public void testWhile() {
        assertSuccess(parser.parse("""
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
            """));
    }

    @Test
    public void testFor() {
        assertSuccess(parser.parse("""
            PROGRAM program_name;
            VAR a: ARRAY [1 .. 10] OF INTEGER;
            VAR i: INTEGER;
            BEGIN
                FOR i := 1 TO 10 DO a[i] := i
            END.
            """));
    }

    @Test
    public void testIfThenElse() {
        assertSuccess(parser.parse("""
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
            """));
    }

    @Test
    public void testLiterals() {
        assertSuccess(parser.parse("""
            PROGRAM program_name;
            CONST float = 1.4e9;
            CONST greeting = 'Hello World!';
            CONST int = 1234567890;
            CONST yes = TRUE;
            CONST no = FALSE;
            BEGIN
            END.
            """));
    }

    @Test
    public void testExpressions() {
        assertParseTree(List.of(
            ExpectedNode.ofSymbol("UnaryOperator",
                ExpectedNode.ofContent("-")),
            ExpectedNode.ofSymbol("Comparable",
                ExpectedNode.ofSymbol("Identifier",
                    ExpectedNode.ofContent("c1")))),
            parser.parse("-c1", "Comparable").getChildren());
        assertParseTree(List.of(
            ExpectedNode.ofSymbol("Term",
                ExpectedNode.ofSymbol("Factor",
                    ExpectedNode.ofSymbol("Comparable",
                        ExpectedNode.ofSymbol("Identifier",
                            ExpectedNode.ofContent("a"))),
                    ExpectedNode.ofSymbol("RelationalOperator",
                        ExpectedNode.ofContent("=")),
                    ExpectedNode.ofSymbol("Comparable",
                        ExpectedNode.ofSymbol("Literal",
                            ExpectedNode.ofSymbol("IntegerLiteral",
                                ExpectedNode.ofContent("0"))))),
                ExpectedNode.ofSymbol("MultiplicativeOperation")),
            ExpectedNode.ofSymbol("AdditiveOperation")),
            parser.parse("a = 0", "Expression").getChildren());
        assertParseTree(List.of(
            ExpectedNode.ofSymbol("Term",
                ExpectedNode.ofSymbol("Factor",
                    ExpectedNode.ofSymbol("Comparable",
                        ExpectedNode.ofSymbol("Identifier",
                            ExpectedNode.ofContent("a")))),
                ExpectedNode.ofSymbol("MultiplicativeOperation",
                    ExpectedNode.ofSymbol("MultiplicativeOperator",
                        ExpectedNode.ofContent("/")),
                    ExpectedNode.ofSymbol("Factor",
                        ExpectedNode.ofSymbol("Comparable",
                            ExpectedNode.ofSymbol("Identifier",
                                ExpectedNode.ofContent("b")))),
                    ExpectedNode.ofSymbol("MultiplicativeOperation"))),
            ExpectedNode.ofSymbol("AdditiveOperation")),
            parser.parse("a / b", "Expression").getChildren());
        assertParseTree(List.of(
            ExpectedNode.ofSymbol("Term",
                ExpectedNode.ofSymbol("Factor",
                    ExpectedNode.ofSymbol("Comparable",
                        ExpectedNode.ofSymbol("Literal",
                            ExpectedNode.ofContent("TRUE")))),
                ExpectedNode.ofSymbol("MultiplicativeOperation")),
            ExpectedNode.ofSymbol("AdditiveOperation",
                ExpectedNode.ofSymbol("AdditiveOperator",
                    ExpectedNode.ofContent("OR")),
                ExpectedNode.ofSymbol("Term",
                    ExpectedNode.ofSymbol("Factor",
                        ExpectedNode.ofSymbol("Comparable",
                            ExpectedNode.ofSymbol("Literal",
                                ExpectedNode.ofContent("FALSE")))),
                    ExpectedNode.ofSymbol("MultiplicativeOperation")),
                ExpectedNode.ofSymbol("AdditiveOperation"))),
            parser.parse("TRUE OR FALSE", "Expression").getChildren());
        assertParseTree(List.of(
            ExpectedNode.ofSymbol("Term",
                ExpectedNode.ofSymbol("Factor",
                    ExpectedNode.ofSymbol("Comparable",
                        ExpectedNode.ofSymbol("Literal",
                            ExpectedNode.ofSymbol("IntegerLiteral",
                                ExpectedNode.ofContent("1"))))),
                ExpectedNode.ofSymbol("MultiplicativeOperation",
                    ExpectedNode.ofSymbol("MultiplicativeOperator",
                        ExpectedNode.ofContent("*")),
                    ExpectedNode.ofSymbol("Factor",
                        ExpectedNode.ofSymbol("Comparable",
                            ExpectedNode.ofSymbol("Literal",
                                ExpectedNode.ofSymbol("IntegerLiteral",
                                    ExpectedNode.ofContent("2"))))),
                    ExpectedNode.ofSymbol("MultiplicativeOperation"))),
            ExpectedNode.ofSymbol("AdditiveOperation",
                ExpectedNode.ofSymbol("AdditiveOperator",
                    ExpectedNode.ofContent("+")),
                ExpectedNode.ofSymbol("Term",
                    ExpectedNode.ofSymbol("Factor",
                        ExpectedNode.ofSymbol("Comparable",
                            ExpectedNode.ofSymbol("Literal",
                                ExpectedNode.ofSymbol("IntegerLiteral",
                                    ExpectedNode.ofContent("3"))))),
                    ExpectedNode.ofSymbol("MultiplicativeOperation",
                        ExpectedNode.ofSymbol("MultiplicativeOperator",
                            ExpectedNode.ofContent("*")),
                        ExpectedNode.ofSymbol("Factor",
                            ExpectedNode.ofSymbol("Comparable",
                                ExpectedNode.ofSymbol("Literal",
                                    ExpectedNode.ofSymbol("IntegerLiteral",
                                        ExpectedNode.ofContent("4"))))),
                        ExpectedNode.ofSymbol("MultiplicativeOperation"))),
                ExpectedNode.ofSymbol("AdditiveOperation"))),
            parser.parse("1*2+3*4", "Expression").getChildren());
        assertParseTree(List.of(
            ExpectedNode.ofSymbol("Term",
                ExpectedNode.ofSymbol("Factor",
                    ExpectedNode.ofSymbol("Comparable",
                        ExpectedNode.ofSymbol("Identifier",
                            ExpectedNode.ofContent("a")))),
                ExpectedNode.ofSymbol("MultiplicativeOperation",
                    ExpectedNode.ofSymbol("MultiplicativeOperator",
                        ExpectedNode.ofContent("*")),
                    ExpectedNode.ofSymbol("Factor",
                        ExpectedNode.ofSymbol("Comparable",
                            ExpectedNode.ofContent("("),
                            ExpectedNode.ofSymbol("Expression",
                                ExpectedNode.ofSymbol("Term",
                                    ExpectedNode.ofSymbol("Factor",
                                        ExpectedNode.ofSymbol("Comparable",
                                            ExpectedNode.ofSymbol("Identifier",
                                                ExpectedNode.ofContent("b")))),
                                    ExpectedNode.ofSymbol("MultiplicativeOperation")),
                                ExpectedNode.ofSymbol("AdditiveOperation",
                                    ExpectedNode.ofSymbol("AdditiveOperator",
                                        ExpectedNode.ofContent("+")),
                                    ExpectedNode.ofSymbol("Term",
                                        ExpectedNode.ofSymbol("Factor",
                                            ExpectedNode.ofSymbol("Comparable",
                                                ExpectedNode.ofSymbol("Identifier",
                                                    ExpectedNode.ofContent("c")))),
                                        ExpectedNode.ofSymbol("MultiplicativeOperation")),
                                    ExpectedNode.ofSymbol("AdditiveOperation"))),
                            ExpectedNode.ofContent(")"))),
                    ExpectedNode.ofSymbol("MultiplicativeOperation",
                        ExpectedNode.ofSymbol("MultiplicativeOperator",
                            ExpectedNode.ofContent("*")),
                        ExpectedNode.ofSymbol("Factor",
                            ExpectedNode.ofSymbol("Comparable",
                                ExpectedNode.ofSymbol("Identifier",
                                    ExpectedNode.ofContent("d")))),
                        ExpectedNode.ofSymbol("MultiplicativeOperation")))),
            ExpectedNode.ofSymbol("AdditiveOperation")),
            parser.parse("a*(b+c)*d", "Expression").getChildren());
        assertParseTree(List.of(
            ExpectedNode.ofSymbol("Comparable",
                ExpectedNode.ofContent("("),
                ExpectedNode.ofSymbol("Expression",
                    ExpectedNode.ofSymbol("Term",
                        ExpectedNode.ofSymbol("Factor",
                            ExpectedNode.ofSymbol("Comparable",
                                ExpectedNode.ofSymbol("Identifier",
                                    ExpectedNode.ofContent("p1")))),
                        ExpectedNode.ofSymbol("MultiplicativeOperation")),
                    ExpectedNode.ofSymbol("AdditiveOperation",
                        ExpectedNode.ofSymbol("AdditiveOperator",
                            ExpectedNode.ofContent("-")),
                        ExpectedNode.ofSymbol("Term",
                            ExpectedNode.ofSymbol("Factor",
                                ExpectedNode.ofSymbol("Comparable",
                                    ExpectedNode.ofSymbol("Identifier",
                                        ExpectedNode.ofContent("p2")))),
                            ExpectedNode.ofSymbol("MultiplicativeOperation")),
                        ExpectedNode.ofSymbol("AdditiveOperation"))),
                ExpectedNode.ofContent(")")),
            ExpectedNode.ofSymbol("RelationalOperator",
                ExpectedNode.ofContent("<")),
            ExpectedNode.ofSymbol("Comparable",
                ExpectedNode.ofSymbol("Identifier",
                    ExpectedNode.ofContent("c0")))),
            parser.parse("(p1 - p2) < c0", "Factor").getChildren());
        assertSuccess(parser.parse("""
            PROGRAM const_expressions;
            CONST c1 = 1 + 2;
            CONST c2 = -c1;
            CONST c3 = TRUE OR FALSE;
            CONST c4 = NOT c3;
            CONST c5 = (c1 - c2 < 0) OR c3 AND NOT c4;
            BEGIN
            END.
            """));
    }

    @Test
    public void testComment() {
        assertSuccess(parser.parse("""
            (*
            ** A Pascal Program
            *)
            PROGRAM program_name;
            (* Main *)(* *)
            BEGIN
            END.
            (**)"""));
    }

    @Test
    public void testRangeExpressions() {
        assertSuccess(parser.parse("""
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
            """));
    }

    @Test
    public void testMissingProgramKeyword() {
        assertParseTree(
            List.of(ExpectedNode.ofError("Program", "Not replaceable")),
            parser.parse("""
                (*PROGRAM*)program_name;
                BEGIN
                END.
                """).getChildren());
    }

    @Test
    public void testMissingProgramName() {
        assertParseTree(
            List.of(
                ExpectedNode.ofError("Program", "Not replaceable",
                    keyword("PROGRAM"),
                    ExpectedNode.ofError("Identifier", "Cannot resolve",
                        ExpectedNode.ofError("Identifier", "Not replaceable")))),
            parser.parse("""
                PROGRAM (*program_name*);
                BEGIN
                END.
                """).getChildren());
    }

    @Test
    public void testKeywordForProgramName() {
        Node tree = parser.parse("""
            PROGRAM case;
            BEGIN
            END.
            """);
        assertTrue(tree.getError().isPresent());
    }

    @Test
    public void testMissingEndDot() {
        Node tree = parser.parse("""
            PROGRAM program_name ;
            BEGIN
            END(*.*)
            """);
        assertTrue(tree.getError().isPresent());
    }

    private static void assertSuccess(Node actual) {
        actual.getChildren().forEach(node -> {
            if (node.getError().isPresent()) {
                fail("Error in line" + node.startLine() + ", Symbol " + node.getSymbol());
            }
        });
    }

    private static void assertParseTree(List<ExpectedNode> expected, List<Node> actual) {
        assertParseTree(null, expected, actual);
    }

    private static void assertParseTree(Node parent, List<ExpectedNode> expected, List<Node> actual) {
        int line = (parent == null) ? 0 : parent.startLine();
        assertEquals(expected.size(), actual.size(), () -> "Line " + line + ": Child count of '" + ((parent == null) ? "root" : parent.getSymbol()) + '\'');
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

        public static ExpectedNode ofSymbol(String symbol, List<ExpectedNode> children) {
            return new ExpectedNode(symbol, null, children);
        }

        public static ExpectedNode ofSymbol(String symbol, String content, List<ExpectedNode> children) {
            return new ExpectedNode(symbol, content, children);
        }

        public static ExpectedNode ofSymbol(String symbol, String content, ExpectedNode... children) {
            return new ExpectedNode(symbol, content, Arrays.asList(children));
        }

        public static ExpectedNode ofContent(String content) {
            return new ExpectedNode(null, content, Collections.emptyList());
        }

        public static ExpectedNode ofError(String symbol, String error, ExpectedNode... children) {
            return new ExpectedNode(symbol, null, Arrays.asList(children), error);
        }

        public static ExpectedNode ofError(String symbol, String error) {
            return new ExpectedNode(symbol, null, null, error);
        }

        public ExpectedNode(String symbol, String content, List<ExpectedNode> children) {
            this(symbol, content, children, null);
        }

        public ExpectedNode(String symbol, String content, List<ExpectedNode> children, String error) {
            this.symbol = symbol;
            this.content = content;
            this.children = children;
            this.error = error;
        }

        public void assertParserNode(Node node) {
            if (symbol != null) {
                assertEquals(symbol, node.getSymbol(), () -> "Line " + node.startLine() + ": Symbol");
            }
            if (content != null) {
                assertEquals(content, node.content(), () -> "Line " + node.startLine() + ": Content");
            }
            if (children != null) {
                assertParseTree(node, children, node.getChildren());
            }
            if (error != null) {
                assertEquals(Optional.of(error), node.getError(), () -> "Line " + node.startLine() + ": Error");
            }
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            if (symbol != null) {
                builder.append(symbol);
            }
            if (children != null) {
                builder.append(" (").append(children.size()).append(')');
            }
            if (content != null) {
                builder.append(" '").append(content).append('\'');
            }
            if (error != null) {
                builder.append("Error: ").append(error);
            }
            return builder.toString();
        }

        private final String symbol;
        private final String content;
        private final List<ExpectedNode> children;
        private final String error;
    }


    private Parser parser;

}
