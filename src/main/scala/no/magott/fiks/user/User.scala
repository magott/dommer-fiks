package no.magott.fiks.user

import java.util.UUID

import com.mongodb.DBObject
import com.mongodb.casbah.commons.MongoDBObject
import no.magott.fiks.ParameterImplicits
import no.magott.fiks.data.FiksLoginService

import scala.xml.Utility

case class User(username: String, password: Option[String], email: String, calendarId: Option[String], beta: Boolean, invoiceData: Option[InvoiceData]) {

  def toMongo = {
    val builder = MongoDBObject.newBuilder
    builder += "username" -> username
    password.map(builder += "password" -> _)
    builder += "email" -> email
    builder += "beta" -> beta
    if (calendarId.isDefined) {
      builder += "calid" -> calendarId
    }
    invoiceData.map(builder += "invoiceData" -> _.toMongo)
    builder.result
  }

  def applyUpdates(u: User) = {
    val cal = if(u.password.isEmpty) None else if(calendarId.isEmpty) Some(UUID.randomUUID().toString) else calendarId
    copy(password = u.password, email = u.email, calendarId = cal, invoiceData = u.invoiceData)
  }

  def nameForInvoice = invoiceData.flatMap(_.name).getOrElse("")
  def muncipalForInvoice = invoiceData.flatMap(_.taxMuncipal).getOrElse("")
  def accountForInvoice = invoiceData.flatMap(_.accountNumber).getOrElse("")
  def addressForInvoice = invoiceData.flatMap(_.address).getOrElse("")
  def zipForInvoice = invoiceData.flatMap(_.postalCode).getOrElse("")
  def cityForInvoice = invoiceData.flatMap(_.city).getOrElse("")
  def phoneForInvoice = invoiceData.flatMap(_.phone).getOrElse("")

}

object User{

  def fromMongo(mo: MongoDBObject) = new User(mo.getAs[String]("username").get,
    mo.getAs[String]("password"),
    mo.getAsOrElse[String]("email", ""),
    Option(mo.getAs[String]("calid").getOrElse(null)),
    mo.getAs[Boolean]("beta").getOrElse(false),
    mo.getAs[DBObject]("invoiceData").map(InvoiceData.fromMongo(_)))

  import scalaz.Scalaz._
  def validate(username: String, password: Option[String], email: Option[String], calendarId: Option[String], name: Option[String], address: Option[String], postalCode:Option[String], city:Option[String], phone:Option[String], accountNumber:Option[String], taxMuncipal:Option[String]) = {

    val vPassword = validatePassword(username, password)
    val vEmail = validateEmail(email)
    val validated = (vPassword |@| vEmail ) {
          val invoiceData = if(List(name, address, postalCode, city, phone, accountNumber, taxMuncipal).exists(_.nonEmpty))
            Some(InvoiceData(name, address, postalCode, city, phone, accountNumber, taxMuncipal))
          else None
      (pwd, em) => User(username, pwd, em, calendarId, false, invoiceData)
    }
    validated.disjunction
  }

  private def validatePassword(username:String, password:Option[String]) = {
    password.map { p =>
      if (FiksLoginService.login(username, p, false).isRight) password.successNel
      else "Passordet stemmer ikke med passordet i FIKS".failureNel
    }.getOrElse(password.successNel)
  }

  private def validateEmail(email:Option[String]) = {
    val emailRegex = """\b[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}\b"""
    if(email.isEmpty) "E-post må fylles ut".failureNel
    else if(!email.get.matches(emailRegex)) "Ugydlig e-post adresse".failureNel
    else email.get.toLowerCase.successNel
  }
}

case class InvoiceData(name:Option[String], address: Option[String], postalCode:Option[String], city:Option[String], phone:Option[String], accountNumber:Option[String], taxMuncipal:Option[String]) {
  def toMongo = {
    val builder = MongoDBObject.newBuilder
    name.foreach(builder += "name" -> _)
    address.foreach(builder += "address" -> _)
    postalCode.foreach(builder += "zip" -> _)
    city.foreach(builder += "city" -> _)
    phone.foreach(builder += "phone" -> _)
    accountNumber.foreach(builder += "accountNo" -> _)
    taxMuncipal.foreach(builder += "taxMuncipal" -> _)
    builder.result
  }
}

object InvoiceData{
  import com.mongodb.casbah.Implicits._
  def fromMongo(mo: DBObject) : InvoiceData = new InvoiceData(
    mo.getAs[String]("name"),
    mo.getAs[String]("address"),
    mo.getAs[String]("zip"),
    mo.getAs[String]("city"),
    mo.getAs[String]("phone"),
    mo.getAs[String]("accountNo"),
    mo.getAs[String]("taxMuncipal")
  )
}
