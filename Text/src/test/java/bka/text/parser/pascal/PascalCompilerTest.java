/*
** © Bart Kampers
*/

package bka.text.parser.pascal;

import com.fasterxml.jackson.databind.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.*;
import run.*;
import uml.statechart.*;
import uml.structure.*;

public class PascalCompilerTest {

    @BeforeEach
    public void init() {
        parser = new PascalParser(getPascalGrammar());
        walker = new PascalCompiler();
    }

    private static Map<String, List<List<String>>> getPascalGrammar() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(Paths.get("resources/grammars/pascal.json").toFile(), Map.class);
        }
        catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Test
    public void testParseTreeWalker() throws StateMachineException {
        List<PascalParser.Node> tree = parser.buildTree("""
            PROGRAM empty;
            VAR i: INTEGER;
            BEGIN
                i := 8;
                WHILE i > 0 DO
                    i := i - 1;
            END.
            """);
        assertNotNull(tree);
        uml.structure.Class program = (uml.structure.Class) walker.createObject(tree);
        List<Operation> operations = program.getOperations();
        Operation main = operations.getFirst();
        Collection<Transition<Event, GuardCondition, Action>> transitions = walker.getMethod(main);
        StateMachine stateMachine = new StateMachine(transitions);
        stateMachine.start();
    }

    private PascalParser parser;
    private PascalCompiler walker;

}
