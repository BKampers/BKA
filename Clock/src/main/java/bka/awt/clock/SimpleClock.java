package bka.awt.clock;

import java.awt.*;

public class SimpleClock extends ClockRenderer {

    public SimpleClock() {
        super(null);
    }

    public void setDiameter(int diameter) {
        this.diameter = diameter;
    }

    public void setBackground(Paint background) {
        this.background = background;
    }
    
    @Override
    public void paintFace(Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(Color.BLACK);
        graphics.fillOval(0, 0, diameter, diameter);
        if (background != null) {
            graphics.setPaint(background);
        }
        graphics.fillOval(4, 4, diameter - 8, diameter - 8);
    }

    private int diameter;
    private Paint background;
    
}
