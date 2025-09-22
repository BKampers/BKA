/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans, humanity, the environment or the universe.
*/
package bka.text.parser.pascal;

import bka.text.parser.Node;
import java.util.*;
import java.util.regex.*;
import java.util.stream.*;

public class PascalParser {

    PascalParser(Map<String, List<List<String>>> grammar) {
        this.grammar = Objects.requireNonNull(grammar);
    }

    public String parse(String sourceCode) {
        long startTime = System.nanoTime();
        Node tree = buildTree(sourceCode);
        long duration = System.nanoTime() - startTime;
        if (tree.getError().isPresent()) {
            return "Error: " + tree.getError().get();
        }
        return "Program parsed successfully in " + (duration / 1000) + " microseconds";
    }

    public Node buildTree(String sourceCode) {
        source = sourceCode;
        matchers.clear();
        return createTreeNode(0, "Program");
    }

    private List<Node> buildTree(int index, String symbol) {
        List<Node> tree = null;
        List<Node> errorTree = null;
        Node errorNode = null;
        List<List<String>> choice = grammar.get(symbol);
        final int i = skipWhitespaceAndComment(index);
        for (List<String> expression : choice) {
            if (tree == null) {
                List<Node> resolution = resolve(i, expression);
                Optional<Node> error = findError(resolution);
                if (error.isEmpty()) {
                    tree = resolution;
                }
                else if (errorNode == null || error.get().getStart() >= errorNode.getStart()) {
                    errorNode = error.get();
                    errorTree = resolution;
                }
            }
            else if (expression.size() > 1 && symbol.equals(expression.getFirst())) {
                List<Node> resolution = resolve(skipWhitespaceAndComment(tree.getLast().getEnd()), remainder(expression));
                Optional<Node> error = findError(resolution);
                if (error.isEmpty()) {
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

    private static List<String> remainder(List<String> expression) {
        return expression.stream().skip(1).collect(Collectors.toList());
    }

    private List<Node> resolve(int index, List<String> expression) {
        List<Node> resolution = new ArrayList<>();
        int sourceIndex = index;
        for (String symbol : expression) {
            Node node = createNode(sourceIndex, symbol);
            resolution.add(node);
            if (node.getError().isPresent()) {
                return resolution;
            }
            sourceIndex = skipWhitespaceAndComment(node.getEnd());
        }
        return resolution;
    }

    private Node createNode(int index, String symbol) {
        if (grammar.keySet().contains(symbol)) {
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
        return new Node(source, symbol, index, "No match");
    }

    private static Optional<Node> findError(List<Node> nodes) {
        return nodes.stream().filter(node -> node.getError().isPresent()).findAny();
    }

    private Matcher createMatcher(String symbol) {
        return Pattern.compile(symbol, Pattern.CASE_INSENSITIVE).matcher(source);
    }

    private int skipWhitespaceAndComment(int index) {
        boolean ready = false;
        while (!ready) {
            while (index < source.length() && Character.isWhitespace(source.charAt(index))) {
                index++;
            }
            ready = true;
            if (source.startsWith(COMMENT_START, index)) {
                index = source.indexOf(COMMENT_END, index + COMMENT_START.length());
                if (index < 0) {
                    return source.length();
                }
                index += COMMENT_END.length();
                ready = false;
            }
        }
        return index;
    }

    private final Map<String, List<List<String>>> grammar;
    private String source;

    private final Map<String, Matcher> matchers = new HashMap<>();

    private static final String COMMENT_START = "(*";
    private static final String COMMENT_END = "*)";

}
