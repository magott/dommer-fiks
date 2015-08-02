package no.magott.fiks.message

import no.magott.fiks.FiksScraper
import no.magott.fiks.data.{ScrapeError, ScrapeTimeout}
import no.magott.fiks.user.UserSession
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import scala.collection.JavaConverters._
import scalaz.\/
import scalaz.syntax.id._

/**
 *
 */
class MessageScraper extends FiksScraper{

  val messageDateTimeFormat = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm:ss")  //03.07.2015 10:51:21

  def scrapeMessages(userSession: UserSession) : ScrapeError \/ List[Message] = {
    withAutomaticReAuth(userSession, doScrapeMessages).map(parseMessages)
  }

  private def doScrapeMessages(userSession: UserSession) : Document = {
    Jsoup.connect("https://fiks.fotball.no/FogisDomarKlient/Start/StartMeddelandeLista.aspx").cookie(COOKIE_NAME, userSession.sessionToken).timeout(25000).get()
  }
  private[message] def parseMessages(doc: Document) = {
    doc.select("div#divMeddelanden").iterator.asScala.map { el =>
      val author = el.select("span[id$=lblPersonNamn]").text
      val timestamp = LocalDateTime.parse(el.select("span.datumGra").text, messageDateTimeFormat)
      val title = el.select("span[id$=lblRubrik]").text()
      val body = el.select("span[id$=lblMeddelande]").html()
      Message(author, timestamp, title, body)
    }.toList
  }
}
