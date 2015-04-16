package no.magott.fiks.invoice

import no.magott.fiks.invoice.Invoice.PassengerAllowance
import org.scalatest.{Matchers, FlatSpec, FunSuite}
import org.joda.time.DateTime

/**
 * Created by morten on 09/04/14.
 */
class InvoiceTest extends FlatSpec with Matchers{

  "Total amount" should "be equal to sum of parts" in {
    val invoice = createInvoice(DateTime.now)
    import invoice._
    val sum  = matchFee + (km.getOrElse(0d) * 4.10) + toll.getOrElse(0d) + perDiem.get + passengerAllowance.map(_.getTotal).getOrElse(0d) + otherExpenses.getOrElse(0d)
    sum shouldBe total
  }

  "Kilometer calculations" should "use date sensitive km multiplier" in {
    val invoice = createInvoice(DateTime.now.withYear(2014))
    import invoice._
    val sum  = matchFee + (km.getOrElse(0d) * 4.05) + toll.getOrElse(0d) + perDiem.get + passengerAllowance.map(_.getTotal).getOrElse(0d) + otherExpenses.getOrElse(0d)
    sum shouldBe total
  }

  def createInvoice(kickoff: DateTime) = {
    val matchData = MatchData("123", "031000000", "Blåbær", "Øvre Årdal", "Hjemmebanen kunstgress", "3 div avd 01", kickoff)
    val invoice = Invoice(None, "morten.andersen.gott", matchData,
      900, Some(77.5), Some(280), None, None, Some(10), Some(10), Some(PassengerAllowance(2,2)))
    invoice
  }

}
