/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package bka.awt.graphcanvas;

import java.awt.*;
import java.util.*;


public interface Paintable {

    void paint(Graphics2D graphics);

    void paint(Graphics2D graphics, Paint paint, Stroke stroke);

    void setPaint(Object key, Paint paint);

    Paint getPaint(Object key);

    void setStroke(Object key, Stroke stroke);

    Stroke getStroke(Object key);

    Collection<Object> getPaintKeys();

    Collection<Object> getStrokeKeys();

}
