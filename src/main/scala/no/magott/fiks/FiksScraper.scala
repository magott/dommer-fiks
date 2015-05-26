package no.magott.fiks

import org.joda.time.format.DateTimeFormat

/**
 *
 */
trait FiksScraper {

  val COOKIE_NAME = "ASP.NET_SessionId"
  val fiksDateFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")
  val VIEWSTATE = "__VIEWSTATE"
  val VIEWSTATEGENERATOR = "__VIEWSTATEGENERATOR"




}
