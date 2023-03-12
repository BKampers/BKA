/*
** © Bart Kampers
*/

package bka.demo.clock;

import java.awt.*;
import java.io.*;
import javax.imageio.*;
import org.apache.batik.transcoder.*;
import org.apache.batik.transcoder.image.*;


public class ImageFactory {

    public static Image loadImage(String filename) throws IOException {
        return loadImage(filename, null);
    }

    public static Image loadImage(String filename, int width, int height) throws IOException {
        return loadImage(filename, new Dimension(width, height));
    }

    public static Image loadImage(String filename, Dimension dimension) throws IOException {
        if (filename.endsWith(".svg")) {
            return loadSvgImage(filename, dimension);
        }
        return loadDefaultImage(filename, dimension);
    }

    private static Image loadDefaultImage(String filename, Dimension dimension) throws IOException {
        Image image = ImageIO.read(new File(filename));
        if (dimension != null) {
            return image.getScaledInstance(dimension.width, dimension.height, Image.SCALE_SMOOTH);
        }
        return image;
    }

    private static Image loadSvgImage(String filename, Dimension dimension) throws IOException {
        Transcoder transcoder = new PNGTranscoder();
        if (dimension != null) {
            transcoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, (float) dimension.getWidth());
            transcoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, (float) dimension.getHeight());
        }
        try (FileInputStream inputStream = new FileInputStream(new File(filename))) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            TranscoderOutput output = new TranscoderOutput(outputStream);
            transcoder.transcode(new TranscoderInput(inputStream), output);
            outputStream.flush();
            outputStream.close();
            byte[] imgData = outputStream.toByteArray();
            return ImageIO.read(new ByteArrayInputStream(imgData));
        }
        catch (TranscoderException ex) {
            throw new IOException(ex);
        }
    }
}
