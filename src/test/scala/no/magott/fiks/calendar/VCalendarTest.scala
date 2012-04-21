package no.magott.fiks.calendar

import no.magott.fiks.data.AssignedMatch
import org.joda.time.LocalDateTime

object VCalendarTesten extends App{

  val m = AssignedMatch(LocalDateTime.now,"Foo - Bar","12341234","Foo - Bar","Et sted","Foo, Bar, Foobar")
  println(
  new VCalendar(m,m.copy(teams="A - B"), m.copy(teams="C - D")).feed
  )

  println("--")
  println(
    new VCalendar(Nil).feed
  )

}
