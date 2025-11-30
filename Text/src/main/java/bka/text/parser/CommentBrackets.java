package bka.text.parser;

import java.util.*;

public class CommentBrackets {

    public static CommentBrackets blockComment(String start, String end) {
        return new CommentBrackets(Objects.requireNonNull(start), Objects.requireNonNull(end));
    }

    public static CommentBrackets lineComment(String start) {
        return new CommentBrackets(Objects.requireNonNull(start), null);
    }

    private CommentBrackets(String start, String end) {
        this.start = start;
        this.end = end;
    }

    public String getStart() {
        return start;
    }

    public String getEnd() {
        return Objects.requireNonNull(end);
    }

    public boolean isBlockComment() {
        return end != null;
    }

    private final String start;
    private final String end;

}
