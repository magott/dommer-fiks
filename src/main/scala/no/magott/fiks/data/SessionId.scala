package no.magott.fiks.data

import unfiltered.request.{Cookies, HttpRequest}

object SessionId {
  def unapply[T](req: HttpRequest[T]) : Option[String] = {
    val cookies = Cookies.unapply(req).get
    cookies("fiksToken").map(_.value)
  }

}
