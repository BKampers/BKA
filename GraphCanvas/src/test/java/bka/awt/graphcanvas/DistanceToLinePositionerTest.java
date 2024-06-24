package bka.awt.graphcanvas;

import java.awt.*;
import java.util.*;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;


public class DistanceToLinePositionerTest {
    
    @Test
    public void testMovingLine() {
        // Horizontal line
        Point[] line = new Point[] { new Point(0, 0), new Point(100, 0)};
        // Point 5 pixels above the center of line
        Point point = new Point(50, 5);
        DistanceToLinePositioner positioner = DistanceToLinePositioner.create(point, 0, i -> line[i]);
        // Line has not been changed, so positioner must supply the original point
        assertEquals(point, positioner.get());
        // Shorten line
        line[0].move(50, 0);
        // positioner must still supply a point 5 pixels above the center of the shortened line
        assertEquals(new Point(75, 5), positioner.get());
        // Move line down
        line[0].move(50, 50);
        line[1].move(100, 50);
        // positioner must still supply a point 5 pixels above the center of the moved line
        assertEquals(new Point(75, 55), positioner.get());
    }
    
    @Test
    public void testTiltingLine() {
        // Horizontal line
        Point[] line = new Point[] { new Point(0, 0), new Point(100, 0)};
        // Point 5 pixels above the center of line
        Point point = new Point(50, 5);
        DistanceToLinePositioner positioner = DistanceToLinePositioner.create(point, 0, i -> line[i]);
        // Line has not been changed, so positioner must supply the original point
        assertEquals(point, positioner.get());
        // Tilt to -45 degrees 
        line[0].move(0, 100);
        line[1].move(100, 0);
        // positioner now must supply a point on the line perpendicular to the tilted line 
        // at a distance of 5 pixels below the center of the tilted line
        // That is Point 50 + sqrt(5^2 / 2), 50 + sqrt(5^2 / 2), rounded to whole pixels.
        assertEquals(new Point(54, 54), positioner.get());
        // Tilt line to vertical
        line[0].move(0, 0);
        line[1].move(0, 100);
        // positioner now must supply a point 5 pixels left to the center of the vertical line
        assertEquals(new Point(-5, 50), positioner.get());
        // Tilt line to 45 degrees
        line[0].move(100, 0);
        // positioner now must supply a point on the line perpendicular to the tilted line 
        // at a distance of 5 pixels below the center of the tilted line
        // That is Point 50 - sqrt(5^2 / 2), 50 + sqrt(5^2 / 2), rounded to whole pixels.
        assertEquals(new Point(46, 54), positioner.get());
    }
    
    @Test
    public void testMovingIndex() {
        // Horizontal line
        List<Point> line = new ArrayList<>(List.of(new Point(0, 100), new Point(200, 100)));
        // Point 5 pixels above the left quarter of line
        Point point = new Point(50, 5);
        DistanceToLinePositioner positioner = DistanceToLinePositioner.create(point, 0, i -> line.get(i));
        // insert one point at the center of the line
        line.add(1, new Point(100, 100));
        //positioner now must supply a point 5 pixels above a quarter of the first part of the line
        assertEquals(new Point(25, 5), positioner.get());
        // move the positioner's index to the second part of the line
        positioner.setIndex(1);
        //positioner now must supply a point 5 pixels above a quarter of the second part of the line
        assertEquals(new Point(125, 5), positioner.get());
    }
    
    @Test
    public void testPointInserted() {
        List<Point> line = new ArrayList<>();
        Point originalPosition = new Point(100, 5);
        DistanceToLinePositioner positioner = createPositionerWithEndPoints(line, 0, originalPosition);
        // Add new point at index, nearer to start point than to endpoint
        Point newPoint = new Point(200, 0);
        line.add(0, newPoint);
        positioner.pointInserted(0, newPoint);
        // No index change; positioner's result changes due to line tilt
        assertEquals(0, positioner.getIndex());
        Point position = positioner.get();
        assertNotEquals(originalPosition, position);
        // Add new point inserted after index:
        newPoint = new Point(0, 0);
        line.add(1, newPoint);
        positioner.pointInserted(1, newPoint);
        // No index change nor positioner's result change, since inserted point is after index
        assertEquals(0, positioner.getIndex());
        assertEquals(position, positioner.get());
        // Add new point at index, nearer to point after index than to point at index
        newPoint = new Point(0, 25);
        line.add(0, newPoint);
        positioner.pointInserted(0, newPoint);
        // Index increased; positioner's result change due to line tilt
        assertEquals(1, positioner.getIndex());
        assertNotEquals(position, positioner.get());
        position = positioner.get();
        // Add new point before index
        newPoint = new Point(200, 0);
        line.add(0, newPoint);
        positioner.pointInserted(0, newPoint);
        // Index increased positioner's result not changed
        assertEquals(2, positioner.getIndex());
        assertEquals(position, positioner.get());
    }
    
    @Test
    public void testPointRemoved() {
        List<Point> line = new ArrayList<>(List.of(new Point(50, 25), new Point(100, 50), new Point(150, 25)));
        Point originalPosition = new Point(50, 5);
        DistanceToLinePositioner positioner = createPositionerWithEndPoints(line, 2, originalPosition);
        // Remove point before index
        line.remove(0);
        positioner.pointRemoved(0);
        // Index decreased; positioner result not changed
        assertEquals(1, positioner.getIndex());
        assertEquals(originalPosition, positioner.get());
        // Remove point after index
        line.remove(1);
        positioner.pointRemoved(1);
        // Index unchanged, positioner result changed due to line tilt
        assertEquals(1, positioner.getIndex());
        assertNotEquals(originalPosition, positioner.get());
    }

    private DistanceToLinePositioner createPositionerWithEndPoints(List<Point> line, int index, Point originalPosition) {
        return DistanceToLinePositioner.create(originalPosition, index, i -> {
            if (i == 0) {
                return new Point(0, 100);
            }
            if (i == line.size() + 1) {
                return new Point(200, 100);
            }
            return line.get(i - 1);
        });
    }
}
