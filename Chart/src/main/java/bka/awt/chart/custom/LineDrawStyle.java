/*
** Copyright © Bart Kampers
*/

package bka.awt.chart.custom;


import bka.awt.chart.geometry.*;
import java.awt.*;


public class LineDrawStyle<G extends AreaGeometry> {


    private LineDrawStyle(Paint linePaint, Stroke lineStroke, AreaDrawStyle<G> markerDrawStyle) {
        this.linePaint = linePaint;
        this.lineStroke = lineStroke;
        this.markerDrawStyle = markerDrawStyle;
    }


    public static LineDrawStyle create(Color linePaint, Stroke lineStroke, AreaDrawStyle markerDrawStyle) {
        return new LineDrawStyle(linePaint, lineStroke, markerDrawStyle);
    }


    public static LineDrawStyle create(Color linePaint, Stroke lineStroke) {
        return new LineDrawStyle(linePaint, lineStroke, null);
    }


    public static LineDrawStyle create(Color linePaint) {
        return create(linePaint, new BasicStroke(2.0f));
    }


    public static LineDrawStyle create(Color linePaint, AreaDrawStyle markerDrawStyle) {
        return create(linePaint, new BasicStroke(2.0f), markerDrawStyle);
    }


    public Paint getLinePaint() {
        return linePaint;
    }


    public Stroke getLineStroke() {
        return lineStroke;
    }


    public AreaDrawStyle<G> getMarkerDrawStyle() {
        return markerDrawStyle;
    }


    public Paint getBottomAreaPaint() {
        return bottomAreaPaint;
    }


    public void setBottomAreaPaint(Paint bottomAreaPaint) {
        this.bottomAreaPaint = bottomAreaPaint;
    }


    public Paint getTopAreaPaint() {
        return topAreaPaint;
    }


    public void setTopAreaPaint(Paint topAreaPaint) {
        this.topAreaPaint = topAreaPaint;
    }


    private final Paint linePaint;
    private final Stroke lineStroke;
    private final AreaDrawStyle<G> markerDrawStyle;
    private Paint bottomAreaPaint;
    private Paint topAreaPaint;

}
