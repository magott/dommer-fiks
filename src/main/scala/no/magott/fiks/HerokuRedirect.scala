package no.magott.fiks

import unfiltered.request._
import unfiltered.response._

object HerokuRedirect {
  def apply[A, B](req: HttpRequest[A], path: String): ResponseFunction[B] = {
    val absolutepath = if (path.startsWith("/")) path else "/" + path
    req match {
      case XForwardProto(_) & Host(host) => Found ~> Location("https://%s%s".format(host, absolutepath))
      case Host(host) => Found ~> Location("http://%s%s".format(host, absolutepath))
      case _ => Redirect(absolutepath)
    }
  }

  object XForwardProto extends StringHeader("x-forwarded-proto")

}
