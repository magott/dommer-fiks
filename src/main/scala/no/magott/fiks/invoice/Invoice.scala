package no.magott.fiks.invoice

import no.magott.fiks.invoice.Invoice.PassengerAllowance
import org.joda.time.{LocalDate, DateTime}
import com.mongodb.casbah.query.Imports._
import scala.collection.mutable
import scalaz._, Scalaz._
import argonaut._, Argonaut._

case class Invoice(id:Option[ObjectId], username:String, matchData:MatchData, matchFee: Int, toll:Option[Double], millageAllowance:Option[Double], perDiem: Option[Int], total: Double, reminder:Option[DateTime], settled:Option[DateTime], km:Option[Double], otherExpenses:Option[Double], passengerAllowance: Option[PassengerAllowance]) {

  def status = if(settled.isDefined) s"Betalt ${settled.get.toString("dd.MM")}" else if(reminder.isDefined) s"Purret ${reminder.get.toString("dd.MM")}" else "Utestående"
  def rowClass = if(settled.isDefined) "success" else if(moreDaysPassedThan(10)) "danger" else if(moreDaysPassedThan(7)) "warning" else if(moreDaysPassedThan(5)) "info" else ""

  def isNew = id.isEmpty

  private def asMap = {
    val map = mutable.Map[String, Any](
      "username" -> username,
      "matchData" -> matchData.toMongo,
      "matchFee" -> matchFee,
      "total" -> total
    )
    toll.foreach(x => map += "toll" -> x)
    millageAllowance.foreach(x => map += "millageAllowance" -> x)
    km.foreach(x => map += "km" -> x)
    otherExpenses.foreach(x => map += "otherExpenses" -> x)
    perDiem.foreach(x=> map += "perDiem" -> x)
    passengerAllowance.foreach(map += "passengerAllowance" -> _.toMongo)
    map
  }

  def asMongoUpdate:DBObject = {
    $set(Seq(asMap.toSeq:_*))
  }

  def asMongoInsert: DBObject = {
    MongoDBObject(asMap.toList:_*)
  }

  def calculateTotal = {
    matchFee + perDiem.getOrElse(0) + toll.getOrElse(0d) + otherExpenses.getOrElse(0d) + km.map(_ * 4.10).getOrElse(0d) + passengerAllowance.map(_.getTotal).getOrElse(0d)
  }

  def updateClause : MongoDBObject = MongoDBObject("_id" -> id.get)

  private def moreDaysPassedThan(days:Int) = matchData.date.plusDays(days).isBeforeNow

  def toJson: Json = {
    Json.obj(
      "id" := id.get.toString,
      "match" := matchData.toJson,
      "matchFee" := matchFee,
      "toll" := toll,
      "millageAllowance" := millageAllowance,
      "perDiem" := perDiem,
      "total" := total,
      "rowClass" := rowClass,
      "status" := status,
      "settled" := settled.isDefined
    )
  }

}


object Invoice {
  def createNew(username:String, matchData:MatchData, matchFee: Int, toll:Option[Double], millageAllowance:Option[Double], perDiem: Option[Int], total: Double, kms:Option[Double], otherExpenses:Option[Double], passengerAllowance:Option[PassengerAllowance]) = {
    Invoice(None, username, matchData, matchFee, toll, millageAllowance, perDiem, total, None, None, kms, otherExpenses, passengerAllowance)
  }

  def fromMongo(m:DBObject) = {
    val id = m.as[ObjectId]("_id")
    val username = m.as[String]("username")
    val matchData = m.getAs[DBObject]("matchData").map(MatchData.fromMongo).get
    val matchFee = m.as[Int]("matchFee")
    val settled = m.getAs[DateTime]("settled")
    val reminder = m.getAs[DateTime]("reminder")
    val total = m.as[Double]("total")
    val toll = m.getAs[Double]("toll")
    val millageAllowance = m.getAs[Double]("millageAllowance")
    val km = m.getAs[Double]("km")
    val otherExpenses = m.getAs[Double]("otherExpenses")
    val perDiem = m.getAs[Int]("perDiem")
    val passAllowance = m.getAs[DBObject]("passengerAllowance").map(PassengerAllowance.fromMongo(_))
    Invoice(Some(id), username, matchData, matchFee, toll, millageAllowance, perDiem, total, reminder, settled, km, otherExpenses, passAllowance)
  }

  def unsettledJson = """{"buttonText":"Merk betalt", "buttonClass":"btn"}"""
  def settledJson = """{"buttonText":"Betalt", "buttonClass":"btn btn-success"}"""
  def remindedJson = """{"buttonText":"Purret", "buttonClass":"btn btn-warning"}"""
  def notRemindedJson = """{"buttonText":"Merk purret", "buttonClass":"btn btn-inverse"}"""


  case class PassengerAllowance(pax: Int, km:Double){
    def toMongo = {
      MongoDBObject("pax" -> pax, "km" -> km)
    }

    def getTotal:Double = pax * km
  }

  object PassengerAllowance{
    def fromMongo(m:DBObject) = {
      PassengerAllowance(m.as[Int]("pax"), m.as[Double]("km"))
    }

    def fromWeb(paxOpt:Option[Int], kmOpt:Option[Double]) = {
      if(paxOpt.isDefined && kmOpt.isDefined){
        Some(PassengerAllowance(paxOpt.get, kmOpt.get))
      }else None
    }
  }
}



