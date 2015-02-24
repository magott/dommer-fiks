package no.magott.fiks.invoice

import argonaut.Argonaut._
import argonaut.EncodeJson
import org.joda.time.{DateTime, LocalDateTime}
import com.mongodb.casbah.query.Imports._
import no.magott.fiks.data.AssignedMatch
import argonaut._, Argonaut._


/**
 * Created by morten on 31/03/14.
 */
case class MatchData(fiksId:String, matchId:String, home:String, away:String, venue:String, tournament:String, date:DateTime) {

  def dateString = date.toString("dd.MM.yyyy")
  def teams = s"$home - $away"

  def toMongo = MongoDBObject(
    "matchId" -> matchId,
    "home" -> home,
    "away" -> away,
    "venue" -> venue,
    "date" -> date,
    "fiksId" -> fiksId,
    "tournament" -> tournament
  )

  def toJson = {
    Json.obj(
      "date" := date.toString(),
      "home" := home,
      "away" := away
    )
  }

}

object MatchData{
  def fromAssignedMatch(m:AssignedMatch) = MatchData(m.fiksId, m.matchId, m.homeTeam, m.awayTeam, m.venue, m.tournament, m.date.toDateTime)
  def fromMongo(m:DBObject) = {
    MatchData(m.as[String]("fiksId"), m.as[String]("matchId"), m.as[String]("home"), m.as[String]("away"), m.as[String]("venue"), m.as[String]("tournament"), m.as[DateTime]("date"))
  }

}
