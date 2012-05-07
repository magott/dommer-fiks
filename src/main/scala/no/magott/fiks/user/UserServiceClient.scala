package no.magott.fiks.user

import no.magott.fiks.data.{MongoSetting, MatchScraper}
import util.Properties
import com.mongodb.casbah.commons.MongoDBObject
import org.joda.time.{DateTime, LocalDateTime}


object UserServiceClient extends App {

  import com.mongodb.casbah.commons.conversions.scala._
  RegisterJodaTimeConversionHelpers()

  println("Start")
  (new UserService).newUser("æøå", "æøå", "hey@da.com")
  val MongoSetting(db) = Properties.envOrNone("MONGOLAB_URI")
  db("foo").save(MongoDBObject("fooz" -> new LocalDateTime))
//  val username = (new UserService).byUsername("æøå")
//  println(username)
//  (new UserService).newCalendarId("hey")
//  val user = (new UserService).byUsername("morten.andersen.gott")
//  println(user)
//  val user2 = (new UserService).byUsername("andersen.gott")
//  println(user2)
}
