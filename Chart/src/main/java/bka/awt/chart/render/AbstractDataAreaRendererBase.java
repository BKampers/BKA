/*
** © Bart Kampers
*/

package bka.awt.chart.render;

import bka.awt.chart.custom.*;
import bka.awt.chart.geometry.*;
import java.awt.*;


abstract class AbstractDataAreaRendererBase<G extends AreaGeometry> extends AbstractDataAreaRenderer<G> {


    public AbstractDataAreaRendererBase(AreaDrawStyle drawStyle) {
        super(drawStyle);
    }


    @Override
    public void drawLegend(Graphics2D g2d, Object key, LegendGeometry geometry) {
        FontMetrics fontMetrics = g2d.getFontMetrics();
        drawSymbol(g2d, geometry.getX(), geometry.getY() + fontMetrics.getHeight() - fontMetrics.getDescent());
        g2d.setColor(geometry.getColor());
        g2d.setFont(geometry.getFont());
        g2d.drawString(key.toString(), geometry.getX() + geometry.getSpace(), geometry.getY() + fontMetrics.getHeight());
        geometry.setY(geometry.getY() + geometry.getFeed() + fontMetrics.getHeight());
    }

    
    protected void drawSymbol(Graphics2D g2d, int x, int y) {
        draw(g2d, createSymbolGeometry(x, y));
    }


    protected abstract G createSymbolGeometry(int x, int y);

}
