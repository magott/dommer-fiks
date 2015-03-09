package no.magott.fiks

import data.Pages
import unfiltered.request.{GET, Seg, Path}
import unfiltered.response.{InternalServerError, Html5, NotFound, Pass}
import unfiltered.filter.{Intent, Plan}

class FallbackPlan extends Plan {

  def intent = fallback

  val fallback = Intent {
    case r@GET(Path(Seg("error" :: Nil))) => InternalServerError ~> Html5(Pages(r).error(<div>Her inntraff det en ukjent feil. Gå til <a href="/">forsiden</a></div>))
    case r@GET(Path(Seg("fiks" :: "mymatches" :: Nil))) => HerokuRedirect(r, "/login?message=loginRequired")
    case r@GET(Path(Seg("fiks" :: "availablematches" :: Nil))) => HerokuRedirect(r, "/login?message=loginRequired")
    case r@GET(Path(Seg("calendar" :: _ :: Nil))) => HerokuRedirect(r, "/login?message=loginRequired")
    case Path(Seg("img" :: _ :: Nil)) | Path(Seg("css" :: _ :: Nil)) | Path(Seg("img" :: "yr" :: _ :: Nil))
         | Path(Seg("js" :: _ :: Nil)) | Path(Seg("favicon.ico" :: Nil)) => Pass
//         | Path(Seg("robots.txt" :: Nil)) => Pass
    case r@GET(_) => NotFound ~> Html5(Pages(r).notFound)
  }
}
