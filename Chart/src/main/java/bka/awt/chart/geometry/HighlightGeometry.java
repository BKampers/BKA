/*
** © Bart Kampers
*/

package bka.awt.chart.geometry;

import java.awt.*;


public class HighlightGeometry {


    public HighlightGeometry(AreaGeometry areaGeometry, Point labelLocation) {
        this.areaGeometry = areaGeometry;
        this.labelLocation = labelLocation;
    }


    public AreaGeometry getAreaGeometry() {
        return areaGeometry;
    }


    public Point getLabelLocation() {
        return labelLocation;
    }


    private final AreaGeometry areaGeometry;
    private final Point labelLocation;

}
