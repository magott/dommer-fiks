package no.magott.fiks

import java.util.Locale

import org.joda.time.format.DateTimeFormat

/**
 *
 */
package object data {

  val norwegianLongFormat = DateTimeFormat.forPattern("EEEE d. MMMM yyyy").withLocale(new Locale("nb","NO"))

}
