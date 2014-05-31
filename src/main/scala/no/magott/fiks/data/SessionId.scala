package no.magott.fiks.data

import unfiltered.request.{Cookies, HttpRequest}

object SessionId {
  def unapply[T](req: HttpRequest[T]) = {
    val cookies = Cookies.unapply(req).get
    cookies("fiksToken") match {
      case Some(c) => Some(c.value)
      case None => None
    }
  }

}
