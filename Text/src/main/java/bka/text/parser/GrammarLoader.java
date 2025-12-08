package bka.text.parser;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 */
public class GrammarLoader {

    private GrammarLoader() {
        // Util class must not be initialized.
    }

    public static Grammar loadJsonFile(String filename) throws IOException {
        GrammarTransporter grammar = new ObjectMapper().readValue(Paths.get(filename).toFile(), GrammarTransporter.class);
        return new Grammar(grammar.rules, grammar.startSymbol, grammar.unmarshallCommentBrackets());
    }

    private record GrammarTransporter(
        @JsonProperty("rules") Map<String, List<List<String>>> rules,
        @JsonProperty("start-symbol") String startSymbol,
        @JsonProperty("comments")
        Collection<CommentBracketsTransporter> comments) {

        public Collection<CommentBrackets> unmarshallCommentBrackets() {
            return (comments == null)
                ? Collections.emptyList()
                : comments.stream().map(CommentBracketsTransporter::unmarshall).toList();
        }

    }

    private record CommentBracketsTransporter(
        @JsonProperty("start") String start,
        @JsonProperty("end") String end) {

        public CommentBrackets unmarshall() {
            return (end == null)
                ? CommentBrackets.lineComment(start)
                : CommentBrackets.blockComment(start, end);
        }

    }

}
