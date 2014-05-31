package no.magott.fiks.user

import com.mongodb.casbah.commons.MongoDBObject
import org.joda.time.DateTime

case class UserSession(id:String, sessionToken:String, longLivedToken:String, username:String, validTo:DateTime) {

  private val created = DateTime.now();

  def asMongoDbObject = {
    import com.mongodb.casbah.commons.conversions.scala._
    val builder = MongoDBObject.newBuilder
    builder += "id" -> id
    builder += "sessionToken" -> sessionToken
    builder += "longLivedToken" -> longLivedToken
    builder += "username" -> username
    builder += "validTo" -> validTo
    builder += "created" -> created
    builder.result
  }

}

object UserSession{
  def fromMongo(mo:MongoDBObject) = new UserSession(
    mo.getAs[String]("id").get,
    mo.getAs[String]("sessionToken").get,
    mo.getAs[String]("longLivedToken").get,
    mo.getAs[String]("username").get,
    mo.getAs[DateTime]("validTo").get
  )
}
