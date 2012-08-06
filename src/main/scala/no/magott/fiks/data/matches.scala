package no.magott.fiks.data

import unfiltered.request.{HttpRequest, Params}
import org.joda.time.{LocalTime, LocalDateTime}
import validation.{Label, FormField, InputOk}


object ResultType extends Enumeration{
  type ResultType = Value
  val FinalResult = Value("Sluttresultat")
  val HalfTime = Value("Pauseresultat")
  val Undefined = Value("Ukjent resultattype")
}

import ResultType._

case class AvailableMatch(category: String, tournament: String, date: LocalDateTime,
                          matchId: String, teams: String, venue: String, role: String, availabilityId: Option[String])

case class AssignedMatch(date:LocalDateTime, tournament: String, matchId:String, teams:String, venue:String, referees:String, fiksId:String){
  lazy val refereeTuples : Array[(String,String)] = referees.split('(').drop(1).map(s=> (s.split(')')(0) -> s.split(')')(1).trim))
  def isReferee = refereeTuples.find(_._1 == "Dommer").exists(!_._2.contains(","))
  def externalMatchInfoUrl = "http://www.fotball.no/System-pages/Kampfakta/?matchId=%s".format(fiksId)
}
case class MatchResult(fiksId:String, teams:String, matchId:String, finalScore:Option[Score] = None, halfTimeScore:Option[Score] = None,
                       attendance:Int = 0, firstHalfAddedTime:Option[Int] = None, secondHalfAddedTime:Option[Int] = None,
                       protestHomeTeam:Boolean = false, protestAwayTeam:Boolean = false, resultReports:Set[ResultReport] = Set.empty){

  def asInputFields:Map[String,FormField] = {
    Map(
      InputOk("teams",Some(teams)).toTuple,
      InputOk("matchId",Some(matchId)).toTuple,
      InputOk("finalHomeGoals", finalScore.map(_.home.toString)).toTuple,
      InputOk("finalAwayGoals", finalScore.map(_.away.toString)).toTuple,//XXX: Getter on inner option!!
      InputOk("halfTimeHomeGoals", halfTimeScore.map(_.home.toString)).toTuple,
      InputOk("halfTimeAwayGoals", halfTimeScore.map(_.away.toString)).toTuple,
      InputOk("attendance", Some(attendance.toString)).toTuple,
      InputOk("firstHalfAddedTime", firstHalfAddedTime.map(_.toString)).toTuple,
      InputOk("secondHalfAddedTime", secondHalfAddedTime.map(_.toString)).toTuple
    )
  }

  def applyFormFields(fields:Map[String,FormField]) = {
    this.copy(
      teams = fields("teams").value.get,
      matchId = fields("matchId").value.get,
      finalScore = Score.stringScoreToOption(fields("finalHomeGoals").value, fields("finalAwayGoals").value),
      halfTimeScore = Score.stringScoreToOption(fields("halfTimeHomeGoals").value, fields("halfTimeAwayGoals").value),
      attendance = fields("attendance").value.get.toInt
    )
  }

  def isDeletionRequired = ! requiredDeletions.isEmpty

  lazy val requiredDeletions = {
    resultReports.filter(
    p => p.resultType match {
      case HalfTime => halfTimeScore.forall(_ != p.score)
      case FinalResult => finalScore.forall(_ != p.score)
      case _ => false
    }
    )
  }

  def resultReport(resultType:ResultType) = resultReports.find(_.resultType==resultType)

}

case class Score(home:Int, away:Int){
  def toLiteral = home + " - " + away
}

object Score{
  def apply(home:String, away:String):Score = Score(home.toInt, away.toInt)
  def toOption(home:String, away:String) = if(isValidSingleScore(home) && isValidSingleScore(away)) Some(Score(home.toInt,away.toInt)) else None
  def stringScoreToOption(home:Option[String], away:Option[String]):Option[Score] = if(home.isDefined && away.isDefined) Score.toOption(home.get,away.get) else None
  def isValidSingleScore(s:String) = !s.trim.isEmpty && s.forall(_.isDigit)
}
case class ResultReport(resultType:ResultType, score:Score, reportId:String, reporter:String)



object MatchStuff{
  def allMatches[T](req: HttpRequest[T]) = {
    val p = Params.unapply(req).get
    p.contains("all")
  }


}


