/*
** Copyright © Bart Kampers
*/

package bka.awt;

import java.awt.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.*;


public class PaletteTest {

    @Test
    public void testGenerateColors() {
        for (int count = 0; count <= 25; ++count) {
            assertEquals(count, countDistinctColors(Palette.generateColors(count)));
        }
    }

    private static int countDistinctColors(Color[] colors) {
        return new HashSet<>(Arrays.asList(colors)).size();
    }

}
