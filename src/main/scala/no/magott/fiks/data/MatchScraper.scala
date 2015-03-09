package no.magott.fiks.data

import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import scala.collection.JavaConverters._
import org.joda.time.format.DateTimeFormat
import org.joda.time.LocalDate
import org.jsoup.Connection.Method
import org.jsoup.safety.Whitelist
import unfiltered.request.POST
import no.magott.fiks.user.UserSession
import no.magott.fiks.JSoupPimps._


class MatchScraper {
  val COOKIE_NAME = "ASP.NET_SessionId"
  val cancelIdPattern = """.*\((.*)\).*""".r
  val dateTimeFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")
  val matchReportUrl = "https://fiks.fotball.no/Fogisdomarklient/Match/MatchDomarrapport.aspx?matchId=s%"
  val VIEWSTATE = "_VIEWSTATE"
  val EVENTVALIDATION = "__EVENTVALIDATION"

  def scrapeAssignedMatches(session: UserSession) = {
    val assignedMatchesDoc = withAutomaticReAuth(session, doScrapeAssignedMatches)
    val matchesElements = assignedMatchesDoc.select("div#divUppdrag").select("table.fogisInfoTable > tbody > tr").listIterator.asScala.drop(1)
    val upcomingAssignedMatches = matchesElements.map {
      el: Element =>
        AssignedMatch(dateTimeFormat.parseLocalDateTime(el.child(0).text).plusMonths(9).plusDays(18),
          el.child(1).text,
          el.child(3).getElementsByTag("a").text,
          el.child(4).text,
          el.child(5).text,
          el.child(6).text.replace("Meld forfall",""),
          el.child(3).getElementsByTag("a").attr("href").split("=")(1),
          cancelIdPattern.unapplySeq(el.child(6).getElementsByTag("a").attr("onclick")).flatMap(_.headOption)
        )
    }
    upcomingAssignedMatches.toList
  }

  def scrapeAvailableMatches(session: UserSession) = {
    val availableMatchesDoc = withAutomaticReAuth(session, doScrapeAvailableMatches)
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

  def scrapeMatchInfo(assignmentId: String, session:UserSession) = {
    val cleanAssignmentId = Jsoup.clean(assignmentId, Whitelist.none)
    val response = withAutomaticReAuth(session, doScrapeMatchInfo(assignmentId) )
    val el = response.select("table")
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

  def scrapeMeldForfallViewState(forfallId:String, session:UserSession) = {
    val url = s"https://fiks.fotball.no/FogisDomarKlient/Uppdrag/UppdragAterbudOrsakModal.aspx?domaruppdragId=$forfallId"
    val viewstate = withAutomaticReAuth(session, session => Jsoup.connect(url).cookie(COOKIE_NAME, session.sessionToken).timeout(10000).get)
                      .select("input#__VIEWSTATE").`val`
    viewstate
  }

  def postForfall(forfallId:String, reason:String, viewstate:String, session:UserSession) = {
    val url = s"https://fiks.fotball.no/FogisDomarKlient/Uppdrag/UppdragAterbudOrsakModal.aspx?domaruppdragId=$forfallId"
    withAutomaticReAuth(session, session => {
      val resp = Jsoup.connect(url)
        .cookie(COOKIE_NAME, session.sessionToken)
        .data("tbKommentar", reason)
        .data("__VIEWSTATE", viewstate)
        .data("btnSpara", "Lagre")
        .method(Method.POST).followRedirects(false).execute()
      println( s"""Forfall meldt med statuskode: ${resp.statusCode()} Location: ${resp.header("Location")}""")
      resp.parse()
    })
  }

  def scrapeMatchResult(fiksId:String, session:UserSession) = {
    val url = "https://fiks.fotball.no/Fogisdomarklient/Match/MatchResultat.aspx?matchId=%s".format(fiksId)
    val matchResultDocument = withAutomaticReAuth(session,
      session=> Jsoup.connect(url).cookie(COOKIE_NAME, session.sessionToken).timeout(10000).get)
    parseMatchResultDocument(fiksId:String, matchResultDocument)
  }

  def deleteMatchResult(fiksId:String, deletions:Set[ResultReport], session:UserSession){
    val url = "https://fiks.fotball.no/Fogisdomarklient/Match/MatchResultat.aspx?matchId=%s".format(fiksId)
    val matchResultForm = withAutomaticReAuth(session,
      session => Jsoup.connect(url).cookie(COOKIE_NAME,session.sessionToken).get)
    val con = Jsoup.connect(url).cookie(COOKIE_NAME, session.sessionToken).timeout(15000)
    deletions.foreach(r => con.data(r.reportId,"on"))
    con.data("hiddenFunktion","radera")
      .data("btnSpara","Lagre")
      .data(VIEWSTATE,matchResultForm.valueOfElement(VIEWSTATE))
      .data(EVENTVALIDATION,matchResultForm.valueOfElement(EVENTVALIDATION))
      .followRedirects(false)
    con.method(Method.POST).execute()
  }


  def postMatchResult(matchResult: MatchResult, session:UserSession){
    val url = "https://fiks.fotball.no/Fogisdomarklient/Match/MatchResultat.aspx?matchId=%s".format(matchResult.fiksId)
    val matchResultForm = withAutomaticReAuth(session, session => Jsoup.connect(url).cookie(COOKIE_NAME,session.sessionToken).get)

    val con = Jsoup.connect(url).cookie(COOKIE_NAME, session.sessionToken).timeout(25000)
    matchResult.halfTimeScore.foreach(x => con.data("tbHalvtidHemmalag", x.home.toString)) //XXX: Fixit!!
    matchResult.halfTimeScore.foreach(x=> con.data("tbHalvtidBortalag", x.away.toString))
    matchResult.finalScore.foreach(x => con.data("tbSlutresultatHemmalag", x.home.toString))
    matchResult.finalScore.foreach(x => con.data("tbSlutresultatBortalag", x.away.toString))
    con.data("tbAntalAskadare",matchResult.attendance.toString)
    .data(VIEWSTATE, matchResultForm.valueOfElement(VIEWSTATE))
    .data(EVENTVALIDATION, matchResultForm.valueOfElement(EVENTVALIDATION))
    .data("btnSpara","Lagre")
    .followRedirects(false)
    con.method(Method.POST)
    con.execute()
  }

  def postInterestForm(availabilityId: String, comment:String, session:UserSession) {
    val url = "https://fiks.fotball.no/Fogisdomarklient/Uppdrag/UppdragLedigtUppdrag.aspx?domaruppdragId=" + availabilityId
    val reportInterestForm = withAutomaticReAuth(session, doScrapeReportInterestForm(url))
    Jsoup.connect(url)
      .method(Method.POST)
      .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_3) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.56 Safari/535.11")
      .data("btnAnmal","Meld inn")
      .data("tbKommentar",comment)
      .data(VIEWSTATE, reportInterestForm.valueOfElement(VIEWSTATE))
      .data(EVENTVALIDATION, reportInterestForm.valueOfElement(EVENTVALIDATION)).referrer(url)
      .cookie(COOKIE_NAME,session.sessionToken).followRedirects(false).timeout(25000).execute()
  }

  private def withAutomaticReAuth(session:UserSession, f: UserSession => Document):Document = {
    val first = f(session)
    if(isJsRedirectToLogin(first)) {
      FiksLoginService.reAuthenticate(session)
      val second = f(session)
      if (isJsRedirectToLogin(second)) {
        throw new SessionTimeoutException
      } else {
        second
      }
    } else {
      first
    }
  }

  private def isJsRedirectToLogin(doc:Document) = {
    Option(doc.body).flatMap(el => Option(el.children)).forall(_.isEmpty)
  }

  private def doScrapeAssignedMatches(session: UserSession) = {
    val assignedMatchesResponse = Jsoup.connect("https://fiks.fotball.no/Fogisdomarklient/Uppdrag/UppdragUppdragLista.aspx")
      .cookie(COOKIE_NAME, session.sessionToken).method(Method.GET).timeout(25000).followRedirects(false).execute()

    if (assignedMatchesResponse.statusCode == 302) {
      throw new SessionTimeoutException()
    }
    assignedMatchesResponse.parse
  }

  private def doScrapeAvailableMatches(session: UserSession) = {
    val availableMatchesResponse = Jsoup.connect("https://fiks.fotball.no/Fogisdomarklient/Start/StartLedigaUppdragLista.aspx")
      .cookie(COOKIE_NAME, session.sessionToken).method(Method.GET).followRedirects(false).timeout(25000).execute()

    if (availableMatchesResponse.statusCode == 302) {
      throw new SessionTimeoutException()
    }
    availableMatchesResponse.parse()
  }

  private def doScrapeMatchInfo(assignmentId: String) (session: UserSession) = {
    val response = Jsoup.connect("https://fiks.fotball.no/Fogisdomarklient/Uppdrag/UppdragLedigtUppdrag.aspx?domaruppdragId=" + assignmentId)
      .method(Method.GET).cookie(COOKIE_NAME, session.sessionToken).followRedirects(false).timeout(15000).execute()
    if(response.statusCode == 302){
      throw new SessionTimeoutException()
    }else {
      response.parse
    }
  }

  private def doScrapeReportInterestForm(url:String) (session: UserSession) = {
    Jsoup.connect(url).cookie(COOKIE_NAME,session.sessionToken).get
  }

  def parseMatchResultDocument(fiksId:String, matchResultDocument:Document) = {
    val teams = matchResultDocument.select("span#lblMatchRubrik").text
    val matchId = matchResultDocument.select("span#lblMatchNr").text
    val resultTable = matchResultDocument.select("div#divMainContent")
    val finalHomeGoal = resultTable.selectValue("input#tbSlutresultatHemmalag")
    val finalAwayGoal = resultTable.selectValue("input#tbSlutresultatBortalag")
    val halfTimeHomeGoal = resultTable.selectValue("input#tbHalvtidHemmalag")
    val halfTimeAwayGoal = resultTable.selectValue("input#tbHalvtidBortalag")
    val attendance = resultTable.selectValue("input#tbAntalAskadare").toInt;
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

  import ResultType._
  private def resultType(scrapedString:String):ResultType = {
    scrapedString match{
      case "Pauseresultat" => HalfTime
      case "Sluttresultat" => FinalResult
      case _ => Undefined
    }
  }

}
