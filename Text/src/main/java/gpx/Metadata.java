/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package gpx;

import java.time.*;
import java.util.*;


/**
 * File metadata ({@code metadata} / metadataType).
 */
public final class Metadata {

    public Metadata(
        Optional<String> name,
        Optional<String> description,
        Optional<Person> author,
        Optional<Copyright> copyright,
        List<Link> links,
        Optional<Instant> time,
        Optional<String> keywords,
        Optional<Bounds> bounds,
        List<Extension> extensions
    ) {
        this.name = Objects.requireNonNull(name);
        this.description = Objects.requireNonNull(description);
        this.author = Objects.requireNonNull(author);
        this.copyright = Objects.requireNonNull(copyright);
        this.links = List.copyOf(links);
        this.time = Objects.requireNonNull(time);
        this.keywords = Objects.requireNonNull(keywords);
        this.bounds = Objects.requireNonNull(bounds);
        this.extensions = List.copyOf(extensions);
    }

    public Optional<String> getName() {
        return name;
    }

    public Optional<String> getDescription() {
        return description;
    }

    public Optional<Person> getAuthor() {
        return author;
    }

    public Optional<Copyright> getCopyright() {
        return copyright;
    }

    public List<Link> getLinks() {
        return links;
    }

    public Optional<Instant> getTime() {
        return time;
    }

    public Optional<String> getKeywords() {
        return keywords;
    }

    public Optional<Bounds> getBounds() {
        return bounds;
    }

    public List<Extension> getExtensions() {
        return extensions;
    }

    private final Optional<String> name;
    private final Optional<String> description;
    private final Optional<Person> author;
    private final Optional<Copyright> copyright;
    private final List<Link> links;
    private final Optional<Instant> time;
    private final Optional<String> keywords;
    private final Optional<Bounds> bounds;
    private final List<Extension> extensions;

}
