package no.magott.fiks.data

import geo.LatLong
import org.scalatest._
import org.scalatest.matchers._
class StadiumServiceTest extends FlatSpec with Matchers{

  "StadiumService" should "read file " in{
    val lines = new StadiumService().fromFile
    assert(!lines.isEmpty)
  }

  it should " parse stadium line" in {
    val stadium = new StadiumService().parseCsvStadiumLine("Jarmyra idrettspark - kunstgressbane m/ lys;219021309;Eksisterende;Bærum kommune;Øvrevoll/Hosle IL;Kommuneanlegg;Fotballanlegg;Fotball kunstgressbane;Ikke vurdert;Lengde 100.00 ;Bredde 60.00 ;  ;  ;214185;-5880;7;6651041;254857;33")
    assert(stadium.name == "Jarmyra idrettspark - kunstgressbane m/ lys")
  }

  it should "read file and parse stadiums" in {
    val service = new StadiumService()
    val stadiums = service.stadiumsFromFile
    assert(!stadiums.isEmpty)
  }

  it should "parse valid json from Gjermshus" in {
    val json = """{"name":"Bislett stadion","latLong":{"lat":59.9249984454,"long":10.7323990904}}"""
    val response = new StadiumService().parseGjermshusResponse(json)
    import org.scalatest.OptionValues._
    response.value shouldBe MongoStadium("Bislett stadion", LatLong(59.9249984454, 10.7323990904))
  }

  it should "handle gurba from Gjermshus gracefully" in {
    val fckedJson = "<html><h1>I am HTML, NOT JSON!!</h1><html>"
    val response = new StadiumService().parseGjermshusResponse(fckedJson)
    response shouldBe (None)
  }

}
