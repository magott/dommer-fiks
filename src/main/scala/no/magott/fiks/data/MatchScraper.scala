package no.magott.fiks.data

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import scala.collection.JavaConverters._
import org.joda.time.format.DateTimeFormat
import org.joda.time.LocalDate
import org.jsoup.Connection.Method
import org.jsoup.safety.Whitelist

class MatchScraper {
  val COOKIE_NAME = "ASP.NET_SessionId"
  val dateTimeFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")

  def scrapeMatchInfo(assignmentId: String, loginToken:String) = {
    val cleanAssignmentId = Jsoup.clean(assignmentId, Whitelist.none)
    val response = Jsoup.connect("https://fiks.fotball.no/Fogisdomarklient/Uppdrag/UppdragLedigtUppdrag.aspx?domaruppdragId=" + cleanAssignmentId)
      .method(Method.GET).cookie(COOKIE_NAME, loginToken).followRedirects(false).timeout(10000).execute()
    if(response.statusCode == 302){
      throw new SessionTimeoutException()
    }
    val el = response.parse().select("table")
    AvailableMatch(
      el.select("span#lblTavlingskategori").text,
      el.select("span#lblTavling").text,
      dateTimeFormat.parseLocalDateTime(el.select("span#lblTid").text),
      el.select("span#lblMatchnr").text,
      el.select("span#lblMatch").text,
      el.select("span#lblAnlaggning").text,
      el.select("span#lblUppdrag").text,
      Some(cleanAssignmentId)
    )
  }

  def scrapeAvailableMatches(loginToken: String) = {
    println("Scraping available matches for token \t"+loginToken)
    val availableMatchesResponse = Jsoup.connect("https://fiks.fotball.no/Fogisdomarklient/Start/StartLedigaUppdragLista.aspx")
      .cookie(COOKIE_NAME, loginToken).method(Method.GET).followRedirects(false).timeout(10000).execute()

    if (availableMatchesResponse.statusCode == 302) {
      throw new SessionTimeoutException();
    }

    val availableMatchesDoc = availableMatchesResponse.parse()
    val matchElements = availableMatchesDoc.select("div#divMainContent").select("table.fogisInfoTable > tbody > tr").listIterator.asScala.drop(1)

    matchElements.map {
      el: Element =>
        AvailableMatch(el.child(0).text,
          el.child(1).text,
          dateTimeFormat.parseLocalDateTime(el.child(2).text),
          el.child(4).text,
          el.child(5).text,
          el.child(6).text,
          el.child(7).text,
          if(el.child(8).child(0).nodeName =="a"){
            Some(el.child(8).child(0).attr("href").split("=")(1))
          }else{
            None
          }
        )
    }.toList
  }

  def scrapeAssignedMatches(loginToken: String) = {
    val assignedMatchesResponse = Jsoup.connect("https://fiks.fotball.no/Fogisdomarklient/Uppdrag/UppdragUppdragLista.aspx")
      .cookie(COOKIE_NAME, loginToken).method(Method.GET).timeout(10000).followRedirects(false).execute()

    if (assignedMatchesResponse.statusCode == 302) {
      throw new SessionTimeoutException()
    }
    val assignedMatchesDoc = assignedMatchesResponse.parse;
    val matchesElements = assignedMatchesDoc.select("div#divUppdrag").select("table.fogisInfoTable > tbody > tr").listIterator.asScala.drop(1)
    val upcomingAssignedMatches = matchesElements.map {
      el: Element =>
        AssignedMatch(dateTimeFormat.parseLocalDateTime(el.child(0).text),
          el.child(1).text,
          el.child(3).getElementsByTag("a").text,
          el.child(4).text,
          el.child(5).text,
          el.child(6).text)
    }.filter(_.date.toLocalDate.isAfter(LocalDate.now.minusDays(1)))
    upcomingAssignedMatches.toList
  }

}
