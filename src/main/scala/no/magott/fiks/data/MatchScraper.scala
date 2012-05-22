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
      .method(Method.GET).cookie(COOKIE_NAME, loginToken).followRedirects(false).timeout(15000).execute()
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
    val availableMatchesResponse = Jsoup.connect("https://fiks.fotball.no/Fogisdomarklient/Start/StartLedigaUppdragLista.aspx")
      .cookie(COOKIE_NAME, loginToken).method(Method.GET).followRedirects(false).timeout(25000).execute()

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
      .cookie(COOKIE_NAME, loginToken).method(Method.GET).timeout(25000).followRedirects(false).execute()

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
          el.child(6).text,
          el.child(3).getElementsByTag("a").attr("href").split("=")(1))
    }
    upcomingAssignedMatches.toList
  }

  def postInterestForm(availabilityId: String, comment:String, loginToken:String) {
  val url = "https://fiks.fotball.no/Fogisdomarklient/Uppdrag/UppdragLedigtUppdrag.aspx?domaruppdragId=" + availabilityId
  val reportInterestForm = Jsoup.connect(url).cookie(COOKIE_NAME,loginToken).get
  val viewstate = reportInterestForm.getElementById("__VIEWSTATE").attr("value")
  val eventvalidation = reportInterestForm.getElementById("__EVENTVALIDATION").attr("value")
  val response = Jsoup.connect(url)
    .method(Method.POST)
    .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_3) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.56 Safari/535.11")
    .data("btnAnmal","Meld inn")
    .data("tbKommentar",comment)
    .data("__VIEWSTATE",viewstate)
    .data("__EVENTVALIDATION",eventvalidation)
    .referrer(url)
    .cookie(COOKIE_NAME,loginToken).followRedirects(false).timeout(10000).execute()
  }

}
