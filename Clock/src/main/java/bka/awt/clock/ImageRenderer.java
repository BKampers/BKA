/*
** © Bart Kampers
*/

package bka.awt.clock;

import bka.awt.*;
import java.awt.*;
import java.util.*;


public class ImageRenderer implements Renderer {

    public ImageRenderer(Image image) {
        this(image, image.getWidth(null) / -2, image.getHeight(null) / -2);
    }

    public ImageRenderer(Image image, int x, int y) {
        this.image = Objects.requireNonNull(image);
        this.x = x;
        this.y = y;
    }

    @Override
    public void paint(Graphics2D graphics) {
        graphics.drawImage(image, x, y, null);
    }

    private final Image image;
    private int x;
    private int y;

}
