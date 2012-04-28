package no.magott.fiks.calendar

import no.magott.fiks.data.AssignedMatch
import org.joda.time.{DateTimeZone, LocalDateTime}

class VCalendar(assignedMatches:List[AssignedMatch]) {

  def this(assignedMatches: AssignedMatch*) = this(assignedMatches.toList)

  private val calendarFormatString = "yyyyMMdd'T'HHmmss'Z"

  def feed = {
    assignedMatches.foldLeft(start)(_+"\n" + vevent(_)) + "\n"+end
  }

  private def vevent(m:AssignedMatch) = {
    val start = toUTC(m.date)
    """BEGIN:VEVENT
    |DTSTART:%s
    |DTEND:%s
    |DTSTAMP:%s
    |UID:%s
    |LOCATION:%s
    |SUMMARY:%s
    |DESCRIPTION:%s
    |END:VEVENT""".stripMargin.format(
                start.toString(calendarFormatString),
                start.plusHours(2).toString(calendarFormatString),
                start.toString(calendarFormatString),
                m.matchId+"@fiks.herokuapp.com",
                m.venue,
                m.teams,
                m.referees
    )
  }

  private def start =
    """BEGIN:VCALENDAR
    |VERSION:2.0
    |PRODID:-//dommerfiks/kampoppsett//NONSGML v1.0//EN
    |METHOD:PUBLISH
    |X-WR-CALNAME:Dommer-FIKS
    |X-WR-CALDESC:Dommer-FIKS
    |X-PUBLISHED-TTL:PT1H""".stripMargin

  private def end = "END:VCALENDAR"

  private def toUTC(dateTime: LocalDateTime) = {
    dateTime.toDateTime(DateTimeZone.forID("Europe/Oslo")).withZone(DateTimeZone.UTC).toLocalDateTime
  }

}
