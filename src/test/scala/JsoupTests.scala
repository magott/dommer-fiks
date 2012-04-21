import org.jsoup.Jsoup
import org.jsoup.safety.Whitelist

object JsoupTests extends App {

  val clean = Jsoup.clean("<small>1234</small1>", Whitelist.none)
  Console println clean

}
