import no.magott.fiks.data._
import org.eclipse.jetty.server.session.SessionHandler
import util.Properties
import unfiltered.jetty

object Web {
  def main(args: Array[String]) {
    val matchscraper = new MatchScraper
    val matchservice = new MatchService(matchscraper)
    val port = Properties.envOrElse("PORT", "8080").toInt
    println("Starting on port:" + port)
    val http = jetty.Http(port)
    http.current.setSessionHandler(new SessionHandler)
      http.resources(getClass().getResource("/static"))
      .plan(new SecurityPlan(matchservice))
      .plan(new FiksPlan(matchservice))
      .plan(new FallbackPlan)
    .run
  }

}