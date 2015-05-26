package no.magott.fiks.vacation

import argonaut.Json
import argonaut._, Argonaut._
import org.joda.time.{LocalTime, DateTime, LocalDate, LocalDateTime}

/**
 *
 */
case class Vacation(id:Option[Long], start:LocalDateTime, end:LocalDateTime, reason:Option[String]) {

  def asJson = Json.obj(
    "id" := id,
    "start" := start.toString,
    "end" := end.toString,
    "reason" := reason.getOrElse("")
  )

  def fiksStartDateParam = start.toString("yyyy-MM-dd")
  def fiksStartTimeParam = start.toString("HH:mm")
  def fiksEndDateParam = end.toString("yyyy-MM-dd")
  def fiksEndTimeParam = end.toString("HH:mm")

}

object Vacation{
  import scalaz._, Scalaz._

  def create(from: LocalDateTime, to: LocalDateTime, reason:Option[String]) = {
    Vacation(None, from, to, reason)
  }

  def validate(fromDate:Option[String], fromTime: Option[String], toDate:Option[String], toTime:Option[String], reason:Option[String]): NonEmptyList[String] \/ Vacation = {
    def validFromDate = validateLocalDate(fromDate, "Fra dato").validation.toValidationNel
    def validToDate = validateLocalDate(toDate, "Til dato").validation.toValidationNel
    def validFromTime = validateTime(fromTime, "Fra klokkeslett").validation.toValidationNel
    def validToTime = validateTime(toTime, "Til klokkeslett").validation.toValidationNel

    (validFromDate |@| validFromTime |@| validToDate |@| validToTime){
      (fromD, fromT, toD, toT) => {
        Vacation.create(fromD.toLocalDateTime(fromT.getOrElse(LocalTime.MIDNIGHT)), toD.toLocalDateTime(toT.getOrElse(LocalTime.MIDNIGHT.minusMinutes(1))), reason)
      }
    }.disjunction
  }

  def validateLocalDate(iso:Option[String], fieldName:String) : String \/ LocalDate = {
    for {
      i <- iso \/> s"$fieldName mangler"
      d <- \/.fromTryCatch(DateTime.parse(i).toLocalDate).leftMap(_ => s"$fieldName er ugydlig")
    } yield d
  }

  def validateTime(iso:Option[String], fieldName:String) : String \/ Option[LocalTime] = {
    iso.map(i =>
      \/.fromTryCatch(LocalTime.parse(i)).rightMap(lt => Some(lt)).leftMap(_ => s"$fieldName er ugyldig")
    ).getOrElse(None.right)
  }
}
