/*
** © Bart Kampers
*/

package bka.demo.graphs;

import java.awt.*;
import java.util.*;


public abstract class Paintable {

    public abstract void paint(Graphics2D graphics);

    public void setPaint(Object key, Paint paint) {
        paints.put(key, paint);
    }

    public Paint getPaint(Object key) {
        return paints.get(key);
    }

    public void setStroke(Object key, Stroke stroke) {
        strokes.put(key, stroke);
    }

    public Stroke getStroke(Object key) {
        return strokes.get(key);
    }

    public Collection<Object> getPaintKeys() {
        return paints.keySet();
    }

    private final Map<Object, Paint> paints = new HashMap<>();
    private final Map<Object, Stroke> strokes = new HashMap<>();


}
