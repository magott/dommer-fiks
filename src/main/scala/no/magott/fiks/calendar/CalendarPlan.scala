package no.magott.fiks.calendar

import unfiltered.request._
import no.magott.fiks.HerokuRedirect
import unfiltered.response._
import no.magott.fiks.data.{CalendarContentType, FiksLoginService, Pages}
import unfiltered.filter.{Intent, Plan}
import javax.servlet.http.HttpServletRequest
import no.magott.fiks.user.{LoggedOnUser, IsBetaUser, UserService}

class CalendarPlan(calendarService: CalendarService, userservice: UserService) extends Plan {
  val requiredParams = Vector("username", "password", "terms", "email")


  def intent = {
    calendarSignup orElse modifyCalendar orElse getFeed orElse notBeta
  }

  val calendarSignup = Intent {
    case r@POST(Path(Seg("calendar" :: "mycal" :: Nil))) & Params(p) & IsBetaUser(beta) => handleSignup(r, p)
    case r@GET(Path(Seg("calendar" :: "mycal" :: Nil))) & LoggedOnUser(user) & IsBetaUser(betauser) => {
      user.calendarId match {
        case Some(calendarId) => Html5(Pages(r).calendarInfo(calendarId))
        case None => Html5(Pages(r).calendarSignup())
      }
    }
  }

  val modifyCalendar = Intent {
    case r@GET(Path(Seg("calendar" :: Nil))) & Params(FeedIdParameter(feedId) & EditCalendarParameter(action)) & LoggedOnUser(user) => {
      action match {
        case "delete" => userservice.removeCalendarFor(user.username)
        case "reset" => userservice.newCalendarId(user.username)
      }
      HerokuRedirect(r,"calendar/mycal") //Delete or refresh add & Username(user)
    }
  }
  val getFeed = Intent {
    case r@GET(Path(Seg("calendar" :: Nil))) & Params(FeedIdParameter(feedId)) => calendarFeed(feedId)
  }

  val notBeta = Intent {
    case r@Path(Seg("calendar" :: _ :: Nil))  => Html5(Pages(r).betaOnly)
  }

  def calendarFeed(calendarId: String) = {
    userservice.byCalendarId(calendarId) match {
      case None => BadRequest ~> ResponseString("Ugyldig kalender id")
      case Some(user) => {
        calendarService.calendarForUser(user) match {
          case Some(matches) => {
            userservice.incrementPollcount(user)
            Ok ~> CalendarContentType ~> ResponseString(new VCalendar(matches).feed)
          }
          case None => Unauthorized ~> ResponseString("Ugyldig brukernavn/passord, har du byttet passord? Slett kalender og opprett på nytt")
        }
      }
    }

  }

  def handleSignup[T <: HttpServletRequest](req: HttpRequest[T], params: Map[String, Seq[String]]) = {
    val missingParams = requiredParams.filter(params.getOrElse(_, Nil).mkString("").isEmpty)
    if (!missingParams.isEmpty) {
      Html5(Pages(req).calendarSignup(missingParams))
    } else {
      val username = params("username").head
      val password = params("password").head
      val email = params("email").head
      FiksLoginService.login(username, password) match {
        case Left(_) => Html5(Pages(req).calendarSignup("badcredentials" :: Nil))
        case Right(_) => {
          userservice.newUser(username, password, email)
          HerokuRedirect(req, "/calendar/mycal")
        }
      }
    }
  }

  object FeedIdParameter extends Params.Extract("id", Params.first)

  object EditCalendarParameter extends Params.Extract("action", Params.first)


}
