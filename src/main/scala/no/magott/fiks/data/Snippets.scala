package no.magott.fiks.data

import xml.{XML, NodeSeq}
import org.joda.time.{LocalDate, DateTimeZone, DateTime, LocalDateTime}
import unfiltered.request.{Path, Seg, HttpRequest}
import javax.servlet.http.HttpServletRequest
import MatchStuff.allMatches
import validation.FormField
import no.magott.fiks.VCard
import no.magott.fiks.invoice.{InvoiceTotals, MatchData, Invoice}

case class Snippets[T <: HttpServletRequest] (req: HttpRequest[T]) {

  val calendarFormatString = "yyyyMMdd'T'HHmmss'Z"
  val isLoggedIn = SessionId.unapply(req).isDefined && SessionId.unapply(req).get.nonEmpty
  val pages = Path(req)

  def navbar = {
    <div class="navbar navbar-fixed-top navbar-inverse">
      <div class="navbar-inner">
        <div class="container-fluid">
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
            }
            {

             if(isLoggedIn){
               <li class={if (pages.contains("calendar")) "active" else "inactive"}>
                 <a href="/calendar/mycal">Kalender</a>
               </li>
                 <li class={if (pages.contains("invoice")) "active" else "inactive"}>
                   <a href="/invoice/">Dommerregning</a>
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
              <li class={if (pages.contains("about")) "active" else "inactive"}>
                <a href="/fiks/about">Om</a>
              </li>
            </ul>
          </div> <!--/.nav-collapse -->
        </div>
      </div>
    </div>
  }

  def emptyPage(body: NodeSeq, scripts:Option[NodeSeq] = None): NodeSeq =
    <html lang="no">
      <head>
          <meta charset="utf-8"/>
        <title>Fiks fix</title>
          <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
          <meta name="description" content="Fiks, without the #fail"/>
          <meta name="author" content="Morten Andersen-Gott"/>
          <meta name="google-site-verification" content="ptF2AFWdgpfQFz8_Uu2o_kDR704noD60eKR4nHC3uT8"/>
          <meta http-equiv="Content-Language" content="no" />
          <link rel="shortcut icon" href="/favicon.ico" />

        <!-- Le styles -->
          <link href="//cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/2.3.1/css/bootstrap.min.css" rel="stylesheet"/>
        <style type="text/css">
          {
          {"""
          body{
            padding-top: 60px;
            padding-bottom: 40px;
          }
          .sidebar-nav{
          padding: 9 px 0;
          }
           """}
          }
        </style>
          <link href="//cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/2.3.1/css/bootstrap-responsive.min.css" rel="stylesheet"/>
          <link href="/css/fiks.css" rel="stylesheet"/>

        <!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
        <!--[if lt IE 9]>
    <script src="//html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->
      </head>
      <body>

        {navbar}
      <div class="container-fluid">

        {body}

        <footer class="footer">
          <p>
            <a href="http:///www.andersen-gott.com">Morten Andersen-Gott</a>
            (c) 2013</p>
          <p>
            <a href="http://www.facebook.com/dommerfiks">Foreslå forbedringer eller rapportér feil</a>
          </p>
        </footer>
      </div> <!-- /container -->


        <!-- Placed at the end of the document so the pages load faster -->
        <script src="//cdnjs.cloudflare.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>
        <script src="//cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/2.3.1/js/bootstrap.min.js"></script>
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
        <script type="text/javascript">
          {"""
          if (navigator.userAgent.match(/IEMobile\/10\.0/)) {
            var msViewportStyle = document.createElement('style');
            msViewportStyle.appendChild(
              document.createTextNode(
                '@-ms-viewport{width:auto!important}'
              )
            );
            document.getElementsByTagName('head')[0].appendChild(msViewportStyle);
          }
        """}
        </script>
        {if(scripts.isDefined) scripts.get}
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
              {m.refs} &nbsp;
              {
               m.cancellationId.map(c => <a href={s"mymatches/${m.fiksId}/yield?cancellationId=${c}"}>Meld forfall</a> ).getOrElse("")
              }
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
      <li class="inactive"><a href="./invoice">Regning</a></li>
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
        m.roleAndNames.map(t =>
          <tr>
            <th>{t._1}</th>
            <td>{t._2} {m.contactLink(t._1).getOrElse("")}</td>
          </tr>
        )
      }
      <tr>
        <th>Sted</th>
        <td id="venue">{m.venue}<span id="location" class="beta"></span></td>
      </tr>
      <tr>
        <th>Turnering</th>
        <td>{m.tournament}</td>
      </tr>
      <tr>
        <th>Mer info</th>
        <td><a href={m.externalMatchInfoUrl} target="_blank">Kampinfo fra fotball.no</a></td>
      </tr>
      {if(m.displayDismissalReportLink)
      <tr>
        <th>Utvisningsrapport</th>
        <td><a href={m.dismissalUrl} target="_blank">Send inn utvisningsrapport</a></td>
      </tr>
      }
      <tr>
        <th>Værmelding</th>
        <td>
          <div class="forecast-table" id="weather"> <!-- -->

          </div>
          <div id="spinner"></div>
        </td>
      </tr>
        {if(m.officials.size > 1){
        <tr>
          <th>SMS</th>
          <td>{m.officials.filter(_.mobile.isDefined).map(_.smsCheckbox) ++ (<a href="" id="sms" class="btn">Send</a>)}</td>
        </tr>
      }}
      <tr>
        <th></th>
        <td>
          <a class="btn btn-primary" href="/fiks/mymatches"><i class="icon-circle-arrow-left icon-white"></i> Tilbake</a>
        </td>
      </tr>
    </table>
  }

  def forecasts(w: Seq[MatchForecast]) = {
      <div class="forecasts">
        {w.map(_.asHtml)}
      </div>
  }

  def assignedMatchResultForm(r: MatchResult, fields:Map[String, FormField] = Map.empty) = {
    <ul class="nav nav-tabs">
      <li class="inactive">
        <a href="./">Info</a>
      </li>
      <li class="active"><a href="result">Resultat</a></li>
      <li class="inactive"><a href="invoice">Regning</a></li>
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
          <button type="submit" class="btn btn-primary">Send inn</button>
        </div>
      </fieldset>
    </form>
    <div>
      {
      if(!r.resultReports.isEmpty){
        <h4>Historikk</h4>
          <table class="table table-striped table-bordered table-condensed">
            <thead>
              <tr><th>Resultattype</th><th>Resultat</th><th>Rapportert av</th></tr>
            </thead>
            <tbody>
              {
              r.resultReports.map{res => <tr><td>{res.resultType}</td><td>{res.score.toLiteral}</td><td>{res.reporter}</td></tr>}
              }
            </tbody>
          </table>
      }
      }
    </div>
  }

  def invoiceTableSPA = {
    val years = (2014 to LocalDate.now.getYear).reverse
    <div ng-app="invoiceapp">
      <div ng-controller="ctrl" data-ng-init={s"loadInvoices(${years.head})"}>
        <div class="btn-group">
          {
            years.map(y=> <button class="btn" ng-click={s"loadInvoices($y)"}>{y}</button> )
          }
        </div>
        <input type="search" class="input-medium search-query pull-right" name="search" ng-model="search" id="search" placeholder="Filter.."></input>
        <table class="table table-striped table-bordered table-condensed">
          <thead>
            <tr>
              <th>Dato</th>
              <th>Kamp</th>
              <th>Total</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody ng-cloak="">
            <tr ng-repeat="invoice in invoices | filter:search as filtered" class={"{{invoice.rowClass}}"}>
              <td>{"{{invoice.match.date | date:'dd-MM-yyyy'}}"}</td>
              <td><a href={"{{invoice.id}}"}>{"{{invoice.match.home}} - {{invoice.match.away}}"}</a></td>
              <td>{"{{invoice.total}}"}</td>
              <td>{"{{invoice.status}}"}</td>
            </tr>
          </tbody>
        </table>
        <div class="pull-right" ng-cloak="">
          <table class="table">
            <tr>
              <td>Betalt</td>
              <td>{"{{sumSettled(filtered)}}"}</td>
            </tr>
            <tr>
              <td>Utestående</td>
              <td>{"{{sumUnsettled(filtered)}}"}</td>
            </tr>
            <tr>
              <td><strong>Totalt</strong></td>
              <td><strong>{"{{sumTotal(filtered)}}"}</strong></td>
            </tr>
          </table>
        </div>
      </div>
    </div>
  }

  def invoiceTable(invoices:Seq[Invoice]) = {
    <table class="table table-striped table-bordered table-condensed">
      <thead>
        <tr>
          <th>Dato</th>
          <th>Kamp</th>
          <th>Total</th>
          <th>Status</th>
        </tr>
      </thead>
      <tbody>
        {
          invoices.map(invoice =>
            <tr class={invoice.rowClass}>
              <td>{invoice.matchData.dateString}</td>
              <td><a href={invoice.id.get.toString}>{invoice.matchData.teams}</a></td>
              <td>{invoice.total}</td>
              <td>{invoice.status}</td>
            </tr>
          )
        }
      </tbody>
    </table>
  }

  def invoiceTotals(t:InvoiceTotals) =
  <div class="pull-right">
    <table class="table">
      <tr>
        <td>Betalt</td>
        <td>{t.settled}</td>
      </tr>
      <tr>
        <td>Utestående</td>
        <td>{t.outstanding}</td>
      </tr>
      <tr>
        <td><strong>Totalt</strong></td>
        <td><strong>{t.total}</strong></td>
      </tr>
    </table>
  </div>

  def invoiceNavBar(m:AssignedMatch) = {
    <ul class="nav nav-tabs">
      <li class="inactive">
        <a href={s"/fiks/mymatches/${m.fiksId}/"}>Info</a>
      </li>
      {
        if(m.isReferee){
          <li class="inactive"><a href={s"/fiks/mymatches/${m.fiksId}/result"}>Resultat</a></li>
        }
      }
      <li class="active"><a href={s"/fiks/mymatches/${m.fiksId}/invoice"}>Regning</a></li>
    </ul>
  }
  def invoiceForm(i:Option[Invoice]) = {
    <legend>Dommerregning</legend>
    <form class="form-horizontal" id="invoice" method="post">
      <div class="control-group">
        <label class="control-label" for="matchFee">Kamphonorar</label>
        <div class="controls">
          <input type="number" id="matchFee" name="matchFee" placeholder="" required="required" value={i.map(_.matchFee.toString).getOrElse("")}/>
          <span class="help-inline"></span>
        </div>
      </div>
      <div class="control-group">
        <label class="control-label" for="millageAllowance">Kilometergodtgjørelse</label>
        <div class="controls">
          <input type="number" class="input-mini" id="km" name="km" placeholder="ant km" step="0.01"/>
          <input type="number" class="input-mini" id="allowance" name="allowance" value="4.05" step="0.01"/>
          <input type="number" id="millageAllowance" name="millageAllowance" class="input-medium" placeholder="sum" step="0.01" value={i.flatMap(_.millageAllowance.map(_.toString)).getOrElse("")}/>
        </div>
      </div>
      <div class="control-group">
        <label class="control-label" for="toll">Bompenger</label>
        <div class="controls">
          <input type="number" step="0.01" id="toll" placeholder=" " name="toll" class="input-medium" value={i.flatMap(_.toll.map(_.toString)).getOrElse("")}/>
          <span class="help-inline"></span>
        </div>
      </div>
      <div class="control-group">
        <label class="control-label" for="perDiem">Diett</label>
        <div class="controls">
          <input type="number" id="perDiem" name="perDiem" placeholder=" " class="input-medium" value={i.flatMap(_.perDiem.map(_.toString)).getOrElse("")}/>
          <span class="help-inline"></span>
        </div>
      </div>
      <div class="control-group">
        <label class="control-label" for="total">Total</label>
        <div class="controls">
          <input type="number" step="0.01" id="total" placeholder=" " name="total" class="input-medium" value={i.map(_.total.toString).getOrElse("")}/>
          <span class="help-inline"></span>
        </div>
      </div>
      <div class="control-group">
        <div class="controls">
          <button type="submit" class="btn btn-primary">Lagre</button>
          {
            if(i.isDefined){
              <button type="button" id="delete" class="btn btn-danger"><i class="icon-trash icon-white"></i> Slett</button>
            }
          }
          </div>
        </div>
      <div class="control-group">
        <div class="controls">
          {
            if(i.isDefined){
              if(i.get.reminder.isDefined)
                <button type="button" id="reminder" class="btn btn-warning">Purret</button>
              else
                <button type="button" id="reminder" class="btn btn-inverse">Merk purret</button>
            }
          }
          {
            if(i.isDefined){
              if(i.get.settled.isDefined)
                <button type="button" id="settled" class="btn btn-success">Betalt</button>
              else
                <button type="button" id="settled" class="btn">Merk betalt</button>
            }
          }
          </div>
        </div>
    </form>
  }

  def invoiceMatchDataPanel(m:MatchData) = {
    <legend>Kampinformasjon</legend>
    <table class="table table-striped table-bordered table-condensed">
      <tr>
        <th>Kamp</th>
        <td>{s"${m.home} - ${m.away}"}</td>
      </tr>
      <tr>
        <th>Arena</th>
        <td>{m.venue}</td>
      </tr>
      <tr>
        <th>Kampnummer</th>
        <td>{m.matchId}</td>
      </tr>      <tr>
        <th>Turnering</th>
        <td>{m.tournament}</td>
      </tr>
    </table>
  }


  def invoiceScripts = {
    <script src="//cdnjs.cloudflare.com/ajax/libs/jquery-validate/1.11.0/jquery.validate.min.js" type="text/javascript"></script>
    <script src="//cdnjs.cloudflare.com/ajax/libs/underscore.js/1.4.4/underscore-min.js" type="text/javascript"></script>
    <script src="//cdnjs.cloudflare.com/ajax/libs/angular.js/1.3.13/angular.min.js" type="text/javascript"></script>
    <script src="/js/invoice.js" type="text/javascript"></script>
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

  def yieldMatchForm(m: AssignedMatch) = {
    <table class="table table-striped table-bordered table-condensed">
      <tr>
        <th>Kamp</th>
        <td>
          {m.teams}
        </td>
      </tr>
      <tr>
        <th>Tidspunkt</th>
        <td>
          {m.date.toString("dd.MM.yyyy HH:mm")}
        </td>
      </tr>
      <tr>
        <th>Bane</th>
        <td>
          {m.venue}
        </td>
      </tr>
      <tr>
      <th>Turnering</th>
      <td>
        {m.tournament}
      </td>
    </tr>
    <tr>
      <th>Dommeroppsett</th>
      <td>
        {m.referees}
      </td>
    </tr>
      <form class="well" method="post">
        <tr>
          <th>Begrunnelse</th>
          <td>
            <textarea name="reason" id="reason"></textarea>
          </td>
        </tr>
        <tr>
          <td></td>
          <td>
            <button type="submit" class="btn btn-primary">Meld forfall</button>
          </td>
        </tr>
      </form>
    </table>
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
              <textarea name="comment" id="comment"></textarea>
          </td>
        </tr>
        <tr>
          <td></td>
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

  def donateButton = {
    <form action="https://www.paypal.com/cgi-bin/webscr" method="post">
      <input type="hidden" name="cmd" value="_s-xclick" />
      <input type="hidden" name="encrypted" value="-----BEGIN PKCS7-----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-----END PKCS7-----"/>
      <input type="image" src="https://www.paypalobjects.com/en_US/i/btn/btn_donate_LG.gif" border="0" name="submit" alt="PayPal - The safer, easier way to pay online!" />
      <img alt="" border="0" src="https://www.paypalobjects.com/en_US/i/scr/pixel.gif" width="1" height="1" />
    </form>
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



   private def controlGroup(fields:Map[String, FormField], parameterNames:String*):String = {
    if(fields.filterKeys(parameterNames.contains(_)).values.exists(_.isError)){
      "control-group error"
    }else{
      "control-group"
    }
  }
  private def errorInGroup(fields:Map[String, FormField], parameterNames:String*):Boolean = {
    fields.filterKeys(parameterNames.contains(_)).values.exists(_.isError)
  }


  private def momentAngularJS = <script src="//cdnjs.cloudflare.com/ajax/libs/angular-moment/0.9.0/angular-moment.min.js" type="text/javascript"></script>
  def momentJS = <script src="//cdnjs.cloudflare.com/ajax/libs/moment.js/2.9.0/moment.min.js" type="text/javascript"></script>
  def lodashJS = <script src="//cdnjs.cloudflare.com/ajax/libs/lodash.js/3.3.0/lodash.min.js" type="text/javascript"></script>

}
