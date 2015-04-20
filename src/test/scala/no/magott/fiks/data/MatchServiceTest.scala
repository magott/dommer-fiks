package no.magott.fiks.data

import argonaut.Parse
import no.magott.fiks.invoice.InvoiceGenerator._
import org.scalatest.{Matchers, FlatSpec, FunSuite}
import org.joda.time.{DateTime, LocalDateTime}
import no.magott.fiks.user.UserSession

import scala.io.Source

class MatchServiceTest extends FlatSpec with Matchers{


  "cache" should "update after reporting interest" in {

    val service = new MatchService(new MatchScraper(){
      override def scrapeAvailableMatches(session:UserSession) = AvailableMatch("Foo","5. Div", LocalDateTime.now,"123","Foo - Bar","Foobar","Dommer",Some("123")) :: Nil
      override def postInterestForm(matchId: String, comment:String, session:UserSession){}
    })

    val session = UserSession("", "", "", "", DateTime.now)
    val available = service.availableMatches(session).head
    assert(available.availabilityId.isDefined)

    service.reportInterest("123","", session)
    val availableAfterPostingInterest = service.availableMatches(session).head
    assert(availableAfterPostingInterest.availabilityId.isEmpty)

  }

  "matchinfo json" should "be parsable" in {
    val stream = getClass.getResourceAsStream("/gjermshus-matchinfo.json")
    val content = Source.fromInputStream(stream,"UTF-8")
    val json = content.getLines().mkString
    val service = new MatchService(new MatchScraper)
    import service._
    val appinfo = Parse.decodeEither[AppointmentInfo](json)
    appinfo.fold(
    error => fail("should not fail"),
    success => {
      assert(success.ref.isDefined)
    }
    )
    println(appinfo)
  }


}
