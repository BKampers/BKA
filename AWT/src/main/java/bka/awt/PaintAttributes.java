
/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package bka.awt;

import java.awt.*;
import java.util.*;


public class PaintAttributes {
    
    public PaintAttributes(Map<Object, Paint> paints, Map<Object, Stroke> strokes) {
        this.paints = new HashMap<>(paints);
        this.strokes = new HashMap<>(strokes);
    }
    
    public PaintAttributes(PaintAttributes attributes) {
        this(attributes.paints, attributes.strokes);
    }
 
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
    
    private final Map<Object, Paint> paints;
    private final Map<Object, Stroke> strokes;
}
