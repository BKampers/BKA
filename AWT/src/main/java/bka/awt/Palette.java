/*
** Copyright © Bart Kampers
*/

package bka.awt;

import java.awt.*;


public class Palette {

        
    public static Color[] generateColors(int count) {
        Color[] colors = new Color[count];
        float range = count;
        for (int i = 0; i < count; ++i) {
            colors[i] = generate(i, range);
        }
        return colors;
    }


    public static Color generateColor(int index, int count) {
        return generate(index, count);
    }


    private static Color generate(int index, float range) {
        return Color.getHSBColor(index / range, 1.0f, 1.0f);
    }
    

}
