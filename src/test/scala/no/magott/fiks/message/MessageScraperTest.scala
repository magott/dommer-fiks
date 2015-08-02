package no.magott.fiks.message

import no.magott.fiks.data.MatchScraper
import org.jsoup.Jsoup
import org.scalatest.{Matchers, FlatSpec}

/**
 *
 */
class MessageScraperTest extends FlatSpec with Matchers{

  "MessageScraper" should "parse message html" in {
    val doc = Jsoup.parse(getClass.getResourceAsStream("/message_page.html"), "UTF-8","")
    val messages = new MessageScraper().parseMessages(doc)
    messages should have size 5
    messages.foreach(println)
  }

}
