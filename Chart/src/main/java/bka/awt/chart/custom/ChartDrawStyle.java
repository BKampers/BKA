/*
** © Bart Kampers
*/

package bka.awt.chart.custom;

import java.awt.*;


public class ChartDrawStyle {


    public ChartDrawStyle() {
        this(Color.BLACK, Color.WHITE, Color.YELLOW);
    }


    public ChartDrawStyle(Color titleColor, Color gridBackground, Color highlightBackground) {
        this.titleColor = titleColor;
        this.gridBackground = gridBackground;
        this.highlightBackground = highlightBackground;
    }


    public Color getTitleColor() {
        return titleColor;
    }


    public Color getGridBackground() {
        return gridBackground;
    }


    public Color getHighlightBackground() {
        return highlightBackground;
    }


    private final Color titleColor;
    private final Color gridBackground;
    private final Color highlightBackground;

}
