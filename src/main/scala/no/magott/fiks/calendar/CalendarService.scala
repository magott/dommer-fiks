package no.magott.fiks.calendar

import org.joda.time.LocalDateTime
import no.magott.fiks.data.{AssignedMatch, FiksLoginService, MatchScraper}
import java.util.concurrent.TimeUnit
import com.google.common.cache.{Cache, CacheBuilder}
import no.magott.fiks.user.User

class CalendarService(matchscraper: MatchScraper) {

  val cache:Cache[String,List[AssignedMatch]] = CacheBuilder.newBuilder().maximumSize(1000).expireAfterWrite(12,TimeUnit.HOURS).build()

  def calendarForUser(user:User):Option[List[AssignedMatch]] = {
    val cachedValue = Option(cache.getIfPresent(user.calendarId.get))
    if(cachedValue.isDefined){
      Some(cachedValue.get)
    }else{
      val logintoken = FiksLoginService.login(user.username, user.password)
      logintoken match{
        case(Right(x)) => {
          val matches = matchesThisYearForLogin(x._2)
          cache.put(user.calendarId.get,matches)
          Some(matches)
        }
        case _ => None
      }
    }
  }

  private def matchesThisYearForLogin(logintoken:String) = {
    matchscraper.scrapeAssignedMatches(logintoken, _.date.year == LocalDateTime.now.year)
  }

}
