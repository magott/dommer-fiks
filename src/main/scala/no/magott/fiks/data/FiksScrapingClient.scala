package no.magott.fiks.data

import org.jsoup.Connection.Response

object FiksScrapingClient extends App{

  val username = System.getProperty("fiks.username")
  val pass = System.getProperty("fiks.password")
  val login =  new FiksLogin().login(username, pass)
  login match {
    case Right(cookie) => new FiksLogin().availableMatches(cookie)
    case Left(exception) => Console println "Login failed"
  }
//  Console println login.parse.body.html


}
