import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers
import java.util.Locale
import no.magott.fiks.calendar.{CalendarService, CalendarPlan}
import no.magott.fiks.data._
import no.magott.fiks.FallbackPlan
import no.magott.fiks.invoice.{InvoiceRepository, InvoicePlan}
import no.magott.fiks.user.{UserPlan, UserService}
import org.joda.time.{Weeks, DateTimeUtils}
import scala.util.Properties
import unfiltered.jetty

object Web {
  def main(args: Array[String]) {
    RegisterJodaTimeConversionHelpers()
    System.setProperty("user.timezone", "Europe/Oslo")
    Locale.setDefault(new Locale("nb","NO"))
    val matchscraper = new MatchScraper
    val matchservice = new MatchService(matchscraper)
    val calendarservice = new CalendarService(matchscraper)
    val userservice = new UserService
    val stadiumservice = new StadiumService
    val invoiceRepository = new InvoiceRepository
    val port = Properties.envOrElse("PORT", "8080").toInt
    println("Starting on port:" + port)
    val http = jetty.Http(port)
    http.resources(getClass().getResource("/static"))
      .plan(new SecurityPlan(matchservice, userservice))
      .plan(new FiksPlan(matchservice, stadiumservice, invoiceRepository, userservice))
      .plan(new CalendarPlan(calendarservice,userservice))
      .plan(new StadiumPlan(stadiumservice))
      .plan(new InvoicePlan(matchservice, userservice, invoiceRepository))
      .plan(new UserPlan(userservice))
      .plan(new FallbackPlan)
    .run()
  }

}