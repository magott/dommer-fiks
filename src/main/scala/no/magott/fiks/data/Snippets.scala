package no.magott.fiks.data

import xml.{XML, NodeSeq}
import org.joda.time.{DateTimeZone, DateTime, LocalDateTime}
import unfiltered.request.{Path, Seg, HttpRequest}
import javax.servlet.http.HttpServletRequest
import MatchStuff.allMatches
import validation.InputField

case class Snippets[T <: HttpServletRequest] (req: HttpRequest[T]) {

  val calendarFormatString = "yyyyMMdd'T'HHmmss'Z"
  val isLoggedIn = FiksCookie.unapply(req).isDefined && FiksCookie.unapply(req).get.nonEmpty
  val pages = Path(req)

  def navbar(page: Option[String]) = {
    println("Pages: "+pages)
    <div class="navbar navbar-fixed-top">
      <div class="navbar-inner">
        <div class="container">
          <button type="button" class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </button>
          <a class="brand" href="/">Dommer-FIKS</a>
          <div class="nav-collapse">
            <ul class="nav">
            {
              if (isLoggedIn) {
                <li class={if (pages.contains("mymatches")) "active" else "inactive"}>
                  <a href="/fiks/mymatches">Mine oppdrag</a>
                </li>
                <li class={if (pages.contains("availablematches")) "active" else "inactive"}>
                    <a href="/fiks/availablematches">Ledige oppdrag</a>
                </li>
              }
            }<li class={if (pages.contains("about")) "active" else "inactive"}>
              <a href="/fiks/about">Om</a>
            </li>
            {

             if(isLoggedIn){
               <li class={if (pages.contains("calendar")) "active" else "inactive"}>
                 <a href="/calendar/mycal">Kalender</a>
               </li>

               <li class={"inactive"}>
                 <a href="/logout">Logg ut</a>
               </li>
             }else{
               <li class={if (pages.contains("login")) "active" else "inactive"}>
                 <a href="/login" >Logg inn</a>
               </li>
             }

            }
            </ul>
          </div> <!--/.nav-collapse -->
        </div>
      </div>
    </div>
  }

  def emptyPage(body: NodeSeq, page: Option[String] = None): NodeSeq =
    <html lang="en">
      <head>
          <meta charset="utf-8"/>
        <title>Fiks fix</title>
          <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
          <meta name="description" content="Fiks, without the #fail"/>
          <meta name="author" content="Morten Andersen-Gott"/>
          <meta name="google-site-verification" content="ptF2AFWdgpfQFz8_Uu2o_kDR704noD60eKR4nHC3uT8"/>
          <link rel="shortcut icon" href="/favicon.ico" />

        <!-- Le styles -->
          <link href="/css/bootstrap.min.css" rel="stylesheet"/>
        <style type="text/css">
          {"""body
        {padding-top: 60px; /* 60px to make the container go all the way to the bottom of the topbar */}
        """}
        </style>
          <link href="/css/bootstrap-responsive.min.css" rel="stylesheet"/>
          <link href="/css/fiks.css" rel="stylesheet"/>

        <!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
        <!--[if lt IE 9]>
    <script src="//html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->
      </head>
      <body>

        {navbar(page)}<div class="container">

        {body}

      </div> <!-- /container -->
        <footer class="footer">
          <p>
            <a href="http:///www.andersen-gott.com">Morten Andersen-Gott</a>
            (c) 2012</p>
          <p>
            <a href="http://www.facebook.com/dommerfiks">Foreslå forbedringer eller rapportér feil</a>
          </p>
        </footer>


        <!-- Placed at the end of the document so the pages load faster -->
        <script src="/js/jquery.js"></script>
        <script src="/js/bootstrap.min.js"></script>
        <script type="text/javascript">
          {"""
          var _gaq = _gaq || [];
          _gaq.push(['_setAccount', 'UA-29904717-1']);
          _gaq.push(['_trackPageview']);
          (function() {
          var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
  ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
  var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
  })();
  """}

        </script>
       </body>
    </html>

  def tableOfAssignedMatches(assignedMatches: List[AssignedMatch]) = {
    <small>
      {
        if(allMatches(req)){
          <a href="mymatches">Skjul spilte kamper</a>
        }else{
          <a href="mymatches?all">Vis spilte kamper</a>
        }
      }
      </small>

    <table class="table table-striped table-bordered table-condensed">
      <thead>
        <tr>
          <th>Dato</th>
          <th>Tid</th>
          <th>Turnering</th>
          <th>Kamp</th>
          <th>Sted</th>
          <th>Dommere</th>
          <th>Kalender</th>
        </tr>

      </thead>
      <tbody>
        {assignedMatches.map {
        m =>
          <tr>
            <td>
              {m.date.toString("dd.MM.YYYY")}
            </td>
            <td>
              {m.date.toString("HH:mm")}
            </td>
            <td>
              {m.tournament}
            </td>
            <td>
              <a href={"mymatches/"+m.fiksId+"/"}>
                {m.teams}
              </a>
            </td>
            <td>
              {m.venue}
            </td>
            <td>
              {m.referees}
            </td>
            <td>
              {googleCalendarLink(m.date, m.teams, m.venue, m.referees)}
              |
              {icsLink(m.date, m.teams, m.venue, m.referees, m.matchId)}
            </td>
          </tr>
      }}
      </tbody>
    </table>

  }

  def assignedMatchDetailsTable(m:AssignedMatch) = {
    <ul class="nav nav-tabs">
      <li class="active">
        <a href="./">Info</a>
      </li>
      {
        if(m.isReferee){
          <li class="inactive"><a href="./result">Resultat</a></li>
        }
      }
    </ul>
    <table class="table table-striped table-bordered table-condensed">
        <tr>
          <th>Kampnummer</th>
          <td>{m.matchId}</td>
        </tr>
        <tr>
        <th>Kamp</th>
        <td>{m.teams}</td>
      </tr>
      <tr>
        <th>Dato</th>
        <td>{m.date.toString("dd.MM.yyyy")}</td>
      </tr>
      <tr>
        <th>Tidspunkt</th>
        <td>{m.date.toString("HH:mm")}</td>
      </tr>
      {
        m.refereeTuples.map(t =>
          <tr>
            <th>{t._1}</th>
            <td>{t._2}</td>
          </tr>
        )
      }
      <tr>
        <th>Sted</th>
        <td>{m.venue}</td>
      </tr>
      <tr>
        <th>Turnering</th>
        <td>{m.tournament}</td>
      </tr>
      <tr>
        <th>Mer info</th>
        <td><a href={m.externalMatchInfoUrl} target="_blank">Kampinfo fra fotball.no</a></td>
      </tr>
      <tr>
        <th></th>
        <td>
          <a class="btn btn-primary" href="/fiks/mymatches"><i class="icon-circle-arrow-left icon-white"></i> Tilbake</a>
        </td>
      </tr>
    </table>
  }

  def assignedMatchResultForm(r: MatchResult, fields:Map[String, InputField] = Map.empty) = {
    <ul class="nav nav-tabs">
      <li class="inactive">
        <a href="./">Info</a>
      </li>
      <li class="active"><a href="result">Resultat</a></li>
    </ul>
    <p>
      <h3>{fields("teams").value.get} <small>{fields("matchId").value.get}</small></h3>
    </p>
    <div>
    {val errorMsgs:Set[String] = fields.filter(_._2.isError).values.map(_.errorMessage.get).toSet
     if(!errorMsgs.isEmpty){
       <div class="alert alert-error">
         {errorMsgs.map(s => <div>{s}</div>)}
       </div>
     }
    }
    </div>
    <form action="result" class="form-horizontal" method="post">
      <fieldset>
        <div class={controlGroup(fields, "finalHomeGoals", "finalAwayGoals")}>
          <label class="control-label" for="finalHomeGoals">Sluttresultat</label>
          <div class="controls">
              <input type="number" class="input-micro" id="finalHomeGoals" name="finalHomeGoals" value={fields("finalHomeGoals").value.getOrElse("")}/>
              -
              <input type="number" class="input-micro" id="finalAwayGoals" name="finalAwayGoals" value={fields("finalAwayGoals").value.getOrElse("")}/>
           </div>
        </div>
        <div class={controlGroup(fields, "halfTimeHomeGoals", "halfTimeAwayGoals")}>
          <label class="control-label" for="halfTimeHomeGoals">Pauseresultat</label>
          <div class="controls">
              <input type="number" class="input-micro" id="halfTimeHomeGoals" name="halfTimeHomeGoals" value={fields("halfTimeHomeGoals").value.getOrElse("")}/>
            -
              <input type="number" class="input-micro" id="halfTimeAwayGoals" name="halfTimeAwayGoals" value={fields("halfTimeAwayGoals").value.getOrElse("")}/>
          </div>
        </div>
        <div class="control-group">
          <label class="control-label" for="attendance">Antall tilskuere</label>
          <div class="controls">
              <input type="number" class="input-mini" name="attendance" id="attendance" value={fields("attendance").value.getOrElse("")}/>
          </div>
        </div>
        <!--
        <div class="control-group">
          <label class="control-label" for="protestHome">Protest varslet</label>
          <div class="controls">
            <input type="checkbox" name="protestHome"/> Hjemmelag
          </div>
          <div class="controls">
            <input type="checkbox" name="protestHome"/> Bortelag
          </div>
        </div>
        -->
        <div>
          <span class="label label-warning">NB!</span>
          Denne forenklete resultatrapporteringen er <strong>ikke egnet</strong> for cup-kamper med ekstraomganger og straffesparkkonkurranse
        </div>
        <div class="form-actions">
          <a class="btn" href="./">Tilbake</a>
          <button type="submit" class="btn btn-primary">Send inn <sup>beta</sup></button>
        </div>
      </fieldset>
    </form>
  }

  def tableOfAvailableMatches(availableMatches: List[AvailableMatch]) = {
    <table class="table table-striped table-bordered table-condensed">
      <thead>
        <tr>
          <th>Tid</th>
          <th>Turnering</th>
          <th>Kamp</th>
          <th>Sted</th>
          <th>Type</th>
          <th>Meld interesse</th>
        </tr>
      </thead>
      <tbody>
        {availableMatches.map {
        m =>
          <tr>
            <td>
              {m.date.toString("dd.MM.yyyy HH:mm")}
            </td>
            <td>
              {m.tournament}
            </td>
            <td>
              {m.teams}
            </td>
            <td>
              {m.venue}
            </td>
            <td>
              {m.role}
            </td>
            <td>
              {m.availabilityId match {
              case Some(id) => <a href={"""availablematches?matchid=""" + m.availabilityId.get + """"""}>Meld interesse</a>
              case None => <div>Meldt inn</div>
            }}
            </td>
          </tr>
      }}
      </tbody>
    </table>
  }

  def loginMessages
  (messageParams: Map[String, Seq[String]]) = {
    messageParams.get("message") match {
      case Some(msg) => msg.mkString("") match {
        case "loginRequired" => <div class="alert alert-info">Du må logge inn for å få tilgang til denne siden</div>
        case "loginFailed" => <div class="alert alert-error">Login feilet, prøv igjen</div>
        case "sessionTimeout" => <div class="alert alert">Sesjonen din er for gammel, du må logge inn på nytt</div>
        case "logout" => <p><strong>Du er nå logget ut</strong></p>
        case _ =>
      }
      case None =>
    }
  }

  def reportInterestForm(availableMatch: AvailableMatch) = {
    <table class="table table-striped table-bordered table-condensed">
      <tr>
        <th>Oppdrag</th>
        <td>
          {availableMatch.role}
        </td>
      </tr>
      <tr>
        <th>Kamp</th>
        <td>
          {availableMatch.teams}
        </td>
      </tr>
      <tr>
        <th>Tidspunkt</th>
        <td>
          {availableMatch.date.toString("dd.MM.yyyy HH:mm")}
        </td>
      </tr>
      <tr>
        <th>Bane</th>
        <td>
          {availableMatch.venue}
        </td>
      </tr>
      <tr>
        <th>Turnering</th>
        <td>
          {availableMatch.tournament}
        </td>
      </tr>
      <form class="well" action={"""availablematches?matchid=""" + availableMatch.availabilityId.get} method="post">
        <tr>
          <th>Kommentarer</th>
          <td>
              <textarea name="comment" id="comment"/>
          </td>
        </tr>
        <tr>
            <td/>
          <td>
            <button type="submit" class="btn btn-primary">Meld interesse</button>
          </td>
        </tr>
      </form>
    </table>
  }

  def calendarSignupForm(validationErrors: Seq[String]) = {
    <legend>Sett opp kalender</legend>
      <p>
        I <a href="fiks/mymatches">ditt kampoppsett</a> er det mulig å eksportere hver enkelt kamp til din kalender.
        Dersom du ønsker en kalender feed som automatisk oppdaterer din kalender (f.eks Outlook, Google Calendar eller iCal)
        når nye kamper kommer til, kamper endrer tidspunkt eller faller fra trenger Dommer-FIKS å lagre ditt brukernavn og passord.
        Passordet ditt vil lagres kryptert slik at det ikke er mulig for andre å stjele ditt passord.
        Dersom du ikke ønsker dette anbefales det at du bruker den manuelle eksportmuligheten under <a href="fiks/mymatches">mitt kampoppsett</a>.
        Hvis du ønsker å benytte deg av muligheten for automatisk oppdatert kalender, må du fylle ut skjema under.
      </p>
      <form class="well" action="mycal" method="post">
        <div>
          {
            if(validationErrors.contains("badcredentials")){
              <div class="alert alert-error">
                Ugyldig brukernavn/passord, prøv igjen
              </div>
            }
          }
        </div>
        <label>Brukernavn</label>
        {
          if(validationErrors.contains("username")){
            <div class="control-group error">
            <input type="text" name="username" id="inputError"/>
            <span class="help-inline">Brukernavn må fylles ut</span>
            </div>
          }else{
            <input type="text" name="username"/>
          }
        }

        <label>Passord</label>
        {
          if(validationErrors.contains("password")){
          <div class="control-group error">
              <input type="password" name="password" id="inputError"/>
            <span class="help-inline">Passord må fylles ut</span>
          </div>
          }else{
            <input type="password" name="password"/>
          }
        }

        <label>E-post</label>
        {
          if(validationErrors.contains("email")){
            <div class="control-group error">
                <input type="email" name="email" id="inputError"/>
              <span class="help-inline">E-post må fylles ut</span>
            </div>
          }else{
              <input type="email" name="email"/>
              <span class="help-inline">Brukes <strong>kun</strong> dersom Dommer-FIKS har viktige meldinger til deg om tjenesten</span>
          }
        }
        {
          if(validationErrors.contains("terms")){
            <div class="control-group error">
              <label class="checkbox">
                  <input type="checkbox" name="terms"/>
                Jeg godtar at Dommer-FIKS oppbevarer mitt brukernavn og passord (påkrevd)
              </label>
            </div>
          }else{
            <label class="checkbox"><input type="checkbox" name="terms"/>
              Jeg godtar at Dommer-FIKS oppbevarer mitt brukernavn og passord
            </label>
          }
        }
        <button type="submit" class="btn btn-primary">Sett opp kalender</button>
      </form>
  }

  def googleCalendarLink
  (start: LocalDateTime, heading: String, location: String, details: String): NodeSeq = {
    val utcStart = toUTC(start)
    val timeString = utcStart.toString(calendarFormatString) + "/" + utcStart.plusHours(2).toString(calendarFormatString)
    val link = "http://www.google.com/calendar/event?action=TEMPLATE&text=%s&dates=%s&details=%s&location=%s&trp=false&sprop=&sprop=name:".format(heading,timeString,details,location)
    <a href={link} target="_blank">Google</a>
  }


  private def urlEscape(url: String) = {
    url.replaceAllLiterally(" ", "%20")
      .replaceAllLiterally("-", "%2D")
      .replaceAllLiterally("æ", "%C3%A6")
      .replaceAllLiterally("Æ", "%C3%86")
      .replaceAllLiterally("ø", "%C3%B8")
      .replaceAllLiterally("Ø", "%C3%98")
      .replaceAllLiterally("å", "%C3%A5")
      .replaceAllLiterally("Å", "%C3%85")
      .replaceAllLiterally("&", "%26")
      .replaceAllLiterally("<br/>", "%0A")
      .replaceAllLiterally("<br>", "%0A")
      .replaceAllLiterally("</br>", "%0A")
  }

  private def toUTC(dateTime: LocalDateTime) = {
    dateTime.toDateTime(DateTimeZone.forID("Europe/Oslo")).withZone(DateTimeZone.UTC).toLocalDateTime
  }

  private def icsLink(start: LocalDateTime, heading: String, location: String, details: String, matchId:String) = {
    val url = "/match.ics?matchid="+matchId
    <a href={url}>Outlook/iCal</a>
  }



   private def controlGroup(fields:Map[String, InputField], parameterNames:String*):String = {
    if(fields.filterKeys(parameterNames.contains(_)).values.exists(_.isError)){
      "control-group error"
    }else{
      "control-group"
    }
  }
  private def errorInGroup(fields:Map[String, InputField], parameterNames:String*):Boolean = {
    fields.filterKeys(parameterNames.contains(_)).values.exists(_.isError)
  }


}
