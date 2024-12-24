/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package bka.text.parser.sax;

import java.util.*;


class Children {

    void insert(String qualifiedName, Object object) {
        children
            .computeIfAbsent(qualifiedName, name -> new ArrayList<>())
            .add(object);
    }

    /**
     * @param <T> type of elements
     * @param qualifiedName of the elements to get
     * @return List of elements with given qualified name, an empty list if no such elements are available
     */
    public <T> List<T> getList(String qualifiedName) {
        List<Object> elements = children.get(qualifiedName);
        if (elements == null) {
            return Collections.emptyList();
        }
        return (List<T>) Collections.unmodifiableList(elements);
    }

    /**
     * @param <T> type of element
     * @param qualifiedName of the element to get
     * @return Single element with given qualified name
     * @throws NoSuchElementException if no element with given qualifiedName is available
     * @throws ClassCastException if the single element is not of type T
     * @throws IllegalArgumentException if multiple elements with given qualifieName are available
     */
    public <T> T getElement(String qualifiedName) {
        List<Object> elements = children.get(qualifiedName);
        if (elements == null) {
            throw new NoSuchElementException(qualifiedName);
        }
        if (elements.size() > 1) {
            throw new IllegalArgumentException("Multiple elements of '" + qualifiedName + "'");
        }
        return (T) elements.get(0);
    }

    public Set<String> keySet() {
        return children.keySet();
    }

    private final Map<String, List<Object>> children = new HashMap<>();
}
