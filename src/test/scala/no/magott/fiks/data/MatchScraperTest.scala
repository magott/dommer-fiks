package no.magott.fiks.data

import org.scalatest.FunSuite
import org.jsoup.Jsoup
import java.io.File

class MatchScraperTest extends FunSuite{

  val scraper = new MatchScraper

  test("Matchresult is parsed correctly"){
    val doc = Jsoup.parse(getClass.getResourceAsStream("/result_page.html"), "UTF-8","")
    val result:MatchResult = scraper.parseMatchResultDocument("123", doc)
    import result._
    assert(finalScore == Some(Score(7,2)), "Result does not match expectation")
    assert(resultReport(ResultType.FinalResult).isDefined)
    assert(resultReport(ResultType.FinalResult).get.reporter == "Sverre Lie Nordby")
    assert(resultReport(ResultType.HalfTime).isDefined)
  }

  test("Available matches is parsed correctly"){
    val doc = Jsoup.parse(getClass.getResourceAsStream("/availableMatches.html"), "UTF-8","")
    val matches: List[AvailableMatch] = scraper.parseAvailableMatches(doc)
    matches
  }


}
