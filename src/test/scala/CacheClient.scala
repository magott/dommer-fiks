import no.magott.fiks.data.{FiksLoginService, MatchService, MatchScraper}

object CacheClient extends App {

  val username = System.getProperty("fiks.username")
  val pass = System.getProperty("fiks.password")
  val scraper = new MatchScraper
  val service = new MatchService(scraper)
  val login =  FiksLoginService.login(username, pass)
  login match {
    case Right(cookie) =>
      service.availableMatches(cookie)
      service.availableMatches(cookie)
      println("Clearing cache")
      service.availableMatchesCache.invalidate(cookie._2)

      service.availableMatches(cookie)
      service.availableMatches(cookie)

    case Left(exception) => Console println "Login failed"
  }

}
