package no.magott.fiks.data

import scala.concurrent.ops.spawn
import java.util.concurrent.{Executors, TimeUnit}
import util.Properties
import com.google.common.cache.{LoadingCache, Cache, CacheLoader, CacheBuilder}
import org.joda.time.{DateTimeZone, LocalDateTime, LocalDate}

class MatchService(val matchscraper:MatchScraper) {

  import Scala2GuavaConversions.scalaFunction2GuavaFunction
  val assignedMatchesCache = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).maximumSize(100).build(CacheLoader.from((loginToken: String) => matchscraper.scrapeAssignedMatches(loginToken)))
  val availableMatchesCache:LoadingCache[String, List[AvailableMatch]] = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).maximumSize(100).build(CacheLoader.from((loginToken:String) => matchscraper.scrapeAvailableMatches(loginToken)))
  val matchInfoCache:Cache[String,AvailableMatch] = CacheBuilder.newBuilder().expireAfterWrite(60, TimeUnit.MINUTES).maximumSize(50).build()

  def assignedMatches(loginToken:String): List[AssignedMatch] = {
    assignedMatchesCache.get(loginToken).filter(_.date.year == LocalDateTime.now.year)
  }

  def upcomingAssignedMatches(loginToken:String): List[AssignedMatch] = {
    assignedMatchesCache.get(loginToken).filter(upcomingFilter)
  }

  def cachedUpcomingAssignedMatches(loginToken:String) : Option[List[AssignedMatch]] = {
    Option(assignedMatchesCache.getIfPresent(loginToken)).map(_.filter(upcomingFilter))
  }

  def availableMatches(loginToken:String):List[AvailableMatch] = {
    availableMatchesCache.get(loginToken)
  }

  def reportInterest(availabilityId: String, comment:String, loginToken:String) {
    matchscraper.postInterestForm(availabilityId, comment, loginToken)
    updateCacheWithInterestReported(availabilityId,loginToken)
  }

  def postMatchResult(result:MatchResult, loginToken:String) {
    if(result.isDeletionRequired){
      matchscraper.deleteMatchResult(result.fiksId, result.requiredDeletions, loginToken)
    }
    matchscraper.postMatchResult(result, loginToken);
  }

  def yieldMatch(cancellationId:String, reason:String, loginToken:String) = {
    val viewstate = matchscraper.scrapeMeldForfallViewState(cancellationId, loginToken)
    matchscraper.postForfall(cancellationId, reason, viewstate, loginToken)
    assignedMatchesCache.invalidate(loginToken)
  }

  def matchDetails(matchId:String, loginToken:String) = assignedMatches(loginToken).find(_.fiksId == matchId)

  def availableMatchInfo(assignmentId: String, loginToken:String) = {
    val cached = Option(matchInfoCache.getIfPresent(assignmentId))
    cached.getOrElse{
      val matchInfo = matchscraper.scrapeMatchInfo(assignmentId, loginToken)
      matchInfoCache.put(assignmentId,matchInfo)
      matchInfo
    }
  }

  def prefetchAssignedMatches(loginToken:String) {
    spawn{
      assignedMatchesCache.get(loginToken)
    }
  }

  def prefetchAvailableMatches(loginToken:String) {
    spawn{
      availableMatchesCache.get(loginToken)
    }
  }

  def matchResult(matchId:String, loginToken:String):MatchResult = {
    matchscraper.scrapeMatchResult(matchId,loginToken)
  }

  private def upcomingFilter(m:AssignedMatch)  =
    m.date.toLocalDate.isAfter(LocalDate.now(DateTimeZone.forID("Europe/Oslo")).minusDays(1))

  private def updateCacheWithInterestReported(availabilityId: String, loginToken:String){
    val updatedCacheEntry = availableMatchesCache.get(loginToken).map(a => if(a.availabilityId == Some(availabilityId)) a.copy(availabilityId = None) else a )
    availableMatchesCache.put(loginToken, updatedCacheEntry)
    println("Cache entry updated for availability id "+availabilityId+" token "+loginToken )
  }




}
