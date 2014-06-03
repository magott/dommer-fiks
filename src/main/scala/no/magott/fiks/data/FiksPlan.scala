package no.magott.fiks.data

import unfiltered.request._
import unfiltered.response._
import unfiltered.filter.{Intent, Plan}
import no.magott.fiks._
import no.magott.fiks.calendar.VCalendar
import MatchStuff.allMatches
import unfiltered.Cookie
import java.util.concurrent.ExecutionException
import javax.servlet.http.HttpServletRequest
import com.google.common.util.concurrent.UncheckedExecutionException
import java.net.SocketTimeoutException
import QParams._
import validation.Validators._
import validation.FormField
import no.magott.fiks.user.{LoggedOnUser, UserSession, UserService, IsBetaUser}
import org.joda.time.Interval
import no.magott.fiks.invoice.InvoiceRepository
import scala.Some
import unfiltered.response.Html
import unfiltered.response.Html5
import unfiltered.Cookie
import unfiltered.response.ResponseString
import scala.Some
import unfiltered.response.Html
import unfiltered.response.Html5
import unfiltered.Cookie
import unfiltered.response.ResponseString

class FiksPlan(matchservice: MatchService, stadiumService:StadiumService, invoiceRepository:InvoiceRepository, userService:UserService) extends Plan {

  val weatherServie = new WeatherService

  def intent = {
    matchInfo orElse myMatches orElse availableMatches orElse about orElse reportInterest
  }

  val matchInfo = Intent {
    case r@Path(Seg("fiks" :: "mymatches" :: fiksId :: "forecast" :: Nil)) & SessionId(sessionId) => redirectToLoginIfTimeout(r,{
      val session = userService.userSession(sessionId).get //TODO: YOLO
      val matchOption = matchservice.assignedMatches(session).find(_.fiksId == fiksId)
      if(matchOption.isEmpty) Forbidden ~> Html5(<div>Feil</div>)
      else{
        val m = matchOption.get
        val stadiumOpt = stadiumService.findStadium(m.venue)
        if(stadiumOpt.isEmpty){
          NotFound ~> Html5(<div>{"Fant ikke arenaen %s".format(m.venue)}</div>)
        }else{
          val forecast = weatherServie.findForecast(m.date, m.date.plus(m.playingTime.toDuration), stadiumOpt.get.latLong)
          if(forecast.isEmpty){
            NotFound ~> CacheControl("public, max-age=3600") ~> Html5(<div>Fant ikke værmelding for denne datoen</div>)
          }else{
            Ok ~> CacheControl("public, max-age=3600") ~> Html5(Snippets(r).forecasts(forecast))
          }
        }
      }
    })
    case r@Path(Seg("fiks" :: "mymatches" :: fiksId :: "yield" :: Nil)) & SessionId(sessionId)  & Params(CancellationIdParameter(cancellationId)) => redirectToLoginIfTimeout(r,{
      val session = userService.userSession(sessionId).get //TODO: YOLO
      r match{
        case POST(_) => {
          val m = matchservice.assignedMatches(session).find(_.fiksId == fiksId)
          if(m.isEmpty)
            Forbidden ~> Html(Pages(r).forbidden)
          else{
            val reason = ReasonParameter.unapply(Params.unapply(r).get).get
            matchservice.yieldMatch(cancellationId, reason, session)
            HerokuRedirect(r,"/fiks/mymatches")
          }
        }
        case GET(_) => {
          val m = matchservice.assignedMatches(session).find(_.fiksId == fiksId)
          if(m.isEmpty)
            Forbidden ~> Html(Pages(r).forbidden)
          else{
            Ok ~> Html5(Pages(r).yieldMatch(m.get))
          }
        }
      }
    })
    case r@Path(Seg("fiks" :: "mymatches" :: fiksId :: "invoice" :: Nil)) & SessionId(sessionId)  => redirectToLoginIfTimeout(r,{
      val session = userService.userSession(sessionId).get //TODO: YOLO
      r match {
        case GET(_) => {
          val invoiceOpt = invoiceRepository.findInvoice(session.username, fiksId)
          if(invoiceOpt.isDefined){
            HerokuRedirect(r, s"/invoice/${invoiceOpt.get.id.get.toString}")
          }else{
            val matchOpt = matchservice.assignedMatches(session).find(_.fiksId == fiksId)
            if(matchOpt.isDefined){
              HerokuRedirect(r, s"/invoice/new?matchid=$fiksId")
            }else{
              Forbidden ~> Html5(Pages(r).forbidden)
            }
          }
        }
        case POST(_) => MethodNotAllowed ~> ResponseString("Method not allowed. Allowed methods: GET")
      }
    })

    case r@Path(Seg("fiks" :: "mymatches" :: fiksId :: "contacts" :: role :: Nil)) & SessionId(sessionId) => redirectToLoginIfTimeout(r,{
      val session = userService.userSession(sessionId).get //TODO: YOLO
      val vcard = matchservice.assignedMatches(session).find(_.fiksId == fiksId).flatMap(_.refereeTuples.find(_._1 == role)).map(x=> new VCard(x._2))
      if(vcard.exists(_.canBeVCard)){
        Ok ~> VCardContentType ~> ResponseString(vcard.get.asVCardString)
      }else{
        NotFound ~> Html5(Pages(r).notFound)
      }
    })
    case r@Path(Seg("fiks" :: "mymatches" :: fiksId :: "result" :: Nil)) & SessionId(sessionId) => redirectToLoginIfTimeout(r,{
      val session = userService.userSession(sessionId).get
      val matchOption = matchservice.assignedMatches(session).find(_.fiksId == fiksId)
      if(matchOption.isEmpty || !matchOption.get.isReferee){
        Forbidden ~> Html5(Pages(r).forbidden)
      }else{
        r match {
          case GET(_) => redirectToLoginIfTimeout(r, {
            val matchresult = matchservice.matchResult(fiksId, session)
            Html5(Pages(r).assignedMatchResult(matchresult, matchresult.asInputFields))
          })
          case POST(_) & Params(params) => redirectToLoginIfTimeout(r, {
              val matchresult = matchservice.matchResult(fiksId, session)
              handleSubmitMatchResult(r, params, matchresult, session)
          })
          case _ => MethodNotAllowed ~> Html5(Pages(r).notFound)
        }
      }
    })

    case r@GET(Path(Seg("fiks" :: "mymatches" :: matchId :: Nil))) & SessionId(sessionId) => redirectToLoginIfTimeout(r, {
      val session = userService.userSession(sessionId).get //TODO: Yolo
      matchservice.matchDetails(matchId, session) match {
        case Some(m) => Html5(Pages(r).assignedMatchInfo(m))
        case None => Html5(Pages(r).notFound)
      }
    })
  }

  val myMatches = Intent {
    case r@GET(Path(Seg("fiks" :: "mymatches" :: Nil))) & SessionId(loginToken) => {
      val session = userService.userSession(loginToken).get //TODO: YOLO
      redirectToLoginIfTimeout(r, {
        if (allMatches(r)) {
          val assigned = matchservice.assignedMatches(session)
          Ok ~> Html5(Pages(r).assignedMatches(assigned))
        } else {
          val assigned = matchservice.upcomingAssignedMatches(session)
          Ok ~> Html5(Pages(r).assignedMatches(assigned))
        }
      })
    }
    case r@GET(Path(Seg("match.ics" :: Nil))) & SessionId(sessionId) & Params(MatchIdParameter(matchId)) => redirectToLoginIfTimeout(r, {
      val session = userService.userSession(sessionId).get //TODO: YOLO
      val assigned = matchservice.assignedMatches(session).find(_.matchId == matchId)
      Ok ~> CalendarContentType ~> ResponseString(new VCalendar(assigned.get).feed)
    })
    case r@GET(Path(Seg(Nil))) & SessionId(_) => HerokuRedirect(r, "/fiks/mymatches")
    case r@GET(Path(Seg(Nil))) => HerokuRedirect(r, "/login")
  }

  val availableMatches = Intent {
    case r@GET(Path(Seg("fiks" :: "availablematches" :: Nil))) & Params(MatchIdParameter(matchId)) & SessionId(loginToken) => redirectToLoginIfTimeout(r, {
      val session = userService.userSession(loginToken).get //TODO: YOLO
      val matchInfo = matchservice.availableMatchInfo(matchId, session)
      Ok ~> Html5(Pages(r).reportInterestIn(matchInfo))
    })
    case r@GET(Path(Seg("fiks" :: "availablematches" :: Nil))) & SessionId(loginToken) => redirectToLoginIfTimeout(r, {
      val session = userService.userSession(loginToken).get //TODO: YOLO
      val available = matchservice.availableMatches(session)
      Ok ~> Html5(Pages(r).availableMatches(available))
    })
  }

  val reportInterest = Intent {
    case r@POST(Path(Seg("fiks" :: "availablematches" :: Nil))) & Params(MatchIdParameter(matchId)) & Params(CommentParameter(comment)) & SessionId(loginToken) => redirectToLoginIfTimeout(r, {
      val session = userService.userSession(loginToken).get //TODO: YOLO
      matchservice.reportInterest(matchId, comment, session)
      HerokuRedirect(r, "/fiks/availablematches")
    })
  }

  val about = Intent {
    case r@GET(Path(Seg("fiks" :: "about" :: Nil))) => Ok ~> Html(Pages(r).about)
  }

  def redirectToLoginIfTimeout[T <: HttpServletRequest](req: HttpRequest[T], f: => ResponseFunction[Any]) = {
    try {
      f
    } catch {
      case e: SessionTimeoutException => SetCookies(Cookie(name = "fiksToken", value = "", maxAge = Some(0))) ~> displayReauthentication(req)
      case e: ExecutionException => GatewayTimeout ~> handleException(e, req)
      case e: UncheckedExecutionException => GatewayTimeout ~> handleException(e, req)
      case e: Exception => InternalServerError ~> Html5(Pages(req).error(e))
    }
  }

  def handleException[T <: HttpServletRequest](e: Exception, req: HttpRequest[T]) = {
    if (e.getCause.isInstanceOf[SessionTimeoutException]) {
      SetCookies(Cookie(name = "fiksToken", value = "", maxAge = Some(0))) ~> displayReauthentication(req)
    } else if (e.getCause.isInstanceOf[SocketTimeoutException]) {
      Html5(Pages(req).error(e.getCause.asInstanceOf[SocketTimeoutException]))
    } else {
      println("EXCEPTION " + e.getClass + " : " + e.getMessage + "\n" + e.getStackTraceString)
      Html5(Pages(req).error(e))
    }
  }

  def displayReauthentication[A](req: HttpRequest[A]) = {
    HerokuRedirect(req, "/login?message=sessionTimeout")
  }

  def handleSubmitMatchResult[T <: HttpServletRequest](req:HttpRequest[T], params: Map[String, Seq[String]], matchResult: MatchResult, session:UserSession): ResponseFunction[Any] = {
    val finalHomeGoals = isBlankOrInt("finalHomeGoals",params("finalHomeGoals").head,"Sluttresultat er ugyldig")
    val finalAwayGoals = isBlankOrInt("finalAwayGoals",params("finalAwayGoals").head,"Sluttresultat er ugyldig")
    val halfTimeHomeGoals = isBlankOrInt("halfTimeHomeGoals", params("halfTimeHomeGoals").head, "Pauseresultat er ugyldig")
    val halfTimeAwayGoals = isBlankOrInt("halfTimeAwayGoals", params("halfTimeAwayGoals").head, "Pauseresultat er ugyldig")
    val attendance:FormField = isBlankOrInt("attendance", params("attendance").head, "Tilskuertall er ugyldig")
    val fields:Map[String,FormField] = matchResult.asInputFields
      .+ (attendance.toTuple)
      .+ (isBlankOrInt("finalHomeGoals",params("finalHomeGoals").head,"Sluttresultat er ugyldig").toTuple)
      .+ (halfTimeHomeGoals.toTuple)
      .+ (halfTimeAwayGoals.toTuple)
      .++(finalHomeGoals.and(finalAwayGoals)(bothSetOrUnset("Sluttresultat er ugyldig")))
      .++(halfTimeHomeGoals.and(halfTimeAwayGoals)(bothSetOrUnset("Pauseresultat er ugyldig")))

    if(fields.values.forall(_.isValid)){
      matchservice.postMatchResult(matchResult.applyFormFields(fields), session)
      HerokuRedirect(req, "fiks/mymatches/"+ matchResult.fiksId +"/result")
    }else{
      BadRequest ~> Html5(Pages(req).assignedMatchResult(matchResult, fields))
    }
  }


}



