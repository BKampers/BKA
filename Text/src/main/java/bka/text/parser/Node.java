/*
** © Bart Kampers
*/
package bka.text.parser;

import bka.text.parser.Node;
import java.util.*;
import java.util.stream.*;


public class Node {

    public Node(String source, String symbol, int start, int end) {
        this(source, symbol, start, end, Collections.emptyList(), Optional.empty());
    }

    public Node(String source, String symbol, int start, List<Node> children) {
        this(source, symbol, start, (children.isEmpty()) ? start : children.getLast().end, children, Optional.empty());
    }

    public Node(String source, String symbol, int start, List<Node> children, String error) {
        this(source, symbol, start, start, children, Optional.of(error));
    }

    public Node(String source, String symbol, int start, String error) {
        this(source, symbol, start, start, Collections.emptyList(), Optional.of(error));
    }

    private Node(String source, String symbol, int start, int end, List<Node> children, Optional<String> error) {
        if (start < 0 || source.length() < start) {
            throw new IndexOutOfBoundsException(start);
        }
        if (end < start || source.length() < end) {
            throw new IndexOutOfBoundsException(end);
        }
        this.source = Objects.requireNonNull(source);
        this.symbol = Objects.requireNonNull(symbol);
        this.start = start;
        this.end = end;
        this.children = (children.isEmpty()) ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(children));
        this.error = error;
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

    public Optional<String> getError() {
        return error;
    }

    public String content() {
        if (start == end) {
            return "";
        }
        return source.substring(start, end);
    }

    public int startLine() {
        return source.substring(0, start).split("\n").length;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        error.ifPresent(message -> builder.append("Error: ").append(message).append(" "));
        builder.append(symbol);
        builder.append(children.stream().map(Node::getSymbol).collect(Collectors.joining(", ", " (", ") ")));
        builder.append('\'').append(content()).append('\'');
        return builder.toString();
    }

    private final String source;
    private final String symbol;
    private final List<Node> children;
    private final int start;
    private final int end;
    private final Optional<String> error;

}
