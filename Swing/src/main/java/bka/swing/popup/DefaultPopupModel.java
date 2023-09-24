/*
** © Bart Kampers
*/
package bka.swing.popup;

import java.awt.*;
import java.util.*;
import java.util.function.*;


    public abstract class DefaultPopupModel<T> implements PopupModel<T> {

        
        protected DefaultPopupModel(Rectangle bounds, T initialValue, Consumer<T> onApply) {
            this.bounds = Objects.requireNonNull(bounds);
            this.initialValue = initialValue;
            this.onApply = Objects.requireNonNull(onApply);
        }
        
        
        @Override
        public Point getLocation() {
            return bounds.getLocation();
        }
 
        
        @Override
        public Dimension getSize() {
            return bounds.getSize();
        }
        
        
        @Override
        public T getInitialValue() {
            return initialValue;
        }
        
        
        @Override
        public void applyNewValue() {
            onApply.accept(getNewValue());
        }
        
        
        protected abstract T getNewValue();
        
        
        private final Rectangle bounds;
        private final T initialValue;
        private final Consumer<T> onApply;
        
    }