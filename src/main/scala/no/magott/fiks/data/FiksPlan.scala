package no.magott.fiks.data

import unfiltered.request._
import unfiltered.response._
import unfiltered.filter.{Intent, Plan}
import no.magott.fiks.HerokuRedirect
import no.magott.fiks.calendar.VCalendar
import MatchStuff.allMatches
import unfiltered.Cookie
import java.util.concurrent.ExecutionException
import javax.servlet.http.HttpServletRequest
import com.google.common.util.concurrent.UncheckedExecutionException
import java.net.SocketTimeoutException
import QParams._
import validation.Validators._
import validation.InputField
import no.magott.fiks.user.IsBetaUser

class FiksPlan(matchservice: MatchService) extends Plan {

  def intent = {
    matchInfo orElse myMatches orElse availableMatches orElse about orElse reportInterest
  }

  val matchInfo = Intent {
    case r@Path(Seg("fiks" :: "mymatches" :: matchId :: "result" :: Nil)) & FiksCookie(loginToken) => redirectToLoginIfTimeout(r,{
      val matchOption = matchservice.assignedMatches(loginToken).find(_.fiksId == matchId)
      if(matchOption.isEmpty){
        Forbidden ~> Html5(Pages(r).forbidden)
      }else{
        r match {
          case GET(_) => redirectToLoginIfTimeout(r, {
            val matchresult = matchservice.matchResult(matchId, loginToken)
            Html5(Pages(r).assignedMatchResult(matchresult, matchresult.inputFields))
          })
          case POST(_) & Params(params) => redirectToLoginIfTimeout(r, {
            //Beta match
            if(IsBetaUser.unapply(r).isEmpty){
              Forbidden ~> Html5(Pages(r).betaOnly)
            }else{
              val matchresult = matchservice.matchResult(matchId, loginToken)
              handleSubmitMatchResult(r, params, matchresult,loginToken)
            }
          })
          case _ => MethodNotAllowed ~> Html5(Pages(r).notFound)
        }
      }
    })

    case r@GET(Path(Seg("fiks" :: "mymatches" :: matchId :: Nil))) & FiksCookie(loginToken) => redirectToLoginIfTimeout(r, {
      matchservice.matchDetails(matchId, loginToken) match {
        case Some(m) => Html5(Pages(r).assignedMatchInfo(m))
        case None => Html5(Pages(r).notFound)
      }
    })
  }

  val myMatches = Intent {
    case r@GET(Path(Seg("fiks" :: "mymatches" :: Nil))) & FiksCookie(loginToken) =>
      redirectToLoginIfTimeout(r, {
        if (allMatches(r)) {
          val assigned = matchservice.assignedMatches(loginToken)
          Ok ~> Html5(Pages(r).assignedMatches(assigned))
        } else {
          val assigned = matchservice.upcomingAssignedMatches(loginToken)
          Ok ~> Html5(Pages(r).assignedMatches(assigned))
        }
      })
    case r@GET(Path(Seg("match.ics" :: Nil))) & FiksCookie(loginToken) & Params(MatchIdParameter(matchId)) => redirectToLoginIfTimeout(r, {
      val assigned = matchservice.assignedMatches(loginToken).find(_.matchId == matchId)
      Ok ~> CalendarContentType ~> ResponseString(new VCalendar(assigned.get).feed)
    })
    case r@GET(Path(Seg(Nil))) & FiksCookie(_) => HerokuRedirect(r, "/fiks/mymatches")
    case r@GET(Path(Seg(Nil))) => HerokuRedirect(r, "/login")
  }

  val availableMatches = Intent {
    case r@GET(Path(Seg("fiks" :: "availablematches" :: Nil))) & Params(MatchIdParameter(matchId)) & FiksCookie(loginToken) => redirectToLoginIfTimeout(r, {
      val matchInfo = matchservice.availableMatchInfo(matchId, loginToken)
      Ok ~> Html5(Pages(r).reportInterestIn(matchInfo))
    })
    case r@GET(Path(Seg("fiks" :: "availablematches" :: Nil))) & FiksCookie(loginToken) => redirectToLoginIfTimeout(r, {
      val available = matchservice.availableMatches(loginToken)
      Ok ~> Html5(Pages(r).availableMatches(available))
    })
  }

  val reportInterest = Intent {
    case r@POST(Path(Seg("fiks" :: "availablematches" :: Nil))) & Params(MatchIdParameter(matchId)) & Params(CommentParameter(comment)) & FiksCookie(loginToken) => redirectToLoginIfTimeout(r, {
      matchservice.reportInterest(matchId, comment, loginToken)
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
      case e: ExecutionException => handleException(e, req)
      case e: UncheckedExecutionException => handleException(e, req)
      case e: Exception => Html5(Pages(req).error(e))
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

  def handleSubmitMatchResult[T <: HttpServletRequest](req:HttpRequest[T], params: Map[String, Seq[String]], matchResult: MatchResult, loginToken:String): ResponseFunction[Any] = {
    val finalHomeGoals = isBlankOrInt("finalHomeGoals",params("finalHomeGoals").head,"Sluttresultat er ugyldig")
    val finalAwayGoals = isBlankOrInt("finalAwayGoals",params("finalAwayGoals").head,"Sluttresultat er ugyldig")
    val halfTimeHomeGoals = isBlankOrInt("halfTimeHomeGoals", params("halfTimeHomeGoals").head, "Pauseresultat er ugyldig")
    val halfTimeAwayGoals = isBlankOrInt("halfTimeAwayGoals", params("halfTimeAwayGoals").head, "Pauseresultat er ugyldig")
    val attendance:InputField = isBlankOrInt("attendance", params("attendance").head, "Tilskuertall er ugyldig")
    val fields:Map[String,InputField] = matchResult.inputFields
      .+ (attendance.toTuple)
      .+ (isBlankOrInt("finalHomeGoals",params("finalHomeGoals").head,"Sluttresultat er ugyldig").toTuple)
      .+ (halfTimeHomeGoals.toTuple)
      .+ (halfTimeAwayGoals.toTuple)
      .++(finalHomeGoals.and(finalAwayGoals)(bothSetOrUnset("Sluttresultat er ugyldig")))
      .++(halfTimeHomeGoals.and(halfTimeAwayGoals)(bothSetOrUnset("Pauseresultat er ugyldig")))

    if(fields.values.forall(_.isValid)){
//      val m = matchResult.applyInputFields(fields)
      matchservice.postMatchResult(matchResult.applyInputFields(fields), loginToken)
      HerokuRedirect(req, "fiks/mymatches/"+ matchResult.fiksId +"/result")
    }else{
      BadRequest ~> Html5(Pages(req).assignedMatchResult(matchResult, fields))
    }
  }


  object MatchIdParameter extends Params.Extract("matchid", Params.first)

  object ResultParameter extends Params.Extract("result", Params.first)

  object CommentParameter extends Params.Extract("comment", Params.first)


}
