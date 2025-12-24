/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans, humanity, the environment or the universe.
*/
package bka.text.parser;

import java.util.*;
import java.util.function.*;
import java.util.regex.*;

public class Parser {

    public Parser(Grammar grammar) {
        if (grammar.hasSelfProducingRule()) {
            throw new IllegalArgumentException("Grammar contains self-producing rule");
        }
        this.grammar = grammar;
    }

    public Node parse(String sourceCode) {
        return parse(sourceCode, grammar.getStartSymbol().get());
    }

    public Node parse(String sourceCode, String startSymbol) {
        if (!grammar.getRules().getNonterminals().contains(startSymbol)) {
            throw new IllegalArgumentException("Invalid start symbol: '" + startSymbol + "'");
        }
        return new Engine(sourceCode).parse(startSymbol);
    }


    private class Engine {

        private Engine(String source) {
            this.source = Objects.requireNonNull(source);
        }

        public Node parse(String startSymbol) {
            Node tree = createTreeNode(0, startSymbol);
            if (tree.getError().isPresent()) {
                return tree;
            }
            return validateRemainder(tree);
        }

        private Node createTreeNode(int index, String symbol) {
            List<Node> children = buildTree(index, symbol);
            Optional<Node> error = findError(children);
            if (error.isPresent()) {
                return new Node(source, symbol, index, children, error.get().getError().get());
            }
            return new Node(source, symbol, index, children);
        }

        private List<Node> buildTree(int index, String nonterminal) {
            int headIndex = skipWhitespaceAndComment(index);
            if (headIndex < 0) {
                return List.of(new Node(source, nonterminal, index, UNTERMINATED_COMMENT));
            }
            List<Node> resolution = resolve(nonterminal, headIndex);
            if (findError(resolution).isPresent()) {
                return resolution;
            }
            List<Node> recursiveResolution = resolveLeftRecursive(nonterminal, headIndex, resolution);
            return (recursiveResolution != null) ? recursiveResolution : resolution;
        }

        private List<Node> resolve(String nonterminal, int headIndex) {
            List<Node> errorList = new ArrayList<>();
            for (Sentential sentential : grammar.getSententials(nonterminal).stream().filter(isLeftRecursive(nonterminal).negate()).toList()) {
                List<Node> resolution = resolve(headIndex, sentential.getSymbols());
                Optional<Node> error = findError(resolution);
                if (error.isEmpty()) {
                    return resolution;
                }
                errorList.add(new Node(source, nonterminal, headIndex, resolution, error.get().getError().get()));
            }
            return errorList;
        }

        private List<Node> resolveLeftRecursive(String nonterminal, int headIndex, List<Node> resolutionHead) {
            List<Sentential> leftRecursiveSententials = grammar.getSententials(nonterminal).stream().filter(isLeftRecursive(nonterminal)).toList();
            if (leftRecursiveSententials.isEmpty()) {
                return null;
            }
            int tailIndex = skipWhitespaceAndComment(resolutionHead.getLast().getEnd());
            if (tailIndex < 0) {
                return List.of(new Node(source, nonterminal, resolutionHead.getLast().getEnd(), UNTERMINATED_COMMENT));
            }
            for (Sentential sentential : leftRecursiveSententials) {
                List<Node> resolutionTail = resolve(tailIndex, tail(sentential));
                if (findError(resolutionTail).isEmpty()) {
                    List<Node> resolution = new ArrayList<>();
                    Node head = new Node(source, nonterminal, headIndex, resolutionHead);
                    resolution.add(head);
                    resolution.addAll(resolutionTail);
                    return resolution;
                }
            }
            return null;
        }

        private static Predicate<Sentential> isLeftRecursive(String nonterminal) {
            return sentential -> Grammar.isLeftRecursiveRule(nonterminal, sentential);
        }

        private static List<String> tail(Sentential sentential) {
            return sentential.getSymbols().stream().skip(1).toList();
        }

        private List<Node> resolve(int index, List<String> symbols) {
            List<Node> resolution = new ArrayList<>();
            int sourceIndex = index;
            for (String symbol : symbols) {
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
            if (grammar.getRules().getNonterminals().contains(symbol)) {
                return createTreeNode(index, symbol);
            }
            return createMatchNode(symbol, index);
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
            return (brackets.isBlockComment())
                ? skipBlockComment(index, brackets)
                : skipLineComment(index, brackets);
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
            return grammar.getCommentBrackets().stream().filter(brackets -> source.startsWith(brackets.getStart(), index)).findAny();
        }

        private final String source;
        private final Map<String, Matcher> matchers = new HashMap<>();
        private final Map<Integer, Integer> skips = new HashMap<>();
    }

    private final Grammar grammar;

    private static final String NO_MATCH = "No match";
    private static final String UNPARSABLE_CODE_AFTER_SYMBOL = "Unparsable code after symbol [%s]";
    private static final String UNTERMINATED_COMMENT = "Unterminated comment";

}
