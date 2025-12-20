package bka.text.parser;

import java.util.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 */
public class GrammarTest {

    @Test
    public void testHasSelfproducingRules() {
        assertTrue(Grammar.of(new Rules(Map.of(
            "S", List.of(Sentential.of("S"))))
        ).hasSelfProducingRule());
        assertTrue(Grammar.of(new Rules(Map.of(
            "S", List.of(Sentential.of("T")),
            "T", List.of(Sentential.of("T"))))
        ).hasSelfProducingRule());
        assertFalse(Grammar.of(new Rules(Map.of(
            "S", List.of(Sentential.of("S", "T")),
            "T", List.of(Sentential.of("b"))))
        ).hasSelfProducingRule());
    }

    @Test
    public void testIsSelfProducingRule() {
        assertTrue(Grammar.isSelfProducingRule("S", Sentential.of("S")));
        assertTrue(Grammar.isSelfProducingRule("S", Sentential.of("S", "S")));
        assertFalse(Grammar.isSelfProducingRule("S", Sentential.of()));
        assertFalse(Grammar.isSelfProducingRule("S", Sentential.of("a", "S")));
        assertFalse(Grammar.isSelfProducingRule("S", Sentential.of("S", "a")));
    }

    @Test
    public void testIsLeftRecursiveRule() {
        assertTrue(Grammar.isLeftRecursiveRule("S", Sentential.of("S", "a")));
        assertFalse(Grammar.isLeftRecursiveRule("S", Sentential.of("a", "S")));
    }

}
