package no.magott.fiks.data

import org.scalatest.FunSuite
import org.joda.time.LocalDateTime

class MatchServiceTest extends FunSuite {


  test("cache is updated after reporting interest"){

    val service = new MatchService(new MatchScraper(){
      override def scrapeAvailableMatches(loginToken:String) = AvailableMatch("Foo","5. Div", LocalDateTime.now,"123","Foo - Bar","Foobar","Dommer",Some("123")) :: Nil
      override def postInterestForm(matchId: String, comment:String, loginToken:String){}
    })

    val available = service.availableMatches(("", (""))).head
    assert(available.availabilityId.isDefined)

    service.reportInterest("123","","")
    val availableAfterPostingInterest = service.availableMatches(("", "")).head
    assert(availableAfterPostingInterest.availabilityId.isEmpty)

  }


}
