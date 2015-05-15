package no.magott.fiks.data

import unfiltered.request.{HttpRequest, Params}
import org.joda.time.{DateTimeZone, Interval, LocalDateTime}
import validation.{FormField, InputOk}
import fix.UriString._
import scala.xml.NodeSeq
import no.magott.fiks.VCard
import argonaut._, Argonaut._
import scalaz._, Scalaz._


object ResultType extends Enumeration{
  type ResultType = Value
  val FinalResult = Value("Sluttresultat")
  val HalfTime = Value("Pauseresultat")
  val Undefined = Value("Ukjent resultattype")
}

import ResultType._

case class AvailableMatch(category: String, tournament: String, date: LocalDateTime,
                          matchId: String, teams: String, venue: String, role: String, availabilityId: Option[String]){
  def asJson = {
    Json.obj(
      "matchId" := matchId,
      "category" := category,
      "tournament" := tournament,
      "date" := date.toString,
      "teams" := teams,
      "venue" := venue,
      "role" := role,
      "availabilityId" := availabilityId
    )
  }
}

case class AppointmentInfo(fiksId:String, ref:Option[String], ass1:Option[String], ass2:Option[String], fourth:Option[String]){
  def roles = List(ref.map("Dommer" -> _), ass1.map("AD1" -> _), ass2.map("AD2" -> _))
  def asJson = Json.obj(
    "fiksId" := fiksId,
    "ref" := ref,
    "ass1" := ass1,
    "ass2" := ass2,
    "fourth" := fourth,
    "roles" := roles.flatMap(_.map(role => Json.obj(
        "role" := role._1,
        "name" := role._2
      ))
    )
  )
}

case class AssignedMatch(date:LocalDateTime, tournament: String, matchId:String, teams:String, venue:String, referees:String, fiksId:String, cancellationId:Option[String]){
  lazy val refereeTuples : Array[(String,String)] = referees.split('(').drop(1).map(s=> (s.split(')')(0) -> s.split(')')(1).trim))
  def isReferee = refereeTuples.find(findReferee).exists(!_._2.contains(","))
  def externalMatchInfoUrl = s"http://www.fotball.no/System-pages/Kampfakta/?matchId=${fiksId}"
  def displayDismissalReportLink = date.isBefore(LocalDateTime.now) && isReferee
  def playingTime = new Interval(date.toDateTime, date.toDateTime.plusHours(2))

  def roleAndNames = refereeTuples.map((roleName) => roleName._1 -> toPhoneSpan(roleName._2))
  def officials = refereeTuples.map(Official.fromTuple)

  def refs:NodeSeq = toPhoneSpan(referees)

  def contactLink(role:String) : Option[NodeSeq] = {
    val url = s"/fiks/mymatches/$fiksId/contacts/$role"
    refereeTuples.find(_._1 == role).flatMap(x => {
      if(new VCard(x._2).canBeVCard) Some(<a href={url}><small>Legg til i kontakter</small></a>) else None
    })
  }

  def refsObjects = officials.toList.map(_.asJson)

  def asJson = {
    Json.obj(
      "fiksId" := fiksId,
      "date" := date.toString,
      "teams" := teams,
      "competition" := tournament,
      "venue" := venue,
      "cancellationId" := cancellationId,
      "officials" := refsObjects
    )
  }

  def toPhoneSpan(input: String) = {
    val phone = "((\\+47)?)[\\d ]{8,11}".r
    val withPhoneLink = phone.replaceAllIn(input, m => s"""<a href="tel:${m.toString.replaceAll(" ","")}">${m}</a>""")
    wrapXML(withPhoneLink)
  }

  def wrapXML(in: String, name: String = "span"): NodeSeq = {
    xml.XML.loadString(s"<${name}>${in}</${name}>")
  }

  def refereeLastName = {
    val last = refereeTuples.find(findReferee).get._2.split(" ").takeRight(1).mkString
    if(last.endsWith("\u00A0")) last.dropRight(1) else last
  }
  def refereeFirstName = refereeTuples.find(findReferee).get._2.split(" ").dropRight(1).mkString(" ").trim
  def first2DigitsMatchId = matchId.take(2)
  private def last9DigitsMatchId = matchId.drop(2)
  private def month3Letters = date.toString("MMM").capitalize
  private def day2Digits = date.toString("dd")
  private def year4Digits = date.toString("yyyy")
  def homeTeam = teams.split("\u00A0-\u00A0")(0)
  def awayTeam  = teams.split("\u00A0-\u00A0")(1)
  private def findReferee(refTuple:(String,String)) = refTuple._1 == "Dommer"

  def dismissalUrl = uri"http://www.formstack.com/forms?form=1351154&viewkey=N8yMtQ9qxb&field17868550=Offisiell%20kamp%20(serie/cup%20i%20regi%20av%20krets/NFF%20-%20kamp%20har%20kampnr.)&field17868644=$first2DigitsMatchId&field17868658=$last9DigitsMatchId&field17869114=$homeTeam&field17869126=$awayTeam&field17950255-first=$refereeFirstName&field17950255-last=$refereeLastName&field17950248M=$month3Letters&field17950248D=$day2Digits&field17950248Y=$year4Digits"

}

object AssignedMatch {
  val calendarFormatString = "yyyyMMdd'T'HHmmss'Z"

  def googleCalendarLink(m:AssignedMatch): NodeSeq = {
    import m._
    val utcStart = toUTC(date)
    val timeString = utcStart.toString(calendarFormatString) + "/" + utcStart.plusHours(2).toString(calendarFormatString)
    val details =
      s"""Kampnummer:  $matchId %0A
        |Turnering: $tournament %0A
        |${referees.replaceAllLiterally(" (", "%0A(")}%0A
        |Kampinfo: $externalMatchInfoUrl """.stripMargin.trim
    val link = s"http://www.google.com/calendar/event?action=TEMPLATE&text=$teams&dates=$timeString&details=$details&location=$venue&trp=false&sprop=&sprop=name:"
    <a href={link} target="_blank">Google Calendar</a>
  }

  private def toUTC(dateTime: LocalDateTime) = {
    dateTime.toDateTime(DateTimeZone.forID("Europe/Oslo")).withZone(DateTimeZone.UTC).toLocalDateTime
  }

  def icsLink(m:AssignedMatch) = {
    val url = "/match.ics?matchid="+m.matchId
    <a href={url}>Outlook/iCal</a>
  }
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


