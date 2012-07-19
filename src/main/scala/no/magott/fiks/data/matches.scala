package no.magott.fiks.data

import unfiltered.request.{HttpRequest, Params}
import org.joda.time.{LocalTime, LocalDateTime}
import validation.{InputField, InputOk}

case class AvailableMatch(category: String, tournament: String, date: LocalDateTime,
                          matchId: String, teams: String, venue: String, role: String, availabilityId: Option[String])

case class AssignedMatch(date:LocalDateTime, tournament: String, matchId:String, teams:String, venue:String, referees:String, fiksId:String){
  lazy val refereeTuples : Array[(String,String)] = referees.split('(').drop(1).map(s=> (s.split(')')(0) -> s.split(')')(1).trim))
  def isReferee = refereeTuples.find(_._1 == "Dommer").exists(!_._2.contains(","))
  def externalMatchInfoUrl = "http://www.fotball.no/System-pages/Kampfakta/?matchId=%s".format(fiksId)
}
case class MatchResult(fiksId:String, teams:String, matchId:String, finalScore:(Option[Int],Option[Int]) = (None,None), halfTimeScore:(Option[Int],Option[Int]) = (None,None),
                       attendance:Int = 0, firstHalfAddedTime:Option[Int] = None, secondHalfAddedTime:Option[Int] = None,
                       protestHomeTeam:Boolean = false, protestAwayTeam:Boolean = false){

  def inputFields = {
    Map(
      InputOk("teams",Some(teams)).toTuple,
      InputOk("matchId",Some(matchId)).toTuple,
      InputOk("finalHomeGoals", finalScore._1.map(_.toString)).toTuple,
      InputOk("finalAwayGoals", finalScore._2.map(_.toString)).toTuple,
      InputOk("halfTimeHomeGoals", halfTimeScore._1.map(_.toString)).toTuple,
      InputOk("halfTimeAwayGoals", halfTimeScore._2.map(_.toString)).toTuple,
      InputOk("attendance", Some(attendance.toString)).toTuple,
      InputOk("firstHalfAddedTime", firstHalfAddedTime.map(_.toString)).toTuple,
      InputOk("secondHalfAddedTime", secondHalfAddedTime.map(_.toString)).toTuple
    )
  }

  def applyInputFields(fields:Map[String,InputField]) = {
    this.copy(
      teams = fields("teams").value.get,
      matchId = fields("matchId").value.get,
      finalScore = (fields("finalHomeGoals").value.map(_.toInt), fields("finalAwayGoals").value.map(_.toInt)),
      halfTimeScore = (fields("halfTimeHomeGoals").value.map(_.toInt), fields("halfTimeAwayGoals").value.map(_.toInt)),
      attendance = fields("attendance").value.get.toInt
    )
  }
}


object MatchStuff{
  def allMatches[T](req: HttpRequest[T]) = {
    val p = Params.unapply(req).get
    p.contains("all")
  }
}

