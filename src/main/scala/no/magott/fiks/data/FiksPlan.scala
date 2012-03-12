package no.magott.fiks.data

import unfiltered.filter.Plan
import unfiltered.request._
import unfiltered.response._

object FiksPlan extends Plan {
  def intent = {
    case GET(Path(Seg("fiks" :: "mymatches" :: Nil))) & FiksCookie(loginToken) => redirectToLoginIfTimeout {
      Ok ~> Html(Pages.assignedMatches(MatchScraper.assignedMatches(FiksLogin.COOKIE_NAME, loginToken)))
    }
    case GET(Path(Seg("ical" :: Nil))) & Params(p) => Ok ~> CalendarContentType ~> ResponseString(Snippets.isc(p))
    case GET(Path(Seg(Nil))) => Redirect("/fiks/mymatches")
  }

  def redirectToLoginIfTimeout(f: => ResponseFunction[Any]) = {
    try {
      f
    } catch {
      case e: SessionTimeoutException => Redirect("/login?message=sessionTimeout")
    }
  }

  object CalendarContentType extends CharContentType("text/calendar")

}
