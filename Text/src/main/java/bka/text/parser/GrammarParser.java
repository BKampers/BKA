/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans, humanity, the environment or the universe.
*/
package bka.text.parser;

import java.util.*;
import java.util.regex.*;

public class GrammarParser {

    public GrammarParser(Map<String, List<List<String>>> grammar) {
        this(new Grammar(grammar, Collections.emptyList()));
    }

    public GrammarParser(Map<String, List<List<String>>> grammar, Collection<CommentBrackets> commentBrackets) {
        this(new Grammar(grammar, commentBrackets));
    }

    public GrammarParser(Grammar grammar) {
        this.grammar = Objects.requireNonNull(grammar);
    }

    public String parse(String sourceCode, String startSymbol) {
        long startTime = System.nanoTime();
        Node tree = buildTree(sourceCode, startSymbol);
        long duration = System.nanoTime() - startTime;
        if (tree.getError().isPresent()) {
            return "Error: " + tree.getError().get();
        }
        return "Program parsed successfully in " + (duration / 1000) + " microseconds";
    }

    public Node buildTree(String sourceCode) {
        return buildTree(sourceCode, grammar.getStartSymbol().get());
    }

    public Node buildTree(String sourceCode, String startSymbol) {
        if (!grammar.getNonterminals().contains(startSymbol)) {
            throw new IllegalArgumentException("Invalid symbol: " + startSymbol);
        }
        source = sourceCode;
        matchers.clear();
        skips.clear();
        Node tree = createTreeNode(0, startSymbol);
        if (tree.getError().isPresent()) {
            return tree;
        }
        return validateRemainder(tree);
    }

    private Node validateRemainder(Node tree) {
        int index = skipWhitespaceAndComment(tree.getEnd());
        if (index < 0) {
            return createErrorTree(tree, UNTERMINATED_COMMENT);
        }
        if (index < source.length()) {
            return createErrorTree(tree, String.format(UNPARSABLE_CODE_AFTER_SYMBOL, tree.getSymbol()));
        }
        return tree;
    }

    private Node createErrorTree(Node tree, String message) {
        Node error = new Node(source, "", tree.getEnd(), message);
        List<Node> children = new ArrayList<>(tree.getChildren());
        children.add(error);
        return new Node(source, tree.getSymbol(), tree.getStart(), children, message);
    }

    private List<Node> buildTree(int index, String nonterminal) {
        List<Node> tree = null;
        List<Node> errorTree = null;
        Node errorNode = null;
        final int headIndex = skipWhitespaceAndComment(index);
        if (headIndex < 0) {
            return List.of(new Node(source, nonterminal, index, UNTERMINATED_COMMENT));
        }
        for (List<String> sentential : grammar.getSententials(nonterminal)) {
            if (tree == null) {
                List<Node> resolution = resolve(headIndex, sentential);
                Optional<Node> error = findError(resolution);
                if (error.isEmpty()) {
                    tree = resolution;
                }
                else if (errorNode == null || error.get().getStart() >= errorNode.getStart()) {
                    errorNode = error.get();
                    errorTree = resolution;
                }
            }
            else if (sentential.size() > 1 && nonterminal.equals(sentential.getFirst())) {
                final int tailIndex = skipWhitespaceAndComment(tree.getLast().getEnd());
                if (tailIndex < 0) {
                    return List.of(new Node(source, nonterminal, tree.getLast().getEnd(), UNTERMINATED_COMMENT));
                }
                List<Node> resolution = resolve(tailIndex, tail(sentential));
                Optional<Node> error = findError(resolution);
                if (error.isEmpty()) {
                    Node head = new Node(source, nonterminal, headIndex, tree);
                    tree = new ArrayList<>();
                    tree.add(head);
                    tree.addAll(resolution);
                    return tree;
                }
                else if (errorNode == null || error.get().getStart() >= errorNode.getStart()) {
                    errorNode = error.get();
                    errorTree = resolution;
                }
            }
        }
        if (tree == null) {
            return errorTree;
        }
        return tree;
    }

    private static List<String> tail(List<String> sentential) {
        return sentential.stream().skip(1).toList();
    }

    private List<Node> resolve(int index, List<String> sentential) {
        List<Node> resolution = new ArrayList<>();
        int sourceIndex = index;
        for (String symbol : sentential) {
            Node node = createNode(sourceIndex, symbol);
            resolution.add(node);
            if (node.getError().isPresent()) {
                return resolution;
            }
            sourceIndex = skipWhitespaceAndComment(node.getEnd());
            if (sourceIndex < 0) {
                resolution.add(new Node(source, "", node.getEnd(), UNTERMINATED_COMMENT));
                return resolution;
            }
        }
        return resolution;
    }

    private Node createNode(int index, String symbol) {
        if (grammar.getNonterminals().contains(symbol)) {
            return createTreeNode(index, symbol);
        }
        return createMatchNode(symbol, index);
    }

    private Node createTreeNode(int index, String symbol) {
        List<Node> children = buildTree(index, symbol);
        Optional<Node> error = findError(children);
        if (error.isPresent()) {
            return new Node(source, symbol, index, children, error.get().getError().get());
        }
        return new Node(source, symbol, index, children);
    }

    private Node createMatchNode(String symbol, int index) {
        Matcher matcher = matchers.computeIfAbsent(symbol, this::createMatcher);
        if (matcher.find(index) && matcher.start() == index) {
            return new Node(source, symbol, index, matcher.end());
        }
        return new Node(source, symbol, index, NO_MATCH);
    }

    private static Optional<Node> findError(List<Node> nodes) {
        return nodes.stream().filter(node -> node.getError().isPresent()).findAny();
    }

    private Matcher createMatcher(String symbol) {
        return Pattern.compile(symbol, Pattern.CASE_INSENSITIVE).matcher(source);
    }

    private int skipWhitespaceAndComment(int startIndex) {
        return skips.computeIfAbsent(startIndex, this::skip);
    }

    private int skip(int startIndex) {
        int endIndex = startIndex;
        for (;;) {
            endIndex = skipWhitespace(endIndex);
            Optional<CommentBrackets> brackets = findCommentBrackets(endIndex);
            if (brackets.isEmpty()) {
                return endIndex;
            }
            endIndex = skipComment(endIndex, brackets.get());
            if (endIndex < 0) {
                return -1;
            }
        }
    }

    private int skipWhitespace(int startIndex) {
        int index = startIndex;
        while (index < source.length() && Character.isWhitespace(source.charAt(index))) {
            index++;
        }
        return index;
    }

    private int skipComment(int index, CommentBrackets brackets) {
        if (brackets.isBlockComment()) {
            return skipBlockComment(index, brackets);
        }
        return skipLineComment(index, brackets);
    }

    private int skipBlockComment(int startIndex, CommentBrackets brackets) {
        int endIndex = source.indexOf(brackets.getEnd(), startIndex + brackets.getStart().length());
        if (endIndex < 0) {
            return -1;
        }
        return endIndex + brackets.getEnd().length();
    }

    private int skipLineComment(int startIndex, CommentBrackets brackets) {
        int endIndex = source.indexOf('\n', startIndex + brackets.getStart().length());
        if (endIndex < 0) {
            return source.length();
        }
        return endIndex + 1;
    }

    private Optional<CommentBrackets> findCommentBrackets(int index) {
        return grammar.getCommentBrackets().stream().filter(entry -> source.startsWith(entry.getStart(), index)).findAny();
    }

    private String source;
    private final Map<String, Matcher> matchers = new HashMap<>();
    private final Map<Integer, Integer> skips = new HashMap<>();

    private final Grammar grammar;

    private static final String NO_MATCH = "No match";
    private static final String UNPARSABLE_CODE_AFTER_SYMBOL = "Unparsable code after symbol [%s]";
    private static final String UNTERMINATED_COMMENT = "Unterminated comment";

}
