/*
** © Bart Kampers
*/

package bka.awt.graphcanvas;

import java.awt.*;
import java.awt.image.*;
import java.util.function.*;
import org.junit.*;
import static org.junit.Assert.*;


public final class StadiumVertexPaintableTest {

    @Test
    public void testResizeChangesWidthAndHeightIndependently() {
        StadiumVertexPaintable shape = new StadiumVertexPaintable(new Dimension(100, 40));
        Point location = new Point(0, 0);
        shape.resize(location, new Point(0, -30), ResizeDirection.NORTH);
        assertEquals(100, shape.getWidth());
        assertEquals(60, shape.getHeight());
        shape.resize(location, new Point(80, 0), ResizeDirection.EAST);
        assertEquals(160, shape.getWidth());
        assertEquals(60, shape.getHeight());
    }

    @Test
    public void testDiagonalResizeKeepsTheCursorOnTheCap() {
        StadiumVertexPaintable shape = new StadiumVertexPaintable(new Dimension(100, 40));
        Point location = new Point(0, 0);
        Point onCap = new Point(44, -14);
        shape.resize(location, onCap, ResizeDirection.NORTH_EAST);
        assertEquals(100, shape.getWidth());
        assertEquals(40, shape.getHeight());
        assertEquals(0, shape.distance(location, onCap), 1.0);

        Point dragged = new Point(60, -20);
        shape.resize(location, dragged, ResizeDirection.NORTH_EAST);
        assertEquals(60, shape.getWidth() - shape.getHeight());
        assertEquals(0, shape.distance(location, dragged), 1.0);

        Point otherCap = new Point(-60, 20);
        shape.resize(location, otherCap, ResizeDirection.SOUTH_WEST);
        assertEquals(0, shape.distance(location, otherCap), 1.0);
    }

    @Test
    public void testResizeDirectionFollowsTheContour() {
        StadiumVertexPaintable shape = new StadiumVertexPaintable(new Dimension(100, 40));
        Point location = new Point(0, 0);
        assertEquals(ResizeDirection.NORTH, shape.resizeDirection(location, new Point(0, -20)));
        assertEquals(ResizeDirection.NORTH, shape.resizeDirection(location, new Point(25, -20)));
        assertEquals(ResizeDirection.SOUTH, shape.resizeDirection(location, new Point(-20, 20)));
        assertEquals(ResizeDirection.EAST, shape.resizeDirection(location, new Point(50, 0)));
        assertEquals(ResizeDirection.EAST, shape.resizeDirection(location, new Point(49, -3)));
        assertEquals(ResizeDirection.WEST, shape.resizeDirection(location, new Point(-50, 0)));
        assertEquals(ResizeDirection.NORTH_EAST, shape.resizeDirection(location, new Point(47, -10)));
        assertEquals(ResizeDirection.SOUTH_EAST, shape.resizeDirection(location, new Point(47, 10)));
        assertEquals(ResizeDirection.NORTH_WEST, shape.resizeDirection(location, new Point(-47, -10)));
        assertEquals(ResizeDirection.SOUTH_WEST, shape.resizeDirection(location, new Point(-47, 10)));
    }

    @Test
    public void testSignedDistanceFollowsTheStadium() {
        StadiumVertexPaintable shape = new StadiumVertexPaintable(new Dimension(100, 40));
        Point location = new Point(0, 0);
        assertEquals(-20, shape.distance(location, new Point(0, 0)), DISTANCE_TOLERANCE);
        assertEquals(0, shape.distance(location, new Point(0, -20)), DISTANCE_TOLERANCE);
        assertEquals(0, shape.distance(location, new Point(50, 0)), DISTANCE_TOLERANCE);
        assertEquals(10, shape.distance(location, new Point(60, 0)), DISTANCE_TOLERANCE);
        assertEquals(-10, shape.distance(location, new Point(40, 0)), DISTANCE_TOLERANCE);
        assertTrue(shape.distance(location, new Point(-50, -20)) > 0);
    }

    @Test
    public void testTallerThanWideUsesSemicirclesOfTheHeight() {
        StadiumVertexPaintable shape = new StadiumVertexPaintable(new Dimension(20, 40));
        Point location = new Point(0, 0);
        assertEquals(0, shape.distance(location, new Point(20, 0)), DISTANCE_TOLERANCE);
        assertEquals(0, shape.distance(location, new Point(0, -20)), DISTANCE_TOLERANCE);
        assertEquals(new Point(20, 0), shape.getConnectorPoint(location, new Point(100, 0)));
        assertEquals(new Point(0, -20), shape.getConnectorPoint(location, new Point(0, -100)));
    }

    @Test
    public void testConnectorLiesOnTheContour() {
        StadiumVertexPaintable shape = new StadiumVertexPaintable(new Dimension(100, 40));
        Point location = new Point(200, 150);
        assertEquals(location, shape.getConnectorPoint(location, new Point(location)));
        assertEquals(new Point(250, 150), shape.getConnectorPoint(location, new Point(400, 150)));
        assertEquals(new Point(150, 150), shape.getConnectorPoint(location, new Point(0, 150)));
        assertEquals(new Point(200, 130), shape.getConnectorPoint(location, new Point(200, 0)));
        assertEquals(new Point(200, 170), shape.getConnectorPoint(location, new Point(200, 400)));
        Point capConnector = shape.getConnectorPoint(location, new Point(300, 110));
        assertEquals(0, shape.distance(location, capConnector), 1.01);
        assertTrue(capConnector.x > location.x);
        assertTrue(capConnector.y < location.y);
    }

    @Test
    public void testDistancePositionerFollowsResizeAndMove() {
        StadiumVertexPaintable shape = new StadiumVertexPaintable(new Dimension(80, 40));
        Point location = new Point(10, 10);
        Supplier<Point> positioner = shape.distancePositioner(location, new Point(10, -20));
        assertEquals(new Point(10, -20), positioner.get());
        location.translate(5, -3);
        assertEquals(new Point(15, -23), positioner.get());
        shape.setSize(120, 40);
        assertEquals(new Point(15, -23), positioner.get());
        shape.setSize(120, 60);
        assertEquals(new Point(15, -33), positioner.get());
    }

    @Test
    public void testCapPositionerKeepsItsOffset() {
        StadiumVertexPaintable shape = new StadiumVertexPaintable(new Dimension(80, 40));
        Point location = new Point(0, 0);
        Supplier<Point> positioner = shape.distancePositioner(location, new Point(50, 5));
        assertEquals(new Point(50, 5), positioner.get());
        shape.setSize(100, 40);
        assertEquals(new Point(60, 5), positioner.get());
    }

    @Test
    public void testTransparentFillShowsOnlyTheContour() {
        Render render = render(shape(new Color(255, 0, 0, 0)), true);
        assertInteriorUntouched(render);
        assertContourDrawn(render);
        assertEquals(BACKGROUND_RGB, render.rgb(-50, -10));
        assertEquals(BACKGROUND_RGB, render.rgb(-50, -20));
    }

    @Test
    public void testMissingFillShowsOnlyTheContour() {
        StadiumVertexPaintable shape = new StadiumVertexPaintable(new Dimension(100, 40));
        shape.setPaint(VertexPaintable.BORDER_PAINT_KEY, Color.BLACK);
        shape.setStroke(VertexPaintable.BORDER_STROKE_KEY, new BasicStroke(1f));
        Render render = render(shape, true);
        assertInteriorUntouched(render);
        assertContourDrawn(render);
    }

    @Test
    public void testOpaqueFillDoesNotCoverTheContourOrPaintSeams() {
        Render render = render(shape(Color.RED), true);
        assertEquals(FILL_RGB, render.rgb(0, 0));
        assertEquals(FILL_RGB, render.rgb(-30, 0));
        assertEquals(FILL_RGB, render.rgb(30, 8));
        assertContourDrawn(render);
        assertEquals(BACKGROUND_RGB, render.rgb(-50, -10));
    }

    @Test
    public void testSemiTransparentFillHasNoSeam() {
        Render render = render(shape(new Color(255, 0, 0, 128)), true);
        int interior = render.rgb(0, 0);
        assertNotEquals(BACKGROUND_RGB, interior);
        assertEquals(interior, render.rgb(0, -8));
        assertEquals(interior, render.rgb(-30, 0));
        assertEquals(interior, render.rgb(30, 0));
        assertEquals(interior, render.rgb(-30, 8));
        assertEquals(interior, render.rgb(30, -8));
        assertContourDrawn(render);
    }

    @Test
    public void testCircleKeepsItsInteriorClearWhenFillIsMissing() {
        StadiumVertexPaintable shape = new StadiumVertexPaintable(new Dimension(20, 40));
        shape.setPaint(VertexPaintable.BORDER_PAINT_KEY, Color.BLACK);
        shape.setStroke(VertexPaintable.BORDER_STROKE_KEY, new BasicStroke(1f));
        Render render = render(shape, true);
        assertEquals(BACKGROUND_RGB, render.rgb(0, 0));
        assertEquals(BACKGROUND_RGB, render.rgb(0, -10));
        assertEquals(BORDER_RGB, render.rgb(20, 0));
        assertEquals(BORDER_RGB, render.rgb(0, -20));
    }

    @Test
    public void testHighlightDrawsOnlyTheContour() {
        StadiumVertexPaintable shape = shape(Color.RED);
        Render render = render(shape, false);
        assertEquals(BACKGROUND_RGB, render.rgb(0, 0));
        assertEquals(BACKGROUND_RGB, render.rgb(-30, 0));
        assertContourDrawn(render);
    }

    @Test
    public void testBorderStrokeIsUsed() {
        StadiumVertexPaintable shape = shape(new Color(0, 0, 0, 0));
        shape.setStroke(VertexPaintable.BORDER_STROKE_KEY, new BasicStroke(5f));
        Render render = render(shape, true);
        assertEquals(BORDER_RGB, render.rgb(0, -18));
        assertEquals(BACKGROUND_RGB, render.rgb(-30, 0));
    }

    private static void assertInteriorUntouched(Render render) {
        assertEquals(BACKGROUND_RGB, render.rgb(0, 0));
        assertEquals(BACKGROUND_RGB, render.rgb(0, -8));
        assertEquals(BACKGROUND_RGB, render.rgb(-30, 0));
        assertEquals(BACKGROUND_RGB, render.rgb(30, 0));
        assertEquals(BACKGROUND_RGB, render.rgb(-30, 8));
        assertEquals(BACKGROUND_RGB, render.rgb(30, -8));
    }

    private static void assertContourDrawn(Render render) {
        assertEquals(BORDER_RGB, render.rgb(-50, 0));
        assertEquals(BORDER_RGB, render.rgb(50, 0));
        assertEquals(BORDER_RGB, render.rgb(0, -20));
        assertEquals(BORDER_RGB, render.rgb(0, 20));
    }

    private static StadiumVertexPaintable shape(Color fill) {
        StadiumVertexPaintable shape = new StadiumVertexPaintable(new Dimension(100, 40));
        shape.setPaint(VertexPaintable.FILL_PAINT_KEY, fill);
        shape.setPaint(VertexPaintable.BORDER_PAINT_KEY, Color.BLACK);
        shape.setStroke(VertexPaintable.BORDER_STROKE_KEY, new BasicStroke(1f));
        return shape;
    }

    private static Render render(StadiumVertexPaintable shape, boolean filled) {
        final int margin = 8;
        int halfWidth = Math.max(shape.getWidth(), shape.getHeight()) / 2;
        int radius = shape.getHeight() / 2;
        int centerX = halfWidth + margin;
        int centerY = radius + margin;
        int imageWidth = centerX + halfWidth + margin;
        int imageHeight = centerY + radius + margin;
        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            graphics.setPaint(BACKGROUND);
            graphics.fillRect(0, 0, imageWidth, imageHeight);
            graphics.translate(centerX + 0.5, centerY + 0.5);
            if (filled) {
                shape.paint(graphics);
            }
            else {
                shape.paint(graphics, Color.BLACK, new BasicStroke(1f));
            }
        }
        finally {
            graphics.dispose();
        }
        return new Render(image, centerX, centerY);
    }

    private static final class Render {

        private Render(BufferedImage image, int centerX, int centerY) {
            this.image = image;
            this.centerX = centerX;
            this.centerY = centerY;
        }

        private int rgb(int localX, int localY) {
            return image.getRGB(centerX + localX, centerY + localY);
        }

        private final BufferedImage image;
        private final int centerX;
        private final int centerY;

    }

    private static final double DISTANCE_TOLERANCE = 1e-6;

    private static final Color BACKGROUND = Color.BLUE;
    private static final int BACKGROUND_RGB = BACKGROUND.getRGB();
    private static final int BORDER_RGB = Color.BLACK.getRGB();
    private static final int FILL_RGB = Color.RED.getRGB();

}
