package bka.text.parser;

import java.util.*;

/**
 */
public class Grammar {

    public static Grammar of(Rules rules) {
        return new Grammar(rules, null, Collections.emptyList());
    }

    public static Grammar of(Rules rules, String startSymbol) {
        return new Grammar(rules, Objects.requireNonNull(startSymbol), Collections.emptyList());
    }

    public static Grammar of(Rules rules, Collection<CommentBrackets> commentBrackets) {
        return new Grammar(rules, null, commentBrackets);
    }

    public static Grammar of(Rules rules, String startSymbol, Collection<CommentBrackets> commentBrackets) {
        return new Grammar(rules, Objects.requireNonNull(startSymbol), commentBrackets);
    }

    private Grammar(Rules rules, String startSymbol, Collection<CommentBrackets> commentBrackets) {
        this.rules = Objects.requireNonNull(rules);
        this.startSymbol = startSymbol;
        this.commentBrackets = List.copyOf(commentBrackets);

    }

    public Rules getRules() {
        return rules;
    }

    public Optional<String> getStartSymbol() {
        return Optional.ofNullable(startSymbol);
    }

    public List<Sentential> getSententials(String nonterminal) {
        return rules.getSententials(nonterminal);
    }


    public Collection<CommentBrackets> getCommentBrackets() {
        return commentBrackets;
    }

    private final Rules rules;
    private final String startSymbol;
    private final Collection<CommentBrackets> commentBrackets;

}
