package no.magott.fiks.data

import org.jsoup.Jsoup
import org.jsoup.Connection.Method
import scala.collection.JavaConverters._
import unfiltered.request.HttpRequest
import unfiltered.Cookie
import unfiltered.response.{Html5, SetCookies}

object FiksLoginService {

  val COOKIE_NAME = "ASP.NET_SessionId"

  def doLogin(username: String, password: String) = {
    val loginPage = Jsoup.connect("https://fiks.fotball.no/Fogisdomarklient/Login/Login.aspx").method(Method.GET).timeout(10000).execute()
    val loginDocument = loginPage.parse
    val sessionId = loginPage.cookie(COOKIE_NAME)
    val viewstate = loginDocument.getElementById("__VIEWSTATE").attr("value")
    val eventvalidation = loginDocument.getElementById("__EVENTVALIDATION").attr("value")
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
      Html5(<h1>Feil</h1>)
    }
  }

  def login(username: String, password: String) = {
    val loginPage = Jsoup.connect("https://fiks.fotball.no/Fogisdomarklient/Login/Login.aspx").method(Method.GET).timeout(10000).execute()
    val loginDocument = loginPage.parse
    val sessionId = loginPage.cookie(COOKIE_NAME)
    val viewstate = loginDocument.getElementById("__VIEWSTATE").attr("value")
    val eventvalidation = loginDocument.getElementById("__EVENTVALIDATION").attr("value")
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
