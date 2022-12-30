package bka.awt.clock;

import java.awt.*;
import java.util.*;

public class ImageValueRing extends ValueRing {

    public ImageValueRing(Point center, int radius, Scale scale, Image[] images, Dimension dimension) {
        this(center, radius, scale, 1.0, images, dimension);
    }

    public ImageValueRing(Point center, int radius, Scale scale, double interval, Image[] images, Dimension dimension) {
        this(center, radius, scale, interval, true, images, dimension);
    }

    public ImageValueRing(Point center, int radius, Scale scale, double interval, boolean itemsRotated, Image[] images, Dimension dimension) {
        super(center, radius, scale, interval, itemsRotated);
        setImages(images);
        setDimensions(dimension);
    }

    public final void setImages(Image[] images) {
        this.images = requireValidArray(images);
    }

    public final void setDimensions(Dimension[] dimensions) {
        this.dimensions = requireValidArray(dimensions);
    }

    private static <T> T[] requireValidArray(T[] array) {
        if (array.length != 10) {
            throw new IllegalArgumentException("Array count not 10");
        }
        if (Arrays.stream(array).anyMatch(Objects::isNull)) {
            throw new NullPointerException("Array element");
        }
        return array;
    }

    public final void setDimensions(Dimension dimension) {
        Objects.requireNonNull(dimension);
        dimensions = new Dimension[10];
        for (int i = 0; i < 10; i++) {
            dimensions[i] = dimension;
        }
    }

    @Override
    protected void paintValue(Graphics2D graphics, String value) {
        int[] indexes = value.chars().filter(ch -> '0' <= ch && ch <= '9').map(ch -> ch - '0').toArray();
        int width = 0;
        int height = 0;
        for (int i : indexes) {
            width += dimensions[i].width;
            height = Math.max(height, dimensions[i].height);
        }
        width /= -2;
        height /= -2;
        for (int i : indexes) {
            graphics.drawImage(images[i], width, height, null);
            width += dimensions[i].width;
        }
    }

    private Image[] images;
    private Dimension[] dimensions; 
    
}
