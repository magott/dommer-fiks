import no.magott.fiks.data.{FiksLoginService, MatchService, MatchScraper}

object CacheClient extends App {

  val username = System.getProperty("fiks.username")
  val pass = System.getProperty("fiks.password")
  val scraper = new MatchScraper
  val service = new MatchService(scraper)
  val login =  FiksLoginService.login(username, pass)
  login match {
    case Right(cookie) =>
      service.availableMatches(cookie._2)
      service.availableMatches(cookie._2)
      println("Clearing cache")
      service.availableMatchesCache.invalidate(cookie._2)

      service.availableMatches(cookie._2)
      service.availableMatches(cookie._2)

    case Left(exception) => Console println "Login failed"
  }

}
