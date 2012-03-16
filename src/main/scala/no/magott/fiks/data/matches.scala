package no.magott.fiks.data

import org.joda.time.LocalDateTime

case class AvailableMatch(val category: String, val tournament: String, val date: LocalDateTime,
                          val matchId: String, val teams: String, val venue: String, val role: String, val availabilityId: Option[String])

case class AssignedMatch(val date:LocalDateTime, val tournament: String, val matchId:String, val teams:String, val venue:String, val referees:String)

