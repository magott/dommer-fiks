package no.magott.fiks.vacation

import no.magott.fiks.user.UserSession
import org.jsoup.Jsoup
import org.scalatest.{Matchers, FlatSpec}

/**
 *
 */
import org.scalatest.OptionValues._
class VacationScraperTest extends FlatSpec with Matchers{

  "Vacation table" should "be parsed" in {
    val doc = Jsoup.parse(getClass.getResourceAsStream("/vacation_page.html"), "UTF-8","")
    val vacations: List[Vacation] = new VacationScraper().parseVacation(doc)
    vacations should not be(empty)
    vacations.exists(_.reason.isDefined) should be (true)
  }

  "Add Vacation reponse" should "be parsed" in {
    val doc = Jsoup.parse(getClass.getResourceAsStream("/vacation_add_response.html"), "UTF-8","")
    val errorOpt = new VacationScraper().parseAddVacationResponse(doc)
    errorOpt.value should be("Ferien kunne ikke lagres, da de kolliderer med dine oppdrag")
  }

}
