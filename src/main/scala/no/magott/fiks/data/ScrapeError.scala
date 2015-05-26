package no.magott.fiks.data

/**
 *
 */
abstract sealed class ScrapeError{
  def message:String
}
case class ScrapeTimeout(message:String) extends ScrapeError
case class ReAuthRequired(message:String) extends ScrapeError
case class ClientError(message:String) extends ScrapeError