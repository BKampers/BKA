/*
** © Bart Kampers
*/
package bka.swing.popup;

import java.awt.*;


public interface PopupModel<T> {

    Component getComponent();
    Point getLocation();
    Dimension getSize();
    void bindListener(Runnable whenReady);
    T getInitialValue();
    void applyNewValue();
    
  }
