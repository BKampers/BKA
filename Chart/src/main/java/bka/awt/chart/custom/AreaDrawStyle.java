/*
 * Copyright © Bart Kampers
 */
package bka.awt.chart.custom;


import bka.awt.chart.geometry.*;
import java.awt.*;


/**
 * @param <G> geometry
 */
public interface AreaDrawStyle<G extends AreaGeometry> {

    public Image getImage();
    public Paint getPaint(G geometry);
    public Paint getBorderPaint(G geometry);
    public Stroke getBorderStroke(G geometry);
    public Paint getLabelPaint(G geometry);
    public Font getLabelFont(G geometry);
    
}
