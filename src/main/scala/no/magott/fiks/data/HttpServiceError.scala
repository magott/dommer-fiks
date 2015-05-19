package no.magott.fiks.data

/**
 *
 */
sealed abstract class HttpServiceError

case class JsonParseError(errorMessage:String) extends HttpServiceError
case class HttpError(statusCode:Int, message:String) extends HttpServiceError
