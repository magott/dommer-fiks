package no.magott.fiks.data

import unfiltered.filter.Plan
import unfiltered.request._
import collection.immutable.Map
import unfiltered.Cookie
import unfiltered.response.{Pass, Html5, SetCookies}
import no.magott.fiks.HerokuRedirect
import no.magott.fiks.HerokuRedirect.XForwardProto
import javax.servlet.http.HttpServletRequest
import no.magott.fiks.user.{UserSession, UserService}

class SecurityPlan(matchservice:MatchService, userservice:UserService) extends Plan{

  def intent = {
    case r@GET(_) & XForwardProto("http") => {
      r match {
        case r@GET(Path(Seg("calendar" :: Nil))) & Params(FeedIdParameter(feedId)) => Pass
        case _ => HerokuRedirect(r,r.uri)
      }
    }
    case r@GET(Path(Seg("login" :: Nil))) & Params(p)=> {
      SetCookies(Cookie(name="fiksToken", value="deleted", maxAge=Some(-360000))) ~>
        Html5(Pages(r).loginForm(p))
    }
    case r@POST(Path(Seg("login" :: Nil))) & Params(p) => handleLogin(r,p)
    case r@GET(Path(Seg("logout" :: Nil))) => handleLogout(r)
    case r@SessionId(token) => {
      if(userservice.userSession(token).isDefined){
        Pass
      }else{
        HerokuRedirect(r,"/login?message=sessionTimeout")
      }
    }
  }

  def handleLogin[T<:HttpServletRequest](req: HttpRequest[T], map: Map[String, Seq[String]]) = {
    val username = map.get("username").get.head.toLowerCase
    val password = map.get("password").get.head
    val rememberMe = true //map.get("RememberMe").exists(_.contains("on"))
    println(s"Logging in $username")
    FiksLoginService.login(username, password, rememberMe) match {
      case Right(session) => {
        println(s"Logged in $username")
        matchservice.prefetchAvailableMatches(session)
        userservice.save(session)
        val secure = req match { case XForwardProto("https") => Some(true) case _ => Some(false)}
        val maxAge = if(rememberMe) Some(3600*24*365) else None
        SetCookies(Cookie(name = "fiksToken", value=session.id, secure = secure, httpOnly = true, maxAge=maxAge)) ~>
          HerokuRedirect(req,"/fiks/mymatches")
      }
      case _ => HerokuRedirect(req,"/login?message=loginFailed")
    }
  }

  def handleLogout(req: HttpRequest[Any]) = SetCookies(Cookie(name="fiksToken", value="", maxAge=Some(0))) ~> HerokuRedirect(req, "/login?message=logout")

  object FeedIdParameter extends Params.Extract("id", Params.first)

}
