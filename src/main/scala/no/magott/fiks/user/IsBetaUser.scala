package no.magott.fiks.user

import javax.servlet.http.HttpServletRequest
import unfiltered.request.HttpRequest
import no.magott.fiks.data.FiksCookie

object IsBetaUser {

  def unapply[T <: HttpServletRequest](req: HttpRequest[T]) = {
    val user = LoggedOnUser.unapply(req)
    user.filter(_.beta)
  }

}
