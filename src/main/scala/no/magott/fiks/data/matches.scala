package no.magott.fiks.data

import org.joda.time.LocalDateTime
import unfiltered.request.{HttpRequest, Params}

case class AvailableMatch(category: String, tournament: String, date: LocalDateTime,
                          matchId: String, teams: String, venue: String, role: String, availabilityId: Option[String])

case class AssignedMatch(date:LocalDateTime, tournament: String, matchId:String, teams:String, venue:String, referees:String, fiksId:String){
  def refereeTuples = referees.split('(').drop(1).map(s=> (s.split(')')(0) -> s.split(')')(1).trim))
}

object MatchStuff{
  def allMatches[T](req: HttpRequest[T]) = {
    val p = Params.unapply(req).get
    p.contains("all")
  }
}

