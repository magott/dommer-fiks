package no.magott.fiks.vacation

import no.magott.fiks.FiksScraper
import no.magott.fiks.data.{ReAuthRequired, ScrapeError, SessionTimeoutException, FiksLoginService}
import no.magott.fiks.user.UserSession
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import org.jsoup.Connection.Method
import org.jsoup.Jsoup
import org.jsoup.nodes.{Element, Document}
import scalaz._
import Scalaz._
import scala.collection.JavaConverters._
import scala.util.control.Exception._
import no.magott.fiks.JSoupPimps._
/**
 *
 */
class VacationScraper extends FiksScraper{

  def getVacationList(session: UserSession) = {
    val vacationDoc = withAutomaticReAuth(session, scrapeVacationList)
    vacationDoc.map(parseVacation)
  }

  def addVacation(session:UserSession, vacation: Vacation) = {
    val resp = withAutomaticReAuth(session, session => {
      val url = "https://fiks.fotball.no/FogisDomarKlient/Domare/DomareLedighet.aspx"
      val vacationFormDoc = Jsoup.connect(url).cookie(COOKIE_NAME, session.sessionToken).timeout(10000).get()
      val viewState = vacationFormDoc.valueOfElement(VIEWSTATE)
      val viewStateGenerator = vacationFormDoc.valueOfElement(VIEWSTATEGENERATOR)

      val request = Jsoup.connect(url).cookie(COOKIE_NAME, session.sessionToken).data(VIEWSTATE, viewState)
        .data(VIEWSTATEGENERATOR, viewStateGenerator)
        .data("__LASTFOCUS","").data("__EVENTARGUMENT","").data("__EVENTTARGET","") //Required garbage
        .data("DatumValjareStartdatum$tbDatum", vacation.fiksStartDateParam)
        .data("tbStarttid", vacation.fiksStartTimeParam)
        .data("DatumValjareSlutdatum$tbDatum", vacation.fiksEndDateParam)
        .data("tbSluttid", vacation.fiksEndTimeParam)
        .data("tbAnledning", vacation.reason.getOrElse(""))
        .data("btnSpara", "Lagre")
        .timeout(25000).method(Method.POST)
      println(request)
      val response = request.execute()
//      if(response.statusCode == 200) //This is JS-dialog error page, validation error
      response.parse()
    })
    resp.rightMap(parseAddVacationResponse) //TODO: How to handle conflict vs parse error
  }


  def scrapeVacationList(session: UserSession) : Document = {
    val url = "https://fiks.fotball.no/Fogisdomarklient/Domare/DomareLedighetLista.aspx"
    val req = Jsoup.connect(url)
      .method(Method.GET)
      .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_3) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.56 Safari/535.11")
      .cookie(COOKIE_NAME,session.sessionToken).followRedirects(false).timeout(25000)
    val response = req.execute()
    val doc = response.parse()
    doc
  }

  def deleteVacation(session:UserSession, vacationId:String) : Unit = {
    val url = s"https://fiks.fotball.no/Fogisdomarklient/Domare/DomareLedighetLista.aspx"
    val vacationForm = Jsoup.connect(url).cookie(COOKIE_NAME, session.sessionToken).get()
    val viewstate = vacationForm.valueOfElement(VIEWSTATE)
    val viewstateGenerator = vacationForm.valueOfElement(VIEWSTATEGENERATOR)
    val req = Jsoup.connect(url)
      .method(Method.POST)
      .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_3) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.56 Safari/535.11")
      .cookie(COOKIE_NAME, session.sessionToken).followRedirects(false).timeout(25000)
      .data("btnRadera","Slett")
      .data(s"cb_$vacationId", "on")
      .data(VIEWSTATE, viewstate)
      .data(VIEWSTATEGENERATOR, viewstateGenerator)
    val response = req.execute()
    response

  }

  private[vacation] def parseAddVacationResponse(doc:Document) = {
    val elements = doc.select("body > form > script[type=text/javascript]")
    val errorMessage = elements.iterator().asScala.toList.find(_.data.contains("meddelande")).map(js => js.data()).map(js => js.replaceAllLiterally("meddelande('","").replaceAllLiterally("')",""))
    errorMessage
  }
  private[vacation] def parseVacation(doc:Document) = {
    val vacations = doc.select("table.fogisInfoTable > tbody > tr")
      .listIterator.asScala.drop(1).map { el:Element =>
      val vacationId = el.child(1).getElementsByTag("a").attr("href").split("=")(1).takeWhile(_ != '&')
      Vacation(
        Some(vacationId.toLong),
        fiksDateFormat.parseLocalDateTime(el.child(1).text),
        fiksDateFormat.parseLocalDateTime(el.child(2).text),
        Option(el.child(3)).flatMap(reasonTd => allCatch.opt(reasonTd.child(0).attr("alt"))).filter(_.nonEmpty))
    }.toList
    vacations
  }

  private def withAutomaticReAuth(session: UserSession, f: UserSession => Document): ScrapeError \/ Document = {
    val first = f(session)
    if (isJsRedirectToLogin(first)) {
      FiksLoginService.reAuthenticate(session)
      val second = f(session)
      if (isJsRedirectToLogin(second)) {
        ReAuthRequired("Login required").left
      } else {
        second.right
      }
    } else {
      first.right
    }
  }

  private def isJsRedirectToLogin(doc:Document) = {
    Option(doc.body).flatMap(el => Option(el.children)).forall(_.isEmpty)
  }

}
