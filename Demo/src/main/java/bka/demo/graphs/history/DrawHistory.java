/*
** © Bart Kampers
*/

package bka.demo.graphs.history;

import java.util.*;


public class DrawHistory {

    public interface Listener {

        void historyChanged(DrawHistory history);
    }

    public void addListener(Listener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void removeListener(Listener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    public List<Mutation> getMutattions() {
        return Collections.unmodifiableList(history);
    }

    public int getIndex() {
        return index;
    }

    public boolean canUndo() {
        return index > 0;
    }

    public Mutation getUndo() {
        if (!canUndo()) {
            throw new NoSuchElementException("Cannot undo");
        }
        index--;
        notifyListeners();
        return history.get(index);
    }

    public void undo() {
        if (canUndo()) {
            getUndo().undo();
        }
    }

    public boolean canRedo() {
        return index < history.size();
    }

    public Mutation getRedo() {
        if (!canRedo()) {
            throw new NoSuchElementException("Cannot redo");
        }
        Mutation mutation = history.get(index);
        index++;
        notifyListeners();
        return mutation;
    }

    public void redo() {
        if (canRedo()) {
            getRedo().redo();
        }
    }

    public void add(Mutation mutation) {
        while (index < history.size()) {
            history.removeLast();
        }
        history.add(mutation);
        index++;
        notifyListeners();
    }

    private void notifyListeners() {
        synchronized (listeners) {
            for (Listener listener : listeners) {
                listener.historyChanged(DrawHistory.this);
            }
        }
    }

    private final LinkedList<Mutation> history = new LinkedList<>();
    private final Collection<Listener> listeners = new ArrayList<>();

    private int index;

}
