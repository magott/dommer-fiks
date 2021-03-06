package no.magott.fiks.calendar

import org.joda.time.LocalDateTime
import no.magott.fiks.data.{AssignedMatch, FiksLoginService, MatchScraper}
import java.util.concurrent.TimeUnit
import com.google.common.cache.{Cache, CacheBuilder}
import no.magott.fiks.user.{UserSession, User}

class CalendarService(matchscraper: MatchScraper) {

  val cache:Cache[String,List[AssignedMatch]] = CacheBuilder.newBuilder().maximumSize(1000).expireAfterWrite(2,TimeUnit.HOURS).build()

  def calendarForUser(user:User):Option[List[AssignedMatch]] = {
    val cachedValue = Option(cache.getIfPresent(user.calendarId.get))
    if(cachedValue.isDefined){
      Some(cachedValue.get)
    }else{
      val logintoken = FiksLoginService.login(user.username, user.password.get, false)
      logintoken match{
        case(Right(session)) => {
          val matches = matchesThisYearForLogin(session)
          cache.put(user.calendarId.get,matches)
          Some(matches)
        }
        case _ => None
      }
    }
  }

  private def matchesThisYearForLogin(session:UserSession) = {
    matchscraper.scrapeAssignedMatches(session).filter(_.date.year == LocalDateTime.now.year)
  }

}
