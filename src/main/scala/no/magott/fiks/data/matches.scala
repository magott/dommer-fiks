package no.magott.fiks.data

import fix.UriString
import unfiltered.request.{HttpRequest, Params}
import org.joda.time.{Interval, LocalTime, LocalDateTime}
import validation.{FormField, InputOk}
import fix.UriString._


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
  def isReferee = refereeTuples.find(findReferee).exists(!_._2.contains(","))
  def externalMatchInfoUrl = "http://www.fotball.no/System-pages/Kampfakta/?matchId=%s".format(fiksId)
  def displayDismissalReportLink = date.isBefore(LocalDateTime.now) && isReferee
  def playingTime = new Interval(date.toDateTime, date.toDateTime.plusHours(2))
  def refereeLastName = {
    val last = refereeTuples.find(findReferee).get._2.split(" ").takeRight(1).mkString
    if(last.endsWith("\u00A0")) last.dropRight(1) else last
  }
  def refereeFirstName = refereeTuples.find(findReferee).get._2.split(" ").dropRight(1).mkString(" ").trim
  def first2DigitsMatchId = matchId.take(2)
  private def last9DigitsMatchId = matchId.drop(2)
  private def month3Letters = date.toString("MMM")
  private def day2Digits = date.toString("dd")
  private def year4Digits = date.toString("yyyy")
  private def homeTeam = teams.split("\u00A0-\u00A0")(0)
  private def awayTeam  = teams.split("\u00A0-\u00A0")(1)
  private def findReferee(refTuple:(String,String)) = refTuple._1 == "Dommer"

  def dismissalUrl = uri"http://www.formstack.com/forms?form=1351154&viewkey=N8yMtQ9qxb&field17868550=Offisiell%20kamp%20(serie/cup%20i%20regi%20av%20krets/NFF%20-%20kamp%20har%20kampnr.)&field17868644=$first2DigitsMatchId&field17868658=$last9DigitsMatchId&field17869114=$homeTeam&field17869126=$awayTeam&field17950255-first=$refereeFirstName&field17950255-last=$refereeLastName&field17950248M=$month3Letters&field17950248D=$day2Digits&field17950248Y=$year4Digits"

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
  def fromString(home:String, away:String):Score = Score(home.toInt, away.toInt)
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


