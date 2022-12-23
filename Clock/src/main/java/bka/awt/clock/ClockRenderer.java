package bka.awt.clock;

import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.util.List;

public abstract class ClockRenderer {

    protected ClockRenderer(ImageObserver imageObserver) {
        this.imageObserver = imageObserver;
    }

    public abstract void paintFace(Graphics2D graphics);

    public void setNeedles(LinkedList<Needle> needles) {
        this.needles = needles;
    }

    public void addNeedle(Needle needle) {
        needles.add(needle);
    }

    public void setRings(LinkedList<Ring> rings) {
        this.rings = rings;
    }

    public void addRing(Ring ring) {
        rings.add(ring);
    }

    public void addText(Text text) {
        texts.add(text);
    }

    public void paint(Graphics2D graphics) {
        paintFace(graphics);
        texts.forEach(text -> text.paint(graphics));
        rings.forEach(ring -> ring.paint(graphics));
        needles.forEach(needle -> needle.paint(graphics));
    }

    protected ImageObserver getImageObserver() {
        return imageObserver;
    }

    private List<Needle> needles = new ArrayList<>(1);
    private List<Ring> rings = new ArrayList<>(0);
    private final List<Text> texts = new ArrayList<>(0);

    private final ImageObserver imageObserver;
    
}
