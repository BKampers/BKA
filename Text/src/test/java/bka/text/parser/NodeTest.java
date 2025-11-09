/*
** © Bart Kampers
*/

package bka.text.parser;

import java.util.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NodeTest {

    @Test
    public void testCreateTree() {
        Node sentence = createTree();
        assertTrue(sentence.getError().isEmpty());
        assertTrue(sentence.getChildren().stream().allMatch(child -> child.getError().isEmpty()));
    }

    @Test
    public void testFindChild() {
        Node sentence = createTree();
        assertTrue(sentence.findChild("Subject").isPresent());
        assertTrue(sentence.findChild("FiniteVerb").isPresent());
        assertTrue(sentence.findChild("Predicate").isPresent());
        assertFalse(sentence.findChild("Object").isPresent());
    }

    @Test
    public void testContent() {
        Node sentence = createTree();
        assertEquals(SOURCE, sentence.content());
        assertEquals("I", sentence.getChild("Subject").content());
        assertEquals("can", sentence.getChild("FiniteVerb").content());
        assertEquals("parse", sentence.getChild("Predicate").content());
        assertEquals(".", sentence.getChild("Period").content());
    }

    @Test
    public void testLine() {
        Node sentence = createTree();
        assertEquals(1, sentence.startLine());
        assertEquals(1, sentence.getChild("Subject").startLine());
        assertEquals(2, sentence.getChild("FiniteVerb").startLine());
        assertEquals(3, sentence.getChild("Predicate").startLine());
        assertEquals(3, sentence.getChild("Period").startLine());
    }

    @Test
    public void testError() {
        Node error = new Node("", "Sentence", 0, "Cannot parse empty source");
        assertTrue(error.getError().isPresent());
        assertEquals(0, error.getStart());
        assertEquals(0, error.getEnd());
        assertTrue(error.content().isEmpty());
    }

    @Test
    public void testErrorWithChildren() {
        String invalidSource = "parse cannot You";
        Node error = new Node(
            invalidSource,
            "Sentence", 6,
            List.of(
                new Node(invalidSource, "FiniteVerb", 0, 5),
                new Node(invalidSource, "Subject", 6, "Subject expected")
            ),
            "Syntax error");
        assertTrue(error.getError().isPresent());
        assertEquals(6, error.getStart());
        assertEquals(6, error.getEnd());
        assertTrue(error.content().isEmpty());
    }

    private static Node createTree() {
        return new Node(SOURCE, "Sentence", 0, List.of(
            new Node(SOURCE, "Subject", 0, 1),
            new Node(SOURCE, "FiniteVerb", 2, 5),
            new Node(SOURCE, "Predicate", 6, 11),
            new Node(SOURCE, "Period", 11, 12)));
    }

    private static final String SOURCE = "I\ncan\nparse.";
}