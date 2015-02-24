package no.magott.fiks

import org.jsoup.nodes.Document
import org.jsoup.Connection
import org.jsoup.select.Elements

/**
 *
 */
object JSoupPimps {


  implicit class JSoupElementPimp(val underlying: Document) extends AnyVal{

    def valueOfElement(id: String): Option[String] = {
      Option(underlying.getElementById(id)).flatMap(el => Option(el.`val`))
    }
  }

  implicit class JSoupConnectionPimp(val underlying: Connection) extends AnyVal{
    def data(key: String, optValue:Option[String]) = {
      optValue.foreach(value => underlying.data(key, value))
      underlying
    }
  }

  implicit class JSoupElementsPimp(val underlying: Elements) extends AnyVal{
    def selectValue(query: String) = {
      underlying.select(query).`val`
    }
    def selectText(query: String) = {
      underlying.select(query).text
    }
  }
}
