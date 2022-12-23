package bka.awt.clock;


import java.awt.*;

public class Text {
    
    public Text(String string, Point point) {
        this.string = string;
        this.point = point;
    }
    
    
    public Text(String string, Point point, Font font, Color color) {
        this.string = string;
        this.point = point;
        this.font = font;
        this.color = color;
    }

    
    public void paint(Graphics2D graphics) {
        graphics.setFont(font);
        graphics.setColor(color);
        int width = graphics.getFontMetrics().stringWidth(string);
        int height = graphics.getFontMetrics().getHeight();
        graphics.drawString(string, point.x - width / 2, point.y + height / 2);
    }

    
    public String getString() {
        return string;
    }
    
    
    public Font getFont() {
        return font;
    }
    
    
    public Color getColor() {
        return color;
    }
    
    
    private String string;
    private Point point;
    
    private Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 50);
    private Color color = Color.BLACK;
    
}
