package no.magott.fiks.data

import scala.concurrent.ops.spawn
import com.google.common.cache.{Cache, CacheLoader, CacheBuilder}
import Guava2ScalaConversions._
import org.jsoup.Connection.Method
import org.jsoup.Jsoup
import java.util.concurrent.{Executors, TimeUnit}
import util.Properties

class MatchService(val matchscraper:MatchScraper) {
  val COOKIE_NAME = "ASP.NET_SessionId"
  val assignedMatchesCache = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).maximumSize(100).build(CacheLoader.from((loginToken: String) => matchscraper.scrapeAssignedMatches(loginToken)))
  val availableMatchesCache = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).maximumSize(100).build(CacheLoader.from((loginToken:String) => matchscraper.scrapeAvailableMatches(loginToken)))
  val matchInfoCache:Cache[String,AvailableMatch] = CacheBuilder.newBuilder().expireAfterWrite(60, TimeUnit.MINUTES).maximumSize(50).build()

  if(Properties.envOrElse("PRINT_STATS","false").toBoolean){
    scheduleStatsPrinting
  }else{
    println("No stats print configured")
  }

  def assignedMatches(loginCookie: (String, String)): List[AssignedMatch] = {
    assignedMatchesCache.get(loginCookie._2)
  }

  def availableMatches(loginCookie: (String, String)) = {
    availableMatchesCache.get(loginCookie._2)
  }

  def reportInterest(matchId: String, comment:String, loginToken:String) {
    println("Flushing cache for \t"+loginToken)
    availableMatchesCache.invalidate(loginToken)
    println("Flushed cache for \t"+loginToken)
    val url = "https://fiks.fotball.no/Fogisdomarklient/Uppdrag/UppdragLedigtUppdrag.aspx?domaruppdragId=" + matchId
    val reportInterestForm = Jsoup.connect(url).cookie(COOKIE_NAME,loginToken).get
    val viewstate = reportInterestForm.getElementById("__VIEWSTATE").attr("value")
    val eventvalidation = reportInterestForm.getElementById("__EVENTVALIDATION").attr("value")
    val response = Jsoup.connect(url)
      .method(Method.POST)
      .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_3) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.56 Safari/535.11")
      .data("btnAnmal","Meld inn")
      .data("tbKommentar",comment)
      .data("__VIEWSTATE",viewstate)
      .data("__EVENTVALIDATION",eventvalidation)
      .referrer(url)
      .cookie(COOKIE_NAME,loginToken).followRedirects(false).timeout(10000).execute()
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

  private def scheduleStatsPrinting {
    val scheduler = Executors.newScheduledThreadPool(1)
    scheduler.scheduleAtFixedRate(
      new Runnable {
        def run() {
          println("Assigned: \t" + assignedMatchesCache.stats)
          println("Available: \t" + availableMatchesCache.stats)
          println("MatchInfo: \t" + matchInfoCache.stats)
        }
      }, 60, 60, TimeUnit.SECONDS
    )
  }

}
