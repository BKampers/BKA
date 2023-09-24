/*
** Copyright © Bart Kampers
*/
package bka.swing.popup;

import java.awt.*;
import javax.swing.*;


public final class PopupControl {

      
    public static void show(Component parent, PopupModel model) {
        PopupControl control = new PopupControl(model);
        control.show(parent);
    }
    
    
    private PopupControl(PopupModel model) {
        this.model = model;
        container.setBorder(BorderFactory.createEmptyBorder());
        container.setPreferredSize(model.getSize());
        container.add(model.getComponent());
        model.bindListener(this::apply);
    }
    
    
    private void show(Component parent) {
        Point location = model.getLocation();
        container.show(parent, location.x, location.y);
        model.getComponent().requestFocus();
    }
    
    
    private void apply() {
        model.applyNewValue();
        container.setVisible(false);
    }
    
    
    private final JPopupMenu container = new JPopupMenu();
    private final PopupModel model;

}
