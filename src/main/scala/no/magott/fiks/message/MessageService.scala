package no.magott.fiks.message

import java.util.concurrent.TimeUnit

import com.google.common.cache.{LoadingCache, CacheLoader, CacheBuilder}
import no.magott.fiks.user.UserSession
import com.google.common.cache.Cache
import org.joda.time.LocalDateTime

/**
 *
 */
class MessageService(messageRepository: MessageRepository, messageScraper: MessageScraper) {



  private val messageCache: Cache[UserSession, List[Message]] = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build()

  def markRead(userSession: UserSession) = messageRepository.updateReadDate(userSession, LocalDateTime.now)

  def getMessages(userSession: UserSession) : List[Message] = {
    Option(messageCache.getIfPresent(userSession)).getOrElse{
      messageScraper.scrapeMessages(userSession).fold(
        error => {
          println(s"Error fetching messages $error")
          List.empty
        }, messages => {
          messageCache.put(userSession, messages)
          messages
        }
      )
    }
  }

  def findMessagesWithUnreadCount(userSession: UserSession) = {
    val messages = getMessages(userSession)
    val unread = getUnreadCount(userSession)
    UserMessages(unread, messages)
  }

  def getUnreadCount(user: UserSession) : Int = {
      getUnreadCount(user, getMessages(user))
  }

  private def getUnreadCount(user:UserSession, msgs: List[Message]) = {
    msgs.filter(msg => messageRepository.getLastRead(user).forall(_.isBefore(msg.timestamp))).size
  }

}
