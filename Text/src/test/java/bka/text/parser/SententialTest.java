package bka.text.parser;

import java.util.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class SententialTest {

    @Test
    public void testGetSymbols() {
        assertEquals(List.of(), new Sentential(List.of()).getSymbols());
        assertEquals(List.of("a"), new Sentential(List.of("a")).getSymbols());
    }

    @Test
    public void testIsErasing() {
        assertTrue(new Sentential(List.of()).isErasing());
        assertFalse(new Sentential(List.of("a")).isErasing());
    }

    @Test
    public void testLength() {
        assertEquals(0, Sentential.of().length());
        assertEquals(1, Sentential.of("a").length());
        assertEquals(2, Sentential.of("a", "b").length());
    }

    @Test
    public void testInvalidInstantiations() {
        assertThrows(IllegalArgumentException.class, () -> new Sentential(List.of("")));
        assertThrows(IllegalArgumentException.class, () -> Sentential.of("a", "", "b"));
    }

}
