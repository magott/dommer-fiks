import no.magott.fiks.calendar.{CalendarService, CalendarPlan}
import no.magott.fiks.data._
import no.magott.fiks.FallbackPlan
import no.magott.fiks.user.UserService
import util.Properties
import unfiltered.jetty

object Web {
  def main(args: Array[String]) {
    System.setProperty("user.timezone", "Europe/Oslo")
    val matchscraper = new MatchScraper
    val matchservice = new MatchService(matchscraper)
    val calendarservice = new CalendarService(matchscraper)
    val userservice = new UserService
    val stadiumservice = new StadiumService
    val port = Properties.envOrElse("PORT", "8080").toInt
    println("Starting on port:" + port)
    val http = jetty.Http(port)
    http.resources(getClass().getResource("/static"))
      .plan(new SecurityPlan(matchservice))
      .plan(new FiksPlan(matchservice, stadiumservice))
      .plan(new CalendarPlan(calendarservice,userservice))
      .plan(new StadiumPlan(stadiumservice))
      .plan(new FallbackPlan)
    .run
  }

}