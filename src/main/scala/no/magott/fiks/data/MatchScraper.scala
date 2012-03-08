package no.magott.fiks.data

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import scala.collection.JavaConverters._
import org.joda.time.format.DateTimeFormat
import org.joda.time.{LocalDate, LocalDateTime}

object MatchScraper {

  val dateTimeFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")

  def assignedMatches(loginCookie: (String, String)) = {
    //TODO: Handle SocketTimeoutException? is 3000 millis not enough? Increase?
    val assignedMatchesDoc = Jsoup.connect("https://fiks.fotball.no/Fogisdomarklient/Uppdrag/UppdragUppdragLista.aspx").cookie(loginCookie._1, loginCookie._2).get
    val matchesElements = assignedMatchesDoc.select("div#divUppdrag").select("table.fogisInfoTable > tbody > tr").listIterator.asScala.drop(1)
//    matchesElements.foreach(println)
    val assignedMatches = matchesElements.map{
      el:Element =>
        AssignedMatch(dateTimeFormat.parseLocalDateTime(el.child(0).text),
        el.child(1).text,
        el.child(3).getElementsByTag("a").text,
        "",
        "",
        "")
    }
    assignedMatches.filter(_.date.isAfter(LocalDate.now.minusDays(1))).foreach(println)

    assignedMatches;
  }

  def availableMatches(loginCookie: (String, String)) = {
    val availableMatchesDoc = Jsoup.connect("https://fiks.fotball.no/Fogisdomarklient/Start/StartLedigaUppdragLista.aspx").cookie(loginCookie._1, loginCookie._2).get
    val matchElements = availableMatchesDoc.select("tbody > tr").listIterator.asScala.drop(1)

    val matches = matchElements.map {
      el: Element =>
        AvailableMatch(el.child(0).text,  
          el.child(1).text,
          dateTimeFormat.parseLocalDateTime(el.child(2).text),
          el.child(4).text,
          el.child(5).text,
          el.child(6).text,
          el.child(7).text,
          el.child(8).child(0).attr("href")
        )
    }
    matches.foreach(println)
    matches
  }
  case class AvailableMatch(val category: String, val tournament: String, val date: LocalDateTime,
                            val matchId: String, val teams: String, val venue: String, val role: String, val signupUrl: String)

  case class AssignedMatch(val date:LocalDateTime, val tournament: String, val matchId:String, val teams:String, val venue:String, val referees:String)


}
