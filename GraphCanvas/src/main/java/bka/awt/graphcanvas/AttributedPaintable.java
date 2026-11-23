/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package bka.awt.graphcanvas;

import java.awt.*;
import java.util.*;


public abstract class AttributedPaintable implements Paintable {

    @Override
    public void setPaint(Object key, Paint paint) {
        paints.put(key, paint);
    }

    @Override
    public Paint getPaint(Object key) {
        return paints.get(key);
    }

    @Override
    public void setStroke(Object key, Stroke stroke) {
        strokes.put(key, stroke);
    }

    @Override
    public Stroke getStroke(Object key) {
        return strokes.get(key);
    }

    @Override
    public Collection<Object> getPaintKeys() {
        return paints.keySet();
    }

    @Override
    public Collection<Object> getStrokeKeys() {
        return strokes.keySet();
    }

    private final Map<Object, Paint> paints = new HashMap<>();
    private final Map<Object, Stroke> strokes = new HashMap<>();

}
