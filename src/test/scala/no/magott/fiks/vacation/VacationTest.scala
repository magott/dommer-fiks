package no.magott.fiks.vacation

import argonaut.Parse
import org.joda.time.LocalDateTime
import org.scalatest.{Matchers, FlatSpec}
import org.scalatest.OptionValues._

import scalaz.{NonEmptyList, \/}

/**
 *
 */
class VacationTest extends FlatSpec with Matchers{

  "LocalDate validation" should "accept full ISO strings" in {
    val date = Vacation.validateLocalDate(Some("2014-05-05T20:15"), "Foobar")
    val angularDate = Vacation.validateLocalDate(Some("2015-06-03T22:00:00.000Z"),"barfoo")
    date.isRight shouldBe (true)
    angularDate.isRight shouldBe (true)
  }
  it should "fail on malformed strings" in {
    val date = Vacation.validateLocalDate(Some("balle"), "Invalid string")
  }
  it should "fail on empty option" in {
    val date = Vacation.validateLocalDate(None, "Invalid string")
  }

  "LocalTime validation" should "accept HH:mm string" in {
    val time = Vacation.validateTime(Some("12:00"), "foobar")
    time.isRight shouldBe(true)
  }

  "Vacation validation" should "pass" in {
    val validate = Vacation.validate(Some("2015-06-06"), Some("16:00"), Some("2015-06-06"), Some("18:00"), Some("because"))
    validate.fold(
      e => fail("Validation should pass: "+e.list.mkString(",")), v => {
        v.start shouldBe (new LocalDateTime(2015, 6, 6, 16,0,0,0))
        v.end shouldBe (new LocalDateTime(2015, 6, 6, 18,0,0,0))
        v.reason.value shouldBe ("because")
      }
    )
  }

  it should "default to whole days when time not supplied" in {
    val validate = Vacation.validate(Some("2015-06-06"), None, Some("2015-06-06"), None, Some("because"))
    validate.fold(
      e => fail("Validation should pass: "+e.list.mkString(",")), v => {
        v.start shouldBe (new LocalDateTime(2015, 6, 6, 0,0,0,0))
        v.end shouldBe (new LocalDateTime(2015, 6, 6, 23,59,0,0))
        v.reason.value shouldBe ("because")
      }
    )
  }

  "foo" should "bar" in {
    val foo = Parse.decode[Map[String,String]]("""{"foo":"bar"}""")
    println(foo)
  }

}
