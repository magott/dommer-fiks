package no.magott.fiks.invoice

import org.joda.time.{LocalDate, DateTime}
import com.mongodb.casbah.query.Imports._
import scala.collection.mutable

case class Invoice(id:Option[ObjectId], username:String, matchData:MatchData, matchFee: Int, toll:Option[Double], millageAllowance:Option[Double], perDiem: Option[Int], total: Double, reminder:Option[DateTime], settled:Option[DateTime]) {

  def status = if(settled.isDefined) s"Betalt ${settled.get.toString("dd.MM")}" else if(reminder.isDefined) s"Purret ${reminder.get.toString("dd.MM")}" else "Utestående"
  def rowClass = if(settled.isDefined) "success" else if(moreDaysPassedThan(10)) "error" else if(moreDaysPassedThan(7)) "warning" else if(moreDaysPassedThan(5)) "info" else ""
  def toMongo:DBObject = {
    val sets = mutable.Map[String, Any](
    "username" -> username,
    "matchData" -> matchData.toMongo,
    "matchFee" -> matchFee,
    "total" -> total
    )
    toll.foreach(x => sets += "toll" -> x)
    millageAllowance.foreach(x => sets += "millageAllowance" -> x)
    perDiem.foreach(x=> sets += "perDiem" -> x)

    $set(Seq(sets.toSeq:_*))
  }
  def updateClause : MongoDBObject = if(id.isDefined) MongoDBObject("_id" -> id.get) else toMongo
  private def moreDaysPassedThan(days:Int) = matchData.date.plusDays(days).isBeforeNow
}

object Invoice {
  def createNew(username:String, matchData:MatchData, matchFee: Int, toll:Option[Double], millageAllowance:Option[Double], perDiem: Option[Int], total: Double) = {
    Invoice(None, username, matchData, matchFee, toll, millageAllowance, perDiem, total, None, None)
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
    val perDiem = m.getAs[Int]("perDiem")
    Invoice(Some(id), username, matchData, matchFee, toll, millageAllowance, perDiem, total, reminder, settled)
  }

  def unsettledJson = """{"buttonText":"Merk betalt", "buttonClass":"btn"}"""
  def settledJson = """{"buttonText":"Betalt", "buttonClass":"btn btn-success"}"""
  def remindedJson = """{"buttonText":"Purret", "buttonClass":"btn btn-warning"}"""
  def notRemindedJson = """{"buttonText":"Send purring", "buttonClass":"btn btn-inverse"}"""


}

