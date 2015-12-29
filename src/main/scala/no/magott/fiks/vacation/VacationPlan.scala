package no.magott.fiks.vacation

import no.magott.fiks.IdParameter
import no.magott.fiks.data.{Snippets, Pages, MatchScraper, SessionId}
import no.magott.fiks.user.{User, UserService, LoggedOnUser}
import org.joda.time.LocalTime
import unfiltered.filter.Plan
import unfiltered.request._
import unfiltered.response._
import argonaut._, Argonaut._
import scalaz._, Scalaz._
/**
 *
 */
class VacationPlan(userService: UserService, vacationScraper: VacationScraper) extends Plan{

  override def intent = {
    case req@Path(Seg("vacation" :: "new" :: Nil)) => req match {
      case GET(_) & SessionId(sessionId) => {

        Ok ~> Html5(Pages(req).newVacation)
      }
      case POST(_) & SessionId(sessionId) => {
        val sessionOpt = userService.userSession(sessionId)
        if(sessionOpt.isEmpty) Forbidden
        else {
          val response = for {
            vacation <- vacationFromJsonBody(req).leftMap(errors => BadRequest ~> JsonContent ~> ResponseString(errors.toList.asJson.nospaces))
            resp <- vacationScraper.addVacation(sessionOpt.get, vacation).leftMap(error => Forbidden ~>JsonContent ~> ResponseString(List(error.message).asJson.nospaces))
          } yield
            if(resp.isDefined)BadRequest ~> JsonContent ~> ResponseString(List(resp.get).asJson.nospaces)
            else Created ~> ResponseString("")
          response.fold(identity, identity)
        }
      }
      case GET(_) | POST(_) => Forbidden
    }
    case req@Path(Seg("vacation" :: Nil)) => req match {
      case GET(_) & SessionId(sessionId)=> req match{
        case Accepts.Json(_) => {
          val session = userService.userSession(sessionId)
          if(session.isEmpty) Forbidden
          else{
            vacationScraper.getVacationList(session.get).fold(
              error => Forbidden ~> ResponseString(error.message),
              vacationList => Ok ~> JsonContent ~> ResponseString(vacationList.map(_.asJson).jencode.nospaces)
            )
          }
        }
        case _ => Ok ~> Html5(Pages(req).vacationList)
      }
      case DELETE(_) & SessionId(sessionId) & Params(IdParameter(id)) => {
        val session = userService.userSession((sessionId))
        if(session.isEmpty) Forbidden
        else {
          vacationScraper.deleteVacation(session.get, id)
          Ok
        }
      }
      case GET(_) => Forbidden
    }
  }

  def vacationFromJsonBody[T](req:HttpRequest[T]) : NonEmptyList[String] \/ Vacation = {
    val jsonString = Body.string(req)
    val jsonDisjunction = Parse.decodeEither[Map[String,Option[String]]](jsonString).leftMap(NonEmptyList(_))
    for{
      params <- jsonDisjunction
      p <- params.withDefaultValue(None).right
      vacation <- Vacation.validate(p("fromDate"),p("fromTime"), p("toDate"), p("toTime"), p("reason"))
    } yield vacation
  }

  def vacationFromParams(params: Map[String, Seq[String]]) = {
    import no.magott.fiks.ParameterImplicits._
    val fromDate = params.valueOrNone("fromDate")
    val toDate = params.valueOrNone("toDate")
    val fromTime = params.valueOrNone("fromTime")
    val toTime = params.valueOrNone("toTime")
    val reason = params.valueOrNone("reason")
    Vacation.validate(fromDate, fromTime, toDate, toTime, reason)
  }

}

