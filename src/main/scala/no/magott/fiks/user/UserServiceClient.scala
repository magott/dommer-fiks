package no.magott.fiks.user

import no.magott.fiks.data.MatchScraper

object UserServiceClient extends App {

  println("Start")
  (new UserService).newUser("æøå", "æøå", "hey@da.com")
//  val username = (new UserService).byUsername("æøå")
//  println(username)
//  (new UserService).newCalendarId("hey")
//  val user = (new UserService).byUsername("morten.andersen.gott")
//  println(user)
//  val user2 = (new UserService).byUsername("andersen.gott")
//  println(user2)
}
