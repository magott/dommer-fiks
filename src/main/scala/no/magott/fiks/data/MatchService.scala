package no.magott.fiks.data

import scala.concurrent.ops.spawn
import Guava2ScalaConversions._
import org.jsoup.Connection.Method
import org.jsoup.Jsoup
import java.util.concurrent.{Executors, TimeUnit}
import util.Properties
import com.google.common.cache.{LoadingCache, Cache, CacheLoader, CacheBuilder}

class MatchService(val matchscraper:MatchScraper) {
  val assignedMatchesCache = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).maximumSize(100).build(CacheLoader.from((loginToken: String) => matchscraper.scrapeAssignedMatches(loginToken)))
  val availableMatchesCache:LoadingCache[String, List[AvailableMatch]] = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).maximumSize(100).build(CacheLoader.from((loginToken:String) => matchscraper.scrapeAvailableMatches(loginToken)))
  val matchInfoCache:Cache[String,AvailableMatch] = CacheBuilder.newBuilder().expireAfterWrite(60, TimeUnit.MINUTES).maximumSize(50).build()

  if(Properties.envOrElse("PRINT_STATS","false").toBoolean){
    scheduleStatsPrinting
  }else{
    println("No stats print configured")
  }

  def assignedMatches(loginCookie: (String, String)): List[AssignedMatch] = {
    assignedMatchesCache.get(loginCookie._2)
  }

  def availableMatches(loginCookie: (String, String)):List[AvailableMatch] = {
    availableMatchesCache.get(loginCookie._2)
  }

  def reportInterest(availabilityId: String, comment:String, loginToken:String) {
    matchscraper.postInterestForm(availabilityId, comment, loginToken)
    updateCacheWithInterestReported(availabilityId,loginToken)

  }

  def matchInfo(assignmentId: String, loginToken:String) = {
    val cached = Option(matchInfoCache.getIfPresent(assignmentId))
    cached match{
      case Some(x) => x
      case None => {
        val matchInfo = matchscraper.scrapeMatchInfo(assignmentId, loginToken)
        matchInfoCache.put(assignmentId,matchInfo)
        matchInfo
      }
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

  private def updateCacheWithInterestReported(availabilityId: String, loginToken:String){
    val updatedCacheEntry = availableMatchesCache.get(loginToken).map(a => if(a.availabilityId == Some(availabilityId)) a.copy(availabilityId = None) else a )
    availableMatchesCache.put(loginToken, updatedCacheEntry)
    println("Cache entry updated for availability id "+availabilityId+" token "+loginToken )
  }

  private def scheduleStatsPrinting {
    val scheduler = Executors.newSingleThreadScheduledExecutor
    scheduler.scheduleAtFixedRate(
      new Runnable {
        def run() {
          println("Assigned: \t" + assignedMatchesCache.stats)
          println("Available: \t" + availableMatchesCache.stats)
          println("MatchInfo: \t" + matchInfoCache.stats)
        }
      }, 5, 30, TimeUnit.MINUTES
    )
    Runtime.getRuntime.addShutdownHook(
      new Thread(){
        override def run {println("Shutting down statsprinter"); scheduler.shutdownNow}
      }
    )
  }

}
