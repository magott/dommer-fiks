package no.magott.fiks.data

import unfiltered.filter.Intent._
import unfiltered.response.Html5._
import unfiltered.request.{GET, Seg, Path, Method}
import unfiltered.response.{Html5, NotFound, Pass}
import unfiltered.filter.{Intent, Plan}

class FallbackPlan extends Plan{

  def intent = fallback

  val fallback = Intent {
    case r@GET(Path(Seg("fiks" :: "mymatches" :: Nil))) => HerokuRedirect(r, "/login?message=loginRequired")
    case r@GET(Path(Seg("fiks" :: "availablematches" :: Nil))) => HerokuRedirect(r, "/login?message=loginRequired")
    case Path(Seg("img" :: _ :: Nil)) | Path(Seg("css" :: _ :: Nil)) | Path(Seg("js" :: _ :: Nil)) | Path(Seg("favicon.ico" :: Nil))=> Pass
    case r@GET(_) => NotFound ~> Html5(Pages(r).notFound)
  }
}
