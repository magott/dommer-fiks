package no.magott.fiks.invoice

/**
 * Created by morten on 08/04/14.
 */
case class InvoiceTotals(total:Double, settled:Double, outstanding:Double) {
  
  def + (i:Invoice) = {
    InvoiceTotals(total + i.total, settled + (if(i.settled.isDefined) i.total else 0), outstanding + (if(i.settled.isEmpty) i.total else 0) )
  }
}

object InvoiceTotals{
  def empty = new InvoiceTotals(0,0,0)
}