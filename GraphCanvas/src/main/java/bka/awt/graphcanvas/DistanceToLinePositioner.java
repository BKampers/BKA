/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package bka.awt.graphcanvas;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.function.*;


public class DistanceToLinePositioner implements Supplier<Point> {

    private DistanceToLinePositioner(int index, double xDistance, double yDistance, double ratio, IntFunction<Point> pointAt) {
        this.xDistance = xDistance;
        this.yDistance = yDistance;
        this.ratio = ratio;
        this.index = index;
        this.pointAt = Objects.requireNonNull(pointAt);
    }

    public static DistanceToLinePositioner create(Point point, int index, IntFunction<Point> pointAt) {
        Point linePoint1 = pointAt.apply(index);
        Point linePoint2 = pointAt.apply(index + 1);
        Point2D anchor = CanvasUtil.intersectionPoint(point, linePoint1, linePoint2);
        double yDistance = directedDistance(point, anchor, CanvasUtil.slope(linePoint1, linePoint2));
        double xDistance = (linePoint2.x < linePoint1.x) ? -yDistance : yDistance;
        return new DistanceToLinePositioner(index, xDistance, yDistance, directedRatio(linePoint1, linePoint2, anchor), pointAt);
    }

    private static double directedDistance(Point2D distantPoint, Point2D anchor, double slope) {
        double distance = distantPoint.distance(anchor);
        if (slope <= -1) {
            return (distantPoint.getX() < anchor.getX()) ? -distance : distance;
        }
        if (1 <= slope) {
            return (distantPoint.getX() > anchor.getX()) ? -distance : distance;
        }
        return (distantPoint.getY() < anchor.getY()) ? -distance : distance;
    }

    private static double directedRatio(Point2D linePoint1, Point2D linePoint2, Point2D anchor) {
        double ratio = anchor.distance(linePoint1) / linePoint1.distance(linePoint2);
        if (Math.abs(CanvasUtil.slope(linePoint1, linePoint2)) < 1) {
            return (ordered(anchor.getX(), linePoint1.getX(), linePoint2.getX())) ? -ratio : ratio;
        }
        return (ordered(anchor.getY(), linePoint1.getY(), linePoint2.getY())) ? -ratio : ratio;
    }

    private static boolean ordered(double d1, double d2, double d3) {
        return d1 < d2 && d2 < d3 || d3 < d2 && d2 < d1;
    }

    @Override
    public Point get() {
        Point linePoint1 = pointAt.apply(index);
        Point linePoint2 = pointAt.apply(index + 1);
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
        if (insertIndex < index || insertIndex == index && CanvasUtil.squareDistance(pointAt.apply(index), point) < CanvasUtil.squareDistance(pointAt.apply(index + 2), point)) {
            index++;
        }
    }

    public void pointRemoved(int removeIndex) {
        if (removeIndex < index) {
            index--;
        }
    }

    private final IntFunction<Point> pointAt;
    private final double xDistance;
    private final double yDistance;
    private final double ratio;
    private int index;

}
