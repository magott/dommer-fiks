package no.magott.fiks.data

import org.joda.time.{Interval, LocalDate, LocalDateTime}

case class InstantForecast(dateTime:LocalDateTime, temperature: Option[Double], windSpeed:String)

case class IntervalForecast(start:LocalDateTime, end:LocalDateTime, precipitation:Double, symbolId:Option[Int]){
  lazy val interval = new Interval(start.toDateTime.getMillis, end.toDateTime.getMillis)
}

case class WeatherData(instants:Seq[InstantForecast], intervals:Seq[IntervalForecast])

case class MatchForecast(period:Interval, temperature:Option[Double], icon:Option[Int], precipitation:Double){
  import fix.UriString._
  def iconUrl = icon.fold("/img/pixel.png")(x=>uri"/img/yr/${x.toString}.png")
  def periodString = period.getStart.toString("HH.mm") + " - " + period.getEnd.toString("HH.mm")
  def asHtml = {
    <div class="forecast">
      <div class="interval">{periodString}</div>
      <div class="icon"><img src={iconUrl}></img></div>
      <div class="temperature">{temperature.map("%s°".format(_)).getOrElse({""})}</div>
      <div class="precipitation">{"%s mm".format(precipitation)}</div>
    </div>
  }
}