package no.magott.fiks.data

import org.scalatest.FunSuite
import org.joda.time.{DateTime, LocalDateTime}
import no.magott.fiks.user.UserSession

class MatchServiceTest extends FunSuite {


  test("cache is updated after reporting interest"){

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


}
