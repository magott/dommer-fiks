package no.magott.fiks.data

import unfiltered.filter.Plan
import unfiltered.request._
import collection.immutable.Map
import unfiltered.Cookie
import unfiltered.response.{Html5, Redirect, SetCookies, Html}
import no.magott.fiks.data.HerokuRedirect.XForwardProto

object SecurityPlan extends Plan{


  def intent = {
    case r@GET(_) & XForwardProto("http") => HerokuRedirect(r,r.uri)
    case GET(Path(Seg("login" :: Nil))) & Params(p)=> {
      Html5(Pages.loginForm(p));
    }
    case r@POST(Path(Seg("login" :: Nil))) & Params(p) => handleLogin(r,p)

    case GET(Path(Seg("foo" :: Nil))) => Html(Snippets.emptyPage(<p>Security plan</p>))

//    case GET(_) => Html(Snippets.emptyPage(<p>Security plan</p>))
  }

  def handleLogin[A](req: HttpRequest[A], map: Map[String, Seq[String]]) = {
    val username = map.get("username")
    val password = map.get("password")
    FiksLogin.login(username.get.head, password.get.head) match {
      case Right(cookie) => {
        SetCookies(Cookie(name = "fiksToken", value=cookie._2, secure = Some(false))) ~>
          HerokuRedirect(req,"/fiks/mymatches")
      }
      case _ => HerokuRedirect(req,"/login?message=loginFailed")
    }
  }
}
