package no.magott.fiks.user

import com.mongodb.casbah.commons.MongoDBObject

case class User(username: String, password: String, email: String, calendarId: Option[String], beta: Boolean) {


  def toMongoDbObject = {
    val builder = MongoDBObject.newBuilder
    builder += "username" -> username
    builder += "password" -> password
    builder += "email" -> email
    builder += "email" -> email
    builder += "beta" -> beta
    if (calendarId.isDefined) {
      builder += "calid" -> calendarId
    }
    builder.result
  }

  def this(mo: MongoDBObject) = {
    this(
      mo.getAs[String]("username").get,
      mo.getAs[String]("password").get,
      mo.getAs[String]("email").get,
      Option(mo.getAs[String]("calid").getOrElse(null)),
      mo.getAs[Boolean]("beta").getOrElse(false)
    )
  }

  def unapply(mo: MongoDBObject) = toMongoDbObject


}
