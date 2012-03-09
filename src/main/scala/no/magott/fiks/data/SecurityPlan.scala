package no.magott.fiks.data

import unfiltered.filter.Plan
import unfiltered.request._
import collection.immutable.Map
import unfiltered.response.Html

object SecurityPlan extends Plan{


  def intent = {
    case GET(Path(Seg("login" :: Nil))) & Params(p)=> {
      handleLogin(p);
    }
    case POST(Path(Seg("login" :: Nil))) & Params(p) => handleLogin(p)

    case GET(Path(Seg("foo" :: Nil))) => Html(Snippets.emptyPage(<p>Security plan</p>))

//    case GET(_) => Html(Snippets.emptyPage(<p>Security plan</p>))
  }


  def handleLogin(map: Map[String, Seq[String]]) = {
    Html(<h1>Logg inn</h1>)
  }
}
