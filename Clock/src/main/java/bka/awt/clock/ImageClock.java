package bka.awt.clock;

import java.awt.*;


public class ImageClock extends ClockRenderer {
 
    public ImageClock() {
        this(null);
    }
    
    public ImageClock(Image faceImage) {
        this(faceImage, 1.0, 1.0);
    }

    public ImageClock(Image faceImage, double xScale, double yScale) {
        super(null);
        setFaceImage(faceImage);
        setXScale(xScale);
        setYScale(yScale);
    }
    
    public final void setFaceImage(Image image) {
        this.image = image;
    }

    public final void setXScale(double scale) {
        xScale = scale;
    }
    
    public final void setYScale(double scale) {
        yScale = scale;
    }
    
    @Override
    public void paintFace(Graphics2D graphics) {
        graphics.scale(xScale, yScale);
        graphics.drawImage(image, 0, 0, getImageObserver());
    }

    private double xScale = 1.0;
    private double yScale = 1.0;
    private Image image;    
    
}
