package no.magott.fiks.data

import org.jsoup.Connection.Response

object FiksScrapingClient extends App{

  val username = System.getProperty("fiks.username")
  val pass = System.getProperty("fiks.password")
  val scraper = new MatchScraper()
  val login =  FiksLogin.login(username, pass)
  login match {
    case Right(cookie) =>
      scraper.assignedMatches(cookie).foreach(println)
      scraper.assignedMatches(cookie).foreach(println)
    case Left(exception) => Console println "Login failed"
  }
//  val login2 =  FiksLogin.login(username, pass)
//  login2 match {
//    case Right(cookie) =>
//      scraper.assignedMatches(cookie).foreach(println)
//      scraper.assignedMatches(cookie).foreach(println)
//    case Left(exception) => Console println "Login failed"
//  }


}
