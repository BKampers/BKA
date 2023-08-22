/*
** © Bart Kampers
*/

package bka.demo.graphs;


public class Vector {

    public Vector(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector(Vector other) {
        this(other.x, other.y);
    }

    public static double cosine(Vector v1, Vector v2) {
        return dotProduct(v1.normalized(), v2.normalized());
    }

    public Vector normalized() {
        double magnitude = magnitude();
        return new Vector(x / magnitude, y / magnitude);
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

    public static double dotProduct(Vector v1, Vector v2) {
        return v1.x * v2.x + v1.y * v2.y;
    }

    public double magnitude() {
        return Math.sqrt(x * x + y * y);
    }

    private final double x;
    private final double y;

}
