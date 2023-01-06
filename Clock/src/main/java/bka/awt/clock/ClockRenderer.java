package bka.awt.clock;

import java.awt.*;
import java.awt.image.*;
import java.util.*;

public abstract class ClockRenderer {

    protected ClockRenderer(ImageObserver imageObserver) {
        this.imageObserver = imageObserver;
    }

    public abstract void paintFace(Graphics2D graphics);

    public void add(Renderer renderer) {
        components.add(renderer);
    }

    public void paint(Graphics2D graphics) {
        paintFace(graphics);
        components.forEach(renderer -> renderer.paint(graphics));
    }

    protected ImageObserver getImageObserver() {
        return imageObserver;
    }

    private final ArrayList<Renderer> components = new ArrayList<>();
    private final ImageObserver imageObserver;
    
}
