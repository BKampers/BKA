package bka.text.parser;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class GrammarParserTest {

    @Test
    public void testSimpleGrammar() {
        GrammarParser parser = new GrammarParser(Map.of("S", List.of(List.of("a"))), List.of(CommentBrackets.blockComment("/*", "*/"), CommentBrackets.lineComment("//")));
        assertEqualNodes(
            new ExpectedNode("S", 0,
                new ExpectedNode("a", 0, 1)),
            parser.buildTree("a", "S"));
        assertEqualNodes(
            new ExpectedNode("S", 0,
                new ExpectedNode("a", 36, 37)),
            parser.buildTree("// Line comment\n/* Block comment */ a // Line comment without line end", "S"));
        assertEqualNodes(
            new ExpectedNode("S", 0,
                new ExpectedNode("a", 1, 2)),
            parser.buildTree(" a /* Block comment at end of source */", "S"));
        assertEqualNodes(
            new ExpectedNode("S", 0, "Unterminated comment",
                new ExpectedNode("a", 0, 1),
                new ExpectedNode("", 1, "Unterminated comment")),
            parser.buildTree("a /*", "S"));
        assertEqualNodes(
            new ExpectedNode("S", 0, "No match",
                new ExpectedNode("a", 0, "No match")),
            parser.buildTree("b", "S"));
        assertEqualNodes(
            new ExpectedNode("S", 0, "Unparsable code after symbol [S]",
                new ExpectedNode("a", 0, 1),
                new ExpectedNode("", 1, "Unparsable code after symbol [S]")),
            parser.buildTree("aa", "S"));
        assertThrows(IllegalArgumentException.class, () -> parser.buildTree("a", "T"));
    }

    @Test
    public void testGrammarWithOneChoice() {
        GrammarParser parser = new GrammarParser(Map.of("S", List.of(List.of("a", "b"))));
        assertEqualNodes(
            new ExpectedNode("S", 0,
                new ExpectedNode("a", 0, 1),
                new ExpectedNode("b", 1, 2)),
            parser.buildTree("ab", "S"));
        assertEqualNodes(
            new ExpectedNode("S", 0,
                new ExpectedNode("a", 0, 1),
                new ExpectedNode("b", 2, 3)),
            parser.buildTree("a b", "S"));
        assertEqualNodes(
            new ExpectedNode("S", 0, "No match",
                new ExpectedNode("a", 0, 1),
                new ExpectedNode("b", 1, "No match")),
            parser.buildTree("a", "S"));
        assertEqualNodes(
            new ExpectedNode("S", 0, "No match",
                new ExpectedNode("a", 0, 1),
                new ExpectedNode("b", 1, "No match")),
            parser.buildTree("aa", "S"));
    }

    @Test
    public void testGrammarWithTwoDistictChoices() {
        GrammarParser parser = new GrammarParser(
            Map.of("S", List.of(
                List.of("a", "b"),
                List.of("c", "d"))));
        assertEqualNodes(
            new ExpectedNode("S", 0,
                new ExpectedNode("a", 0, 1),
                new ExpectedNode("b", 1, 2)),
            parser.buildTree("ab", "S"));
        assertEqualNodes(
            new ExpectedNode("S", 0,
                new ExpectedNode("c", 0, 1),
                new ExpectedNode("d", 1, 2)),
            parser.buildTree("cd", "S"));
    }

    @Test
    public void testGrammarWithTwoOverlappingHeads() {
        GrammarParser parser = new GrammarParser(
            Map.of("S", List.of(
                List.of("a", "b"),
                List.of("a", "c"))));
        assertEqualNodes(
            new ExpectedNode("S", 0,
                new ExpectedNode("a", 0, 1),
                new ExpectedNode("b", 1, 2)),
            parser.buildTree("ab", "S"));
        assertEqualNodes(
            new ExpectedNode("S", 0,
                new ExpectedNode("a", 0, 1),
                new ExpectedNode("c", 1, 2)),
            parser.buildTree("ac", "S"));
    }

    @Test
    public void testGrammarWithTwoOverlappingTails() {
        GrammarParser parser = new GrammarParser(
            Map.of("S", List.of(
                List.of("a", "b"),
                List.of("c", "b"))));
        assertEqualNodes(
            new ExpectedNode("S", 0,
                new ExpectedNode("a", 0, 1),
                new ExpectedNode("b", 1, 2)),
            parser.buildTree("ab", "S"));
        assertEqualNodes(
            new ExpectedNode("S", 0,
                new ExpectedNode("c", 0, 1),
                new ExpectedNode("b", 1, 2)),
            parser.buildTree("cb", "S"));
    }

    @Test
    public void testGrammarWithThreeSymbols() {
        GrammarParser parser = new GrammarParser(
            Map.of(
                "S", List.of(List.of("T"), List.of("U")),
                "T", List.of(List.of("a", "b")),
                "U", List.of(List.of("c", "d"))));
        assertEqualNodes(
            new ExpectedNode("S", 0,
                new ExpectedNode("T", 0,
                    new ExpectedNode("a", 0, 1),
                    new ExpectedNode("b", 1, 2))),
            parser.buildTree("ab", "S"));
        assertEqualNodes(
            new ExpectedNode("S", 0,
                new ExpectedNode("U", 0,
                    new ExpectedNode("c", 0, 1),
                    new ExpectedNode("d", 1, 2))),
            parser.buildTree("cd", "S"));
    }

    @Test
    public void testSequence() {
        GrammarParser parser = new GrammarParser(
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
            parser.buildTree("{}", "List"));
        assertEqualNodes(
            new ExpectedNode("List", 0,
                new ExpectedNode("\\{", 0, 1),
                new ExpectedNode("Sequence", 1,
                    new ExpectedNode("\\d+", 1, 2)),
                new ExpectedNode("\\}", 2, 3)),
            parser.buildTree("{1}", "List"));
        assertEqualNodes(
            new ExpectedNode("List", 0,
                new ExpectedNode("\\{", 0, 1),
                new ExpectedNode("Sequence", 1,
                    new ExpectedNode("\\d+", 1, 2),
                    new ExpectedNode("\\,", 2, 3),
                    new ExpectedNode("Sequence", 3,
                        new ExpectedNode("\\d+", 3, 4))),
                new ExpectedNode("\\}", 4, 5)),
            parser.buildTree("{1,2}", "List"));
        assertEqualNodes(
            new ExpectedNode("List", 0, "No match",
                new ExpectedNode("\\{", 0, 1),
                new ExpectedNode("Sequence", 1,
                    new ExpectedNode("\\d+", 1, 2)),
                new ExpectedNode("\\}", 2, "No match")),
            parser.buildTree("{1,}", "List"));
    }

    @Test
    public void testEmptyElement() {
        GrammarParser parser = new GrammarParser(
            Map.of(
                "Body", List.of(
                    List.of("\\bBEGIN", "Sequence", "\\bEND")),
                "Sequence", List.of(
                    List.of("\\d+", "\\;", "Sequence"),
                    List.of("\\d+"),
                    List.of())));
        assertEqualNodes(
            new ExpectedNode("Body", 0,
                new ExpectedNode("\\bBEGIN", 0, 5),
                new ExpectedNode("Sequence", 6, 6),
                new ExpectedNode("\\bEND", 6, 9)),
            parser.buildTree("BEGIN END", "Body"));
        assertEqualNodes(
            new ExpectedNode("Body", 0,
                new ExpectedNode("\\bBEGIN", 0, 5),
                new ExpectedNode("Sequence", 6,
                    new ExpectedNode("\\d+", 6, 7),
                    new ExpectedNode("\\;", 7, 8),
                    new ExpectedNode("Sequence", 9, 9)),
                new ExpectedNode("\\bEND", 9, 12)),
            parser.buildTree("BEGIN 1; END", "Body"));
        assertEqualNodes(
            new ExpectedNode("Body", 0,
                new ExpectedNode("\\bBEGIN", 0, 5),
                new ExpectedNode("Sequence", 6,
                    new ExpectedNode("\\d+", 6, 7)),
                new ExpectedNode("\\bEND", 8, 11)),
            parser.buildTree("BEGIN 1 END", "Body"));
        assertEqualNodes(
            new ExpectedNode("Body", 0,
                new ExpectedNode("\\bBEGIN", 0, 5),
                new ExpectedNode("Sequence", 6,
                    new ExpectedNode("\\d+", 6, 7),
                    new ExpectedNode("\\;", 7, 8),
                    new ExpectedNode("Sequence", 9,
                        new ExpectedNode("\\d+", 9, 10),
                        new ExpectedNode("\\;", 10, 11),
                        new ExpectedNode("Sequence", 12, 12))),
                new ExpectedNode("\\bEND", 12, 15)),
            parser.buildTree("BEGIN 1; 2; END", "Body"));
        assertEqualNodes(
            new ExpectedNode("Body", 0,
                new ExpectedNode("\\bBEGIN", 0, 5),
                new ExpectedNode("Sequence", 6,
                    new ExpectedNode("\\d+", 6, 7),
                    new ExpectedNode("\\;", 7, 8),
                    new ExpectedNode("Sequence", 9,
                        new ExpectedNode("\\d+", 9, 10))),
                new ExpectedNode("\\bEND", 11, 14)),
            parser.buildTree("BEGIN 1; 2 END", "Body"));
    }

    @Test
    public void testExpression() {
        GrammarParser parser = new GrammarParser(
            Map.of(
                "Expression", List.of(
                    List.of("\\w+"),
                    List.of("Expression", "\\.", "Expression")
                )));
        assertEqualNodes(
            new ExpectedNode("Expression", 0,
                new ExpectedNode("\\w+", 0, 4)),
            parser.buildTree("word", "Expression"));
        assertEqualNodes(
            new ExpectedNode("Expression", 0,
                new ExpectedNode("Expression", 0,
                    new ExpectedNode("\\w+", 0, 9)),
                new ExpectedNode("\\.", 9, 10),
                new ExpectedNode("Expression", 10,
                    new ExpectedNode("\\w+", 10, 16))),
            parser.buildTree("reference.member", "Expression"));
    }

    private static void assertEqualNodes(ExpectedNode expected, Node actual) {
        assertEquals(expected.getSymbol(), actual.getSymbol(), () -> "symbol of " + expected);
        assertEquals(expected.getStart(), actual.getStart(), () -> "start of " + expected);
        assertEquals(expected.getEnd(), actual.getEnd(), () -> "end of " + expected);
        assertEquals(expected.getError(), actual.getError(), () -> "error of " + expected);
        assertEquals(expected.getChildren().size(), actual.getChildren().size(), () -> "child count of " + expected);
        IntStream.range(0, expected.getChildren().size()).forEach(
            i -> assertEqualNodes(expected.getChildren().get(i), actual.getChildren().get(i)));
    }


    private static class ExpectedNode {

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

        private ExpectedNode(String symbol, int start, int end, List<ExpectedNode> children, Optional<String> error) {
            this.symbol = symbol;
            this.start = start;
            this.end = end;
            this.children = children;
            this.error = error;
        }

        public String getSymbol() {
            return symbol;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        public List<ExpectedNode> getChildren() {
            return children;
        }

        public Optional<String> getError() {
            return error;
        }

        @Override
        public String toString() {
            if (error.isEmpty()) {
                return String.format("'%s' (%d .. %d)", symbol, start, end);
            }
            return String.format("%s: '%s' (%d .. %d)", error.get(), symbol, start, end);
        }

        private final String symbol;
        private final int start;
        private final int end;
        private final List<ExpectedNode> children;
        private final Optional<String> error;
    }


}
