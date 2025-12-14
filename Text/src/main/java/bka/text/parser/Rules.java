package bka.text.parser;

import java.util.*;
import java.util.stream.*;

/**
 * Production rules for a grammar. A set of nonterminals with their sententials. Each nonterminal can have one or more sententials.
 */
public class Rules {

    public Rules(Map<String, List<Sentential>> ruleMap) {
        this.ruleMap = ruleMap.entrySet().stream().collect(Collectors.toUnmodifiableMap(
            entry -> requireNonEmpty(entry.getKey()),
            entry -> List.copyOf(entry.getValue())));
    }

    private static String requireNonEmpty(String nonterminal) {
        if (nonterminal.isEmpty()) {
            throw new IllegalArgumentException("Empty nonterminal symbol in rules");
        }
        return nonterminal;
    }

    public Set<String> getNonterminals() {
        return ruleMap.keySet();
    }

    /**
     * @param nonterminal
     * @return return all sententials for given nonterminal
     * @throws NoSuchElementException if given nonterminal is not present.
     */
    public List<Sentential> getSententials(String nonterminal) {
        List<Sentential> sententials = ruleMap.get(nonterminal);
        if (sententials == null) {
            throw new NoSuchElementException(nonterminal);
        }
        return sententials;
    }

    private final Map<String, List<Sentential>> ruleMap;

}
