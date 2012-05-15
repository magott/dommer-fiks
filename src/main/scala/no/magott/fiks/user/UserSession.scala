package no.magott.fiks.user

import com.mongodb.casbah.commons.MongoDBObject
import org.joda.time.DateTime

case class UserSession(fiksToken:String, username:String) {

  private val created = DateTime.now();

  def asMongoDbObject = {
    import com.mongodb.casbah.commons.conversions.scala._
    RegisterJodaTimeConversionHelpers()
    val builder = MongoDBObject.newBuilder
    builder += "fiksToken" -> fiksToken
    builder += "username" -> username
    builder += "created" -> created
    builder.result
  }

}

object UserSession{
  def fromMongo(mo:MongoDBObject) = new UserSession(mo.getAs[String]("fiksToken").get, mo.getAs[String]("username").get)
}
