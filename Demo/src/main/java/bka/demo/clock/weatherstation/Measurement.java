/*
** © Bart Kampers
 */
package bka.demo.clock.weatherstation;

import java.util.*;
import java.util.function.*;

public enum Measurement {
    TEMPERATURE(station -> station.getTemperature()),
    CHILL(station -> station.getChill().or(station::getTemperature)),
    HUMIDITY(station -> station.getHumidity()),
    WIND_DIRECTION(station -> station.getWindDirection()),
    WIND_SPEED(station -> station.getWindSpeed()),
    SQUALL(station -> computeValue(station.getSquall(), Measurement::toMetersPerSecond, station::getWindSpeed)),
    PRESSURE(station -> station.getPressure()),
    VISIBILITY(station -> computeValue(station.getVisibility(), Measurement::toKilometers));

    private static Double toMetersPerSecond(Double kilomertersPerHour) {
        return kilomertersPerHour / 3.6;
    }

    private static Double toKilometers(Double meters) {
        return meters / 1000;
    }

    private Measurement(Function<WeatherStation, Optional<Double>> provider) {
        this.provider = provider;
    }

    public Optional<Double> getValue(WeatherStation station) {
        return provider.apply(station);
    }

    private static Optional<Double> computeValue(Optional<Double> value, UnaryOperator<Double> processor) {
        return computeValue(value, processor, Optional::empty);
    }


    private static Optional<Double> computeValue(Optional<Double> value, UnaryOperator<Double> processor, Supplier<Optional<Double>> alternative) {
        if (value.isEmpty()) {
            return alternative.get();
        }
        return Optional.of(processor.apply(value.get()));
    }

    private final Function<WeatherStation, Optional<Double>> provider;
}
