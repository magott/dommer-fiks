package no.magott.fiks.data

import unfiltered.request._
import unfiltered.response._
import unfiltered.filter.{Intent, Plan}

class FiksPlan(matchservice: MatchService) extends Plan {

  def intent = {
    myMatches orElse availableMatches orElse about orElse reportInterest
  }

  val myMatches = Intent {
    case r@GET(Path(Seg("fiks" :: "mymatches" :: Nil))) & FiksCookie(loginToken) => redirectToLoginIfTimeout(r, {
      Ok ~> Html5(Pages(r).assignedMatches(matchservice.assignedMatches(FiksLoginService.COOKIE_NAME, loginToken)))
    })
    case r@GET(Path(Seg("match.ics" :: Nil))) & Params(p) => Ok ~> CalendarContentType ~> ResponseString(Snippets(r).isc(p))
    case r@GET(Path(Seg("fiks" :: "mymatches" :: Nil))) => HerokuRedirect(r, "/login?message=loginRequired")
    case r@GET(Path(Seg(Nil))) & FiksCookie(_) => HerokuRedirect(r, "/fiks/mymatches")
    case r@GET(Path(Seg(Nil))) => HerokuRedirect(r, "/login")
  }

  val availableMatches = Intent {
    case r@GET(Path(Seg("fiks" :: "availablematches" :: Nil))) & Params(MatchIdParameter(matchId)) & FiksCookie(loginToken) => redirectToLoginIfTimeout(r, {
      val matchInfo = matchservice.matchInfo(matchId, loginToken)
      Ok ~> Html5(Pages(r).reportInterestIn(matchInfo))
    })
    case r@GET(Path(Seg("fiks" :: "availablematches" :: Nil))) & FiksCookie(loginToken) => redirectToLoginIfTimeout(r, {
      Ok ~> Html5(Pages(r).availableMatches(matchservice.availableMatches(FiksLoginService.COOKIE_NAME, loginToken)))
    })
  }

  val reportInterest = Intent {
    case r@POST(Path(Seg("fiks" :: "availablematches" :: Nil))) & Params(MatchIdParameter(matchId)) & Params(CommentParameter(comment)) & FiksCookie(loginToken) => redirectToLoginIfTimeout(r, {
      matchservice.reportInterest(matchId, comment, loginToken)
      HerokuRedirect(r, "/fiks/availablematches")
    })
  }

  val about = Intent {
    case r@GET(Path(Seg("fiks" :: "about" :: Nil))) => Ok ~> Html(Pages(r).about)
  }

  def redirectToLoginIfTimeout[A](req: HttpRequest[A], f: => ResponseFunction[Any]) = {
    try {
      f
    } catch {
      case e: SessionTimeoutException => HerokuRedirect(req, "/login?message=sessionTimeout")
      case e: Exception =>
        if (e.getCause.isInstanceOf[SessionTimeoutException]) {
          def displayReauthentication: ResponseFunction[Any] = {
            HerokuRedirect(req, "/login?message=sessionTimeout")
          }
          displayReauthentication
        } else {
          println("EXCEPTION "+e.getClass+" : "+ e.getMessage +"\n"+e.getStackTraceString)
          Html5(Pages(req).error(e))
        }
    }
  }

  object MatchIdParameter extends Params.Extract("matchid", Params.first)

  object CommentParameter extends Params.Extract("comment", Params.first)

}
