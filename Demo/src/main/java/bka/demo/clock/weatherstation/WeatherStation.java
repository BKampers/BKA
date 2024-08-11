/*
** © Bart Kampers
*/
package bka.demo.clock.weatherstation;

import java.time.*;
import java.util.*;

public interface WeatherStation {

    String getStationName();

    LocalDateTime getTimestamp();

    Optional<String> getWeatherSummary();

    Optional<Double> getTemperature(); //* celcius

    Optional<Double> getChill(); //* celcius

    Optional<Double> getHumidity(); //* percent

    Optional<Double> getWindDirection(); // degrees clockwise

    Optional<Double> getWindSpeed(); //* m/s

    Optional<Double> getSquall(); //* km/h

    Optional<Double> getVisibility(); //* meters

    Optional<Double> getPressure(); //* hPa

}
