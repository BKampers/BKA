/*
** © Bart Kampers
*/

package bka.text.parser.pascal;

import java.util.*;
import java.util.regex.*;
import java.util.stream.*;

public class PascalParser {

    PascalParser(Map<String, List<List<String>>> grammar) {
        this.grammar = Objects.requireNonNull(grammar);
    }

    public String parse(String sourceCode) {
        long startTime = System.nanoTime();
        List<Node> nodes = buildTree(sourceCode);
        long duration = System.nanoTime() - startTime;
//        nodes.forEach(node -> dump(node, 0));
        Optional<Node> error = findError(nodes);
        if (error.isPresent()) {
            return "Error: " + error.get().toString();
        }
        return ("Program parsed successfully in " + (duration / 1000) + " microseconds");
    }

    public List<Node> buildTree(String sourceCode) {
        source = sourceCode;
        matchers.clear();
        return buildTree(0, "Program");
    }

    private static void dump(Node node, int depth) {
        for (int i = 0; i < depth; ++i) {
            System.out.print('\t');
        }
        System.out.println(node);
        node.getChildren().forEach(child -> dump(child, depth + 1));
    }

    private List<Node> buildTree(int index, String symbol) {
        List<Node> tree = null;
        final int i = skipWhitespaceAndComment(index);
        Node node = new Node(symbol, i);
        node.setError("Could not resolve");
        List<List<String>> choice = grammar.get(symbol);
        List<String> matchingExpression = null;
        for (List<String> expression : choice) {
            if (matchingExpression == null) {
                List<Node> resolution = resolve(i, expression);
                Optional<Node> error = findError(resolution);
                if (error.isEmpty()) {
                    matchingExpression = expression;
                    tree = resolution;
                }
                else if (error.get().getStart() >= node.getStart()) {
                    node = error.get();
                }
            }
            else if (expression.size() >= 2 && symbol.equals(expression.get(0))) {
                List<Node> resolution = resolve(skipWhitespaceAndComment(tree.get(tree.size() - 1).getEnd()), remainder(expression));
                Optional<Node> error = findError(resolution);
                if (error.isEmpty()) {
                    matchingExpression = expression;
                    tree.addAll(resolution);
                }
                else if (error.get().getStart() >= node.getStart()) {
                    node = error.get();
                }
            }
        }
        if (tree == null) {
            return List.of(node);
        }
        return tree;
    }

    private List<Node> resolve(int index, List<String> expression) {
        List<Node> resolution = new ArrayList<>();
        int i = index;
        for (String symbol : expression) {
            Node node = buildNode(i, symbol);
            resolution.add(node);
            if (node.getError() != null) {
                return resolution;
            }
            i = skipWhitespaceAndComment(node.getEnd());
        }
        return resolution;
    }

    private Node buildNode(int index, String symbol) {
        Node node = new Node(symbol, index);
        if (grammar.keySet().contains(symbol)) {
            node.setChildren(buildTree(index, symbol));
            Optional<Node> error = findError(node.getChildren());
            if (error.isPresent()) {
                node.setError(error.get().getError());
            }
            else {
                node.setEnd(node.getChildren().isEmpty() ? node.getStart() : node.getChildren().get(node.getChildren().size() - 1).getEnd());
            }
            return node;
        }
        else {
            Matcher matcher = matchers.computeIfAbsent(symbol, this::createMatcher);
            if (matcher.find(index) && matcher.start() == index) {
                node.setEnd(matcher.end());
                return node;
            }
        }
        node.setError("No match");
        return node;
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

    private static List<String> remainder(List<String> expression) {
        return expression.stream().skip(1).collect(Collectors.toList());
    }

    private static Optional<Node> findError(List<Node> nodes) {
        return nodes.stream().filter(node -> node.getError() != null).findAny();
    }

    public class Node {

        private Node(String symbol, int start) {
            this.symbol = symbol;
            this.start = start;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        public String getSymbol() {
            return symbol;
        }

        public List<Node> getChildren() {
            return children;
        }

        public String getError() {
            return error;
        }

        private void setEnd(int end) {
            this.end = end;
        }

        private void setChildren(List<Node> children) {
            this.children.addAll(children);
        }

        private void setError(String error) {
            this.error = error;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            if (error != null) {
                builder.append("Error: ").append(error).append(' ');
            }
            builder.append(symbol);
            builder.append(" (").append(children.size()).append(')');
            if (0 <= start && start <= end && end < source.length()) {
                builder.append(" '").append(source.substring(start, end)).append('\'');
            }
            return builder.toString();
        }

        public String content() {
            if (0 <= start && start <= end && end <= source.length()) {
                return source.substring(start, end);
            }
            return "";
        }

        public int startLine() {
            return source.substring(0, start).split("\n").length;
        }

        private final String symbol;
        private final int start;
        private int end;
        private final List<Node> children = new ArrayList<>();
        private String error;
    }

    private final Map<String, List<List<String>>> grammar;
    private String source;

    private final Map<String, Matcher> matchers = new HashMap<>();

    private static final String COMMENT_START = "(*";
    private static final String COMMENT_END = "*)";

}
