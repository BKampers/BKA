package bka.text.parser;

import java.io.*;
import java.util.*;
import java.util.function.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GrammarLoaderTest {

    @Test
    public void testSimpleGrammar() throws IOException {
        Grammar grammar = GrammarLoader.loadJsonFile("src/test/resources/parser/simple-grammar.json");
        assertEquals(Set.of("S"), grammar.getNonterminals());
        assertEquals(List.of(List.of("a")), grammar.getSententials("S"));
        assertEquals(Optional.of("S"), grammar.getStartSymbol());
        assertTrue(grammar.getCommentBrackets().isEmpty());
    }

    @Test
    public void testMultiCommentGrammar() throws IOException {
        Grammar grammar = GrammarLoader.loadJsonFile("src/test/resources/parser/multi-comment-grammar.json");
        Collection<CommentBrackets> brackets = grammar.getCommentBrackets();
        assertEquals(2, brackets.size());
        Optional<CommentBrackets> blockComment = brackets.stream().filter(isBlockComment).findAny();
        assertTrue(blockComment.isPresent());
        assertEquals("/*", blockComment.get().getStart());
        assertEquals("*/", blockComment.get().getEnd());
        Optional<CommentBrackets> lineComment = brackets.stream().filter(isLineComment).findAny();
        assertTrue(lineComment.isPresent());
        assertEquals("//", lineComment.get().getStart());
    }

    private static final Predicate<CommentBrackets> isBlockComment = CommentBrackets::isBlockComment;
    private static final Predicate<CommentBrackets> isLineComment = isBlockComment.negate();

}
