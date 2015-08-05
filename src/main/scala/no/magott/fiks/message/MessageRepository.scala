package no.magott.fiks.message

import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers
import com.mongodb.casbah.query.Imports._
import no.magott.fiks.data.MongoSetting
import no.magott.fiks.user.{UserSession, User}
import org.joda.time.{DateTime, DateTimeZone, LocalDateTime}

import scala.util.Properties

/**
 *
 */
class MessageRepository {


  RegisterJodaTimeConversionHelpers()
  val MongoSetting(db) = Properties.envOrNone("MONGOLAB_URI")
  val where = MongoDBObject

  def updateReadDate(user: UserSession, localDateTime: LocalDateTime) = {
    val Norway = DateTimeZone.forID("Europe/Oslo")
    db("messages").update(where("username" -> user.username), $set("lastRead"-> localDateTime.toDateTime(Norway)), upsert=true)
  }

  def getLastRead(user:UserSession) = {
    db("messages").findOne(where("username" -> user.username)).map(_.as[DateTime]("lastRead").toLocalDateTime)
  }

}
