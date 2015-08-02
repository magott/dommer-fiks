package no.magott.fiks.message

import argonaut.Json
import no.magott.fiks.data.{SessionId, Pages}
import no.magott.fiks.user.UserService
import unfiltered.filter.Plan
import unfiltered.request._
import unfiltered.response._
import argonaut.JsonIdentity._
import argonaut.StringWrap._

/**
 *
 */
class MessagePlan(messageService:MessageService, userService: UserService) extends Plan{

  override def intent = {
    case r@Path(Seg("messages" :: Nil)) => r match {
      case Accepts.Json(_) & SessionId(sessionId)=> {
        val sessionOpt = userService.userSession(sessionId)
        if(sessionOpt.isEmpty) Forbidden
        else {
          val messages = messageService.getMessages(sessionOpt.get)
          messageService.markRead(sessionOpt.get)
          Ok ~> JsonContent ~> ResponseString(messages.map(_.asJson).jencode.nospaces)
        }
      }
      case _ => Html5(Pages(r).messages)
    }
    case r@Path(Seg("messages"::"unreadcount" :: Nil)) & SessionId(sessionId) => {
      val count = messageService.getUnreadCount(userService.userSession(sessionId).get)
      Ok ~> JsonContent ~> ResponseString(Json.obj("count" := count).nospaces)
    }
  }
}
