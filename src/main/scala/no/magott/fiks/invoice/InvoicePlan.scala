package no.magott.fiks.invoice

import no.magott.fiks.invoice.Invoice.PassengerAllowance
import unfiltered.filter.Plan
import unfiltered.request._
import unfiltered.response._
import no.magott.fiks.{ExportParameter, ActionParameter, HerokuRedirect, MatchIdParameter}
import no.magott.fiks.data.{AssignedMatch, MatchService, Pages, SessionId}
import no.magott.fiks.user.UserService
import unfiltered.response.ResponseString
import org.joda.time.{LocalDate, DateTime}
import scalaz._, Scalaz._
import argonaut._, Argonaut._
/**
 * Created by morten on 31/03/14.
 */
class InvoicePlan(matchService:MatchService, userService:UserService, invoiceRepository:InvoiceRepository) extends Plan{


  override def intent: Plan.Intent = {
    case req@SessionId(token) => {
      val session = userService.userSession(token).get //TODO: YOLO
      req match {
        //New invoice
        case Path( Seg( "invoice" :: "new" :: Nil)) & Params(MatchIdParameter(matchId)) => req match{
          case GET(_) => {
            val matchOpt = matchService.assignedMatches(session).find(_.fiksId == matchId)
            if (matchOpt.isDefined) {
              Ok ~> Html5(Pages(req).invoiceInfoPage(None, matchOpt))
            }else{
              Forbidden ~> Html5(Pages(req).forbidden)
            }
          }
          case POST(_) => {
            val matchOpt = matchService.assignedMatches(session).find(_.fiksId == matchId)
            if(matchOpt.isDefined){
              val invoice = extractNewInvoiceFromParams(session.username, matchOpt.get, Params.unapply(req).get)
              val id = invoiceRepository.saveInvoice(invoice).getOrElse("")
              HerokuRedirect(req, s"/invoice/${id.toString}")
            }else{
              Forbidden ~> Html5(Pages(req).forbidden)
            }
          }
        }

        //Existing invoice
        case Path( Seg( "invoice" :: id :: Nil) ) => {
          val invoiceOpt= invoiceRepository.getInvoice(id)
          req match{
            case Params(ActionParameter("reminder")) if(invoiceOpt.isDefined) => req match {
              case POST(_)  => {
                val ok = invoiceRepository.reminderSent(id, DateTime.now)
                if(ok) Ok ~> JsonContent ~> ResponseString(Invoice.remindedJson)
                else InternalServerError
              }
              case DELETE(_) => {
                val ok = invoiceRepository.unsetReminder(id)
                if(ok)Ok ~> JsonContent ~> ResponseString(Invoice.notRemindedJson)
                else InternalServerError
              }
            }
            case Params(ActionParameter("settled")) if(invoiceOpt.isDefined) => req match {
              case POST(_) => {
                val ok = invoiceRepository.invoiceSettled(id, DateTime.now)
                if(ok) Ok ~> JsonContent ~> ResponseString(Invoice.settledJson)
                else InternalServerError
              }
              case DELETE(_) => {
                val ok = invoiceRepository.unsetSettled(id)
                if(ok) Ok ~> JsonContent ~> ResponseString(Invoice.unsettledJson)
                else InternalServerError
              }
            }
            case Params(ExportParameter(xslxFormat)) => xslxFormat match {
              case "nffbredde" => {
                val userOpt = userService.byUsername(session.username)
                val invoice = InvoiceGenerator.generateNffBreddeInvoice(invoiceOpt.get, userOpt)
                Ok ~> ContentDisposition(s"""filename="${invoiceOpt.get.matchData.teams}.xlsx"""") ~> XslxResponse(invoice)
              }
              case "nfftopp" => {
                val userOpt = userService.byUsername(session.username)
                val invoice = InvoiceGenerator.generateNffToppInvoice(invoiceOpt.get, userOpt)
                Ok ~> ContentDisposition(s"""filename="${invoiceOpt.get.matchData.teams}.xlsx"""") ~> XslxResponse(invoice)
              }
              case "ofk" => {
                val userOpt = userService.byUsername(session.username)
                val invoice = InvoiceGenerator.generateOfkInvoice(invoiceOpt.get, userOpt)
                Ok ~> ContentDisposition(s"""filename="${invoiceOpt.get.matchData.teams}.xlsx"""") ~> XslxResponse(invoice)
              }
              case _ => BadRequest ~> Html5(Pages(req).error(<div>Ukjent regningstype</div>))
            }
            case GET(_) => {
              if (invoiceOpt.isEmpty) NotFound ~> Html5(Pages(req).notFound)
              else if (invoiceOpt.exists(_.username == session.username)) {
                val matchOpt = matchService.assignedMatches(session).find(_.fiksId == invoiceOpt.get.matchData.fiksId)
                Ok ~> Html5(Pages(req).invoiceInfoPage(invoiceOpt, matchOpt))
              } else {
                Forbidden ~> Html5(Pages(req).forbidden)
              }
            }
            case DELETE(_) => {
              if(invoiceOpt.isEmpty) NotFound ~> Html5(Pages(req).notFound)
              else if(invoiceOpt.exists(_.username == session.username)){
                invoiceRepository.deleteInvoice(id)
                NoContent
              }else{
                Forbidden ~> Html5(Pages(req).forbidden)
              }
            }
            case POST(_) & Params(p)=> {
              if(invoiceOpt.isEmpty) NotFound ~> Html5(Pages(req).notFound)
              else if(invoiceOpt.exists(_.username == session.username)){
                val updatedInvoice = extractUpdatedInvoiceFromParams(session.username, invoiceOpt.get, p)
                invoiceRepository.saveInvoice(updatedInvoice)
                Ok ~> HerokuRedirect(req, s"/invoice/${id}")
              }else{
                Forbidden ~> Html5(Pages(req).forbidden)
              }
            }
           case DELETE(_) => {
             if(invoiceOpt.isEmpty) NotFound
             else if(invoiceOpt.exists(_.username == session.username)){
               invoiceRepository.deleteInvoice(id)
               Ok ~> ResponseString("""{"location": "/invoice/" """)
             }else{
               Forbidden
             }
           }
          }
        }
        case Path(Seg("invoice" :: Nil)) => req match {
          case GET(_) => {
            req match {
              case Accepts.Json(_) =>{
                val year = YearParam.unapply(Params.unapply(req).get).map(_.toInt).getOrElse(LocalDate.now.getYear)
                val invoices = invoiceRepository.findInvoicesForUser(session.username).filter(_.matchData.date.getYear == year)
                val totals = invoices.foldLeft(InvoiceTotals.empty)(_+_)
                Ok ~> JsonContent ~> ResponseString(invoices.toList.map(_.toJson).jencode.nospaces)
              }
              case _ => Ok ~> Html5(Pages(req).invoiceSAP)
            }
          }
        }
        case _ => Pass
      }
    }
  }
  def extractNewInvoiceFromParams(username:String, m:AssignedMatch, params: Map[String, Seq[String]]) = {
    import no.magott.fiks.ParameterImplicits._
    val matchFee = params("matchFee").head.toInt
    val toll = params("toll").headOption.filter(_.trim.nonEmpty).map(_.toDouble)
    val km = params("km").headOption.filter(_.trim.nonEmpty).map(_.toDouble)
    val otherExpenses = params("otherExpenses").headOption.filter(_.trim.nonEmpty).map(_.toDouble)
    val perDiem = params("perDiem").headOption.filter(_.trim.nonEmpty).map(_.toInt)
    val pass = params.valueOrNone("passengers").map(_.toInt)
    val passKm = params.valueOrNone("passengerKm").map(_.toDouble)
    val passengerAllowance = PassengerAllowance.fromWeb(pass, passKm)
    Invoice.createNew(username, MatchData.fromAssignedMatch(m),matchFee, toll, perDiem, km, otherExpenses, passengerAllowance)
  }

  def extractUpdatedInvoiceFromParams(username:String, invoice:Invoice, params: Map[String, Seq[String]]) = {
    import no.magott.fiks.ParameterImplicits._
    val matchFee = params("matchFee").head.toInt
    val toll = params("toll").headOption.filter(_.trim.nonEmpty).map(_.toDouble)
    val perDiem = params("perDiem").headOption.filter(_.trim.nonEmpty).map(_.toInt)
    val km = params("km").headOption.filter(_.trim.nonEmpty).map(_.toDouble)
    val otherExpenses = params("otherExpenses").headOption.filter(_.trim.nonEmpty).map(_.toDouble)
    val pass = params.valueOrNone("passengers").map(_.toInt)
    val passKm = params.valueOrNone("passengerKm").map(_.toDouble)
    val passengerAllowance = PassengerAllowance.fromWeb(pass, passKm)
    invoice.copy(matchFee = matchFee, toll = toll, perDiem = perDiem, km = km, otherExpenses = otherExpenses, passengerAllowance = passengerAllowance)
  }

  object YearParam extends Params.Extract("year", Params.first)

  object ContentDisposition extends HeaderName("Content-Disposition")

}
