package no.magott.fiks.invoice

import no.magott.fiks.data.MongoSetting
import scala.util.Properties
import org.joda.time.{DateTime}
import com.mongodb.casbah.query.Imports._
import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers


/**
 * Created by morten on 31/03/14.
 */
class InvoiceRepository {
  RegisterJodaTimeConversionHelpers()
  val MongoSetting(db) = Properties.envOrNone("MONGOLAB_URI")
  val where = MongoDBObject

  def getInvoice(id:String) = {
    db("invoice").findOne(where("_id" -> new ObjectId(id))).map(Invoice.fromMongo)
  }

  def findInvoice(username:String, fiksId:String) = {
    db("invoice").findOne(where("username"->username, "matchData.fiksId"->fiksId)).map(Invoice.fromMongo)
  }

  def findInvoicesForUser(username:String) = {
    db("invoice").find(where("username" -> username)).map(Invoice.fromMongo)
  }

  def saveInvoice(invoice:Invoice) = {
    db("invoice").update(q = invoice.updateClause, o = invoice.toMongo, upsert = true, multi = false)
    findInvoice(invoice.username, invoice.matchData.fiksId).map(_.id.get)
  }

  def reminderSent(invoiceId:String, date:DateTime) = {
    db("invoice").update(where("_id"->new ObjectId(invoiceId)), $set(Seq("reminder" -> date))).getN == 1
  }

  def unsetReminder(invoiceId:String) = {
    db("invoice").update(where("_id"->new ObjectId(invoiceId)), $unset(Seq("reminder"))).getN == 1
  }

  def invoiceSettled(invoiceId:String, date:DateTime) = {
    db("invoice").update(where("_id"->new ObjectId(invoiceId)), $set(Seq("settled" -> date))).getN == 1
  }

  def unsetSettled(invoiceId:String) = {
    db("invoice").update(where("_id"->new ObjectId(invoiceId)), $unset(Seq("settled"))).getN == 1
  }

}
