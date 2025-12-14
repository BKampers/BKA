package bka.text.parser;

import java.util.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class RulesTest {

    @BeforeAll
    public static void init() {
        rules = new Rules(Map.of(
            "S", List.of(Sentential.of("a", "T")),
            "T", List.of(Sentential.of("b", "c"))
        ));
    }

    @Test
    public void testGetNonterminals() {
        assertEquals(
            Set.of("S", "T"),
            rules.getNonterminals());
    }

    @Test
    public void testGetSententials() {
        assertEquals(List.of(List.of("a", "T")), asLists(rules.getSententials("S")));
        assertEquals(List.of(List.of("b", "c")), asLists(rules.getSententials("T")));
        assertThrows(NoSuchElementException.class, () -> rules.getSententials("U"));
    }

    @Test
    public void testInvalidInstantiations() {
        assertThrows(IllegalArgumentException.class, () -> new Rules(Map.of("", List.of(Sentential.of("a")))));
    }

    private static List<List<String>> asLists(List<Sentential> sententials) {
        return sententials.stream().map(Sentential::getSymbols).toList();
    }

    private static Rules rules;
}
