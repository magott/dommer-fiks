package no.magott.fiks.data

import org.jsoup.Jsoup
import org.jsoup.Connection.Method
import scala.collection.JavaConverters._
import unfiltered.response.Html5
import no.magott.fiks.user.{UserSession, UserService}

object FiksLoginService {


  val APP_COOKIE_NAME = ".AspNet.ApplicationCookie"
  val SESS_COOKIE_NAME = "ASP.NET_SessionId"
  val LOGIN_FORM_URL = "http://www.fiks.fotball.no/FiksWeb/Login"
  val LOGIN_URL = "http://www.fiks.fotball.no/FiksWeb/Login?ReturnUrl=~%2FHome%2FConsolidateUsers"
  val REF_CLIENT_URL = "http://www.fiks.fotball.no/FiksWeb/Home/RedirectToFiksReferee?clubId=0"
  val VALIDATION_COOKIE_NAME = "__RequestVerificationToken_L0Zpa3NXZWI1"
  val REQ_VAL_FORMFIELD_NAME = "__RequestVerificationToken"

  def login(username: String, password: String, rememberMe: Boolean) = {
    val loginPage = Jsoup.connect(LOGIN_FORM_URL).method(Method.GET).timeout(10000).execute()
    val loginDocument = loginPage.parse
    loginPage.cookies().asScala.foreach(println)
    val requestValidationCookie = loginPage.cookie(VALIDATION_COOKIE_NAME)
    val requestValidationFormField = Option(loginDocument.getElementsByAttributeValue("name", REQ_VAL_FORMFIELD_NAME)).flatMap(el=> Option(el.attr("value")))
    val params = Map("UserName" -> Some(username), "Password" -> Some(password), REQ_VAL_FORMFIELD_NAME -> requestValidationFormField, "RememberMe" -> Some(rememberMe.toString.capitalize))
      .collect{
      case (k, Some(v)) => k -> v
    }
    val response = Jsoup.connect(LOGIN_URL)
      .data(params.asJava)
      .method(Method.POST)
      .cookie(VALIDATION_COOKIE_NAME, requestValidationCookie)
      .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_3) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.56 Safari/535.11")
      .followRedirects(false)
      .timeout(15000)
      .execute()

    if (response.statusCode == 302) {
      val applicationCookie = response.cookie(APP_COOKIE_NAME)
      val sessionCookie = Jsoup.connect(REF_CLIENT_URL).method(Method.GET).timeout(10000).cookie(APP_COOKIE_NAME, applicationCookie).execute().cookie(SESS_COOKIE_NAME)
      Right(SESS_COOKIE_NAME -> sessionCookie)
    } else {
      Left(new RuntimeException("Login failed"))
    }
  }

}
