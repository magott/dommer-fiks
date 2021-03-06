package no.magott.fiks.invoice

import java.io.FileOutputStream

import no.magott.fiks.user.{InvoiceData, User}
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.xssf.usermodel.{XSSFCell, XSSFSheet, XSSFWorkbook}

import scala.io.Source
import scala.collection.JavaConverters._

/**
 *
 */
object InvoiceGenerator {
  import XlsxImplicits._

  def generateOfkInvoice(invoice:Invoice, userOpt: Option[User]) = withTemplate("/Dommerregning-2015-OFK_Gjermshus.xlsx"){template =>
    applyToOfkSpreadsheet(template, invoice, userOpt)
    template
  }
  def generateNffToppInvoice(invoice:Invoice, userOpt: Option[User]) =
    withTemplate("/Dommerregning-reise_topp_2015_rev3.xlsx"){ template =>
      applyCommonNffHeaders(template, invoice, userOpt)
      applyToNffSpreadsheet(template, invoice, userOpt)(0)
      template.getSheetAt(0)(18)('F').setDouble(invoice.km) //No km formula in NFF Topp
      template
  }

  def generateNffBreddeInvoice(invoice: Invoice, userOpt: Option[User]) =
    withTemplate("/Dommerregning-reise_bredde-2015.xlsx"){ template =>
      applyCommonNffHeaders(template, invoice, userOpt)
      applyToNffSpreadsheet(template, invoice, userOpt)(0)
      template
  }

  def generateTromsoBreddeInvoice(invoice:Invoice, userOpt:Option[User]) =
    withTemplate("/Dommerregning-reise_bredde-2015_tromsoformula.xlsx"){ template =>
      applyCommonNffHeaders(template, invoice, userOpt)
      applyToTromsoBredde(template, invoice, userOpt)
      applyToNffSpreadsheet(template, invoice, userOpt)(1)
      template
  }

  private def applyCommonNffHeaders(template: XSSFWorkbook, invoice: Invoice, userOpt:Option[User]) = {
    val sheet = template.getSheetAt(0)
    userOpt.foreach { user =>
      sheet(5)('B').setCellValue(user.fnrForInvoice)
      sheet(5)('D').setCellValue(user.nameForInvoice)
      sheet(5)('F').setCellValue(user.addressForInvoice)
      sheet(5)('H').setCellValue(user.zipForInvoice + " " + user.cityForInvoice)
      sheet(7)('B').setCellValue(user.muncipalForInvoice)
      sheet(7)('H').setCellValue(user.accountForInvoice)
      sheet(9)('D').setCellValue(user.email)
      sheet(10)('D').setCellValue(invoice.matchData.matchId + " " + invoice.matchData.teams)
      if (invoice.millageAllowance.isDefined) {
        sheet(14)('B').setString(userOpt.map(_.addressForInvoice))
        sheet(14)('D').setCellValue(invoice.matchData.venue)
        sheet(15)('B').setCellValue(invoice.matchData.venue)
        sheet(15)('D').setString(userOpt.map(_.addressForInvoice))
      }
      sheet(14)('F').setDouble(invoice.km.map(_ / 2))
      sheet(15)('F').setDouble(invoice.km.map(_ / 2))
    }
  }

  private def applyToNffSpreadsheet(template: XSSFWorkbook, invoice: Invoice, userOpt: Option[User])(rowOffset:Int = 0) = {
    val sheet = template.getSheetAt(0)
    invoice.passengerAllowance.foreach{ passengerAllowance =>
      import passengerAllowance._
      sheet(19+rowOffset)('F').setCellValue(km)
      if(pax > 1){
        val passengerRow = 19 + rowOffset
        sheet(passengerRow)('G').setCellValue(s"x $pax,00")
        sheet(passengerRow)('H').setCellFormula(s"F$passengerRow*$pax")
      }
    }
    invoice.toll.foreach { toll =>
      sheet(44+rowOffset)('D').setCellValue("Bompenger")
      sheet(44+rowOffset)('F').setCellValue(toll)
    }

    invoice.otherExpenses.foreach{ otherExpenses =>
      val row = 44 + (if(invoice.toll.isDefined) 1 else 0)
      sheet(row+rowOffset)('F').setCellValue(otherExpenses)
    }

    invoice.perDiem.foreach { perDiem =>
      if (perDiem == 280) {
        sheet(25+rowOffset)('F').setCellValue(1)
      }
      if (perDiem == 520) {
        sheet(26+rowOffset)('F').setCellValue(1)
      }
    }
    sheet(49+rowOffset)('F').setCellValue(invoice.matchFee)
    sheet(53+rowOffset)('B').setCellValue(invoice.matchData.date.toDate)
    sheet(53+rowOffset)('D').setCellValue(userOpt.map(_.cityForInvoice).getOrElse(""))
    sheet(53+rowOffset)('F').setCellValue(userOpt.map(_.nameForInvoice).getOrElse(""))
  }

  private def applyToTromsoBredde(template: XSSFWorkbook, invoice: Invoice, userOpt:Option[User]) = {
    val sheet = template.getSheetAt(0)
    invoice.km.foreach{km =>
      sheet(18)('F').setCellValue(km)
      sheet(19)('F').setCellValue(km)
    }
  }

  private def applyToOfkSpreadsheet(template: XSSFWorkbook, invoice: Invoice, userOpt:Option[User]) = {
    val sheet = template.getSheetAt(0)
    sheet(8)('E').setCellValue(invoice.matchData.home)
    userOpt.foreach { user =>
      sheet(11)('E').setCellValue(user.nameForInvoice)
      sheet(11)('O').setCellValue(user.accountForInvoice)
      sheet(13)('E').setCellValue(user.addressForInvoice)
      sheet(13)('K').setCellValue(user.zipForInvoice)
      sheet(13)('O').setCellValue(user.cityForInvoice)
      sheet(15)('E').setCellValue(user.email)
      sheet(15)('O').setCellValue(user.fnrForInvoice)
      sheet(17)('E').setCellValue(user.phoneForInvoice)
      sheet(17)('O').setCellValue(user.muncipalForInvoice)
      sheet(25)('E').setCellValue(user.addressForInvoice)
      sheet(26)('L').setCellValue(user.addressForInvoice)
      sheet(37)('J').setCellValue(user.nameForInvoice)
    }
    sheet(20)('E').setCellValue(invoice.matchData.matchId)
    sheet(20)('O').setCellValue(invoice.matchData.date.toString("yyyy-MM-dd"))
    sheet(22)('E').setCellValue(invoice.matchData.home)
    sheet(22)('L').setCellValue(invoice.matchData.away)
    sheet(23)('E').setCellValue(invoice.matchData.venue)
    sheet(25)('L').setCellValue(invoice.matchData.venue)
    sheet(26)('E').setCellValue(invoice.matchData.venue)
    sheet(27)('J').setDouble(invoice.km)
    sheet(30)('H').setDouble(invoice.km)
    sheet(32)('H').setCellValue(invoice.matchFee)
    sheet(33)('H').setDouble(invoice.toll)
    sheet(34)('H').setDouble(invoice.otherExpenses)
    invoice.passengerAllowance.foreach{ passengerAllowance =>
      sheet(31)('H').setCellValue(passengerAllowance.km)
      sheet(31)('J').setCellValue(passengerAllowance.pax)
    }
    sheet(37)('C').setCellValue(invoice.matchData.dateString)
  }

  def withTemplate(filename:String)(f: XSSFWorkbook => XSSFWorkbook) = {
    val stream = getClass.getResourceAsStream(filename)
    val template = new XSSFWorkbook(stream)
    f(template)
    template.getCreationHelper().createFormulaEvaluator().evaluateAll()
    stream.close()
    template
  }


  object XlsxImplicits {

    implicit class RichSheet(underlying: XSSFSheet) {

      def apply(rowNum: Int)(cell: Char) = {
        val cellInt = cell.toLower.toInt - 'a'
        underlying.getRow(rowNum - 1).getCell(cellInt)
      }
    }

    implicit class RichCell(underlying: XSSFCell) {

      def setDouble(doubleOpt: Option[Double]) = {
        doubleOpt.foreach(underlying.setCellValue(_))
      }

      def setInt(intOpt: Option[Int]) = {
        intOpt.foreach(underlying.setCellValue(_))
      }

      def setString(stringOpt: Option[String]) = {
        stringOpt.foreach(underlying.setCellValue(_))
      }
    }
  }

}
