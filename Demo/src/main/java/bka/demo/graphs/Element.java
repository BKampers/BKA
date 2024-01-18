/*
** © Bart Kampers
*/

package bka.demo.graphs;

import bka.awt.*;
import bka.demo.graphs.Label;
import java.awt.*;
import java.util.*;
import java.util.function.*;

public abstract class Element implements Renderer {

    public abstract void paintHighlight(Graphics2D graphics, Color color, Stroke stroke);

    public abstract void move(Point vector);

    public abstract Supplier<Point> distancePositioner(Point point);

    public void addLabel(Label label) {
        if (!labels.add(label)) {
            throw new IllegalArgumentException("Element already owns label " + label.getText());
        }
    }

    public void removeLabel(Label label) {
        if (!labels.remove(label)) {
            throw new IllegalArgumentException("Element does not own label " + label.getText());
        }
    }

    public Collection<Label> getLabels() {
        return Collections.unmodifiableCollection(labels);
    }

    private final Collection<Label> labels = new HashSet<>();

}
