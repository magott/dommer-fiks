package no.magott.fiks.data

import org.scalatest.FunSuite

class StadiumServiceTest extends FunSuite{

  test("Can read from file"){
    val lines = new StadiumService().fromFile
    assert(!lines.isEmpty)
  }

  test("Can parse stadium line"){
    val stadium = new StadiumService().parseCsvStadiumLine("Jarmyra idrettspark - kunstgressbane m/ lys;219021309;Eksisterende;Bærum kommune;Øvrevoll/Hosle IL;Kommuneanlegg;Fotballanlegg;Fotball kunstgressbane;Ikke vurdert;Lengde 100.00 ;Bredde 60.00 ;  ;  ;214185;-5880;7;6651041;254857;33")
    assert(stadium.name == "Jarmyra idrettspark - kunstgressbane m/ lys")
  }

  test("Can read file and parse stadiums"){
    val service = new StadiumService()
    val stadiums = service.stadiumsFromFile
    assert(!stadiums.isEmpty)
  }

}
