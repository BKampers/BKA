/*
** © Bart Kampers
*/

package bka.awt.chart.custom;

import java.awt.*;
import java.util.*;


public class AxisStyle {


    public AxisStyle(Color color) {
        axisColor = color;
        markerColor = color;
        labelColor = color;
        titleColor = color;
        unitColor = color;
    }


    public void setColors(Color color) {
        axisColor = color;
        markerColor = color;
        labelColor = color;
        titleColor = color;
        unitColor = color;
    }


    public void setAxisColor(Color axisColor) {
        this.axisColor = axisColor;
    }


    public Color getAxisColor() {
        return axisColor;
    }


    public void setMarkerColor(Color markerColor) {
        this.markerColor = markerColor;
    }


    public Color getMarkerColor() {
        return markerColor;
    }


    public void setLabelColor(Color labelColor) {
        this.labelColor = labelColor;
    }


    public Color getLabelColor() {
        return labelColor;
    }


    public void setTitleColor(Color titleColor) {
        this.titleColor = titleColor;
    }


    public Color getTitleColor() {
        return titleColor;
    }


    public void setUnitColor(Color unitColor) {
        this.unitColor = unitColor;
    }


    public Color getUnitColor() {
        return unitColor;
    }


    public void setTitleFormat(String titleFormat) {
        this.titleFormat = Objects.requireNonNull(titleFormat);
    }


    public String getTitleFormat() {
        return titleFormat;
    }


    public void setUnitFormat(String unitFormat) {
        this.unitFormat = Objects.requireNonNull(unitFormat);
    }


    public String getUnitFormat() {
        return unitFormat;
    }


    public int getLabelOffset() {
        return labelOffset;
    }


    public void setLabelOffset(int labelOffset) {
        this.labelOffset = labelOffset;
    }


    private Color axisColor;
    private Color markerColor;
    private Color labelColor;
    private Color titleColor;
    private Color unitColor;

    private String titleFormat = "%s";
    private String unitFormat = "%s";

    private int labelOffset;

}
