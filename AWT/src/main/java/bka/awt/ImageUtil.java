/*
** © Bart Kampers
*/

package bka.awt;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.function.*;
import javax.imageio.*;


public class ImageUtil {


    public static void main(String[] args) throws IOException {
        BufferedImage image = load(new File("/Users/bartkampers/Library/Mobile Documents/com~apple~CloudDocs/Scouting/Jubileumactiviteiten.png"));
        final int width = image.getWidth(null);
        final int height = image.getHeight(null);
        final int size = width * height;
        int[] rgb = new int[size];
        image.getRGB(0, 0, width, height, rgb, 0, width);
        Map<Integer, Integer> colors = new TreeMap<>();
        for (int i = 0; i < size; ++i) {
            colors.put(rgb[i], colors.computeIfAbsent(rgb[i], c -> 0) + 1);
        }
        colors.forEach((color, count) -> {
            double percentage = ((double) count / (double) size) * 100.0;
            if (percentage >= 1.0) {
                System.out.printf("%08x: %6.2f%%\n", color, percentage);
            }
        });
        ImageMap map = new ImageMap(image);
        Consumer<Integer> modify = (Integer index) -> {
            int distance = map.discatanceToBorder(index);
            int alpha = (distance < 255) ? distance << 24 : 0xFF000000;
            map.setArgb(index, (map.getArgb(index) & 0x00FFFFFF) | alpha);
        };
        for (int x = 0; x < map.getWidth(); ++x) {
            map.area(x, 0, color -> isTransparent(color)).forEach(modify);
            map.area(x, map.getHeight() - 1, color -> isTransparent(color)).forEach(modify);
        }
        for (int y = 0; y < map.getHeight(); ++y) {
            map.area(0, y, color -> isTransparent(color)).forEach(modify);
            map.area(map.getWidth() - 1, y, color -> isTransparent(color)).forEach(modify);
        }
        ImageIO.write(map.getImage(), "png", new File("/Users/bartkampers/Library/Mobile Documents/com~apple~CloudDocs/Scouting/Afbeeldingen/Jubileumactiviteiten.png"));
    }

    private static boolean isTransparent(Color color) {
        return color.getAlpha() <= 1;
    }

    public static BufferedImage load(File file) throws IOException {
        Image image = ImageIO.read(file);
        BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = bufferedImage.createGraphics();
        graphics.drawImage(image, 0, 0, null);
        graphics.dispose();
        return bufferedImage;
    }


    private static class ImageMap {

        public ImageMap(BufferedImage origin) {
            width = origin.getWidth();
            height = origin.getHeight();
            argb = new int[width * height];
            origin.getRGB(0, 0, width, height, argb, 0, width);
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public BufferedImage getImage() {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            image.setRGB(0, 0, width, height, argb, 0, width);
            return image;
        }

        public int getArgb(int index) {
            return argb[index];
        }

        public void setArgb(int index, int argb) {
            this.argb[index] = argb;
        }

        public int discatanceToBorder(int index) {
            int x = x(index);
            int y = y(index);
            return Math.min(x, Math.min(y, Math.min(width - 1 - x, height - 1 - y)));
        }

        public Collection<Integer> area(int x, int y, Predicate<Color> predicate) {
            Set<Integer> indexes = new HashSet<>();
            Collection<Integer> toValidate = java.util.List.of(index(x, y));
            while (!toValidate.isEmpty()) {
                Collection<Integer> neighbors = new HashSet<>();
                toValidate.forEach(index -> {
                    if (!indexes.contains(index) && predicate.test(new Color(argb[index], true))) {
                        indexes.add(index);
                        neighbors.addAll(neighbors(index));
                    }
                });
                toValidate = neighbors;
            }
            return indexes;
        }

        private Collection<Integer> neighbors(int index) {
            Collection<Integer> neighbors = new ArrayList<>(8);
            int x = x(index);
            int y = y(index);
            addValid(x - 1, y - 1, neighbors);
            addValid(x - 1, y, neighbors);
            addValid(x - 1, y + 1, neighbors);
            addValid(x, y - 1, neighbors);
            addValid(x, y + 1, neighbors);
            addValid(x + 1, y - 1, neighbors);
            addValid(x + 1, y, neighbors);
            addValid(x + 1, y + 1, neighbors);
            return neighbors;
        }

        private void addValid(int x, int y, Collection<Integer> indexes) {
            if (0 <= x && x < width && 0 <= y && y < height) {
                indexes.add(index(x, y));
            }
        }

        private int index(int x, int y) {
            return x + y * width;
        }

        private int x(int index) {
            return index % width;
        }

        private int y(int index) {
            return index / width;
        }

        private final int[] argb;
        private final int width;
        private final int height;
    }

    private static final int BLACK = 0xFF000000;
    private static final int BLUE = 0xFF0000FF;
    private static final int GREEN = 0xFF00FF00;
    private static final int RED = 0xFFFF0000;
    private static final int WHITE = 0xFFFFFFFF;

    private static final int TRANSPARENT = 0x00FFFFFF;

}
