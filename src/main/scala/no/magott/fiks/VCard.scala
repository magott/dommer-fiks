package no.magott.fiks

class VCard(refString:String) {

  def canBeVCard = fullName.isDefined

  def asVCardString:String = {
      s"""BEGIN:VCARD
      |VERSION:2.1
      |N;CHARSET=UTF-8:${lastName.getOrElse("")};${firstName.getOrElse("")};;;
      |FN;CHARSET=UTF-8:${fullName.getOrElse("")}
      |TEL;CELL;VOICE:${mobile.map("+47"+_).getOrElse("")}
      |TEL;HOME;VOICE:${home.map("+47"+_).getOrElse("")}
      |END:VCARD"""
        .stripMargin
  }

  def vcardStringOption = {
    if(canBeVCard) Some(asVCardString) else None
  }

  import scala.util.control.Exception._
  def mobile:Option[Int] = phoneNumber("Mobil:")
  def home:Option[Int] = phoneNumber("Tlf.:")
  lazy val fullName:Option[String] = allCatch.opt(refString.substring(0,refString.indexOf("Tlf.:")).trim.replaceAllLiterally("\u00A0",""))
  def lastName = fullName.map(_.split(' ').last)
  def firstName = fullName.map(_.split(' ').dropRight(1).mkString(" "))

  def phoneNumber(prefix:String) = {
    val prefixLength = prefix.length
    allCatch.opt(refString.substring(refString.lastIndexOf(prefix)+prefixLength, refString.lastIndexOf(prefix)+prefixLength+8).toInt)
  }

}
