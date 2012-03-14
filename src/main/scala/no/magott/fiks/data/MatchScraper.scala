package no.magott.fiks.data

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import scala.collection.JavaConverters._
import org.joda.time.format.DateTimeFormat
import org.joda.time.{LocalDate}
import org.jsoup.Connection.Method
import java.util.concurrent.TimeUnit
import Guava2ScalaConversions._
import com.google.common.cache.{CacheLoader, CacheBuilder}

class MatchScraper {
  val COOKIE_NAME = "ASP.NET_SessionId"
  val assignedMatchesCache = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).maximumSize(100).build(CacheLoader.from((loginToken: String) => scrapeAssignedMatches(loginToken)))
  val availableMatchesCache = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).maximumSize(100).build(CacheLoader.from((loginToken:String) => scrapeAvailableMatches(loginToken)))

  val dateTimeFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")

  def assignedMatches(loginCookie: (String, String)): List[AssignedMatch] = {
    assignedMatchesCache.get(loginCookie._2)
  }

  def availableMatches(loginCookie: (String, String)) = {
    availableMatchesCache.get(loginCookie._2)
  }

  def scrapeAvailableMatches(loginToken: String) = {
    val availableMatchesResponse = Jsoup.connect("https://fiks.fotball.no/Fogisdomarklient/Start/StartLedigaUppdragLista.aspx")
      .cookie(COOKIE_NAME, loginToken).method(Method.GET).followRedirects(false).timeout(10000).execute()

    if (availableMatchesResponse.statusCode == 302) {
      throw new SessionTimeoutException();
    }

    val availableMatchesDoc = availableMatchesResponse.parse()
    Console.println(availableMatchesDoc)
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
          el.child(8).child(0).attr("href")
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
