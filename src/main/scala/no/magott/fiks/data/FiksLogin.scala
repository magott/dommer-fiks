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


  def login(username: String, password: String) = {
    val initResponse = Jsoup.connect("https://fiks.fotball.no/Fogisdomarklient/Login/Login.aspx").method(Method.GET).execute()
    val document = initResponse.parse
    val viewstate = document.getElementById("__VIEWSTATE").attr("value")
    val eventvalidation = document.getElementById("__EVENTVALIDATION").attr("value")
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



}
