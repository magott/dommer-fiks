package no.magott.fiks.data

import unfiltered.filter.Plan
import unfiltered.request._
import unfiltered.response.Html
import collection.immutable.Map

class SecurityPlan extends Plan{


  def intent = {
    case GET(Path(Seg("login" :: Nil))) & => {

    }
    case POST(Path(Seg("login" :: Nil))) & Params(p) => handleLogin(p)
  }


  def handleLogin(map: Map[String, Seq[String]]) = {
    Html(<h1>Logg inn</h1>)
  }
}
