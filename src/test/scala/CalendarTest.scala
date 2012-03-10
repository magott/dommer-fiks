import no.magott.fiks.data.Snippets
import org.joda.time.LocalDateTime

object CalendarTest extends App {

  val calendarLink = Snippets.googleCalendarLink(LocalDateTime.now, "heading","location","details")
  Console println calendarLink

}
