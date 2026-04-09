package bka.theworks;

import java.time.*;
import java.util.*;


/**
 */
public interface Track {

    String getTitle();

    Duration getDuration();

    Optional<String> getArtist();

    OptionalInt getYear();

    Optional<ZonedDateTime> getPlayDate();

    OptionalLong getAlbumId();

    OptionalInt getDiscNumber();

    OptionalInt getTrackNumber();
}
