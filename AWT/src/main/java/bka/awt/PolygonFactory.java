/*
** Copyright © Bart Kampers
*/

package bka.awt;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;


public class PolygonFactory {

    @Deprecated
    public static Polygon createPolygon(int count, int radius) {
        return toPolygon(createRegularPolygon(count, radius));
    }

    public static java.util.List<Point2D> createRegularPolygon(int count, double radius) {
        if (count < 3) {
            throw new IllegalArgumentException();
        }
        ArrayList<Point2D> polygon = new ArrayList<>(count);
        double[] point = new double[]{0.0, -radius};
        polygon.add(point2D(point));
        AffineTransform transform = originTransform(2.0 * Math.PI / count);
        for (int i = 1; i < count; ++i) {
            rotate(point, transform);
            polygon.add(point2D(point));
        }
        return polygon;
    }

    @Deprecated
    public static Polygon createStar(int count, int innerRadius, int outerRadius) {
        return toPolygon(createStar(count, (double) innerRadius, (double) outerRadius));
    }

    public static java.util.List<Point2D> createStar(int count, double innerRadius, double outerRadius) {
        if (count < 2) {
            throw new IllegalArgumentException();
        }
        ArrayList<Point2D> star = new ArrayList<>(count * 2);
        double[] outerPoint = new double[]{ 0.0, -outerRadius };
        star.add(point2D(outerPoint));
        double[] innerPoint = new double[]{ 0.0, -innerRadius };
        rotate(innerPoint, originTransform(Math.PI / count));
        star.add(point2D(innerPoint));
        AffineTransform transform = originTransform(2.0 * Math.PI / count);
        for (int i = 1; i < count; ++i) {
            rotate(outerPoint, transform);
            star.add(point2D(outerPoint));
            rotate(innerPoint, transform);
            star.add(point2D(innerPoint));
        }
        return star;
    }

    public static Polygon toPolygon(java.util.List<Point2D> points) {
        int nPoints = points.size();
        int[] xPoints = new int[nPoints];
        int[] yPoints = new int[nPoints];
        for (int i = 0; i < nPoints; ++i) {
            Point2D point = points.get(i);
            xPoints[i] = round(point.getX());
            yPoints[i] = round(point.getY());
        }
        return new Polygon(xPoints, yPoints, nPoints);
    }

    private static int round(double value) {
        return Math.round((float) value);
    }

    private static AffineTransform originTransform(double theta) {
        return AffineTransform.getRotateInstance(theta, 0.0, 0.0);
    }

    private static void rotate(double[] point, AffineTransform transform) {
        transform.transform(point, 0, point, 0, 1);
    }

    private static Point2D point2D(double[] point) {
        return new Point2D.Double(point[0], point[1]);
    }

}
