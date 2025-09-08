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

    OptionalDouble getTemperature(); //* celcius

    OptionalDouble getChill(); //* celcius

    OptionalDouble getHumidity(); //* percent

    OptionalDouble getWindDirection(); // degrees clockwise

    OptionalDouble getWindSpeed(); //* m/s

    OptionalDouble getSquall(); //* km/h

    OptionalDouble getVisibility(); //* meters

    OptionalDouble getPressure(); //* hPa

}
