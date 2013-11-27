package no.magott.fiks.data

import org.jsoup.Jsoup
import org.jsoup.Connection.Method
import scala.collection.JavaConverters._
import unfiltered.response.Html5

object FiksLoginService {

  val COOKIE_NAME = "ASP.NET_SessionId"


  def login(username: String, password: String) = {
    val loginPage = Jsoup.connect("https://fiks.fotball.no/Fogisdomarklient/Login/Login.aspx").method(Method.GET).timeout(10000).execute()
    val loginDocument = loginPage.parse
    val sessionId = loginPage.cookie(COOKIE_NAME)
    val viewstate = Option(loginDocument.getElementById("__VIEWSTATE")).flatMap(el => Option(el.attr("value")))
    val eventvalidation = Option(loginDocument.getElementById("__EVENTVALIDATION")).flatMap(el=> Option(el.attr("value")))
    val params = Map("tbAnvandarnamn" -> Some(username), "tbLosenord" -> Some(password), "__VIEWSTATE" -> viewstate, "__EVENTVALIDATION" -> eventvalidation, "btnLoggaIn" -> Some("Logg inn"))
      .collect{
      case (k, Some(v)) => k -> v
    }
    val response = Jsoup.connect("https://fiks.fotball.no/Fogisdomarklient/Login/Login.aspx")
      .data(params.asJava)
      .method(Method.POST)
      .cookie(COOKIE_NAME, sessionId)
      .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_3) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.56 Safari/535.11")
      .followRedirects(false)
      .timeout(15000)
      .execute()

    if (response.statusCode == 302) {
      Right((COOKIE_NAME, sessionId))
    } else {
      Left(new RuntimeException("Login failed"));
    }
  }



}
