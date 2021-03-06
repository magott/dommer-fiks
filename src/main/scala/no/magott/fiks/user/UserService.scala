package no.magott.fiks.user

import java.util.UUID
import no.magott.fiks.data.{MongoSetting}
import com.mongodb.casbah.commons.MongoDBObject
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor
import org.bouncycastle.jce.provider.BouncyCastleProvider
import com.mongodb.casbah.query.Imports._
import scala.util.Properties
import com.google.common.cache.{CacheLoader, CacheBuilder, LoadingCache}
import java.util.concurrent.TimeUnit

class UserService {
  import no.magott.fiks.data.Scala2GuavaConversions._
  val sessionCache:LoadingCache[String, Option[UserSession]] = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).maximumSize(100).build(CacheLoader.from((loginToken:String) => userSessionFromMongo(loginToken)))

  val MongoSetting(db) = Properties.envOrNone("MONGOLAB_URI")
  val where = MongoDBObject

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

  @deprecated("use saveUser")
  def newUser(username: String, password: String, email:String) = {
    import Encryption._
    val calendarId = UUID.randomUUID.toString
    db("users").update(where("username"->username), $set("calid" -> calendarId, "username" -> username, "password" -> cipher.encrypt(password), "email" -> email), true, false)
    calendarId
  }

  def newCalendarId(username:String) = {
    val newCalendarId = UUID.randomUUID.toString
    val users = db("users")
    users.findAndModify(where("username"->username), $set("calid" -> newCalendarId ))
    newCalendarId
  }

  def save(userSession:UserSession) = {
    val update = db("sessions").update(where("id"->userSession.id), userSession.asMongoDbObject, true, false)
    update
  }

  def saveUser(user:User) = {
    import Encryption._
    val encryptedPassword = user.password.map(cipher.encrypt(_))
    db("users").update(where("username"->user.username), user.copy(password = encryptedPassword).toMongo, true, false)
  }

  def userSession(sessionId:String) = {
    sessionCache.get(sessionId)
  }
  private def userSessionFromMongo(sessionId:String) = {
    db("sessions").findOne(where("id" -> sessionId)) match {
      case None => None
      case Some(dbObj) => Some(UserSession.fromMongo(dbObj))
    }
  }

  def userForSession(fiksToken:String) : Option[User] = {
    userSession(fiksToken).flatMap(session => byUsername(session.username))
  }

  def incrementPollcount(user:User) {
    db("users").update(where("username"->user.username), $inc( "pollcount" -> 1 ))
  }


  private def decrypt(user:User) = {
    import Encryption._
    if(user.password.isDefined){
      user.copy(password=Some(cipher.decrypt(user.password.get)))
    }else{
      user
    }
  }


}
