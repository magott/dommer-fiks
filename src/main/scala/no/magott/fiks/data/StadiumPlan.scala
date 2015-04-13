package no.magott.fiks.data
import fix.UriString._
import unfiltered.filter.Plan
import unfiltered.request._
import unfiltered.response._
import unfiltered.response.ResponseString
import no.magott.fiks.HerokuRedirect.XForwardProto
import no.magott.fiks.user.{User, LoggedOnUser}
import geo.LatLong
import no.magott.fiks.MatchIdParameter

class StadiumPlan(stadiumService: StadiumService) extends Plan{
  private val mailService = new MailgunService
  def intent = {
    case r@Path(Seg("stadium" :: Nil)) & Params(StadiumNameParam(name)) & Params(MatchIdParameter(matchId)) => {
      r match {
        case GET(_) => {
          val jsonTemplate = """{"link":"%s", "name": "%s"}"""
          stadiumService.findStadium(name).map(s => Ok ~> JsonContent ~> CacheControl("public, max-age=3600") ~> ResponseString(jsonTemplate.format(s.googleMapsLink, "Google maps")))
          .getOrElse {
            val submitLink = uri"${XForwardProto.unapply(r).getOrElse("http")}://${Host.unapply(r).get}/stadium/submit?stadiumName=${name}&matchid=${matchId}"
            NotFound ~> ResponseString(jsonTemplate.format(submitLink, "Vet du hvor dette er? Fortell det til oss"))
          }
        }
      }
    }
    case r@Path(Seg("stadium" :: "submit" :: Nil)) & Params(StadiumNameParam(name)) & Params(MatchIdParameter(matchId)) => {
      r match {
        case GET(_) => {
          Ok ~> Html5(Pages(r).submitStadium(name, matchId))
        }
        case POST(_) => {
          handleStadiumSubmission(r, name, matchId) match{
            case MailAccepted(_) => Ok ~> Html5(Snippets(r).emptyPage(<legend>Takk for at du gjør Dommer-FIKS bedre</legend> <p><a href="/fiks/mymatches"> Gå tilbake til kamp oversikten</a></p>))
            case x: MailRejected => InternalServerError ~> Html5(Pages(r).error(<div>Her skjedde det en feil. Feilkoden er: {x.errorCode}</div>))
          }
        }
      }
    }
    case r@Path(Seg("stadium" :: "new" :: Nil)) => {
      r match {
        case GET(_) => r match{
          case LoggedOnUser(User("morten.andersen.gott",_,_,_,_,_)) => {
            val Params(StadiumNameParam(name)) = r
            val Params(LatParam(lat)) = r
            val Params(LongParam(long)) = r
            val stadium = MongoStadium(name, LatLong(lat.toDouble, long.toDouble))
            stadiumService.insertStadium(stadium)
            Ok ~> ResponseString("Okidok")
          }
          case _ => Forbidden ~> Html5(Pages(r).error(<p>w00t u trying to do, punk!?</p>))
        }
        case _ => MethodNotAllowed ~> ResponseString("w00t?")
      }
    }
  }
  def handleStadiumSubmission(r:HttpRequest[_], stadiumName:String, matchId:String) = {
    val fiksEmail = "Dommer-FIKS<fiks@andersen-gott.com>"
    val Params(params) = r
    val gjermhus = stadiumService.lookupStadiumViaGjermshus(matchId)
    val submitterEmail = params("email").headOption
    val email = MailMessage(fiksEmail, fiksEmail, "Ny stadio",
      s"""Ny stadio er sendt inn
      |Stadionavn: ${stadiumName}
      |Kamp: http://www.fotball.no/System-pages/Kampfakta/?matchId=${matchId}
      |Avsender: ${submitterEmail.getOrElse("Ingen e-mail")}
      |Beskrivelse: ${params("description").headOption.getOrElse("Ingen beskrivelse")}
      |
      |Google maps: ${gjermhus.map(_.googleMapsLink).getOrElse("")}
      |Godkjenn mapsplassering: ${gjermhus.map(_.insertLink(r)).getOrElse("")}
      |""".stripMargin)
      .copy(headers = submitterEmail.map(e => Map("h:Reply-To" -> e)).getOrElse(Map.empty))
    mailService.sendMail(email)
  }
  object StadiumNameParam extends Params.Extract("stadiumName", Params.first)
  object LatParam extends Params.Extract("lat", Params.first)
  object LongParam extends Params.Extract("long", Params.first)
}
