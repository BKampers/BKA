/*
** © Bart Kampers
*/

package bka.demo.clock;


public interface WeatherStation {

    String getStationName();

    String getWeatherSummary();

    Double getTemperature(); //* Celcius

    Double getChill(); //* Celcius

    Double getHumidity(); //* percent

    String getWindDirection();

    Double getWindSpeed(); //* m/s

    Double getSquall(); //* km/h

    Double getVisibility(); //* meters

    Double getPressure(); //* hPa

}
