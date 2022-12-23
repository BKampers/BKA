/*
** Copyright © Bart Kampers
*/

package bka.awt.chart.geometry;


import java.awt.*;


public class LegendGeometry {


    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }


    public int getY() {
        return y;
    }


    public void setY(int y) {
        this.y = y;
    }


    public int getSpace() {
        return space;
    }


    public void setSpace(int space) {
        this.space = space;
    }


    public int getFeed() {
        return feed;
    }


    public void setFeed(int feed) {
        this.feed = feed;
    }


    public Font getFont() {
        return font;
    }


    public void setFont(Font font) {
        this.font = font;
    }


    public Color getColor() {
        return color;
    }


    public void setLabelColor(Color color) {
        this.color = color;
    }


    private int x;
    private int y;
    private int space;
    private int feed;
    private Font font;
    private Color color;

}
