package no.magott.fiks.data

import org.jsoup.Jsoup
import org.jsoup.Connection.Method
import scala.collection.JavaConverters._
import unfiltered.response.Html5
import no.magott.fiks.user.{UserSession, UserService}
import java.util.UUID
import org.joda.time.DateTime

object FiksLoginService {


  val APP_COOKIE_NAME = ".AspNet.ApplicationCookie"
  val SESS_COOKIE_NAME = "ASP.NET_SessionId"
  val LOGIN_FORM_URL = "https://fiks.fotball.no/FiksWeb/Login"
  val LOGIN_URL = "https://fiks.fotball.no/FiksWeb/Login?ReturnUrl=~%2FHome%2FConsolidateUsers"
  val REF_CLIENT_URL = "https://fiks.fotball.no/FiksWeb/Home/RedirectToFiksReferee?clubId=0"
  val VALIDATION_COOKIE_NAME = "__RequestVerificationToken_L0Zpa3NXZWI1"
  val REQ_VAL_FORMFIELD_NAME = "__RequestVerificationToken"

  def login(username: String, password: String, rememberMe: Boolean) : Either[Exception, UserSession] = {
    println(s"$username getting loginpage")
    val loginPage = Jsoup.connect(LOGIN_FORM_URL).method(Method.GET).timeout(10000).execute()
    val loginDocument = loginPage.parse
    val requestValidationCookie = loginPage.cookie(VALIDATION_COOKIE_NAME)
    val requestValidationFormField = Option(loginDocument.getElementsByAttributeValue("name", REQ_VAL_FORMFIELD_NAME)).flatMap(el=> Option(el.attr("value")))
    val params = Map("UserName" -> Some(username), "Password" -> Some(password), REQ_VAL_FORMFIELD_NAME -> requestValidationFormField, "RememberMe" -> Some(rememberMe.toString))
      .collect{
      case (k, Some(v)) => k -> v
    }
    println(s"$username posting to loginpage")
    try {
      val loginRequest = Jsoup.connect(LOGIN_URL)
        .data(params.asJava)
        .header("Referer", "https://fiks.fotball.no/FiksWeb/Login")
        .method(Method.POST)
        .cookie(VALIDATION_COOKIE_NAME, requestValidationCookie)
        .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_3) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.56 Safari/535.11")
        .followRedirects(false)
        .timeout(15000)

        println(s"$username executing loginrequest")
        val response = loginRequest.execute()
        if (response.statusCode == 302) {
          val applicationCookie = response.cookie(APP_COOKIE_NAME)
          val session: UserSession = authenticate(username, applicationCookie, None)
          Right(session)
        } else {
          Left(new RuntimeException("Login failed"))
        }
      } catch {
        case e:Exception => {
          e.printStackTrace()
          Left(e)
        }
    }
  }

  def reAuthenticate(session:UserSession) = authenticate(session.id, session.longLivedToken, Some(session.sessionToken))

  def authenticate(username: String, applicationCookie: String, sessionCookie:Option[String]): UserSession = {
    val connection = Jsoup.connect(REF_CLIENT_URL).method(Method.GET).timeout(10000)
      .cookie(APP_COOKIE_NAME, applicationCookie)
    sessionCookie.foreach(value => connection.cookie(SESS_COOKIE_NAME, value))
    val cookieFromServer =  connection.execute().cookie(SESS_COOKIE_NAME)
    val session = UserSession(UUID.randomUUID().toString, cookieFromServer, applicationCookie, username,
      DateTime.now.plusWeeks(14))
    session
  }
}
