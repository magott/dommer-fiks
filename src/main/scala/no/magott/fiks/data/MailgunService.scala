package no.magott.fiks.data

import scala.xml.NodeSeq
import dispatch._
import scala.util.Properties
import scala.concurrent.ExecutionContext.Implicits.global

class MailgunService {
  val mailgunApiKey = Properties.envOrNone("MAILGUN_API_KEY").get
  val mailgunAppName = Properties.envOrElse("MAILGUN_SMTP_LOGIN", "postmaster@app15901556.mailgun.org").split('@')(1)

  def sendMail(mail:MailMessage) : MailReceipt = {
    val httpFuture = Http(mailgunUrl << mail.asMailgunParams)
    val resp = httpFuture()
    if(resp.getStatusCode == 200) MailAccepted(resp.getResponseBody)
    else MailRejected(resp.getResponseBody, resp.getStatusCode)
  }

  private def mailgunUrl = {
    url("https://api.mailgun.net/v2/%s/messages".format(mailgunAppName)).as("api",mailgunApiKey).
      POST <:< (Map("Content-Type" -> "application/x-www-form-urlencoded"))
  }


}

case class MailMessage(from:String, to:Seq[String], cc:Seq[String], bcc:Seq[String], subject:String, body:String, bodyHtml:Option[NodeSeq], headers:Map[String, String] = Map.empty) {

  def asMailgunParams = Seq("from" -> from, "subject" -> subject, "text"->body) ++ to.map("to"-> _) ++ cc.map("cc" -> _) ++ bcc.map("bcc" -> _) ++ bodyHtml.map("html" -> _.toString) ++ headers.toSeq

}

object MailMessage{
  def apply(from:String, to:String, subject:String, body:String):MailMessage = MailMessage(from, to :: Nil, Nil, Nil, subject, body, None)
}

sealed trait MailReceipt{
  def isAccepted:Boolean
}
case class MailAccepted(message:String) extends MailReceipt {
  val isAccepted = true
}
case class MailRejected(message:String, errorCode:Int) extends MailReceipt {
  val isAccepted = false
}
