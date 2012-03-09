import no.magott.fiks.data.{FiksPlan, SecurityPlan, Snippets}
import unfiltered.request.{Path, GET}
import unfiltered.response.Html
import util.Properties
import unfiltered.jetty
import unfiltered.filter

object Web {
  def main(args: Array[String]) {
    val port = Properties.envOrElse("PORT", "8080").toInt
    println("Starting on port:" + port)
    jetty.Http(port).resources(getClass().getResource("/static")).plan(SecurityPlan).plan(FiksPlan)
//    filter(filter.Planify {
//      case GET(Path("/foo")) => Html(<h1>bar</h1>)
//      case GET(Path("/bar")) => Html(Snippets.emptyPage(<p>Foobar</p>))
//    })
      .run
  }
}