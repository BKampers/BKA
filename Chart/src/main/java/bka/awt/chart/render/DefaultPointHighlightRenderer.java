/*
** Copyright © Bart Kampers
*/

package bka.awt.chart.render;


import bka.awt.chart.geometry.*;
import bka.text.*;
import java.awt.*;


public class DefaultPointHighlightRenderer {


    public void setBackground(Color color) {
        this.background = color;
    }


    public Color getBackground() {
        return background;
    }


    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }


    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }

    
    public void draw(Graphics2D g2d, HighlightGeometry geometry) {
        String xLabel = xLabel(geometry.getAreaGeometry());
        String yLabel = yLabel(geometry.getAreaGeometry());
        FontMetrics fontMetrics = g2d.getFontMetrics();
        int xWidth = fontMetrics.stringWidth(xLabel);
        int yWidth = fontMetrics.stringWidth(yLabel);
        int labelWidth = Math.max(xWidth, yWidth);
        Point labelLocation = geometry.getLabelLocation();
        int labelBase = labelLocation.y - fontMetrics.getHeight();
        Composite originalComposite = g2d.getComposite();
        g2d.setPaint(background);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        g2d.fillRoundRect(labelLocation.x - labelWidth / 2, labelBase - fontMetrics.getAscent(), labelWidth, fontMetrics.getHeight() * 2, 5, 5);
        g2d.setComposite(originalComposite);
        g2d.setColor(borderColor);
        g2d.drawRoundRect(labelLocation.x - labelWidth / 2, labelBase - fontMetrics.getAscent(), labelWidth, fontMetrics.getHeight() * 2, 5, 5);
        g2d.setColor(textColor);
        g2d.drawString(xLabel, labelLocation.x - xWidth / 2, labelBase);
        g2d.drawString(yLabel, labelLocation.x - yWidth / 2, labelBase + fontMetrics.getHeight());
    }


    public void setXFormat(String xFormat) {
        this.xFormat = xFormat;
    }


    public void setYFormat(String yFormat) {
        this.yFormat = yFormat;
    }


    public String xLabel(AreaGeometry geometry) {
        return FormatterUtil.format(xFormat, geometry.getX());
    }


    public String yLabel(AreaGeometry geometry) {
        return FormatterUtil.format(yFormat, geometry.getY());
    }

    private Color background = Color.YELLOW;

    private Color textColor = Color.BLACK;
    private Color borderColor = new Color(137, 51, 0);

    private String xFormat;
    private String yFormat;

}
