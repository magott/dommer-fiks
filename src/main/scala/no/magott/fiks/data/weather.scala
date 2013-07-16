package no.magott.fiks.data

import org.joda.time.{Interval, LocalDate, LocalDateTime}

case class InstantForecast(dateTime:LocalDateTime, temperature: Double, windSpeed:String)

case class IntervalForecast(start:LocalDateTime, end:LocalDateTime, precipitation:Double, symbolId:Int){
  lazy val interval = new Interval(start.toDateTime.getMillis, end.toDateTime.getMillis)
}

case class WeatherData(instants:Seq[InstantForecast], intervals:Seq[IntervalForecast])

case class MatchForecast(period:Interval, temperature:Double, icon:Int, precipitation:Double){
  import fix.UriString._
  def iconUrl = uri"http://api.met.no/weatherapi/weathericon/1.0/?symbol=${icon.toString};content_type=image/png"
  def periodString = period.getStart.toString("HH.mm") + " - " + period.getEnd.toString("HH.mm")
  def asHtml = {
    <div class="forecast">
      <div class="interval">{periodString}</div>
      <div class="icon"><img src={iconUrl}></img></div>
      <div class="temperature">{"%s°".format(temperature)}</div>
      <div class="precipitation">{"%s mm".format(precipitation)}</div>
    </div>
  }
}