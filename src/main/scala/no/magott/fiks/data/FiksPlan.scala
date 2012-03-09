package no.magott.fiks.data

import unfiltered.filter.Plan
import unfiltered.response.{Html, Ok, Redirect}
import unfiltered.request._
import unfiltered.Cookie

object FiksPlan extends Plan{
  def intent = {
    case GET(Path(Seg("fiks"::"mymatches" :: Nil))) & Cookies(cookies)=>
      cookies("fiksToken") match{
        case Some(Cookie(_, loginToken, _, _, _, _, _, _)) =>
          Ok ~> Html({
            Pages.assignedMatches(MatchScraper.assignedMatches(FiksLogin.COOKIE_NAME, loginToken))
          })
        case _ => Redirect("/login?message=loginRequired")
      }
  }
}
