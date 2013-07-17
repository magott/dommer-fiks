package no.magott.fiks.data

import scala.xml._
import org.joda.time.{DateTime, Interval, LocalDateTime}
import dispatch._, Defaults._
import geo.LatLong
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import org.specs2.internal.scalaz.Digit._0

class WeatherService {

  def findForecast(start:LocalDateTime, end:LocalDateTime, latLong:LatLong) : Seq[MatchForecast] = {
    val met = url("http://api.met.no/weatherapi/locationforecast/1.8/") <<? Map("lat" -> latLong.lat.toString, "lon" -> latLong.long.toString)
    val forecast = Http(met > as.xml.Elem)
    val weatherData = parseWeatherData(forecast())
    forecastFor(start, end, weatherData)
  }

  def forecastFor(start:LocalDateTime, end:LocalDateTime, data:WeatherData) : Seq[MatchForecast] = {
    def forecastDateMatchesDateOfMatch(forecast:IntervalForecast) = {
      forecast.start.toLocalDate == start.toLocalDate
    }
    val interval = new Interval(start.toDateTime.getMillis, end.toDateTime.getMillis)
    val overlaps = data.intervals.filter(_.interval.overlaps (interval))
    implicit val ordering = new OverlapComperator(interval)
    val sorted = overlaps.sorted
    val top3 = sorted.take(2).takeWhile(forecastDateMatchesDateOfMatch)
    top3.map(toMatchForecast(data.instants))
  }

  def parseWeatherData(data: Elem) = {
    val timeElems = data \ "product" \ "time"
    val (instant, interval) = timeElems.partition(isInstant)
    WeatherData(instant.map(parseInstant), interval.map(parseInterval))
  }

  def parseInstant(t:Node) = {
    val dateTime = toDate(t)
    val temp = (t \ "location" \ "temperature" \ "@value").toString.toDouble
    val wind = (t \ "location" \ "windSpeed" \ "@value").toString
    InstantForecast(dateTime, temp, wind)
  }

  def parseInterval(t:Node) = {
    val from = fromDate(t)
    val to = toDate(t)
    val rain = (t \ "location" \ "precipitation" \ "@value").text.toDouble
    val symbol = (t \ "location" \ "symbol" \ "@number").headOption.map(_.text.toInt)
    IntervalForecast(from, to, rain, symbol)
  }

  def isInstant(node:Node) = {
    fromDate(node) == toDate(node)
  }


  def toDate(node: Node): LocalDateTime = {
    DateTime.parse((node \ "@to").toString).toLocalDateTime
  }

  def fromDate(node: Node): LocalDateTime = {
    DateTime.parse((node \ "@from").toString).toLocalDateTime
  }

  def toMatchForecast(instants:Seq[InstantForecast])(intervalForecast:IntervalForecast):MatchForecast = {
    val instant = instants.find(_.dateTime == intervalForecast.end).get
    MatchForecast(intervalForecast.interval, instant.temperature, intervalForecast.symbolId, intervalForecast.precipitation)
  }

  class OverlapComperator(target:Interval) extends Ordering[IntervalForecast]{
    val targetDuration = target.toDurationMillis
    def compare(x:IntervalForecast, y:IntervalForecast):Int = {
      val xi = x.interval
      val yi = y.interval
      val xOverlap = Option(xi.overlap(target)).getOrElse(EMPTY_INTERVAL) //Not nullsafe (No overlap == null)
      val yOverlap = Option(yi.overlap(target)).getOrElse(EMPTY_INTERVAL) //Not nullsafe (No overlap == null)
      val compared = xOverlap.toDurationMillis.compareTo(yOverlap.toDurationMillis)
      if(compared != 0 ) (compared * -1) else {
        val yMiss = yi.toDurationMillis - yOverlap.toDurationMillis
        val xMiss = xi.toDurationMillis - xOverlap.toDurationMillis
        xMiss.compareTo(yMiss) // The greater the miss, the poorer the match
      }
    }
    lazy val EMPTY_INTERVAL = new Interval(0,0)
  }
}
