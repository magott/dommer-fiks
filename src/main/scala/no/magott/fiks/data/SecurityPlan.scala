package no.magott.fiks.data

import unfiltered.filter.Plan
import unfiltered.request._
import collection.immutable.Map
import unfiltered.Cookie
import unfiltered.response.{Html5, Redirect, SetCookies, Html}

object SecurityPlan extends Plan{


  def intent = {
    case GET(Path(Seg("login" :: Nil))) & Params(p)=> {
      Html5(Pages.loginForm(p));
    }
    case POST(Path(Seg("login" :: Nil))) & Params(p) => handleLogin(p)

    case GET(Path(Seg("foo" :: Nil))) => Html(Snippets.emptyPage(<p>Security plan</p>))

//    case GET(_) => Html(Snippets.emptyPage(<p>Security plan</p>))
  }

  def handleLogin(map: Map[String, Seq[String]]) = {
    val username = map.get("username")
    val password = map.get("password")
    FiksLogin.login(username.get.head, password.get.head) match {
      case Right(cookie) => {
        SetCookies(Cookie(name = "fiksToken", value=cookie._2, secure = Some(false))) ~>
        Redirect("/fiks/mymatches")
      }
      case _ => Redirect("/login?message=loginFailed")
    }
  }
}
