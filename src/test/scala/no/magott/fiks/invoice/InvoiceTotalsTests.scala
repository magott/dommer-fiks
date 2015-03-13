package no.magott.fiks.invoice

import org.scalatest.FunSuite
import org.joda.time.DateTime

/**
 * Created by morten on 09/04/14.
 */
class InvoiceTotalsTests extends FunSuite{

  test("Settled amount increases when adding settled invoice"){
    val totals = InvoiceTotals.empty + invoiceWith(200, Some(DateTime.now))
    assert(totals.total == 200)
    assert(totals.settled == 200)
    assert(totals.outstanding == 0)
  }

  test("Outstanding amount increases when adding settled invoice"){
    val totals = InvoiceTotals.empty + invoiceWith(200, None)
    assert(totals.total == 200)
    assert(totals.outstanding == 200)
    assert(totals.settled == 0)
  }

  test("Add multiple invoices totals, settled and outstanding sums up"){
    val totals = InvoiceTotals.empty + invoiceWith(200, None) + invoiceWith(400, None) + invoiceWith(500, Some(DateTime.now))
    assert(totals.total == 1100)
    assert(totals.outstanding == 600)
    assert(totals.settled == 500)
  }


  def invoiceWith(total:Double, settled:Option[DateTime]) = {
    Invoice(None, "foo", MatchData("fiksId", "matchId", "Dummy home", "dummy way", "dummy venue", "dummy tournament", DateTime.now), total.round.toInt, None, None, None, total, None, settled, None, None)
  }

}
