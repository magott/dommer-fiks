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

  def deleteInvoice(id: String) = db("invoice").remove((where("_id" -> new ObjectId(id))))

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
    db("invoice").find(where("username" -> username)).sort(MongoDBObject("matchData.date"-> -1)).map(Invoice.fromMongo).toSeq
  }

  def all = db("invoice").find().map(Invoice.fromMongo).toList

  def saveInvoice(invoice:Invoice) = {
    if(invoice.isNew){
      db("invoice").save(invoice.asMongoInsert)
    }else{
      db("invoice").update(q = invoice.updateClause, o = invoice.asMongoUpdate, upsert = false, multi = false)
    }
    findInvoice(invoice.username, invoice.matchData.fiksId).map(_.id.get)
  }

  def reminderSent(invoiceId:String, date:DateTime) = {
    db("invoice").update(where("_id"->new ObjectId(invoiceId)), $set("reminder" -> date)).getN == 1
  }

  def unsetReminder(invoiceId:String) = {
    db("invoice").update(where("_id"->new ObjectId(invoiceId)), $unset("reminder")).getN == 1
  }

  def invoiceSettled(invoiceId:String, date:DateTime) = {
    db("invoice").update(where("_id"->new ObjectId(invoiceId)), $set("settled" -> date)).getN == 1
  }

  def unsetSettled(invoiceId:String) = {
    db("invoice").update(where("_id"->new ObjectId(invoiceId)), $unset("settled")).getN == 1
  }

}
