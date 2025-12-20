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
        if (startSymbol != null && !rules.getNonterminals().contains(startSymbol)) {
            throw new IllegalArgumentException(startSymbol + " is not a nonterminal of given rules");
        }
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

    /**
     * Checks for self producing rules.
     *
     * @see #isSelfProducingRule(String, Sentential)
     * @return true if this grammar contains a self producing rule, false otherwise.
     */
    public boolean hasSelfProducingRule() {
        return rules.getNonterminals().stream()
            .anyMatch(nonterminal -> rules.getSententials(nonterminal).stream()
            .anyMatch(sentential -> isSelfProducingRule(nonterminal, sentential)));
    }

    /**
     * Determines if a production rule is self-producing. A rule is self-producing if it is non-empty and every symbol in the sentential form (the
     * right-hand side) is identical to the nonterminal on the left-hand side. These rules are generally considered unproductive or cyclic, as they do
     * not introduce new terminals or different nonterminals into the derivation.
     *
     * @param nonterminal The nonterminal symbol on the left-hand side.
     * @param sentential The sentential form on the right-hand side.
     * @return true if the rule described by the given nonterminal and sentential is self-producing false otherwise.
     */
    public static boolean isSelfProducingRule(String nonterminal, Sentential sentential) {
        return !sentential.isErasing() && sentential.getSymbols().stream().allMatch(symbol -> symbol.equals(nonterminal));
    }

    /**
     * Determines if a production rule is left-recursive. A rule is considered left-recursive if the nonterminal on the left-hand side appears as the
     * first symbol in its sentential form (the right-hand side), provided the sentential form contains additional symbols to allow for expansion.
     *
     * @param nonterminal The nonterminal symbol on the left-hand side of the production.
     * @param sentential The sentential form (right-hand side) of the production rule.
     * @return true if the rule described by the given nonterminal and sentential is left-recursive; false otherwise.
     */
    public static boolean isLeftRecursiveRule(String nonterminal, Sentential sentential) {
        return sentential.length() > 1 && nonterminal.equals(sentential.getSymbols().getFirst());
    }


    private final Rules rules;
    private final String startSymbol;
    private final Collection<CommentBrackets> commentBrackets;

}
