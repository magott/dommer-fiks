package no.magott.fiks

import scala.xml.Utility

object ParameterImplicits {

  implicit class ParameterHelper(val underlying: Map[String, Seq[String]]){
    def valueOrNone(paramName:String) = {
      underlying(paramName).headOption.noneIfEmpty
    }
  }

  implicit class OptionHelper(val underlying: Option[String]) {
    def noneIfEmpty = {
      underlying.flatMap(v => Option(Utility.escape(v)).filter(_.trim.nonEmpty))
    }

  }
}
