package no.magott.fiks.data

import scala.concurrent.ops.spawn
import java.util.concurrent.{TimeUnit}
import util.Properties
import com.google.common.cache.{LoadingCache, Cache, CacheLoader, CacheBuilder}
import org.joda.time.{DateTimeZone, LocalDateTime, LocalDate}
import no.magott.fiks.user.UserSession

class MatchService(val matchscraper:MatchScraper) {

  import Scala2GuavaConversions.scalaFunction2GuavaFunction
  val assignedMatchesCache: LoadingCache[UserSession, List[AssignedMatch]] = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).maximumSize(100).build(CacheLoader.from((session: UserSession) => matchscraper.scrapeAssignedMatches(session)))
  val availableMatchesCache:LoadingCache[UserSession, List[AvailableMatch]] = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).maximumSize(100).build(CacheLoader.from((session:UserSession) => matchscraper.scrapeAvailableMatches(session)))
  val matchInfoCache:Cache[String,AvailableMatch] = CacheBuilder.newBuilder().expireAfterWrite(60, TimeUnit.MINUTES).maximumSize(50).build()

  def assignedMatches(session:UserSession): List[AssignedMatch] = {
    assignedMatchesCache.get(session).filter(_.date.year == LocalDateTime.now.year)
  }

  def upcomingAssignedMatches(session:UserSession): List[AssignedMatch] = {
    assignedMatchesCache.get(session).filter(upcomingFilter)
  }

  def availableMatches(session:UserSession):List[AvailableMatch] = {
    availableMatchesCache.get(session)
  }

  def reportInterest(availabilityId: String, comment:String, session:UserSession) {
    matchscraper.postInterestForm(availabilityId, comment, session)
    updateCacheWithInterestReported(availabilityId, session)
  }

  def postMatchResult(result:MatchResult, session:UserSession) {
    if(result.isDeletionRequired){
      matchscraper.deleteMatchResult(result.fiksId, result.requiredDeletions, session)
    }
    matchscraper.postMatchResult(result, session);
  }

  def yieldMatch(cancellationId:String, reason:String, session:UserSession) = {
    val viewstate = matchscraper.scrapeMeldForfallViewState(cancellationId, session)
    matchscraper.postForfall(cancellationId, reason, viewstate, session)
    assignedMatchesCache.invalidate(session)
  }

  def matchDetails(matchId:String, session:UserSession) = assignedMatches(session).find(_.fiksId == matchId)

  def availableMatchInfo(assignmentId: String, session:UserSession) = {
    val cached = Option(matchInfoCache.getIfPresent(assignmentId))
    cached.getOrElse{
      val matchInfo = matchscraper.scrapeMatchInfo(assignmentId, session)
      matchInfoCache.put(assignmentId,matchInfo)
      matchInfo
    }
  }

  def prefetchAssignedMatches(session:UserSession) {
    spawn{
      assignedMatchesCache.get(session)
    }
  }

  def prefetchAvailableMatches(session:UserSession) {
    spawn{
      availableMatchesCache.get(session)
    }
  }

  def matchResult(matchId:String, session:UserSession):MatchResult = {
    matchscraper.scrapeMatchResult(matchId, session)
  }

  private def upcomingFilter(m:AssignedMatch)  =
    m.date.toLocalDate.isAfter(LocalDate.now(DateTimeZone.forID("Europe/Oslo")).minusDays(1))

  private def updateCacheWithInterestReported(availabilityId: String, session:UserSession){
    val updatedCacheEntry = availableMatchesCache.get(session).map(a => if(a.availabilityId == Some(availabilityId)) a.copy(availabilityId = None) else a )
    availableMatchesCache.put(session, updatedCacheEntry)
    println("Cache entry updated for availability id "+availabilityId+" token "+session )
  }
}
