/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package bka.awt.graphcanvas;

import java.awt.*;
import java.util.*;
import java.util.function.*;


public class DistanceToLinePositioner implements Supplier<Point> {

    public DistanceToLinePositioner(int index, double xDistance, double yDistance, double ratio, Function<Integer, Point> pointAt) {
        this.xDistance = xDistance;
        this.yDistance = yDistance;
        this.ratio = ratio;
        this.index = index;
        this.pointAt = Objects.requireNonNull(pointAt);
    }

    @Override
    public Point get() {
        Point linePoint1 = getPoint(index);
        Point linePoint2 = getPoint(index + 1);
        double xAnchor = linePoint1.x + (linePoint2.x - linePoint1.x) * ratio;
        double yAnchor = linePoint1.y + (linePoint2.y - linePoint1.y) * ratio;
        double cos = (linePoint1.y - linePoint2.y) / linePoint1.distance(linePoint2);
        return CanvasUtil.getPoint(cos * xDistance + xAnchor, Math.sin(Math.acos(cos)) * yDistance + yAnchor);
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public void pointInserted(int insertIndex, Point point) {
        if (insertIndex < index) {
            index++;
        }
        else if (insertIndex == index) {
            if (CanvasUtil.squareDistance(getPoint(index), point) < CanvasUtil.squareDistance(getPoint(index + 2), point)) {
                index++;
            }
        }
    }

    public void pointRemoved(int removeIndex) {
        if (removeIndex < index) {
            index--;
        }
    }

    private Point getPoint(int index) {
        return pointAt.apply(index);
    }

    private final Function<Integer, Point> pointAt;
    private final double xDistance;
    private final double yDistance;
    private final double ratio;
    private int index;

}
