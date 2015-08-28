package no.magott.fiks.user

import no.magott.fiks.HerokuRedirect
import no.magott.fiks.data.{Pages, SessionId, Snippets}
import unfiltered.filter.Plan
import unfiltered.filter.Plan.Intent
import unfiltered.request._
import unfiltered.response.{BadRequest, Ok, Html5}

import scalaz.{-\/, \/-}

/**
 *
 */
class UserPlan(userService: UserService) extends Plan{

  type MultiMap = Map[String, Seq[String]]

  override def intent: Intent = {
    case r@Path(Seg("user" :: Nil)) & SessionId(sessionId) => r match {
      case GET(_) => {
        val userOpt = userService.userForSession(sessionId)
        Ok ~> Html5(Pages(r).userProfile(userOpt, None))
      }
      case POST(_) & Params(p) & SessionId(sessionId)=> {
        val sessionOpt = userService.userSession(sessionId)
        val user = sessionOpt.flatMap(session => userService.byUsername(session.username))
        createUserFromParams(p, sessionOpt.get.username) match {
          case \/-(postedUser) => {
            val toSave = user.map(_.applyUpdates(postedUser)).getOrElse(postedUser)
            userService.saveUser(toSave)
            HerokuRedirect(r, "/user")
          }
          case -\/(errors) => {
            BadRequest ~> Html5(Pages(r).userProfile(user, Some(errors.list)))
          }
        }

      }
    }
  }

  def createUserFromParams(p: MultiMap, username:String) = {
    import no.magott.fiks.ParameterImplicits._
    User.validate(username, p.valueOrNone("password"),
      p.valueOrNone("email"),
      None,
      p.valueOrNone("name"),
      p.valueOrNone("address"),
      p.valueOrNone("zip"),
      p.valueOrNone("city"),
      p.valueOrNone("phone"),
      p.valueOrNone("accountNo"),
      p.valueOrNone("taxMuncipal"),
      p.valueOrNone("fnr"),
      p.valueOrNone("tromso").map(_.toBoolean)
    )
  }
}
