/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans, humanity, the environment or the universe.
*/
package bka.text.parser;

import java.util.*;
import java.util.stream.*;


public final class Node {

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
        this.source = Objects.requireNonNull(source);
        this.symbol = Objects.requireNonNull(symbol);
        this.start = requireValidStartIndex(start);
        this.end = requireValidEndIndex(end);
        this.children = children.stream().collect(Collectors.toUnmodifiableList());
        this.error = error;
    }

    private int requireValidStartIndex(int index) {
        if (index < 0 || source.length() < index) {
            throw new IndexOutOfBoundsException(index);
        }
        return index;
    }

    private int requireValidEndIndex(int index) {
        if (index < start || source.length() < index) {
            throw new IndexOutOfBoundsException(index);
        }
        return index;
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
    
    public boolean startsWith(String symbol) {
        return !children.isEmpty() && symbol.equals(children.getFirst().getSymbol());
    }

    public Node getChild(String symbol) {
        return findChild(symbol).orElseThrow(() -> new NoSuchElementException("Node ```" + content() + "``` has no child of '" + symbol + '\''));
    }
    
    public Optional<Node> findChild(String symbol) {
        return children.stream()
            .filter(node -> symbol.equals(node.symbol))
            .findFirst();
    }

    public int startLine() {
        return (int) source.chars()
            .limit(start)
            .filter(ch -> ch == '\n')
            .count();
    }

    public List<Node> findChildren(String symbol) {
        return children.stream()
            .filter(node -> symbol.equals(node.symbol))
            .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        error.ifPresent(message -> builder.append("Error: '").append(message).append("' "));
        builder.append(symbol);
        builder.append(children.stream().map(Node::getSymbol).collect(Collectors.joining(", ", " (", ") ")));
        builder.append("```").append(content()).append("```");
        return builder.toString();
    }

    private final String source;
    private final String symbol;
    private final List<Node> children;
    private final int start;
    private final int end;
    private final Optional<String> error;

}
