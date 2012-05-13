package no.magott.fiks.user

import com.mongodb.casbah.commons.MongoDBObject

case class User(username: String, password: Option[String], email: String, calendarId: Option[String], beta: Boolean) {


  def toMongo = {
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


}

object User{

  def fromMongo(mo: MongoDBObject) = new User(mo.getAs[String]("username").get,
    mo.getAs[String]("password"),
    mo.getAsOrElse[String]("email", ""),
    Option(mo.getAs[String]("calid").getOrElse(null)),
    mo.getAs[Boolean]("beta").getOrElse(false))
}
