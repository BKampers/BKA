package bka.demo.clock.weatherstation;

import java.time.*;

/*
** © Bart Kampers
*/



public interface WeatherStation {

    String getStationName();

    LocalDateTime getTimestamp();

    String getWeatherSummary();

    Double getTemperature(); //* celcius

    Double getChill(); //* celcius

    Double getHumidity(); //* percent

    Double getWindDirection(); // degrees clockwise

    Double getWindSpeed(); //* m/s

    Double getSquall(); //* km/h

    Double getVisibility(); //* meters

    Double getPressure(); //* hPa

}
