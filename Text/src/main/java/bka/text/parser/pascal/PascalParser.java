/*
** © Bart Kampers
*/

package bka.text.parser.pascal;

import bka.text.parser.*;
import java.util.*;
import java.util.regex.*;

public class PascalParser {

    /**
     */
    PascalParser(Map<String, List<List<String>>> grammar) {
        this.grammar = Objects.requireNonNull(grammar);
    }

    public String parse(String sourceCode) {
        source = sourceCode.toUpperCase();
//        _parse();
        error = null;
        errorIndex = -1;
        long startTime = System.nanoTime();
        parseGlyphs();
        for (int i = 0; i < glyphs.size(); ++i) {
            System.out.println(i + ":" + glyphs.get(i).getText());
        }
        Node root = new Node();
        int result = parseSymbols(0, "Program", root);
        long duration = System.nanoTime() - startTime;
        if (result < 0) {
            return "Error: " + error;
        }
        dump(root, 0);
        return ("Program parsed successfully in " + (duration / 1000) + " microseconds");
    }

    private void dump(Node node, int depth) {
        for (int i = 0; i < depth; ++i) {
            System.out.print('\t');
        }
        System.out.println(node);
        if (node.children != null) {
            for (Node child : node.children) {
                dump(child, depth + 1);
            }
        }
    }

    private void _parse() {
        int index = 0;
//        while (index < source.length()) {
//            while (Character.isWhitespace(source.charAt(index))) {
//                index++;
//            }
            scan(index);
//        }
        sourceSymbols.forEach(System.out::println);
    }

    private void scan(int index) {
        Pattern pattern = Pattern.compile(
                "(?<comment>\\(\\*)|"
                + "(?<keyword>AND|ARRAY|BEGIN|BOOLEAN|BREAK|CASE|CONST|DO|ELSE|END|FALSE|FOR|FUNCTION|INTEGER|IF|NOT|OF|OR|OTHERWISE|PROCEDURE|PROGRAM|REAL|RECORD|REPEAT|STRING|THEN|TO|TRUE|TYPE|UNTIL|VAR|WHILE|XOR)([^A-Z0-9_])|"
                + "(?<identifier>[A-Z]+[A-Z0-9_]*)|"
                + "(?<symbol>\\;|\\,|\\(|\\)|\\[|\\]|\\<|\\>|\\<\\=|\\>\\=|\\<\\>|\\.\\.|\\.|\\:\\=|\\:|\\^|\\*|\\/|\\+|\\-|\\=)|"
                + "(?<string>\\'.*\\')|"
                + "(?<real>\\d+\\.\\d*(E[-+]?\\d+)?)|"
                + "(?<integer>\\d+|\\$[0-9A-F]+)");
        Matcher matcher = pattern.matcher(source);
        while (matcher.find(index)) {
            for (String group : new String[]{"keyword", "symbol", "identifier", "real", "integer", "string", "comment"}) {
                if (matcher.group(group) != null) {
                    if (!group.equals("comment")) {
                        sourceSymbols.add(new SourceSymbol(index, matcher.group(group)));
                        index = matcher.end(group);
                    }
                    else {
                        Matcher endMatcher = Pattern.compile("\\*\\)").matcher(source);
                        if (!endMatcher.find(index + 2)) {
                            throw new IllegalStateException();
                        }
                        index = endMatcher.end();
                    }
                    break;
                }
            }
        }
    }

    private SourceSymbol scanKeyword(int index) {
        Pattern pattern = Pattern.compile("(?<keyword>AND|ARRAY|BEGIN|BOOLEAN|BREAK|CASE|CONST|DO|ELSE|END|FALSE|FOR|FUNCTION|INTEGER|IF|NOT|OF|OR|OTHERWISE|PROCEDURE|PROGRAM|REAL|RECORD|REPEAT|STRING|THEN|TO|TRUE|TYPE|UNTIL|VAR|WHILE|XOR)([^A-Z0-9_])+");
        Matcher matcher = pattern.matcher(source);
        if (matcher.find(index)) {
            SourceSymbol sourceSymbol = new SourceSymbol(index, matcher.group("keyword"));
            return sourceSymbol;
        }
        return null;
    }

    private SourceSymbol scanIdentifier(int index) {
        try {
            Pattern pattern = Pattern.compile("(?<identifier>[A-Z]+[A-Z0-9_]*)");
            Matcher matcher = pattern.matcher(source);
            if (matcher.find(index)) {
                SourceSymbol sourceSymbol = new SourceSymbol(index, matcher.group("identifier"));
                return sourceSymbol;
            }
        }
        catch (RuntimeException ex) {
            throw ex; //FIXME
        }
        return null;
    }

    private int parseSymbols(int index, String symbol, Node node) {
        index = skipWhitespaceAndComment(index);
        node.symbol = symbol;
        List<List<String>> expression = grammar.get(symbol);
        if (expression != null) {
            return parseExpression(index, symbol, expression, node);
        }
        return parseSymbol(index, symbol, node);
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
                    errorIndex = index;
                    error = "Unterminated comment";
                }
                else {
                    index += COMMENT_END.length();
                    ready = false;
                }
            }
        }
        return index;
    }

    private static final String COMMENT_START = "(*";
    private static final String COMMENT_END = "*)";

    private int parseExpression(int index, String name, List<List<String>> expression, Node node) {
        List<List<String>> choices = new ArrayList<>();
        List<List<String>> extendedChoices = new ArrayList();
//        List<String> exclusions = new ArrayList<>();
        for (List<String> choice : expression) {
            if (!choice.isEmpty() && choice.get(0).startsWith("^")) {
//                exclusions.add(choice.get(0).substring(1));
            }
            else if (choice.isEmpty() || !name.equals(choice.get(0))) {
                choices.add(choice);
            }
            else {
                extendedChoices.add(remainder(choice));
            }
        }
        for (List<String> choice : choices) {
            int next = parseChoice(index, choice, node);
            if (next >= 0) {
                for (List<String> remainder : extendedChoices) {
                    Node extenderNode = new Node();
                    int extendedNext = parseChoice(next, remainder, extenderNode);
                    if (extendedNext >= 0) {
                        for (Node child : extenderNode.children) {
                            child.parent = node;
                            node.children.add(child);
                        }
//                        for (String exclusion : exclusions) {
//                            Node exclusionNode = new Node();
//                            if (parseSymbols(index, exclusion, exclusionNode) >= 0) {
//                                return -7;
//                            }
//                        }
                        return extendedNext;
                    }
                }
//                for (String exclusion : exclusions) {
//                    Node exclusionNode = new Node();
//                    if (parseSymbols(index, exclusion, exclusionNode) >= 0) {
//                        return -7;
//                    }
//                }
                return next;
            }
        }
        return -1;
    }

    private static List<String> remainder(List<String> choice) {
        List<String> remainder = new LinkedList<>(choice);
        remainder.remove(0);
        return remainder;
    }

    private int parseChoice(int index, List<String> choice, Node node) {
        List<Node> children = new ArrayList<>(choice.size());
        int next = index;
        Iterator<String> it = choice.iterator();
        while (next >= 0 && it.hasNext()) {
            Node child = new Node();
            children.add(child);
            child.parent = node;
            next = parseSymbols(next, it.next(), child);
        }
        if (next >= 0) {
            node.children = children;
        }
        return next;
    }

    private int parseSymbol(int index, String symbol, Node node) {
//        SourceSymbol sourceSymbol = match(index);
//        if (sourceSymbol == null) {
//            if (index > errorIndex) {
//                error = "Unexpected end of source: '" + symbol + "' expected.";
//                errorIndex = index;
//            }
//            return -3;
//        }
        int next = match(index, symbol);
        if (next < 0) {
            if (index > errorIndex) {
                error = "'" + symbol + "' expected, found '" + source.substring(index) + "'.";
                errorIndex = index;
            }
            return -4;

        }
//        if (!matches(symbol, sourceSymbol)) {
//            if (index > errorIndex) {
//                error = "'" + symbol + "' expected, found '" + sourceSymbol.text + "'.";
//                errorIndex = index;
//            }
//            return -4;
//        }
        node.symbol = symbol;
        node.name = source.substring(index, next);
        return next;
//        return -3;
//        if (sourceSymbols.size() <= index) {
//            if (index > errorIndex) {
//                error = "Unexpected end of source, '" + symbol + "' expected.";
//                errorIndex = index;
//            }
//            return -2;
//        }
//        if (!matches(symbol, sourceSymbols.get(index))) {
//            if (index > errorIndex) {
//                error = "'" + symbol + "' expected, found '" + sourceSymbols.get(index).text + "'.";
//                errorIndex = index;
//            }
//            return -3;
//        }
//        node.symbol = symbol;
//        node.name = sourceSymbols.get(index).text;
//        return index + 1;
    }

    private static int getClass(char character) {
        if ('A' <= character && character <= 'Z' || '0' <= character && character <= '9' || character == '_') {
            return 0;
        }
        return 1;
    }

    private int match(int index, String symbol) {
        Glyph glyph = glyphs.stream().filter(g -> g.getBegin() == index).findAny().orElse(null);
        if (glyph == null) {
            return -6;
        }
        Matcher matcher = Pattern.compile(symbol).matcher(glyph.getText());
        if (!matcher.matches()) {
            return -5;
        }
        return glyph.getEnd();
//        Matcher matcher = Pattern.compile(symbol).matcher(source);
//        if (matcher.find(index) && matcher.start() == index) {
//            if (matcher.groupCount() > 0) {
//                return matcher.end("i");
//            }
//            return matcher.end();
//        }
//        return -5;
    }

    private SourceSymbol match(int index) {
        Pattern pattern = Pattern.compile(
                "(?<comment>\\(\\*)|"
                + "(?<keyword>AND|ARRAY|BEGIN|BOOLEAN|BREAK|CASE|CONST|DO|ELSE|END|FALSE|FOR|FUNCTION|INTEGER|IF|NOT|OF|OR|OTHERWISE|PROCEDURE|PROGRAM|REAL|RECORD|REPEAT|STRING|THEN|TO|TRUE|TYPE|UNTIL|VAR|WHILE|XOR)([^A-Z_0-9])|"
                + "(?<identifier>[A-Z]+[A-Z0-9_]*)|"
                + "(?<symbol>\\;|\\,|\\(|\\)|\\[|\\]|\\<|\\>|\\<\\=|\\>\\=|\\<\\>|\\.\\.|\\.|\\:\\=|\\:|\\^|\\*|\\/|\\+|\\-|\\=)|"
                + "(?<string>\\'.*\\')|"
                + "(?<real>\\d+\\.\\d*(E[-+]?\\d+)?)|"
                + "(?<integer>\\d+|\\$[0-9A-F]+)");
        Matcher matcher = pattern.matcher(source);
        while (matcher.find(index)) {
            for (String group : new String[]{"keyword", "symbol", "identifier", "real", "integer", "string", "comment"}) {
                if (matcher.group(group) != null) {
                    if (!group.equals("comment")) {
                        return new SourceSymbol(matcher.start(group), matcher.group(group));
                    }
                    else {
                        Matcher endMatcher = Pattern.compile("\\*\\)").matcher(source);
                        if (!endMatcher.find(index + 2)) {
                            throw new IllegalStateException("Unterminated comment");
                        }
                        index = endMatcher.end();
                    }
                    break;
                }
            }
        }
        return null;
    }

    private boolean matches(String symbol, SourceSymbol sourceSymbol) {
        return matches(symbol, sourceSymbol.text);
    }

    private boolean matches(String symbol, Glyph glyph) {
        return matches(symbol, glyph.getText());
    }

    private boolean matches(String symbol, String string) {
        try {
            Pattern pattern = Pattern.compile(symbol);
            return (pattern.matcher(string).matches());
        }
        catch (RuntimeException ex) {
            throw ex; //FIXME
        }
    }

    private List<SourceSymbol> getSource(String code, String lineBreak) {
        List<SourceSymbol> source = new ArrayList<>();
        for (String line : code.split(lineBreak)) {
            int index = 0;
            while (index < line.length()) {
                char start = line.charAt(index);
                if (Character.isWhitespace(start)) {
                    index++;
                }
                else if (start == '(' && index + 3 < line.length() && line.charAt(index + 1) == '*') {
                    index = skipComment(index);
                }
                else if (isIdentifierStartCharacter(start)) {
                    index = addWord(index, glyphs);
                }
                else if (start == '\'') {
                    index = addStringLiteral(index, glyphs);
                }
                else if (isAsciiDigit(start)) {
                    index = addNumberLiteral(index, glyphs);
                }
                else {
                    index = addSymbol(index, glyphs);
                }
            }
        }
        throw new UnsupportedOperationException();
    }
    
    private List<Glyph> parseGlyphs() {
        int index = 0;
        while (index < source.length()) {
            char start = source.charAt(index);
            if (Character.isWhitespace(start)) {
                index++;
            }
            else if (start == '(' && index + 3 < source.length() && source.charAt(index + 1) == '*') {
                index = skipComment(index);
            }
            else if (isIdentifierStartCharacter(start)) {
                index = addWord(index, glyphs);
            }
            else if (start == '\'') {
                index = addStringLiteral(index, glyphs);
            }
            else if (isAsciiDigit(start)) {
                index = addNumberLiteral(index, glyphs);
            }
            else {
                index = addSymbol(index, glyphs);
            }
        }
        return glyphs;
    }

    private int skipComment(int begin) {
        int end = begin + 2;
        while (end + 1 < source.length()) {
            if (source.charAt(end) == '*' && source.charAt(end + 1) == ')') {
                return end + 2;
            }
            end++;
        }
        throw new IllegalStateException("Unterminated comment");
    }

    private int addWord(int begin, List<Glyph> glyphs) {
        int end = begin + 1;
        while (end < source.length() && isIdentifierCharacter(source.charAt(end))) {
            end++;
        }
        glyphs.add(new Glyph(begin, end));
        return end;
    }

    private int addSymbol(int begin, List<Glyph> glyphs) {
        int end = begin + 1;
        if (!List.of(';', ',', '(', ')', '[', ']').contains(source.charAt(begin))) {
            while (end < source.length() && !Character.isWhitespace(source.charAt(end)) && !isIdentifierCharacter(source.charAt(end))) {
                end++;
            }
        }
        glyphs.add(new Glyph(begin, end));
        return end;
    }

    private int addStringLiteral(int begin, List<Glyph> glyphs) {
        int end = begin + 1;
        while (end < source.length()) {
            char c = source.charAt(end);
            if (c == '\n' || c == '\r') {
                throw new IllegalStateException("Unterminated string");
            }
            end++;
            if (c == '\'') {
                glyphs.add(new Glyph(begin, end));
                return end;
            }
        }
        throw new IllegalStateException("Unexpected end of source");
    }

    private int addNumberLiteral(int begin, List<Glyph> glyphs) {
        int end = begin + 1;
        while (end < source.length() && isNumberCharacterAt(end)) {
            end++;
        }
        glyphs.add(new Glyph(begin, end));
        return end;
    }

    private boolean isNumberCharacterAt(int index) {
        char character = source.charAt(index);
        return character == '.' || character == 'E' || isAsciiDigit(character) || (character == '-' || character == '+') && source.charAt(index - 1) == 'E';
    }

    private static boolean isIdentifierCharacter(char character) {
        return isIdentifierStartCharacter(character) || isAsciiDigit(character);
    }

    private static boolean isIdentifierStartCharacter(char character) {
        return character == '_' || isAsciiAlphabetic(character);
    }

    private static boolean isAsciiAlphabetic(char character) {
        return 'A' <= character && character <= 'Z';
    }

    private static boolean isAsciiDigit(char character) {
        return '0' <= character && character <= '9';
    }

    private Glyph getGlyph(int index) throws ParserException {
        if (glyphs.size() <= index) {
            throw new ParserException("Unexpected end of source");
        }
        return glyphs.get(index);
    }
    
    private class SourceSymbol {

        public SourceSymbol(int index, String text) {
            this.index = index;
            this.text = text;
        }

        @Override
        public String toString() {
            return "" + index + ": " + text;
        }

        private final int index;
        private final String text;
    }

    private class Glyph {

        private Glyph(int begin, int end) {
            this.begin = begin;
            this.end = end;
        }

        int getBegin() {
            return begin;
        }

        int getEnd() {
            return end;
        }

        @Override
        public String toString() {
            return getText();
        }

        boolean is(String string) {
            return getText().equals(string);
        }

        private boolean isIdentifier() {
            Pattern pattern = Pattern.compile("[A-Z_]+[A-Z0-9_]*");
            return (pattern.matcher(getText()).matches());
        }

        private String getText() {
            return source.substring(begin, end);
        }

        private final int begin;
        private final int end;

    }

    private class Node {

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            if (name != null && !name.equals(symbol)) {
                builder.append("'").append(name).append("\' ");
            }
            builder.append(symbol);
            if (children != null) {
                builder.append(" (").append(children.size()).append(')');
            }
            return builder.toString();
        }

        private String name;
        private String symbol;
        private Node parent;
        private List<Node> children;
    }

    private final Map<String, List<List<String>>> grammar;
    private String source;
    private final List<Glyph> glyphs = new ArrayList<>();
    private final List<SourceSymbol> sourceSymbols = new ArrayList<>();

    private int errorIndex;
    private String error;

}
