package no.magott.fiks.data

import org.scalatest.FunSuite
import scala.xml.{NodeSeq, XML}
import org.joda.time.LocalDateTime

class WeatherServiceTest extends FunSuite{

  test("Can parse weather data"){
    val service = new WeatherService
    val data = service.parseWeatherData(weatherDataStub)
    assert(data.instants.head.temperature.exists(_ == 17.0))
    assert(data.intervals.exists(_.symbolId.isEmpty))
    assert(data.intervals.exists(_.symbolId.isDefined))
  }

  test("Gives two forecasts when interval overlaps two interval forcasts"){
    val service = new WeatherService
    val from = new LocalDateTime(2013, 6, 26, 17, 0)
    val to = new LocalDateTime(2013, 6, 26, 19, 0)
    val data = service.parseWeatherData(weatherDataStub)
    val forecasts = service.forecastFor(from, to, data)
    assert(forecasts.size == 2)
  }

  val weatherDataStub = XML.loadFile("weather-data.xml")
}
