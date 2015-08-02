package no.magott.fiks.message

import argonaut.Json
import argonaut.StringWrap._
import org.joda.time.LocalDateTime

/**
 *
 */
case class Message(author:String, timestamp:LocalDateTime, title:String, body:String) {
  def asJson = {
    Json.obj(
    "author" := author,
    "timestamp" := timestamp.toString,
    "title" := title,
    "body" := body
    )
  }
}
case class UserMessages(undreadCount:Int, messages:List[Message])
