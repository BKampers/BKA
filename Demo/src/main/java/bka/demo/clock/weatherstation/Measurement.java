/*
** © Bart Kampers
*/
package bka.demo.clock.weatherstation;

import java.util.*;
import java.util.function.*;

public enum Measurement {
    TEMPERATURE(station -> station.getTemperature()),
    CHILL(station -> getValue(station.getChill(), () -> station.getTemperature())),
    HUMIDITY(station -> station.getHumidity()),
    WIND_DIRECTION(station -> station.getWindDirection()),
    WIND_SPEED(station -> station.getWindSpeed()),
    SQUALL(station -> computeValue(station.getSquall(), Measurement::toMetersPerSecond, station::getWindSpeed)),
    PRESSURE(station -> station.getPressure()),
    VISIBILITY(station -> computeValue(station.getVisibility(), Measurement::toKilometers));

    private static double toMetersPerSecond(double kilomertersPerHour) {
        return kilomertersPerHour / 3.6;
    }

    private static double toKilometers(double meters) {
        return meters / 1000;
    }

    private Measurement(Function<WeatherStation, OptionalDouble> provider) {
        this.provider = provider;
    }

    public OptionalDouble getValue(WeatherStation station) {
        return provider.apply(station);
    }

    private static OptionalDouble getValue(OptionalDouble value, Supplier<OptionalDouble> alternative) {
        if (value.isEmpty()) {
            return alternative.get();
        }
        return value;
    }
    
    private static OptionalDouble computeValue(OptionalDouble value, DoubleUnaryOperator processor) {
        return computeValue(value, processor, OptionalDouble::empty);
    }


    private static OptionalDouble computeValue(OptionalDouble value, DoubleUnaryOperator processor, Supplier<OptionalDouble> alternative) {
        if (value.isEmpty()) {
            return alternative.get();
        }
        return OptionalDouble.of(processor.applyAsDouble(value.getAsDouble()));
    }

    private final Function<WeatherStation, OptionalDouble> provider;
}
