package no.magott.fiks.invoice

import no.magott.fiks.invoice.Invoice.PassengerAllowance
import org.joda.time.{LocalDate, DateTime}
import com.mongodb.casbah.query.Imports._
import scala.collection.mutable
import scalaz._, Scalaz._
import argonaut._, Argonaut._

case class Invoice(id:Option[ObjectId], username:String, matchData:MatchData, matchFee: Int, toll:Option[Double], perDiem: Option[Int], reminder:Option[DateTime], settled:Option[DateTime], km:Option[Double], otherExpenses:Option[Double], passengerAllowance: Option[PassengerAllowance], kmAllowanceMunicipal:Option[String]) {

  def status = if(settled.isDefined) s"Betalt ${settled.get.toString("dd.MM")}" else if(reminder.isDefined) s"Purret ${reminder.get.toString("dd.MM")}" else "Utestående"
  def rowClass = if(settled.isDefined) "success" else if(moreDaysPassedThan(10)) "danger" else if(moreDaysPassedThan(7)) "warning" else if(moreDaysPassedThan(5)) "info" else ""

  def isNew = id.isEmpty

  def millageAllowance : Option[Double] = {
    km.map{k =>
      (BigDecimal.valueOf(k) * Invoice.kmMultiplierFor(matchData.date, kmAllowanceMunicipal.getOrElse(""))).toDouble
    }
  }

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
    kmAllowanceMunicipal.foreach(map += "kmAllowanceMunicipal" -> _)
    map += "updated" -> DateTime.now
    map
  }

  def unsetList = {
    val unsets = mutable.MutableList.empty[String]
    if(toll.isEmpty) unsets += "toll"
    if(millageAllowance.isEmpty) unsets += "millageAllowance"
    if(passengerAllowance.isEmpty) unsets += "passengerAllowance"
    if(perDiem.isEmpty) unsets += "perDiem"
    if(otherExpenses.isEmpty) unsets += "otherExpenses"
    if(km.isEmpty) unsets += "km"
    if(kmAllowanceMunicipal.isEmpty) unsets += "kmAllowanceMunicipal"

    unsets
  }

  def asMongoUpdate:DBObject = {
    $set(asMap.toSeq:_*) ++ $unset(unsetList:_*)
  }

  def asMongoInsert: DBObject = {
    MongoDBObject(asMap.toList:_*)
  }

  def total = calculateTotal

  def calculateTotal : Double = {
    matchFee + perDiem.getOrElse(0) +
      toll.getOrElse(0d) +
      otherExpenses.getOrElse(0d) +
      millageAllowance.getOrElse(0d) +
      passengerAllowance.map(_.getTotal).getOrElse(0d)
  }

  private[invoice] def debugCalculationString =  s" $matchFee + ${perDiem.getOrElse(0)} + ${toll.getOrElse(0d)} + ${otherExpenses.getOrElse(0d)} + ${millageAllowance.getOrElse(0d)} + ${passengerAllowance.map(_.getTotal).getOrElse(0d)}"

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

  val kmDefaults = Map(2015-> BigDecimal("4.10"), 2014 -> BigDecimal("4.05")).withDefaultValue(BigDecimal("3.80"))
  val kmTromso = Map(2015-> BigDecimal("4.20"), 2014 -> BigDecimal("4.15")).withDefaultValue(BigDecimal("3.90"))
  val kmRates = Map(
    "tromsø" -> kmTromso
  ).withDefaultValue(kmDefaults)


  def createNew(username:String, matchData:MatchData, matchFee: Int, toll:Option[Double], perDiem: Option[Int], kms:Option[Double], otherExpenses:Option[Double], passengerAllowance:Option[PassengerAllowance], kmAllowanceMunicipal:Option[String]) = {
    Invoice(None, username, matchData, matchFee, toll, perDiem, None, None, kms, otherExpenses, passengerAllowance, kmAllowanceMunicipal)
  }

  def fromMongo(m:DBObject) = {
    val id = m.as[ObjectId]("_id")
    val username = m.as[String]("username")
    val matchData = m.getAs[DBObject]("matchData").map(MatchData.fromMongo).get
    val matchFee = m.as[Int]("matchFee")
    val settled = m.getAs[DateTime]("settled")
    val reminder = m.getAs[DateTime]("reminder")
    val toll = m.getAs[Double]("toll")
    val km = m.getAs[Double]("km")
    val otherExpenses = m.getAs[Double]("otherExpenses")
    val perDiem = m.getAs[Int]("perDiem")
    val passAllowance = m.getAs[DBObject]("passengerAllowance").map(PassengerAllowance.fromMongo(_))
    val kmAllowanceMunicipal = m.getAs[String]("kmAllowanceMunicipal")
    Invoice(Some(id), username, matchData, matchFee, toll, perDiem, reminder, settled, km, otherExpenses, passAllowance, kmAllowanceMunicipal)
  }

  def kmMultiplierFor(date:DateTime, municipal:String = "") = {
    val ratesForMunicipal: Map[Int, BigDecimal] = kmRates(municipal.toLowerCase)
    ratesForMunicipal(date.getYear)
  }

  def displayOfkInvoice(i: Invoice) = i.matchData.matchId.startsWith("03") || i.matchData.matchId.startsWith("02") || i.matchData.matchId.startsWith("01")

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



