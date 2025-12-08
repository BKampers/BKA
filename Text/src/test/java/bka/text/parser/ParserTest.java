package bka.text.parser;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ParserTest {

    @Test
    public void testSimpleGrammar() {
        Parser parser = createParser(Map.of("S", List.of(List.of("a"))), List.of(CommentBrackets.blockComment("/*", "*/"), CommentBrackets.lineComment("//")));
        assertEqualNodes(
            new ExpectedNode("S", 0,
                new ExpectedNode("a", 0, 1)),
            parser.parse("a", "S"));
        assertEqualNodes(
            new ExpectedNode("S", 0,
                new ExpectedNode("a", 36, 37)),
            parser.parse("// Line comment\n/* Block comment */ a // Line comment without line end", "S"));
        assertEqualNodes(
            new ExpectedNode("S", 0,
                new ExpectedNode("a", 1, 2)),
            parser.parse(" a /* Block comment at end of source */", "S"));
        assertEqualNodes(
            new ExpectedNode("S", 0, "Unterminated comment",
                new ExpectedNode("a", 0, 1),
                new ExpectedNode("", 1, "Unterminated comment")),
            parser.parse("a /*", "S"));
        assertEqualNodes(
            new ExpectedNode("S", 0, "No match",
                new ExpectedNode("a", 0, "No match")),
            parser.parse("b", "S"));
        assertEqualNodes(
            new ExpectedNode("S", 0, "Unparsable code after symbol [S]",
                new ExpectedNode("a", 0, 1),
                new ExpectedNode("", 1, "Unparsable code after symbol [S]")),
            parser.parse("aa", "S"));
        assertThrows(IllegalArgumentException.class, () -> parser.parse("a", "T"));
    }

    @Test
    public void testGrammarWithOneSentential() {
        Parser parser = createParser(Map.of("S", List.of(List.of("a", "b"))));
        assertEqualNodes(
            new ExpectedNode("S", 0,
                new ExpectedNode("a", 0, 1),
                new ExpectedNode("b", 1, 2)),
            parser.parse("ab", "S"));
        assertEqualNodes(
            new ExpectedNode("S", 0,
                new ExpectedNode("a", 0, 1),
                new ExpectedNode("b", 2, 3)),
            parser.parse("a b", "S"));
        assertEqualNodes(
            new ExpectedNode("S", 0, "No match",
                new ExpectedNode("a", 0, 1),
                new ExpectedNode("b", 1, "No match")),
            parser.parse("a", "S"));
        assertEqualNodes(
            new ExpectedNode("S", 0, "No match",
                new ExpectedNode("a", 0, 1),
                new ExpectedNode("b", 1, "No match")),
            parser.parse("aa", "S"));
    }

    @Test
    public void testGrammarWithTwoDistictSententials() {
        Parser parser = createParser(
            Map.of("S", List.of(
                List.of("a", "b"),
                List.of("c", "d"))));
        assertEqualNodes(
            new ExpectedNode("S", 0,
                new ExpectedNode("a", 0, 1),
                new ExpectedNode("b", 1, 2)),
            parser.parse("ab", "S"));
        assertEqualNodes(
            new ExpectedNode("S", 0,
                new ExpectedNode("c", 0, 1),
                new ExpectedNode("d", 1, 2)),
            parser.parse("cd", "S"));
    }

    @Test
    public void testGrammarWithTwoOverlappingHeads() {
        Parser parser = createParser(
            Map.of("S", List.of(
                List.of("a", "b"),
                List.of("a", "c"))));
        assertEqualNodes(
            new ExpectedNode("S", 0,
                new ExpectedNode("a", 0, 1),
                new ExpectedNode("b", 1, 2)),
            parser.parse("ab", "S"));
        assertEqualNodes(
            new ExpectedNode("S", 0,
                new ExpectedNode("a", 0, 1),
                new ExpectedNode("c", 1, 2)),
            parser.parse("ac", "S"));
    }

    @Test
    public void testGrammarWithTwoOverlappingTails() {
        Parser parser = createParser(
            Map.of("S", List.of(
                List.of("a", "b"),
                List.of("c", "b"))));
        assertEqualNodes(
            new ExpectedNode("S", 0,
                new ExpectedNode("a", 0, 1),
                new ExpectedNode("b", 1, 2)),
            parser.parse("ab", "S"));
        assertEqualNodes(
            new ExpectedNode("S", 0,
                new ExpectedNode("c", 0, 1),
                new ExpectedNode("b", 1, 2)),
            parser.parse("cb", "S"));
    }

    @Test
    public void testGrammarWithThreeNonterminals() {
        Parser parser = createParser(
            Map.of(
                "S", List.of(List.of("T"), List.of("U")),
                "T", List.of(List.of("a", "b")),
                "U", List.of(List.of("c", "d"))));
        assertEqualNodes(
            new ExpectedNode("S", 0,
                new ExpectedNode("T", 0,
                    new ExpectedNode("a", 0, 1),
                    new ExpectedNode("b", 1, 2))),
            parser.parse("ab", "S"));
        assertEqualNodes(
            new ExpectedNode("S", 0,
                new ExpectedNode("U", 0,
                    new ExpectedNode("c", 0, 1),
                    new ExpectedNode("d", 1, 2))),
            parser.parse("cd", "S"));
    }

    @Test
    public void testSequence() {
        Parser parser = createParser(
            Map.of(
                "List", List.of(
                    List.of("\\{", "Sequence", "\\}"),
                    List.of("\\{", "\\}")),
                "Sequence", List.of(
                    List.of("\\d+", "\\,", "Sequence"),
                    List.of("\\d+"))));
        assertEqualNodes(
            new ExpectedNode("List", 0,
                new ExpectedNode("\\{", 0, 1),
                new ExpectedNode("\\}", 1, 2)),
            parser.parse("{}", "List"));
        assertEqualNodes(
            new ExpectedNode("List", 0,
                new ExpectedNode("\\{", 0, 1),
                new ExpectedNode("Sequence", 1,
                    new ExpectedNode("\\d+", 1, 2)),
                new ExpectedNode("\\}", 2, 3)),
            parser.parse("{1}", "List"));
        assertEqualNodes(
            new ExpectedNode("List", 0,
                new ExpectedNode("\\{", 0, 1),
                new ExpectedNode("Sequence", 1,
                    new ExpectedNode("\\d+", 1, 2),
                    new ExpectedNode("\\,", 2, 3),
                    new ExpectedNode("Sequence", 3,
                        new ExpectedNode("\\d+", 3, 4))),
                new ExpectedNode("\\}", 4, 5)),
            parser.parse("{1,2}", "List"));
        assertEqualNodes(
            new ExpectedNode("List", 0, "No match",
                new ExpectedNode("\\{", 0, 1),
                new ExpectedNode("Sequence", 1,
                    new ExpectedNode("\\d+", 1, 2)),
                new ExpectedNode("\\}", 2, "No match")),
            parser.parse("{1,}", "List"));
    }

    @Test
    public void testEmptySentential() {
        Parser parser = createParser(
            Map.of(
                "Body", List.of(
                    List.of("BEGIN", "Sequence", "END")),
                "Sequence", List.of(
                    List.of("\\d+", "\\;", "Sequence"),
                    List.of("\\d+"),
                    List.of())));
        assertEqualNodes(
            new ExpectedNode("Body", 0,
                new ExpectedNode("BEGIN", 0, 5),
                new ExpectedNode("Sequence", 6, 6),
                new ExpectedNode("END", 6, 9)),
            parser.parse("BEGIN END", "Body"));
        assertEqualNodes(
            new ExpectedNode("Body", 0,
                new ExpectedNode("BEGIN", 0, 5),
                new ExpectedNode("Sequence", 6,
                    new ExpectedNode("\\d+", 6, 7),
                    new ExpectedNode("\\;", 7, 8),
                    new ExpectedNode("Sequence", 9, 9)),
                new ExpectedNode("END", 9, 12)),
            parser.parse("BEGIN 1; END", "Body"));
        assertEqualNodes(
            new ExpectedNode("Body", 0,
                new ExpectedNode("BEGIN", 0, 5),
                new ExpectedNode("Sequence", 6,
                    new ExpectedNode("\\d+", 6, 7)),
                new ExpectedNode("END", 8, 11)),
            parser.parse("BEGIN 1 END", "Body"));
        assertEqualNodes(
            new ExpectedNode("Body", 0,
                new ExpectedNode("BEGIN", 0, 5),
                new ExpectedNode("Sequence", 6,
                    new ExpectedNode("\\d+", 6, 7),
                    new ExpectedNode("\\;", 7, 8),
                    new ExpectedNode("Sequence", 9,
                        new ExpectedNode("\\d+", 9, 10),
                        new ExpectedNode("\\;", 10, 11),
                        new ExpectedNode("Sequence", 12, 12))),
                new ExpectedNode("END", 12, 15)),
            parser.parse("BEGIN 1; 2; END", "Body"));
        assertEqualNodes(
            new ExpectedNode("Body", 0,
                new ExpectedNode("BEGIN", 0, 5),
                new ExpectedNode("Sequence", 6,
                    new ExpectedNode("\\d+", 6, 7),
                    new ExpectedNode("\\;", 7, 8),
                    new ExpectedNode("Sequence", 9,
                        new ExpectedNode("\\d+", 9, 10))),
                new ExpectedNode("END", 11, 14)),
            parser.parse("BEGIN 1; 2 END", "Body"));
    }

    @Test
    public void testSententialStartsWithRuleHead() {
        Parser parser = createParser(
            Map.of(
                "Expression", List.of(
                    List.of("\\w+"),
                    List.of("Expression", "\\.", "Expression")
                )));
        assertEqualNodes(
            new ExpectedNode("Expression", 0,
                new ExpectedNode("\\w+", 0, 4)),
            parser.parse("word", "Expression"));
        assertEqualNodes(
            new ExpectedNode("Expression", 0,
                new ExpectedNode("Expression", 0,
                    new ExpectedNode("\\w+", 0, 9)),
                new ExpectedNode("\\.", 9, 10),
                new ExpectedNode("Expression", 10,
                    new ExpectedNode("\\w+", 10, 16))),
            parser.parse("reference.member", "Expression"));
    }

    @Test
    public void testMathematicalExpression() {
        Parser parser = createParser(
            Map.of(
                "expression", List.of(
                    List.of("term", "additive-operation")),
                "term", List.of(
                    List.of("factor", "multiplication")),
                "factor", List.of(
                    List.of("\\d+"),
                    List.of("\\(", "expression", "\\)")),
                "multiplication", List.of(
                    List.of("multiplying-operator", "factor", "multiplication"),
                    List.of()),
                "multiplying-operator", List.of(
                    List.of("\\*"),
                    List.of("\\/"),
                    List.of("\\%")),
                "additive-operation", List.of(
                    List.of("additive-operator", "term", "additive-operation"),
                    List.of()),
                "additive-operator", List.of(
                    List.of("\\+"),
                    List.of("\\-"))));
        assertEqualNodes(
            new ExpectedNode("expression", 0,
                new ExpectedNode("term", 0,
                    new ExpectedNode("factor", 0,
                        new ExpectedNode("\\d+", 0, 1)),
                    new ExpectedNode("multiplication", 1,
                        new ExpectedNode("multiplying-operator", 1,
                            new ExpectedNode("\\*", 1, 2)),
                        new ExpectedNode("factor", 2,
                            new ExpectedNode("\\d+", 2, 3)),
                        new ExpectedNode("multiplication", 3, 3))),
                new ExpectedNode("additive-operation", 3,
                    new ExpectedNode("additive-operator", 3,
                        new ExpectedNode("\\+", 3, 4)),
                    new ExpectedNode("term", 4,
                        new ExpectedNode("factor", 4,
                            new ExpectedNode("\\d+", 4, 5)),
                        new ExpectedNode("multiplication", 5,
                            new ExpectedNode("multiplying-operator", 5,
                                new ExpectedNode("\\*", 5, 6)),
                            new ExpectedNode("factor", 6,
                                new ExpectedNode("\\d+", 6, 7)),
                            new ExpectedNode("multiplication", 7, 7))),
                    new ExpectedNode("additive-operation", 7, 7))),
            parser.parse("1*2+3*4", "expression"));
        assertEqualNodes(
            new ExpectedNode("expression", 0,
                new ExpectedNode("term", 0,
                    new ExpectedNode("factor", 0,
                        new ExpectedNode("\\d+", 0, 1)),
                    new ExpectedNode("multiplication", 1,
                        new ExpectedNode("multiplying-operator", 1,
                            new ExpectedNode("\\*", 1, 2)),
                        new ExpectedNode("factor", 2,
                            new ExpectedNode("\\(", 2, 3),
                            new ExpectedNode("expression", 3,
                                new ExpectedNode("term", 3,
                                    new ExpectedNode("factor", 3,
                                        new ExpectedNode("\\d+", 3, 4)),
                                    new ExpectedNode("multiplication", 4, 4)),
                                new ExpectedNode("additive-operation", 4,
                                    new ExpectedNode("additive-operator", 4,
                                        new ExpectedNode("\\+", 4, 5)),
                                    new ExpectedNode("term", 5,
                                        new ExpectedNode("factor", 5,
                                            new ExpectedNode("\\d+", 5, 6)),
                                        new ExpectedNode("multiplication", 6, 6)),
                                    new ExpectedNode("additive-operation", 6, 6))),
                            new ExpectedNode("\\)", 6, 7)),
                        new ExpectedNode("multiplication", 7,
                            new ExpectedNode("multiplying-operator", 7,
                                new ExpectedNode("\\*", 7, 8)),
                            new ExpectedNode("factor", 8,
                                new ExpectedNode("\\d+", 8, 9)),
                            new ExpectedNode("multiplication", 9, 9)))),
                new ExpectedNode("additive-operation", 9, 9)),
            parser.parse("1*(2+3)*4", "expression"));
    }

    private static Parser createParser(Map<String, List<List<String>>> rules, List<CommentBrackets> comments) {
        return new Parser(Grammar.of(rules, comments));
    }

    private static Parser createParser(Map<String, List<List<String>>> rules) {
        return new Parser(Grammar.of(rules));
    }

    private static void assertEqualNodes(PrintableNode expected, Node actual) {
        assertEquals(expected.getSymbol(), actual.getSymbol(), message("symbol", expected, actual));
        assertEquals(expected.getStart(), actual.getStart(), message("start", expected, actual));
        assertEquals(expected.getEnd(), actual.getEnd(), message("end", expected, actual));
        assertEquals(expected.getError(), actual.getError(), message("error", expected, actual));
        assertEquals(expected.getChildren().size(), actual.getChildren().size(), message("child count", expected, actual));
        IntStream.range(0, expected.getChildren().size()).forEach(
            i -> assertEqualNodes(expected.getChildren().get(i), actual.getChildren().get(i)));
    }

    private static Supplier<String> message(String property, PrintableNode expected, Node actual) {
        return () -> property + " of " + expected + "\n expected: \n" + expected.toString() + "\nactual:\n" + printable(actual).toString();
    }

    private static abstract class PrintableNode {

        public abstract String getSymbol();

        public abstract int getStart();

        public abstract int getEnd();

        public abstract List<PrintableNode> getChildren();

        public abstract Optional<String> getError();

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            toString(builder, 0);
            return builder.toString();
        }

        private void toString(StringBuilder builder, int indent) {
            builder.append("\u25a1".repeat(indent));
            getError().ifPresent(error -> builder.append("Error: ").append(error));
            builder
                .append("\"").append(getSymbol()).append("\" ")
                .append(getStart()).append("..").append(getEnd());
            getChildren().forEach(child -> child.toString(builder.append("\n"), indent + 1));
        }
    }

    private static PrintableNode printable(Node node) {
        return new PrintableNode() {
            @Override
            public String getSymbol() {
                return node.getSymbol();
            }

            @Override
            public int getStart() {
                return node.getStart();
            }

            @Override
            public int getEnd() {
                return node.getEnd();
            }

            @Override
            public List<PrintableNode> getChildren() {
                return node.getChildren().stream().map(child -> printable(child)).toList();
            }

            @Override
            public Optional<String> getError() {
                return node.getError();
            }

        };
    }

    private static class ExpectedNode extends PrintableNode {

        public ExpectedNode(String symbol, int start, int end) {
            this(symbol, start, end, Collections.emptyList(), Optional.empty());
        }

        public ExpectedNode(String symbol, int start, String error) {
            this(symbol, start, start, Collections.emptyList(), Optional.of(error));
        }

        public ExpectedNode(String symbol, int start, ExpectedNode... children) {
            this(symbol, start, children[children.length - 1].end, Optional.empty(), children);
        }

        public ExpectedNode(String symbol, int start, String error, ExpectedNode... children) {
            this(symbol, start, 0, Optional.of(error), children);
        }

        private ExpectedNode(String symbol, int start, int end, Optional<String> error, ExpectedNode... children) {
            this(symbol, start, end, Arrays.stream(children).collect(Collectors.toList()), error);
        }

        private ExpectedNode(String symbol, int start, int end, List<PrintableNode> children, Optional<String> error) {
            this.symbol = symbol;
            this.start = start;
            this.end = end;
            this.children = children;
            this.error = error;
        }

        @Override
        public String getSymbol() {
            return symbol;
        }

        @Override
        public int getStart() {
            return start;
        }

        @Override
        public int getEnd() {
            return end;
        }

        @Override
        public List<PrintableNode> getChildren() {
            return children;
        }

        @Override
        public Optional<String> getError() {
            return error;
        }

        @Override
        public String toString() {
            if (error.isEmpty()) {
                return String.format("'%s' %d..%d", symbol, start, end);
            }
            return String.format("%s: '%s' %d..%d", error.get(), symbol, start, end);
        }

        private final String symbol;
        private final int start;
        private final int end;
        private final List<PrintableNode> children;
        private final Optional<String> error;
    }

}
