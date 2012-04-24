package no.magott.fiks.data

import unfiltered.request._
import unfiltered.response._
import unfiltered.filter.{Intent, Plan}
import unfiltered.Cookie
import no.magott.fiks.HerokuRedirect
import javax.servlet.http.HttpServletRequest
import no.magott.fiks.user.LoggedOnUser
import java.util.concurrent.ExecutionException
import java.net.SocketTimeoutException

class FiksPlan(matchservice: MatchService) extends Plan {

  def intent = {
    myMatches orElse availableMatches orElse about orElse reportInterest
  }

  val myMatches = Intent {
    case r@GET(Path(Seg("fiks" :: "mymatches" :: Nil))) & FiksCookie(loginToken) =>
      val req = r.asInstanceOf[HttpRequest[HttpServletRequest]]
      redirectToLoginIfTimeout(req, {
      val assigned = matchservice.assignedMatches((FiksLoginService.COOKIE_NAME, loginToken))
      Ok ~> Html5(Pages(r).assignedMatches(assigned))
    })
    case r@GET(Path(Seg("match.ics" :: Nil))) & Params(p) => Ok ~> CalendarContentType ~> ResponseString(Snippets(r).isc(p))
    case r@GET(Path(Seg(Nil))) & FiksCookie(_) => HerokuRedirect(r, "/fiks/mymatches")
    case r@GET(Path(Seg(Nil))) => HerokuRedirect(r, "/login")
  }

  val availableMatches = Intent {
    case r@GET(Path(Seg("fiks" :: "availablematches" :: Nil))) & Params(MatchIdParameter(matchId)) & FiksCookie(loginToken) => redirectToLoginIfTimeout(r, {
      val matchInfo = matchservice.matchInfo(matchId, loginToken)
      Ok ~> Html5(Pages(r).reportInterestIn(matchInfo))
    })
    case r@GET(Path(Seg("fiks" :: "availablematches" :: Nil))) & FiksCookie(loginToken) => redirectToLoginIfTimeout(r, {
      val available = matchservice.availableMatches((FiksLoginService.COOKIE_NAME, loginToken))
      Ok ~> Html5(Pages(r).availableMatches(available))
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
  def redirectToLoginIfTimeout[T <: HttpServletRequest](req: HttpRequest[T], f: => ResponseFunction[Any]) = {
    try {
      f
    } catch {
      //TODO: Invalidate session
      case e: SessionTimeoutException =>SetCookies(Cookie(name="fiksToken", value="", maxAge=Some(0))) ~> displayReauthentication(req)
      case e: ExecutionException =>
        if (e.getCause.isInstanceOf[SessionTimeoutException]) {
          SetCookies(Cookie(name="fiksToken", value="", maxAge=Some(0))) ~> displayReauthentication(req)
        } else if(e.getCause.isInstanceOf[SocketTimeoutException]){
          Html5(Pages(req).error(e.getCause.asInstanceOf[SocketTimeoutException]))
        }else{
          println("EXCEPTION " + e.getClass + " : " + e.getMessage + "\n" + e.getStackTraceString)
          Html5(Pages(req).error(e))
        }
      case e: Exception =>
        Html5(Pages(req).error(e))
    }
  }

  def displayReauthentication[A](req: HttpRequest[A]) = {
    HerokuRedirect(req, "/login?message=sessionTimeout")
  }

  object MatchIdParameter extends Params.Extract("matchid", Params.first)

  object CommentParameter extends Params.Extract("comment", Params.first)

}
