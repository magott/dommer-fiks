package no.magott.fiks.data

import unfiltered.filter.Plan
import unfiltered.request._
import unfiltered.response._

object FiksPlan extends Plan {
  def intent = {
    case r@GET(Path(Seg("fiks" :: "mymatches" :: Nil))) & FiksCookie(loginToken) => redirectToLoginIfTimeout(r, {
      Ok ~> Html(Pages.assignedMatches(MatchScraper.assignedMatches(FiksLogin.COOKIE_NAME, loginToken)))
    })
    case r@GET(Path(Seg("fiks" :: "mymatches" :: Nil)))  => HerokuRedirect(r,"/login?message=loginRequired")
    case GET(Path(Seg("ical" :: Nil))) & Params(p) => Ok ~> CalendarContentType ~> ResponseString(Snippets.isc(p))
    case r@GET(Path(Seg(Nil))) & FiksCookie(_) => HerokuRedirect(r,"/fiks/mymatches")
    case r@GET(Path(Seg(Nil))) => HerokuRedirect(r,"/login")
  }

  def redirectToLoginIfTimeout[A](req: HttpRequest[A], f: => ResponseFunction[Any]) = {
    try {
      f
    } catch {
      case e: SessionTimeoutException => HerokuRedirect(req,"/login?message=sessionTimeout")
    }
  }

  object CalendarContentType extends CharContentType("text/calendar")

}
