/*
** © Bart Kampers
*/

package bka.awt.chart.render;

import bka.awt.chart.geometry.*;
import java.awt.*;
import java.util.*;


public abstract class LegendRenderer {

    public abstract void draw(Graphics2D g2d, ChartGeometry geometry, Map<Object, AbstractDataAreaRenderer> renderers, java.util.List<Object> order);

}
