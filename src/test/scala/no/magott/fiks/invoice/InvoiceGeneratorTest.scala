package no.magott.fiks.invoice

import java.io.FileOutputStream

import no.magott.fiks.invoice.Invoice.PassengerAllowance
import no.magott.fiks.user.{User, InvoiceData}
import org.joda.time.DateTime
import org.scalatest.{Matchers, FlatSpec}

/**
 *
 */
class InvoiceGeneratorTest extends FlatSpec with Matchers{

  "NFF-bredde" should "be generated" in {
    val nffBreddeInvoice = InvoiceGenerator.generateNffBreddeInvoice(createInvoice, Some(createUser))
    val output =new FileOutputStream("/tmp/bredde.xlsx");
    //write changes
    nffBreddeInvoice.write(output)
    //close the stream
    output.close()
  }

  def createUser = {
    val invoiceData = InvoiceData(Some("Morten Andersen-Gott"), Some("Adressen min 1A"), Some("1358"), Some("Jar"), Some("99887766"), Some("12345678910"), Some("Bærum"))
    val user = User("morten.andersen.gott", None, "fiks@andersen-gott.com", None, false, Some(invoiceData))
    user
  }

  def createInvoice = {
    val invoice = Invoice(None, "morten.andersen.gott", MatchData("123", "031000000", "Blåbær", "Øvre Årdal", "Hjemmebanen kunstgress", "3 div avd 01", DateTime.now), 900, Some(77.5), Some(41.0), Some(280), 1000, None, None, Some(10), Some(4.1), Some(PassengerAllowance(2,2)))
    invoice
  }

}
