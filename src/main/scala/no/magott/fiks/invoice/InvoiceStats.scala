package no.magott.fiks.invoice

import org.joda.time.DateTime

/**
 * Created by morten on 05/04/14.
 */
class InvoiceStats {

  def badCredit(i:List[Invoice]) = {
    i.filter(stillUnpaidAfter1Week).groupBy(teamGroup).toList.sortBy(_._2.size).reverse
  }

  def stillUnpaidAfter1Week(inv:Invoice) = {
    overdueBy1Week(inv) && inv.settled.isEmpty
  }

  def earlySettlers(inv:Invoice) = {
    inv.settled.exists(_.isBefore(inv.matchData.date.plusDays(3)))
  }

  def overdueBy(days:Int) = (invoice:Invoice) => {
    invoice.settled.getOrElse(DateTime.now).isAfter(invoice.matchData.date.plusDays(days))
  }

  def overdueBy1Week = overdueBy(7)


  def clubGroup(invoice:Invoice) = {
    invoice.matchData.home
  }

  def tournamentGroup(invoice:Invoice) = {
    invoice.matchData.tournament
  }

  def teamGroup(i:Invoice) = {
    clubGroup(i) +" "+ tournamentGroup(i)
  }


}

object InvoiceStats extends App {
  println("starting...")
  private val stats = new InvoiceStats
  val repository = new InvoiceRepository
  val all = repository.all
  private val badcredit = stats.badCredit(all)
  badcredit.map(t => s"""${t._1} \t ${t._2.size}""")foreach(println)
  println("")
  badcredit.take(1).map(s => s._2.map(_.matchData.home)).foreach(println)
}