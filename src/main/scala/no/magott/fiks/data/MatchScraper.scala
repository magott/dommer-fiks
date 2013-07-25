package no.magott.fiks.data

import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import scala.collection.JavaConverters._
import org.joda.time.format.DateTimeFormat
import org.joda.time.LocalDate
import org.jsoup.Connection.Method
import org.jsoup.safety.Whitelist
import unfiltered.request.POST

class MatchScraper {
  val COOKIE_NAME = "ASP.NET_SessionId"
  val dateTimeFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")
  val matchReportUrl = "https://fiks.fotball.no/Fogisdomarklient/Match/MatchDomarrapport.aspx?matchId=s%"

  def scrapeMatchInfo(assignmentId: String, loginToken:String) = {
    val cleanAssignmentId = Jsoup.clean(assignmentId, Whitelist.none)
    val response = Jsoup.connect("https://fiks.fotball.no/Fogisdomarklient/Uppdrag/UppdragLedigtUppdrag.aspx?domaruppdragId=" + cleanAssignmentId)
      .method(Method.GET).cookie(COOKIE_NAME, loginToken).followRedirects(false).timeout(15000).execute()
    if(response.statusCode == 302){
      throw new SessionTimeoutException()
    }
    val el = response.parse().select("table")
    AvailableMatch(
      el.select("span#lblTavlingskategori").text,
      el.select("span#lblTavling").text,
      dateTimeFormat.parseLocalDateTime(el.select("span#lblTid").text),
      el.select("span#lblMatchnr").text,
      el.select("span#lblMatch").text,
      el.select("span#lblAnlaggning").text,
      el.select("span#lblUppdrag").text,
      Some(cleanAssignmentId)
    )
  }

  def scrapeAvailableMatches(loginToken: String) = {
    val availableMatchesResponse = Jsoup.connect("https://fiks.fotball.no/Fogisdomarklient/Start/StartLedigaUppdragLista.aspx")
      .cookie(COOKIE_NAME, loginToken).method(Method.GET).followRedirects(false).timeout(25000).execute()

    if (availableMatchesResponse.statusCode == 302) {
      throw new SessionTimeoutException();
    }

    val availableMatchesDoc = availableMatchesResponse.parse()
    val matchElements = availableMatchesDoc.select("div#divMainContent").select("table.fogisInfoTable > tbody > tr").listIterator.asScala.drop(1)

    matchElements.map {
      el: Element =>
        AvailableMatch(el.child(0).text,
          el.child(1).text,
          dateTimeFormat.parseLocalDateTime(el.child(2).text),
          el.child(4).text,
          el.child(5).text,
          el.child(6).text,
          el.child(7).text,
          if(el.child(8).child(0).nodeName =="a"){
            Some(el.child(8).child(0).attr("href").split("=")(1))
          }else{
            None
          }
        )
    }.toList
  }

  def scrapeAssignedMatches(loginToken: String) = {
    val assignedMatchesResponse = Jsoup.connect("https://fiks.fotball.no/Fogisdomarklient/Uppdrag/UppdragUppdragLista.aspx")
      .cookie(COOKIE_NAME, loginToken).method(Method.GET).timeout(25000).followRedirects(false).execute()

    if (assignedMatchesResponse.statusCode == 302) {
      throw new SessionTimeoutException()
    }
    val assignedMatchesDoc = assignedMatchesResponse.parse
    val matchesElements = assignedMatchesDoc.select("div#divUppdrag").select("table.fogisInfoTable > tbody > tr").listIterator.asScala.drop(1)
    val upcomingAssignedMatches = matchesElements.map {
      el: Element =>
        AssignedMatch(dateTimeFormat.parseLocalDateTime(el.child(0).text),
          el.child(1).text,
          el.child(3).getElementsByTag("a").text,
          el.child(4).text,
          el.child(5).text,
          el.child(6).text,
          el.child(3).getElementsByTag("a").attr("href").split("=")(1))
    }
    upcomingAssignedMatches.toList
  }

  def scrapeMatchResult(fiksId:String, loginToken:String) = {
    val url = "https://fiks.fotball.no/Fogisdomarklient/Match/MatchResultat.aspx?matchId=%s".format(fiksId)
    val matchResultDocument = Jsoup.connect(url).cookie(COOKIE_NAME, loginToken).timeout(10000).get
    parseMatchResultDocument(fiksId:String, matchResultDocument)
  }

  def parseMatchResultDocument(fiksId:String, matchResultDocument:Document) = {
    val teams = matchResultDocument.select("span#lblMatchRubrik").text
    val matchId = matchResultDocument.select("span#lblMatchNr").text
    val resultTable = matchResultDocument.select("div#divMainContent")
    val finalHomeGoal = resultTable.select("input#tbSlutresultatHemmalag").`val`
    val finalAwayGoal = resultTable.select("input#tbSlutresultatBortalag").`val`
    val halfTimeHomeGoal = resultTable.select("input#tbHalvtidHemmalag").`val`
    val halfTimeAwayGoal = resultTable.select("input#tbHalvtidBortalag").`val`
    val attendance = resultTable.select("input#tbAntalAskadare").`val`.toInt;
    val protestHome = resultTable.select("input#ChkProtestHjemme").attr("checked") == "checked"
    val protestAway = resultTable.select("input#ChkProtestBorte").attr("checked") == "checked"

    val reportHistoryTable = matchResultDocument.select("table.fogisInfoTable > tbody > tr").asScala.drop(1)
    val resultReports = reportHistoryTable.map{
      el: Element => ResultReport(
        resultType(el.child(1).text),
        Score.fromString(el.child(2).text, el.child(3).text),
        el.child(0).child(0).attr("name") ,
        el.child(7).text
        )
      }

    MatchResult(fiksId, teams, matchId, finalScore = Score.toOption(finalHomeGoal, finalAwayGoal),
      halfTimeScore = Score.toOption(halfTimeHomeGoal, halfTimeAwayGoal), attendance=attendance,
      protestHomeTeam=protestHome, protestAwayTeam=protestAway, resultReports=resultReports.toSet)
  }

  def deleteMatchResult(fiksId:String, deletions:Set[ResultReport], loginToken:String){
    val url = "https://fiks.fotball.no/Fogisdomarklient/Match/MatchResultat.aspx?matchId=%s".format(fiksId)
    val matchResultForm = Jsoup.connect(url).cookie(COOKIE_NAME,loginToken).get
    val con = Jsoup.connect(url).cookie(COOKIE_NAME, loginToken).timeout(15000)
    deletions.foreach(r => con.data(r.reportId,"on"))
    con.data("hiddenFunktion","radera")
      .data("btnSpara","Lagre")
      .data("__VIEWSTATE",matchResultForm.getElementById("__VIEWSTATE").attr("value"))
      .data("__EVENTVALIDATION",matchResultForm.getElementById("__EVENTVALIDATION").attr("value"))
      .followRedirects(false)
    con.method(Method.POST).execute()
  }

  def postMatchResult(matchResult: MatchResult, loginToken:String){
    val url = "https://fiks.fotball.no/Fogisdomarklient/Match/MatchResultat.aspx?matchId=%s".format(matchResult.fiksId)
    val matchResultForm = Jsoup.connect(url).cookie(COOKIE_NAME,loginToken).get

    val con = Jsoup.connect(url).cookie(COOKIE_NAME, loginToken).timeout(25000)
    matchResult.halfTimeScore.foreach(x => con.data("tbHalvtidHemmalag", x.home.toString)) //XXX: Fixit!!
    matchResult.halfTimeScore.foreach(x=> con.data("tbHalvtidBortalag", x.away.toString))
    matchResult.finalScore.foreach(x => con.data("tbSlutresultatHemmalag", x.home.toString))
    matchResult.finalScore.foreach(x => con.data("tbSlutresultatBortalag", x.away.toString))
    con.data("tbAntalAskadare",matchResult.attendance.toString)
    .data("btnSpara","Lagre")
    .data("__VIEWSTATE",matchResultForm.getElementById("__VIEWSTATE").attr("value"))
    .data("__EVENTVALIDATION",matchResultForm.getElementById("__EVENTVALIDATION").attr("value"))
    .followRedirects(false)
    con.method(Method.POST).execute()
  }


  def postInterestForm(availabilityId: String, comment:String, loginToken:String) {
    val url = "https://fiks.fotball.no/Fogisdomarklient/Uppdrag/UppdragLedigtUppdrag.aspx?domaruppdragId=" + availabilityId
    val reportInterestForm = Jsoup.connect(url).cookie(COOKIE_NAME,loginToken).get
    val viewstate = reportInterestForm.getElementById("__VIEWSTATE").attr("value")
    val eventvalidation = reportInterestForm.getElementById("__EVENTVALIDATION").attr("value")
    val response = Jsoup.connect(url)
      .method(Method.POST)
      .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_3) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.56 Safari/535.11")
      .data("btnAnmal","Meld inn")
      .data("tbKommentar",comment)
      .data("__VIEWSTATE",viewstate)
      .data("__EVENTVALIDATION",eventvalidation)
      .referrer(url)
      .cookie(COOKIE_NAME,loginToken).followRedirects(false).timeout(25000).execute()
  }

  private def stringToOptionOfInt(s:String):Option[Int] = {
    if(s.trim.isEmpty) None else Some(s.toInt)
  }

  import ResultType._
  private def resultType(scrapedString:String):ResultType = {
    scrapedString match{
      case "Pauseresultat" => HalfTime
      case "Sluttresultat" => FinalResult
      case _ => Undefined
    }
  }

}
