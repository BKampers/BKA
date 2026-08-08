/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package gpx;

import java.util.*;


/**
 * Track ({@code trk} / trkType).
 */
public final class Track {

    public Track(
        Optional<String> name,
        Optional<String> comment,
        Optional<String> description,
        Optional<String> source,
        List<Link> links,
        OptionalInt number,
        Optional<String> type,
        List<Extension> extensions,
        List<TrackSegment> segments
    ) {
        this.name = Objects.requireNonNull(name);
        this.comment = Objects.requireNonNull(comment);
        this.description = Objects.requireNonNull(description);
        this.source = Objects.requireNonNull(source);
        this.links = List.copyOf(links);
        this.number = Objects.requireNonNull(number);
        this.type = Objects.requireNonNull(type);
        this.extensions = List.copyOf(extensions);
        this.segments = List.copyOf(segments);
    }

    public Optional<String> getName() {
        return name;
    }

    public Optional<String> getComment() {
        return comment;
    }

    public Optional<String> getDescription() {
        return description;
    }

    public Optional<String> getSource() {
        return source;
    }

    public List<Link> getLinks() {
        return links;
    }

    public OptionalInt getNumber() {
        return number;
    }

    public Optional<String> getType() {
        return type;
    }

    public List<Extension> getExtensions() {
        return extensions;
    }

    public List<TrackSegment> getSegments() {
        return segments;
    }

    private final Optional<String> name;
    private final Optional<String> comment;
    private final Optional<String> description;
    private final Optional<String> source;
    private final List<Link> links;
    private final OptionalInt number;
    private final Optional<String> type;
    private final List<Extension> extensions;
    private final List<TrackSegment> segments;

}
