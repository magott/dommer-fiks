package no.magott.fiks.user

import javax.servlet.http.HttpServletRequest
import unfiltered.request.HttpRequest
import no.magott.fiks.data.FiksCookie

object LoggedOnUser {

  val userservice = new UserService

  def unapply[T <: HttpServletRequest](req: HttpRequest[T]) = {
    FiksCookie.unapply(req) match {
      case None => None
      case Some(token) =>  userservice.userForSession(token)
    }
  }

}
