package no.magott.fiks.data

import org.jsoup.Jsoup
import org.jsoup.Connection.{Response, Method}
import scala.collection.JavaConverters._
import org.jsoup.nodes.{Element, Document}
import java.util.Date
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat

class FiksLogin {

  val COOKIE_NAME = "ASP.NET_SessionId"
  val dateTimeFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")


  def login(username: String, password: String) = {
    val initResponse: Response = Jsoup.connect("https://fiks.fotball.no/Fogisdomarklient/Login/Login.aspx").method(Method.GET).execute()
    val document: Document = initResponse.parse
    val viewstate: String = document.getElementById("__VIEWSTATE").attr("value")
    val eventvalidation: String = document.getElementById("__EVENTVALIDATION").attr("value")
    val sessionId = initResponse.cookie(COOKIE_NAME)
    val params = Map("tbAnvandarnamn" -> username, "tbLosenord" -> password, "__VIEWSTATE" -> viewstate, "__EVENTVALIDATION" -> eventvalidation, "btnLoggaIn" -> "Logg inn")
    val response = Jsoup.connect("https://fiks.fotball.no/Fogisdomarklient/Login/Login.aspx")
      .data(params.asJava)
      .method(Method.POST)
      .cookie(COOKIE_NAME, sessionId)
      .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_3) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.56 Safari/535.11")
      .followRedirects(false)
      .timeout(10000)
      .execute()

    if (response.statusCode == 302) {
      Right((COOKIE_NAME, sessionId))
    } else {
      Left(new RuntimeException("Login failed"));
    }
  }

  def availableMatches(loginCookie: (String, String)) = {
    val availableMatchesDoc = Jsoup.connect("https://fiks.fotball.no/Fogisdomarklient/Start/StartLedigaUppdragLista.aspx").cookie(loginCookie._1, loginCookie._2).timeout(5000).get()
    val matchElements = availableMatchesDoc.select("tbody > tr").listIterator().asScala

    val matches = matchElements.drop(1).map {
      matchElement: Element =>
        AvailableMatch(matchElement.child(0).text,
          matchElement.child(1).text,
          dateTimeFormat.parseLocalDateTime(matchElement.child(2).text),
          matchElement.child(4).text,
          matchElement.child(5).text,
          matchElement.child(6).text,
          matchElement.child(7).text,
          matchElement.child(8).child(0).attr("href")
        )
    }
    matches.foreach(println)
  }

  case class AvailableMatch(val category: String, val tournament: String, val date: LocalDateTime,
                            val matchId: String, val teams: String, val arena: String, val role: String, val signupUrl: String)

}
