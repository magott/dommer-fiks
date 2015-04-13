package no.magott.fiks.calendar

import unfiltered.request._
import no.magott.fiks.HerokuRedirect
import unfiltered.response._
import unfiltered.filter.{Intent, Plan}
import javax.servlet.http.HttpServletRequest
import no.magott.fiks.user.{LoggedOnUser, UserService}
import no.magott.fiks.data.{SessionId, CalendarContentType, FiksLoginService, Pages}
import scala.concurrent.ops.spawn

class CalendarPlan(calendarService: CalendarService, userservice: UserService) extends Plan {
  val requiredParams = Vector("username", "password", "terms", "email")


  def intent = {
    calendarSignup orElse modifyCalendar orElse getFeed orElse notBeta
  }

  val calendarSignup = Intent {
    case r@POST(Path(Seg("calendar" :: "mycal" :: Nil))) & Params(p)  => handleSignup(r, p)
    case r@GET(Path(Seg("calendar" :: "mycal" :: Nil))) & SessionId(token) => {
      userservice.userForSession(token) match {
        case Some(user) => user.calendarId match {
          case Some(calendarId) => Html5(Pages(r).calendarInfo(calendarId))
          case None => Html5(Pages(r).calendarSignUpInfo)
        }
        case None => Html5(Pages(r).calendarSignUpInfo)
      }
    }
  }

  val modifyCalendar = Intent {
    case r@GET(Path(Seg("calendar" :: Nil))) & Params(FeedIdParameter(feedId) & EditCalendarParameter(action)) & LoggedOnUser(user) => {
      action match {
        case "delete" => userservice.removeCalendarFor(user.username)
        case "reset" => userservice.newCalendarId(user.username)
      }
      HerokuRedirect(r,"calendar/mycal")
    }
  }
  val getFeed = Intent {
    case r@GET(Path(Seg("calendar" :: Nil))) & Params(FeedIdParameter(feedId)) => calendarFeed(feedId, r)
  }

  val notBeta = Intent {
    case r@Path(Seg("calendar" :: _ :: Nil))  => Forbidden ~> Html5(Pages(r).betaOnly)
  }

  def calendarFeed(calendarId: String, r:HttpRequest[_]) = {
    userservice.byCalendarId(calendarId) match {
      case None => BadRequest ~> ResponseString("Ugyldig kalender id")
      case Some(user) => {
        calendarService.calendarForUser(user) match {
          case Some(matches) => {
            spawn{
              userservice.incrementPollcount(user)
              val UserAgent(ua) = r
              println("Fetching calendar username %s for agent %s".format(user.username,ua))

            }
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
      val username = params("username").head.toLowerCase
      val password = params("password").head
      val email = params("email").head
      FiksLoginService.login(username, password, false) match {
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
