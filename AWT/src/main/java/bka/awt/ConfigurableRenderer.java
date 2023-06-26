/*
** © Bart Kampers
*/

package bka.awt;

import java.awt.*;


public interface ConfigurableRenderer extends Renderer {

    void setPaint(Paint paint);

    Paint getPaint();

    void setStroke(Stroke stroke);

    Stroke getStroke();

}
