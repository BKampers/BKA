/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package gpx;

import java.util.*;


/**
 * Opaque extension element from a non-GPX namespace.
 */
public final class Extension {

    public Extension(String uri, String localName, String characters, List<Extension> children) {
        this.uri = Objects.requireNonNull(uri);
        this.localName = Objects.requireNonNull(localName);
        this.characters = Objects.requireNonNull(characters);
        this.children = List.copyOf(children);
    }

    public String getUri() {
        return uri;
    }

    public String getLocalName() {
        return localName;
    }

    public String getCharacters() {
        return characters;
    }

    public List<Extension> getChildren() {
        return children;
    }

    private final String uri;
    private final String localName;
    private final String characters;
    private final List<Extension> children;

}
