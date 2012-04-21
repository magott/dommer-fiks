package no.magott.fiks.user

import com.mongodb.casbah.commons.MongoDBObject

case class UserSession(fiksToken:String, username:String) {

  def asMongoDbObject = {
    val builder = MongoDBObject.newBuilder
    builder += "fiksToken" -> fiksToken
    builder += "username" -> username
    builder.result
  }

  def this(mo: MongoDBObject) = {
    this(
      mo.getAs[String]("fiksToken").get,
      mo.getAs[String]("username").get
    )
  }


}
