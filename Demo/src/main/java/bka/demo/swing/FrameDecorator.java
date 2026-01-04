/*
** © Bart Kampers
*/

package bka.demo.swing;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.*;
import javax.swing.*;


public class FrameDecorator {

    public FrameDecorator(JFrame frame, IntFunction<Image> imageCreator) {
        this.frame = Objects.requireNonNull(frame);
        this.imageCreator = Objects.requireNonNull(imageCreator);
    }

    public void updateIcon() {
        String osName = System.getProperty("os.name");
        if (osName.contains("Mac")) {
            Taskbar.getTaskbar().setIconImage(imageCreator.apply(DEFAULT_ICON_SIZE));
        }
        else if (osName.contains("Windows")) {
            frame.setIconImages(List.of(
                imageCreator.apply(THUMB_ICON_SIZE),
                imageCreator.apply(SMALL_ICON_SIZE)));
        }
        else {
            frame.setIconImage(imageCreator.apply(DEFAULT_ICON_SIZE));
        }
    }

    public static void drawIconBackground(Graphics2D graphics, int size) {
        int margin = size / 10;
        int innerSize = size - 2 * margin;
        int arcSize = margin * 3;
        graphics.setColor(Color.WHITE);
        graphics.fillRoundRect(margin, margin, innerSize, innerSize, arcSize, arcSize);
        graphics.setColor(Color.BLUE);
        graphics.setStroke(BORDER_STROKE);
        graphics.drawRoundRect(margin, margin, innerSize, innerSize, arcSize, arcSize);

    }

    private final JFrame frame;
    private final IntFunction<Image> imageCreator;

    private static final int DEFAULT_ICON_SIZE = 1024;
    private static final int SMALL_ICON_SIZE = 256;
    private static final int THUMB_ICON_SIZE = 32;
    private static final Stroke BORDER_STROKE = new BasicStroke(3);

}
