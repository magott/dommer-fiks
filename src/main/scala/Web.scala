import no.magott.fiks.data._
import unfiltered.request.{Path, GET}
import unfiltered.response.Html
import util.Properties
import unfiltered.jetty
import unfiltered.filter

object Web {
  def main(args: Array[String]) {
    val matchscraper = new MatchScraper
    val matchservice = new MatchService(matchscraper)
    val port = Properties.envOrElse("PORT", "8080").toInt
    println("Starting on port:" + port)
    jetty.Http(port).resources(getClass().getResource("/static")).plan(new SecurityPlan(matchservice)).plan(new FiksPlan(matchservice))
    .run
  }

}