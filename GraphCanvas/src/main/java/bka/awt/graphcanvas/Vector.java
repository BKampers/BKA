/*
** © Bart Kampers
*/

package bka.awt.graphcanvas;


public class Vector {

    public Vector(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector(Vector other) {
        this(other.x, other.y);
    }

    public Vector normalized() {
        double magnitude = magnitude();
        return new Vector(x / magnitude, y / magnitude);
    }

    public double magnitude() {
        return Math.sqrt(x * x + y * y);
    }

    public Vector scale(double factor) {
        return new Vector(x * factor, y * factor);
    }

    public Vector add(Vector other) {
        return new Vector(x + other.x, y + other.y);
    }

    public Vector subtract(Vector other) {
        return new Vector(x - other.x, y - other.y);
    }

    public static double cosine(Vector vector1, Vector vector2) {
        return dotProduct(vector1.normalized(), vector2.normalized());
    }

    public static double dotProduct(Vector vector1, Vector vector2) {
        return vector1.x * vector2.x + vector1.y * vector2.y;
    }

    private final double x;
    private final double y;

}
