/*
** © Bart Kampers
*/

package bka.text.parser.sax;

import java.util.*;
import org.xml.sax.*;


public interface XmlElement {


    String getUri();

    String getLocalName();

    String getQualifiedName();

    String getCharacters();

    Attributes getAttributes();

    /**
     * @param <T> type of child
     * @param qualifiedName of the child to get
     * @return Single child of given qualified name
     * @throws NoSuchElementException if no child of given qualifiedName is present
     * @throws ClassCastException if the single child is not of type T
     * @throws IllegalArgumentException if multiple children of given qualifieName are present
     */
    <T> T getChild(String qualifiedName);

    /**
     * @param <T> type of children
     * @param qualifiedName of the children to get
     * @return List of children with given qualified name, an empty list if no such children are present
     */
    <T> List<T> getChildren(String qualifiedName);

    /**
     * @param <T> type of child
     * @param uri of the child to get
     * @param localName of the child to get
     * @return Single child of uri and local name
     * @throws NoSuchElementException if no child of given uri and localName is present
     * @throws ClassCastException if the single child is not of type T
     * @throws IllegalArgumentException if multiple children of given uri and localName are available
     */
    <T> T getChild(String uri, String localName);

    /**
     * @param <T> type of children
     * @param uri of the children to get
     * @param localName of the children to get
     * @return List of children with given uri and local name, an empty list if no such children are present
     */
    <T> List<T> getChildren(String uri, String localName);

    /**
     * @param <T> type of child
     * @param localName of the child to get
     * @return Single child of given local name in the parent's namespace
     * @throws IllegalStateException if the parent element has no namespace
     * @throws NoSuchElementException if no elements with given localName are available in the parents namespace
     * @throws ClassCastException if the single element is not of type T
     * @throws IllegalArgumentException if multiple elements with given qualifieName are available
     */
    <T> T getLocalChild(String localName);

    /**
     * @param <T> type of children
     * @param localName of the children to get
     * @return List of children with given local name in the parent's namespace. An empty list if no children with local name are present for the
     * parent's namespace.
     * @throws IllegalStateException if the parent element has no namespace
     */
    <T> List<T> getLocalChildren(String localName);

}
