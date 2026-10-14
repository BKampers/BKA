/*
** © Bart Kampers
*/

package bka.demo.swing;

import bka.awt.Platform;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.*;
import javax.swing.*;


public final class FrameDecorator {

    public FrameDecorator(JFrame frame, IntFunction<Image> imageCreator) {
        this.frame = Objects.requireNonNull(frame);
        this.imageCreator = Objects.requireNonNull(imageCreator);
        this.detectedPlatform = Platform.system();
    }

    public void updateIcon() {
        detectedPlatform.ifPresentOrElse(
            this::setPlatformIcon,
            () -> frame.setIconImage(imageCreator.apply(DEFAULT_ICON_SIZE)));
    }

    private void setPlatformIcon(Platform platform) throws IllegalStateException {
        switch (platform) {
            case MAC ->
                Taskbar.getTaskbar().setIconImage(imageCreator.apply(DEFAULT_ICON_SIZE));
            case WINDOWS ->
                frame.setIconImages(List.of(
                    imageCreator.apply(THUMB_ICON_SIZE),
                    imageCreator.apply(SMALL_ICON_SIZE)));
            default ->
                throw new IllegalStateException(platform.name());
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
    private final Optional<Platform> detectedPlatform;

    private static final int DEFAULT_ICON_SIZE = 1024;
    private static final int SMALL_ICON_SIZE = 256;
    private static final int THUMB_ICON_SIZE = 32;
    private static final Stroke BORDER_STROKE = new BasicStroke(3);

}
