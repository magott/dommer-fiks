package no.magott.fiks.user

import java.util.UUID
import no.magott.fiks.data.{MongoSetting, MatchScraper}
import util.Properties
import com.mongodb.casbah.commons.MongoDBObject
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor
import org.bouncycastle.jce.provider.BouncyCastleProvider
import com.mongodb.casbah.query._

class UserService {
  val MongoSetting(db) = Properties.envOrNone("MONGOLAB_URI")
  val where = MongoDBObject
  val chiper = new StandardPBEStringEncryptor();
  chiper.setProvider(new BouncyCastleProvider());
  chiper.setPassword(Properties.envOrElse("ENCRYPTION_PWD", "foo"))
  chiper.setAlgorithm("PBEWITHSHA256AND256BITAES-CBC-BC")

  def removeCalendarFor(username: String) {
    db("users").update(where("username"->username), $unset("calid"))

  }

  def byUsername(username:String) = {
    val mongoUser = db("users").findOne(where("username"->username))
    mongoUser match {
      case None => None
      case Some(dbobj) => Some(decrypt(User.fromMongo(dbobj)))
    }
  }

  def byCalendarId(calendarId:String) = {
    val mongoUser = db("users").findOne(where("calid"->calendarId))
    mongoUser match {
      case None => None
      case Some(dbobj) => Some(decrypt(User.fromMongo(dbobj)))
    }
  }

  def newUser(username: String, password: String, email:String) = {
    val calendarId = UUID.randomUUID.toString
    db("users").update(where("username"->username), $set("calid" -> calendarId, "username" -> username, "password" -> chiper.encrypt(password), "email" -> email), true, false)
    calendarId
  }

  def newCalendarId(username:String) = {
    val newCalendarId = UUID.randomUUID.toString
    val users = db("users")
    users.findAndModify(where("username"->username), $set("calid" -> newCalendarId ))
    newCalendarId
  }

  def save(userSession:UserSession) = {
    db("sessions").update(where("fiksToken"->userSession.fiksToken), userSession.asMongoDbObject, true, false)
  }

  def userSession(fiksToken:String) = {
    db("sessions").findOne(where("fiksToken" -> fiksToken)) match {
      case None => None
      case Some(dbObj) => Some(new UserSession(dbObj))
    }
  }

  def userForSession(fiksToken:String) = {
    userSession(fiksToken) match {
      case None => None
      case Some(session) => byUsername(session.username)
    }
  }

  def incrementPollcount(user:User) {
    db("users").update(where("username"->user.username), $inc( "pollcount" -> 1 ))
  }


  private def decrypt(user:User) = {
    if(user.password.isDefined){
      user.copy(password=Some(chiper.decrypt(user.password.get)))
    }else{
      user
    }
  }


}
