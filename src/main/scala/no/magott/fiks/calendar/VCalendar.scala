package no.magott.fiks.calendar

import no.magott.fiks.data.AssignedMatch
import org.joda.time._
import org.joda.time.DateTimeFieldType._

class VCalendar(assignedMatches:List[AssignedMatch]) {

  val thisHour = LocalDateTime.now.withMillisOfSecond(0).withSecondOfMinute(0).withMinuteOfHour(0)

  def this(assignedMatches: AssignedMatch*) = this(assignedMatches.toList)

  private val calendarFormatString = "yyyyMMdd'T'HHmmss'Z"

  def feed = {
    assignedMatches.foldLeft(start)(_+"\n" + vevent(_)) + "\n"+end
  }

  private def vevent(m:AssignedMatch) = {
    val start = toUTC(m.date)
    val desc = """Kampnummer:  %s\nTurnering: %s\n%s \nKampinfo: %s""".format(m.matchId, m.tournament ,m.referees.replaceAllLiterally(" (", " \\n("),m.externalMatchInfoUrl)
    s"""BEGIN:VEVENT
      |DTSTART:%s
      |DTEND:%s
      |DTSTAMP:%s
      |LAST-MODIFIED:%s
      |UID:%s
      |LOCATION:%s
      |SUMMARY:%s
      |DESCRIPTION:%s
      |URL;VALUE=URI:%s
      |FREEBUSY;FBTYPE=BUSY:${start.toString(calendarFormatString)}/${start.plusHours(2).toString(calendarFormatString)}
      |END:VEVENT""".stripMargin.format(
                start.toString(calendarFormatString),
                start.plusHours(2).toString(calendarFormatString),
                toUTC(thisHour).toString(calendarFormatString),
                toUTC(thisHour).toString(calendarFormatString),
                m.matchId+"@fiks.herokuapp.com",
                m.venue,
                m.teams,
                desc,
                m.externalMatchInfoUrl
    )
  }

  val start =
    """BEGIN:VCALENDAR
    |VERSION:2.0
    |PRODID:-//dommerfiks/kampoppsett//NONSGML v1.0//EN
    |METHOD:PUBLISH
    |X-WR-CALNAME:Dommer-FIKS
    |X-WR-CALDESC:Dommer-FIKS
    |X-PUBLISHED-TTL:PT1H""".stripMargin

  val end = "END:VCALENDAR"

  private def toUTC(dateTime: LocalDateTime) = {
    dateTime.toDateTime(DateTimeZone.forID("Europe/Oslo")).withZone(DateTimeZone.UTC).toLocalDateTime
  }

}
