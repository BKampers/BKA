/*
** Copyright © Bart Kampers
*/

package bka.awt.chart.render;


import bka.awt.chart.geometry.*;
import java.awt.*;
import java.util.*;


public class DefaultLegendRenderer extends LegendRenderer {

    
    public DefaultLegendRenderer(int x, int y) {
        this.x = x;
        this.y = y;
    }

    
    @Override
    public void draw(Graphics2D g2d, ChartGeometry geometry, Map<Object, AbstractDataAreaRenderer> renderers, java.util.List<Object> order) {
        LegendGeometry legendGeometry = new LegendGeometry();
        legendGeometry.setX(x);
        legendGeometry.setY(y);
        legendGeometry.setSpace(SPACE);
        legendGeometry.setFeed(FEED);
        legendGeometry.setFont(g2d.getFont());
        legendGeometry.setLabelColor(LABEL_COLOR);
        for (Object key : order) {
            AbstractDataAreaRenderer renderer = renderers.get(key);
            if (renderer != null) {
                renderer.drawLegend(g2d, key, legendGeometry);
            }
        }
    }


    private final int x;
    private final int y;

    private static final int SPACE = 15;
    private static final int FEED = 5;
    private static final Color LABEL_COLOR = Color.BLACK;

}
