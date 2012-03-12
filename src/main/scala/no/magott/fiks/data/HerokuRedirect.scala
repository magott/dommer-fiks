package no.magott.fiks.data

import unfiltered.request.{Host, StringHeader, HttpRequest, &}
import unfiltered.response._

object HerokuRedirect {
  def apply[A, B](req: HttpRequest[A], path: String): ResponseFunction[B] = {
    val absolutepath = if (path.startsWith("/")) path else "/" + path
    req match {
      case XForwardProto("https") & Host(host) => Found ~> Location("https://%s%s".format(host, absolutepath))
      case _ => Redirect(path)
    }
  }

  object XForwardProto extends StringHeader("X-Forward-Proto")

}
