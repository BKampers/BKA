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

    public abstract Point getLocation();

    public abstract void move(Point vector);

    public abstract Supplier<Point> distancePositioner(Point point);

    public void addLabel(Label label) {
        labels.add(label);
    }

    public void removeLabel(Label label) {
        labels.remove(label);
    }

    public Collection<Label> getLabels() {
        return Collections.unmodifiableCollection(labels);
    }

    private final Collection<Label> labels = new ArrayList<>();

}
