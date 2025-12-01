package bka.text.parser;

import java.util.*;

public class CommentBrackets {

    public static CommentBrackets blockComment(String start, String end) {
        return new CommentBrackets(start, Objects.requireNonNull(end));
    }

    public static CommentBrackets lineComment(String start) {
        return new CommentBrackets(start, null);
    }

    private CommentBrackets(String start, String end) {
        this.start = Objects.requireNonNull(start);
        this.end = end;
    }

    public String getStart() {
        return start;
    }

    public String getEnd() {
        if (end == null) {
            throw new IllegalStateException("Line comment does not have an end bracket");
        }
        return end;
    }

    public boolean isBlockComment() {
        return end != null;
    }

    private final String start;
    private final String end;

}
