package no.magott.fiks.invoice

import java.io.FileOutputStream

import no.magott.fiks.invoice.Invoice.PassengerAllowance
import no.magott.fiks.user.{User, InvoiceData}
import org.joda.time.DateTime
import org.scalatest.{Matchers, FlatSpec}

/**
 *
 */
import InvoiceGenerator.XlsxImplicits._
class InvoiceGeneratorTest extends FlatSpec with Matchers{

  "NFF-bredde" should "be generated" in {
    val nffBreddeInvoice = InvoiceGenerator.generateNffBreddeInvoice(createInvoice, Some(createUser))
    val output = new FileOutputStream("/tmp/nffbredde.xlsx")
    //write changes
    nffBreddeInvoice.write(output)
    //close the stream
    output.close()
    val spreadsheetTotal = nffBreddeInvoice.getSheetAt(0)(51)('H').getNumericCellValue
    spreadsheetTotal shouldBe(createInvoice.calculateTotal)
  }

  "NFF-topp" should "be generated" in {
    val nffTopp = InvoiceGenerator.generateNffToppInvoice(createInvoice, Some(createUser))
    val output = new FileOutputStream("/tmp/nfftopp.xlsx")
    nffTopp.write(output)
    output.close()
    val spreadsheetTotal = nffTopp.getSheetAt(0)(53)('H').getNumericCellValue
    spreadsheetTotal shouldBe(createInvoice.calculateTotal)
  }

  "Tromsø" should "be generated" in {
    val tromsoInvoice = createInvoice.copy(kmAllowanceMunicipal = Some("tromsø"))
    val tromso = InvoiceGenerator.generateTromsoBreddeInvoice(tromsoInvoice, Some(createUser))
    val output = new FileOutputStream("/tmp/tromso.xlsx")
    tromso.write(output)
    output.close()
    val spreadsheetTotal = tromso.getSheetAt(0)(52)('H').getNumericCellValue
    println(tromsoInvoice.debugCalculationString)
    spreadsheetTotal shouldBe(tromsoInvoice.calculateTotal)
  }

  "OFK-bredde" should "be generated" in {
    val ofkbreddeInvoice = createInvoice
    val ofkBredde = InvoiceGenerator.generateOfkInvoice(ofkbreddeInvoice, Some(createUser))
    val output = new FileOutputStream("/tmp/ofkbredde.xlsx")
    ofkBredde.write(output)
    output.close()
    val spreadsheetTotal = ofkBredde.getSheetAt(0)(35)('R').getNumericCellValue
    spreadsheetTotal shouldBe(ofkbreddeInvoice.calculateTotal - ofkbreddeInvoice.perDiem.getOrElse(0))
  }

  def createUser = {
    val invoiceData = InvoiceData(Some("Morten Andersen-Gott"), Some("Adressen min 1A"), Some("1358"), Some("Jar"), Some("99887766"), Some("12345678910"), Some("Bærum"), None)
    val user = User("morten.andersen.gott", None, "fiks@andersen-gott.com", None, false, Some(invoiceData))
    user
  }

  def createInvoice = {
    val invoice = Invoice(None, "morten.andersen.gott", MatchData("123", "031000000", "Blåbær", "Øvre Årdal", "Hjemmebanen kunstgress", "3 div avd 01", DateTime.now), 900, Some(77.5), Some(280), None, None, Some(10), Some(10), Some(PassengerAllowance(2,2)), None)
    invoice
  }

}
