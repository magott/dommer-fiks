package no.magott.fiks.data

import unfiltered.filter.Plan
import unfiltered.request._
import collection.immutable.Map
import unfiltered.Cookie
import unfiltered.response.{Html5, Redirect, SetCookies, Html}
import no.magott.fiks.data.HerokuRedirect.XForwardProto

class SecurityPlan(val matchservice:MatchService) extends Plan{


  def intent = {
    case r@GET(_) & XForwardProto("http") => HerokuRedirect(r,r.uri)
    case GET(Path(Seg("login" :: Nil))) & Params(p)=> {
       Html5(Pages.loginForm(p));
    }
    case r@POST(Path(Seg("login" :: Nil))) & Params(p) => handleLogin(r,p)
  }

  def handleLogin[A](req: HttpRequest[A], map: Map[String, Seq[String]]) = {
    val username = map.get("username")
    val password = map.get("password")
    FiksLogin.login(username.get.head, password.get.head) match {
      case Right(cookie) => {
        matchservice.prefetchAssignedMatches(cookie._2)
        matchservice.prefetchAvailableMatches(cookie._2)
        val secure = req match { case XForwardProto("https") => Some(true) case _ => Some(false)}
        SetCookies(Cookie(name = "fiksToken", value=cookie._2, secure = secure, httpOnly = true)) ~>
          HerokuRedirect(req,"/fiks/mymatches")
      }
      case _ => HerokuRedirect(req,"/login?message=loginFailed")
    }
  }
}
