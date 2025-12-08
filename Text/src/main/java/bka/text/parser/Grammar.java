package bka.text.parser;

import java.util.*;
import java.util.stream.*;

/**
 */
public class Grammar {

    public Grammar(Map<String, List<List<String>>> rules, Collection<CommentBrackets> commentBrackets) {
        this(rules, null, commentBrackets);
    }

    public Grammar(Map<String, List<List<String>>> rules, String startSymbol, Collection<CommentBrackets> commentBrackets) {
        this.rules = unmodifiableDeepCopy(rules);
        this.startSymbol = startSymbol;
        this.commentBrackets = List.copyOf(commentBrackets);
    }

    private static Map<String, List<List<String>>> unmodifiableDeepCopy(Map<String, List<List<String>>> ruleMap) {
        return ruleMap.entrySet().stream()
            .collect(Collectors.toUnmodifiableMap(
                Map.Entry::getKey,
                entry -> entry.getValue().stream()
                    .map(List::copyOf)
                    .toList()));
    }

    public Optional<String> getStartSymbol() {
        return Optional.ofNullable(startSymbol);
    }

    public Collection<String> getNonterminals() {
        return rules.keySet();
    }

    public List<List<String>> getSententials(String nonterminal) {
        List<List<String>> sententials = rules.get(nonterminal);
        if (sententials == null) {
            throw new NoSuchElementException(nonterminal);
        }
        return sententials;
    }


    public Collection<CommentBrackets> getCommentBrackets() {
        return commentBrackets;
    }

    private final Map<String, List<List<String>>> rules;
    private final String startSymbol;
    private final Collection<CommentBrackets> commentBrackets;

}
