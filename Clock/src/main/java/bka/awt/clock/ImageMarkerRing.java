package bka.awt.clock;

import java.awt.*;

public class ImageMarkerRing extends MarkerRing {

    public ImageMarkerRing(Point center, int radius, Scale scale) {
        this(center, radius, scale, null, null, null, null);
    }

    public ImageMarkerRing(Point center, int radius, Scale scale, double interval) {
        this(center, radius, scale, interval, null, null, null, null);
    }

    public ImageMarkerRing(Point center, int radius, Scale scale, double interval, boolean itemsRotated) {
        this(center, radius, scale, interval, itemsRotated, null, null, null, null);
    }

    public ImageMarkerRing(Point center, int radius, Scale scale, Image minorImage, Dimension minorDimension, Image majorImage, Dimension majorDimension) {
        this(center, radius, scale, 1.0, true, minorImage, minorDimension, majorImage, majorDimension);
    }

    public ImageMarkerRing(Point center, int radius, Scale scale, double interval, Image minorImage, Dimension minorDimension, Image majorImage, Dimension majorDimension) {
        this(center, radius, scale, interval, true, minorImage, minorDimension, majorImage, majorDimension);
    }

    public ImageMarkerRing(Point center, int radius, Scale scale, double interval, boolean itemsRotated, Image minorImage, Dimension minorDimension, Image majorImage, Dimension majorDimension) {
        super(center, radius, scale, interval, itemsRotated);
        setMajorImage(majorImage, majorDimension);
        setMinorImage(minorImage, minorDimension);
    }
    
    public final void setMajorImage(Image image, Dimension dimension) {
        if (image != null && dimension == null) {
            throw new IllegalArgumentException("Major image requires dimension");
        }
        majorImage = image;
        majorDimension = dimension;
    }

    public final void setMinorImage(Image image, Dimension dimension) {
        if (image != null && dimension == null) {
            throw new IllegalArgumentException("Minor image requires dimension");
        }
        minorImage = image;
        minorDimension = dimension;
    }

    @Override
    public void paintMajorMarker(Graphics2D graphics) {
        if (majorImage != null) {
            paintMarker(graphics, majorImage, majorDimension);
        }
    }

    @Override
    public void paintMinorMarker(Graphics2D graphics) {
        if (minorImage != null) {
            paintMarker(graphics, minorImage, minorDimension);
        }
    }
    
    private void paintMarker(Graphics2D graphics, Image image, Dimension dimension) {
        graphics.drawImage(image, dimension.width / -2, dimension.height / 2, null);
    }

    private Image minorImage;
    private Image majorImage;
    private Dimension minorDimension;
    private Dimension majorDimension;

}
