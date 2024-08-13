/*
** © Bart Kampers
*/
package bka.demo.clock.weatherstation;

import java.util.*;
import java.util.function.*;

public enum Measurement {
    TEMPERATURE(station -> station.getTemperature()),
    CHILL(station -> computeValue(station.getChill(), () -> station.getTemperature())),
    HUMIDITY(station -> station.getHumidity()),
    WIND_DIRECTION(station -> station.getWindDirection()),
    WIND_SPEED(station -> station.getWindSpeed()),
    SQUALL(station -> computeValue(station.getSquall(), value -> Optional.of(value.get() / 3.6), () -> station.getWindSpeed())),
    PRESSURE(station -> station.getPressure()),
    VISIBILITY(station -> computeValue(station.getVisibility(), value -> Optional.of(value.get() / 1000)));

    private Measurement(Function<WeatherStation, Optional<Double>> provider) {
        this.provider = provider;
    }

    public Optional<Double> getValue(WeatherStation station) {
        return provider.apply(station);
    }

    private static Optional<Double> computeValue(Optional<Double> value, Supplier<Optional<Double>> alternative) {
        return computeValue(value, Function.identity(), alternative);
    }

    private static Optional<Double> computeValue(Optional<Double> value, Function<Optional<Double>, Optional<Double>> processor) {
        return computeValue(value, processor, Optional::empty);
    }

    private static Optional<Double> computeValue(Optional<Double> value, Function<Optional<Double>, Optional<Double>> processor, Supplier<Optional<Double>> alternative) {
        if (value.isEmpty()) {
            return alternative.get();
        }
        return processor.apply(value);
    }

    private final Function<WeatherStation, Optional<Double>> provider;
}
