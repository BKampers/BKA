/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package gpx;

import java.util.*;


/**
 * GPX document root ({@code gpx} / gpxType).
 */
public final class Gpx {

    public Gpx(
        String version,
        String creator,
        Optional<Metadata> metadata,
        List<Waypoint> waypoints,
        List<Route> routes,
        List<Track> tracks,
        List<Extension> extensions
    ) {
        this.version = Objects.requireNonNull(version);
        this.creator = Objects.requireNonNull(creator);
        this.metadata = Objects.requireNonNull(metadata);
        this.waypoints = List.copyOf(waypoints);
        this.routes = List.copyOf(routes);
        this.tracks = List.copyOf(tracks);
        this.extensions = List.copyOf(extensions);
    }

    public String getVersion() {
        return version;
    }

    public String getCreator() {
        return creator;
    }

    public Optional<Metadata> getMetadata() {
        return metadata;
    }

    public List<Waypoint> getWaypoints() {
        return waypoints;
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public List<Track> getTracks() {
        return tracks;
    }

    public List<Extension> getExtensions() {
        return extensions;
    }

    private final String version;
    private final String creator;
    private final Optional<Metadata> metadata;
    private final List<Waypoint> waypoints;
    private final List<Route> routes;
    private final List<Track> tracks;
    private final List<Extension> extensions;

}
