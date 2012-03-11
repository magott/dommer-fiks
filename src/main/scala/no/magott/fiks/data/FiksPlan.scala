package no.magott.fiks.data

import unfiltered.filter.Plan
import unfiltered.request._
import unfiltered.Cookie
import unfiltered.response._
import org.joda.time.LocalDateTime

object FiksPlan extends Plan{
  def intent = {
    case GET(Path(Seg("fiks"::"mymatches" :: Nil))) & Cookies(cookies)=>
      cookies("fiksToken") match{
        case Some(Cookie(_, loginToken, _, _, _, _, _, _)) =>
          redirectToLoginIfTimeout{
            Ok ~> Html({
            Pages.assignedMatches(MatchScraper.assignedMatches(FiksLogin.COOKIE_NAME, loginToken))
          })
          }
        case _ => Redirect("/login?message=loginRequired")
      }
    case GET(Path(Seg("ical"::Nil))) & Params(p) => Ok ~> CalendarContentType ~> ResponseString(Snippets.isc(p))
    case GET(Path(Seg("/"::Nil))) => Redirect("/fiks/mymatches")
  }

  def redirectToLoginIfTimeout(f: => ResponseFunction[Any]) = {
    try{
      f
    }catch {
      case e:SessionTimeoutException => Redirect("/login?message=sessionTimeout")
    }
  }

  object CalendarContentType extends CharContentType("text/calendar")

}
