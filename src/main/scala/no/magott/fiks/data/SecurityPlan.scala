package no.magott.fiks.data

import unfiltered.filter.Plan
import unfiltered.request._
import collection.immutable.Map
import unfiltered.Cookie
import unfiltered.response.{Html5, SetCookies}
import no.magott.fiks.HerokuRedirect
import no.magott.fiks.HerokuRedirect.XForwardProto
import javax.servlet.http.HttpServletRequest
import no.magott.fiks.user.{UserSession, UserService}

class SecurityPlan(val matchservice:MatchService) extends Plan{

  val userservice = new UserService

  def intent = {
    case r@GET(_) & XForwardProto("http") => HerokuRedirect(r,r.uri)
    case r@GET(Path(Seg("login" :: Nil))) & Params(p)=> {
      SetCookies(Cookie(name="fiksToken", value="", maxAge=Some(0))) ~>
        Html5(Pages(r).loginForm(p))
    }
    case r@POST(Path(Seg("login" :: Nil))) & Params(p) => handleLogin(r,p)
    case r@GET(Path(Seg("logout" :: Nil))) => handleLogout(r)
  }

  def handleLogin[T<:HttpServletRequest](req: HttpRequest[T], map: Map[String, Seq[String]]) = {
    val username = map.get("username").get.head.toLowerCase
    val password = map.get("password").get.head
    FiksLoginService.login(username, password) match {
      case Right(cookie) => {
        matchservice.prefetchAvailableMatches(cookie._2)
        userservice.save(UserSession(cookie._2,username))
        val secure = req match { case XForwardProto("https") => Some(true) case _ => Some(false)}
        SetCookies(Cookie(name = "fiksToken", value=cookie._2, secure = secure, httpOnly = true)) ~>
          HerokuRedirect(req,"/fiks/mymatches")
      }
      case _ => HerokuRedirect(req,"/login?message=loginFailed")
    }
  }

  def handleLogout(req: HttpRequest[Any]) = SetCookies(Cookie(name="fiksToken", value="", maxAge=Some(0))) ~> HerokuRedirect(req, "/")
}
