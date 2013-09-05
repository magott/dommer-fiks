package no.magott.fiks.data

import scala.xml.NodeSeq

case class Official(name:String, mobile:Option[String], home:Option[String], role:Role){
  def smsCheckbox : NodeSeq = <label class="checkbox"><input type="checkbox" value={mobile.getOrElse("")} class="smscheck"></input>{name}</label>
}

object Official{
  def fromTuple(tuple:Tuple2[String, String]):Official = {
    val NameMobilePhone(name, mobile, home) = tuple._2
    Official(name, mobile, home, Role.fromString(tuple._1))
  }
}

object NameMobilePhone {
  val Structure = "(?:(.+)\\s*?Tlf\\.:?:[\\s]?):?(\\w+)?(?:,\\s*Mobil:?:[\\s]?)(\\w+)?".r
  val Number = "\\d{8}".r

  def unapply(input: String): Option[(String, Option[String], Option[String])] = {
    input match {
      case Structure(n, p, m) => {
        Some(n.trim, Option(m).flatMap(Number.findFirstIn), Option(p).flatMap(Number.findFirstIn))
      }
      case n => Some((n.trim, None, None))
    }
  }

}

sealed abstract class Role(val value:String){
  def isReferee = false
}
abstract class RefereeRole(value:String) extends Role(value){
  override def isReferee = true
}
case object Dommer extends RefereeRole("Dommer")
case object AssistentDommer1 extends RefereeRole("AD1")
case object AssistentDommer2 extends RefereeRole("A2D")
case object FjerdeDommer extends RefereeRole("4. dommer")
case object Dommerveileder extends Role("Dommerveileder")
case object Kampdelegat extends Role("Kampdelegat")
case object Fadder extends Role("Fadder")
case class Unknown(private val v:String) extends Role(v)

object Role{
  def fromString(value:String) = value match{
    case Dommer.value => Dommer
    case AssistentDommer1.value => AssistentDommer1
    case AssistentDommer2.value => AssistentDommer1
    case FjerdeDommer.value => FjerdeDommer
    case Dommerveileder.value => Dommerveileder
    case Fadder.value => Fadder
    case x => Unknown(x)
  }
}
