/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans, humanity, the environment or the universe.
*/
package bka.text.parser.pascal;

import bka.text.parser.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

public class PascalParser extends GrammarParser {

    public PascalParser() {
        super(loadGrammar(), List.of(CommentBrackets.blockComment("(*", "*)")));
    }

    private static Map<String, List<List<String>>> loadGrammar() {
        try {
            return new ObjectMapper().readValue(Paths.get("resources/grammars/pascal.json").toFile(), Map.class);
        }
        catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public String parse(String sourceCode) {
        return parse(sourceCode, ROOT_SYMBOL);
    }

    public Node buildTree(String sourceCode) {
        return buildTree(sourceCode, ROOT_SYMBOL);
    }

    private static final String ROOT_SYMBOL = "Program";

}
