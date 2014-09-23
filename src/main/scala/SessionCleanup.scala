import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers
import no.magott.fiks.data.MongoSetting
import org.joda.time.DateTime
import com.mongodb.casbah.Imports._


import scala.util.Properties

object CleanUpSessions extends App{

  RegisterJodaTimeConversionHelpers()
  val MongoSetting(db) = Properties.envOrNone("MONGOLAB_URI")
  println("Hello, I'm cleaning old sessions")
  val affected = db("sessions").remove( ("validTo" $lt DateTime.now) ).getN
  println("%s stale sessions deleted".format(affected))
}