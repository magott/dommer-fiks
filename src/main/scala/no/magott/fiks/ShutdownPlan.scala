package no.magott.fiks

import no.magott.fiks.data.Snippets
import unfiltered.filter.Plan
import unfiltered.response.{Ok, Html5}
import unfiltered.request._
import unfiltered.response._


/**
  *
  */
class ShutdownPlan extends Plan{

  override def intent = {
    case Path(Seg("img" :: _ :: Nil)) | Path(Seg("css" :: _ :: Nil)) | Path(Seg("img" :: "yr" :: _ :: Nil))
         | Path(Seg("js" :: _ :: Nil)) | Path(Seg("favicon.ico" :: Nil)) => Pass

    case r@_ => Ok ~> Html5(Snippets(r).emptyPage((
      <div class="jumbotron">
        <h1>Dommer-FIKS er stengt</h1>
        <p>NFF blokkerer for tiden serveren Dommer-FIKS kjører på. Dette gjør at det ikke er mulig å hente data om dine kamper eller å logge deg inn</p>
        <p>Dommer-FIKS kan derfor ikke fortsette.</p>
        <p>For ytterligere spørsmål, ta kontakt via <a href="https://www.facebook.com/dommerfiks/">Dommer-FIKS facebookgruppe</a></p>
      </div>
      ), None))
  }
}
