/*
** © Bart Kampers
*/

package bka.awt.clock;

import java.awt.*;
import java.util.*;


public class ImageRenderer implements Renderer {

    public ImageRenderer(Image image) {
        this.image = Objects.requireNonNull(image);
    }

    @Override
    public void paint(Graphics2D graphics) {
        graphics.drawImage(image, image.getWidth(null) / -2, image.getHeight(null) / -2, null);
    }

    private final Image image;

}
