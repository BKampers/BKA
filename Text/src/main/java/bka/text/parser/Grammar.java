package bka.text.parser;

import java.util.*;
import java.util.stream.*;

/**
 */
public class Grammar {

    public Grammar(Map<String, List<List<String>>> rules, Collection<CommentBrackets> commentBrackets) {
        this.rules = unmodifiableDeepCopy(rules);
        this.commentBrackets = List.copyOf(commentBrackets);
    }

    private static Map<String, List<List<String>>> unmodifiableDeepCopy(Map<String, List<List<String>>> ruleMap) {
        return ruleMap.entrySet().stream()
            .collect(Collectors.toUnmodifiableMap(
                Map.Entry::getKey,
                entry -> entry.getValue().stream()
                    .map(List::copyOf)
                    .collect(Collectors.toUnmodifiableList())));
    }

    public Collection<String> getNonterminals() {
        return rules.keySet();
    }

    public List<List<String>> getSententials(String nonterminal) {
        return rules.get(nonterminal);
    }


    public Collection<CommentBrackets> getCommentBrackets() {
        return commentBrackets;
    }

    private final Map<String, List<List<String>>> rules;
    private final Collection<CommentBrackets> commentBrackets;

}
