/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package bka.demo.gps;

import gpx.Gpx;
import gpx.Track;
import gpx.TrackSegment;
import gpx.Waypoint;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import java.util.stream.*;
import javax.swing.*;


/**
 * Canvas that draws GPX tracks as polylines and waypoints as markers.
 *
 * <p>Drag a rectangle with the mouse to zoom in; double-click to reset the view.
 */
public final class GpxCanvas extends JPanel {

    public GpxCanvas() {
        setPreferredSize(new Dimension(900, 700));
        setBackground(BACKGROUND);
        MouseAdapter zoomListener = createZoomListener();
        addMouseListener(zoomListener);
        addMouseMotionListener(zoomListener);
    }

    public void setGpx(Gpx gpx) {
        this.gpx = Objects.requireNonNull(gpx);
        hiddenTracks.clear();
        hiddenWaypoints.clear();
        fitBounds = GeographicBounds.from(gpx);
        bounds = fitBounds;
        selection = null;
        repaint();
    }

    public void setTrackVisible(Track track, boolean visible) {
        if (visible) {
            hiddenTracks.remove(track);
        }
        else {
            hiddenTracks.add(track);
        }
        repaint();
    }

    public void setWaypointVisible(Waypoint waypoint, boolean visible) {
        if (visible) {
            hiddenWaypoints.remove(waypoint);
        }
        else {
            hiddenWaypoints.add(waypoint);
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g2 = (Graphics2D) graphics.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (gpx == null || bounds.isEmpty()) {
                drawPlaceholder(g2);
                return;
            }
            drawTracks(g2);
            drawWaypoints(g2);
            drawSelection(g2);
        }
        finally {
            g2.dispose();
        }
    }

    private MouseAdapter createZoomListener() {
        return new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                if (!SwingUtilities.isLeftMouseButton(event) || gpx == null || bounds.isEmpty()) {
                    return;
                }
                dragOrigin = event.getPoint();
                selection = new Rectangle(dragOrigin);
            }

            @Override
            public void mouseDragged(MouseEvent event) {
                if (dragOrigin == null) {
                    return;
                }
                selection = createSelection(dragOrigin, event.getPoint());
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                if (dragOrigin == null) {
                    return;
                }
                Rectangle zoomArea = createSelection(dragOrigin, event.getPoint());
                dragOrigin = null;
                selection = null;
                if (zoomArea.width >= MIN_ZOOM_PIXELS && zoomArea.height >= MIN_ZOOM_PIXELS) {
                    zoomTo(zoomArea);
                }
                else {
                    repaint();
                }
            }

            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(event) && fitBounds != null) {
                    bounds = fitBounds;
                    selection = null;
                    repaint();
                }
            }
        };
    }

    private void zoomTo(Rectangle area) {
        ViewTransform transform = viewTransform();
        double latitude1 = transform.toLatitude(area.getMinY());
        double longitude1 = transform.toLongitude(area.getMinX());
        double latitude2 = transform.toLatitude(area.getMaxY());
        double longitude2 = transform.toLongitude(area.getMaxX());
        GeographicBounds zoomed = new GeographicBounds(
            Math.min(latitude1, latitude2),
            Math.min(longitude1, longitude2),
            Math.max(latitude1, latitude2),
            Math.max(longitude1, longitude2)
        );
        if (!zoomed.isEmpty()) {
            bounds = zoomed;
        }
        repaint();
    }

    private static Rectangle createSelection(Point origin, Point current) {
        int x = Math.min(origin.x, current.x);
        int y = Math.min(origin.y, current.y);
        int width = Math.abs(origin.x - current.x);
        int height = Math.abs(origin.y - current.y);
        return new Rectangle(x, y, width, height);
    }

    private void drawPlaceholder(Graphics2D g2) {
        g2.setColor(Color.DARK_GRAY);
        g2.setFont(getFont().deriveFont(Font.PLAIN, 16f));
        String message = "Open a GPX file to display tracks and waypoints";
        FontMetrics metrics = g2.getFontMetrics();
        g2.drawString(
            message,
            (getWidth() - metrics.stringWidth(message)) / 2,
            getHeight() / 2
        );
    }

    private void drawTracks(Graphics2D g2) {
        g2.setStroke(TRACK_STROKE);
        int trackIndex = 0;
        for (Track track : gpx.getTracks()) {
            if (!hiddenTracks.contains(track)) {
                g2.setColor(TRACK_COLORS[trackIndex % TRACK_COLORS.length]);
                for (TrackSegment segment : track.getSegments()) {
                    drawPolyline(g2, segment.getPoints());
                }
            }
            trackIndex++;
        }
    }

    private void drawPolyline(Graphics2D g2, List<Waypoint> points) {
        if (points.size() == 1) {
            drawMarker(g2, points.getFirst(), TRACK_POINT_RADIUS);
        }
        else if (points.size() > 1) {
            Path2D path = new Path2D.Double();
            Point2D.Double first = toPixel(points.getFirst());
            path.moveTo(first.x, first.y);
            points.stream()
                .skip(1)
                .map(this::toPixel)
                .forEach(point -> path.lineTo(point.x, point.y));
            g2.draw(path);
        }
    }

    private void drawWaypoints(Graphics2D g2) {
        g2.setColor(WAYPOINT_COLOR);
        for (Waypoint waypoint : gpx.getWaypoints()) {
            if (!hiddenWaypoints.contains(waypoint)) {
                drawMarker(g2, waypoint, WAYPOINT_RADIUS);
                waypoint.getName().ifPresent(name -> drawLabel(g2, waypoint, name));
            }
        }
    }

    private void drawSelection(Graphics2D g2) {
        if (selection == null || selection.isEmpty()) {
            return;
        }
        g2.setColor(SELECTION_FILL);
        g2.fill(selection);
        g2.setColor(SELECTION_BORDER);
        g2.setStroke(SELECTION_STROKE);
        g2.draw(selection);
    }

    private void drawMarker(Graphics2D g2, Waypoint waypoint, int radius) {
        Point2D.Double pixel = toPixel(waypoint);
        g2.fill(new Ellipse2D.Double(pixel.x - radius, pixel.y - radius, radius * 2.0, radius * 2.0));
    }

    private void drawLabel(Graphics2D g2, Waypoint waypoint, String name) {
        Point2D.Double pixel = toPixel(waypoint);
        g2.setFont(getFont().deriveFont(Font.PLAIN, 12f));
        g2.drawString(name, (float) pixel.x + WAYPOINT_RADIUS + 4, (float) pixel.y - 4);
    }

    private Point2D.Double toPixel(Waypoint waypoint) {
        return viewTransform().toPixel(waypoint.getLatitude(), waypoint.getLongitude());
    }

    private ViewTransform viewTransform() {
        double width = getWidth() - 2.0 * MARGIN;
        double height = getHeight() - 2.0 * MARGIN;
        double longitudeSpan = Math.max(bounds.maxLongitude() - bounds.minLongitude(), MIN_SPAN);
        double latitudeSpan = Math.max(bounds.maxLatitude() - bounds.minLatitude(), MIN_SPAN);
        double scale = Math.min(width / longitudeSpan, height / latitudeSpan);
        double offsetX = MARGIN + (width - longitudeSpan * scale) / 2.0;
        double offsetY = MARGIN + (height - latitudeSpan * scale) / 2.0;
        return new ViewTransform(scale, offsetX, offsetY, bounds);
    }


    private record ViewTransform(double scale, double offsetX, double offsetY, GeographicBounds bounds) {

        Point2D.Double toPixel(double latitude, double longitude) {
            double x = offsetX + (longitude - bounds.minLongitude()) * scale;
            double y = offsetY + (bounds.maxLatitude() - latitude) * scale;
            return new Point2D.Double(x, y);
        }

        double toLongitude(double x) {
            return bounds.minLongitude() + (x - offsetX) / scale;
        }

        double toLatitude(double y) {
            return bounds.maxLatitude() - (y - offsetY) / scale;
        }
    }


    private record GeographicBounds(double minLatitude, double minLongitude, double maxLatitude, double maxLongitude) {

        static final GeographicBounds EMPTY = new GeographicBounds(0, 0, 0, 0);

        static GeographicBounds from(Gpx gpx) {
            List<Waypoint> points = Stream.concat(
                gpx.getWaypoints().stream(),
                gpx.getTracks().stream()
                    .flatMap(track -> track.getSegments().stream())
                    .flatMap(segment -> segment.getPoints().stream())
            ).toList();
            if (points.isEmpty()) {
                return EMPTY;
            }
            DoubleSummaryStatistics latitudes = points.stream()
                .mapToDouble(Waypoint::getLatitude)
                .summaryStatistics();
            DoubleSummaryStatistics longitudes = points.stream()
                .mapToDouble(Waypoint::getLongitude)
                .summaryStatistics();
            GeographicBounds bounds = new GeographicBounds(
                latitudes.getMin(),
                longitudes.getMin(),
                latitudes.getMax(),
                longitudes.getMax()
            );
            if (!bounds.isEmpty()) {
                return bounds;
            }
            double latitude = latitudes.getMin();
            double longitude = longitudes.getMin();
            double halfSpan = MIN_SPAN / 2;
            return new GeographicBounds(
                latitude - halfSpan,
                longitude - halfSpan,
                latitude + halfSpan,
                longitude + halfSpan
            );
        }

        double area() {
            return (maxLatitude - minLatitude) * (maxLongitude - minLongitude);
        }

        boolean isEmpty() {
            return area() < EMPTY_AREA;
        }
    }


    private Gpx gpx;
    private GeographicBounds fitBounds = GeographicBounds.EMPTY;
    private GeographicBounds bounds = GeographicBounds.EMPTY;
    private Point dragOrigin;
    private Rectangle selection;
    private final Set<Track> hiddenTracks = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<Waypoint> hiddenWaypoints = Collections.newSetFromMap(new IdentityHashMap<>());

    private static final int MARGIN = 40;
    private static final double MIN_SPAN = 1e-5;
    private static final double EMPTY_AREA = 1e-20;
    private static final int MIN_ZOOM_PIXELS = 8;
    private static final int WAYPOINT_RADIUS = 6;
    private static final int TRACK_POINT_RADIUS = 3;
    private static final Color BACKGROUND = new Color(0xF4F7FA);
    private static final Color WAYPOINT_COLOR = new Color(0xC0392B);
    private static final Color SELECTION_FILL = new Color(0x331F77B4, true);
    private static final Color SELECTION_BORDER = new Color(0x1F77B4);
    private static final Stroke TRACK_STROKE = new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final Stroke SELECTION_STROKE = new BasicStroke(1.0f);
    private static final Color[] TRACK_COLORS = {
        new Color(0x1F77B4),
        new Color(0x2CA02C),
        new Color(0xFF7F0E),
        new Color(0x9467BD),
        new Color(0x17BECF)
    };

}
