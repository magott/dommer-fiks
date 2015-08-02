package no.magott.fiks

import no.magott.fiks.data.{ReAuthRequired, FiksLoginService, ScrapeError}
import no.magott.fiks.user.UserSession
import org.joda.time.format.DateTimeFormat
import org.jsoup.nodes.Document

import scalaz.\/
import scalaz.syntax.id._


/**
 *
 */
trait FiksScraper {

  val COOKIE_NAME = "ASP.NET_SessionId"
  val fiksDateFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")
  val VIEWSTATE = "__VIEWSTATE"
  val VIEWSTATEGENERATOR = "__VIEWSTATEGENERATOR"


  protected def withAutomaticReAuth(session: UserSession, f: UserSession => Document): ScrapeError \/ Document = {
    val first = f(session)
    if (isJsRedirectToLogin(first)) {
      FiksLoginService.reAuthenticate(session)
      val second = f(session)
      if (isJsRedirectToLogin(second)) {
        ReAuthRequired("Login required").left
      } else {
        second.right
      }
    } else {
      first.right
    }
  }

  private def isJsRedirectToLogin(doc:Document) = {
    Option(doc.body).flatMap(el => Option(el.children)).forall(_.isEmpty)
  }

}
