/*
 * © Bart Kampers
 */
package bka.demo.clock.weatherstation;

import java.util.function.*;

public enum Measurement {
    TEMPERATURE(station -> station.getTemperature()),
    CHILL(station -> computeValue(station.getChill(), () -> station.getTemperature())),
    HUMIDITY(station -> station.getHumidity()),
    WIND_DIRECTION(station -> station.getWindDirection()),
    WIND_SPEED(station -> station.getWindSpeed()),
    SQUALL(station -> computeValue(station.getSquall(), value -> value / 3.6, () -> station.getWindSpeed())),
    PRESSURE(station -> station.getPressure()),
    VISIBILITY(station -> computeValue(station.getVisibility(), value -> value / 1000));

    private Measurement(Function<WeatherStation, Double> provider) {
        this.provider = provider;
    }

    public Double getValue(WeatherStation station) {
        return provider.apply(station);
    }

    private static Double computeValue(Double value, Supplier<Double> alternative) {
        return computeValue(value, Function.identity(), alternative);
    }

    private static Double computeValue(Double value, Function<Double, Double> processor) {
        return computeValue(value, processor, () -> null);
    }

    private static Double computeValue(Double value, Function<Double, Double> processor, Supplier<Double> alternative) {
        if (value == null) {
            return alternative.get();
        }
        return processor.apply(value);
    }

    private final Function<WeatherStation, Double> provider;
}
