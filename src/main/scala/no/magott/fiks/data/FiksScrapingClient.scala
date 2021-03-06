package no.magott.fiks.data

import org.jsoup.Connection.Response

object FiksScrapingClient extends App{

  val username = System.getProperty("fiks.username")
  val pass = System.getProperty("fiks.password")
  val scraper = new MatchScraper
  val service = new MatchService(scraper)
  val login =  FiksLoginService.login(username, pass, false)
  login match {
    case Right(session) =>
      service.assignedMatches(session).foreach(println)
    case Left(exception) => Console println "Login failed"
  }

}
