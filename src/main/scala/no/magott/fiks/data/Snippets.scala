package no.magott.fiks.data

import no.magott.fiks.user.User

import xml.{XML, NodeSeq}
import org.joda.time.{LocalDate, DateTimeZone, DateTime, LocalDateTime}
import unfiltered.request.{Path, Seg, HttpRequest}
import javax.servlet.http.HttpServletRequest
import MatchStuff.allMatches
import validation.FormField
import no.magott.fiks.VCard
import no.magott.fiks.invoice.{InvoiceTotals, MatchData, Invoice}

case class Snippets[T <: HttpServletRequest] (req: HttpRequest[T]) {

  val isLoggedIn = SessionId.unapply(req).isDefined && SessionId.unapply(req).get.nonEmpty
  val pages = Path(req)

  def navbar = {
    <nav class="navbar navbar-inverse navbar-fixed-top">
      <div class="container">
        <div class="navbar-header">
          <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar" aria-expanded="false" aria-controls="navbar">
            <span class="sr-only">Toggle navigation</span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </button>
          <a class="navbar-brand" href="/">Dommer-FIKS</a>
          </div>
          <div id="navbar" class="navbar-collapse collapse">
            <ul class="nav navbar-nav">
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
               <li class={if (List("calendar","user").exists(pages.contains(_))) "dropdown active" else "dropdown inactive"}>
                 <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-expanded="false">Bruker <span class="caret"></span></a>
                 <ul class="dropdown-menu" role="menu">
                   <li><a href="/user"><span class="glyphicon glyphicon-user" aria-hidden="true"></span> Min bruker</a></li>
                   <li><a href="/calendar/mycal"><span class="glyphicon glyphicon-calendar" aria-hidden="true"></span> Kalender</a></li>
                 </ul>
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
      </nav>
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
        <link href="//cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/3.3.2/css/bootstrap.min.css" rel="stylesheet"/>
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
      <link href="/css/fiks.css" rel="stylesheet" type="text/css"/>

        <!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
        <!--[if lt IE 9]>
         <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
        <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->
      </head>
      <body>

        {navbar}
      <div class="container-fluid">

        {body}

        <div class="refsworld">
          <a href="http://www.refsworlduk.co.uk?fiks/"><img src="/img/refsworlduk1.gif" class="refsworld"></img></a>
        </div>

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
        <script src="//cdnjs.cloudflare.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>
        <script src="//cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/3.3.2/js/bootstrap.min.js"></script>
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
               document.querySelector('head').appendChild(msViewportStyle);
             }
        """}
        </script>
        {if(scripts.isDefined) scripts.get}
       </body>
    </html>

  def assignedMatchDetailsTable(m:AssignedMatch) = {
    <ul class="nav nav-tabs">
      <li class="active">
        <a href="./">Info</a>
      </li>
      {
        if(m.isReferee){
          <li class="inactive"><a href={s"/fiks/mymatches/${m.fiksId}/result"}>Resultat</a></li>
        }
      }
      <li class="inactive"><a href={s"/fiks/mymatches/${m.fiksId}/invoice"}>Regning</a></li>
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
        <td>{m.date.toString(norwegianLongFormat).capitalize}</td>
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
          <td>{m.officials.filter(_.mobile.isDefined).map(_.smsCheckbox) ++ (<a href="" id="sms" class="btn btn-default">Send</a>)}</td>
        </tr>
      }}
      <tr>
        <th>Legg til i kalender</th>
        <td>{<span>{AssignedMatch.googleCalendarLink(m)}</span> <span> | </span> <span>{AssignedMatch.icsLink(m)}</span>}</td>
      </tr>
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

  def matchesSPATable: NodeSeq = {
      <div ng-app="matchesapp">
        <div ng-controller="ctrl" data-ng-init={s"loadMatches()"}>
          <div class="col-md-12 col-xs-12 table-filter-row">
            <div class="row" ng-show="isReady()" ng-cloak="">
              <div class="btn-group btn-group-sm col-md-8 col-xs-7">
                <button class="btn btn-default" ng-click="setFromDate(yearAgo())" ng-class="{active: isShowingAllMatches()}">Alle</button>
                <button class="btn btn-default" ng-click="setFromDate(today())" ng-class="{active: !isShowingAllMatches()}">Kommende</button>
                <button class="btn btn-default" ng-click="reloadMatches()">
                  <span class="glyphicon glyphicon-refresh" aria-hidden="true"></span>
                </button>
              </div>
              <div class="col-md-3 col-md-offset-1 col-xs-5">
                <input type="search" class="input-sm form-control pull-right" name="search" ng-model="search" id="search" placeholder="Søk.."></input>
              </div>
            </div>
          </div>
          <div class="table-responsive col-md-12 col-xs-12" ng-if="isReady()">
            <table class="table table-striped table-bordered table-condensed" ng-cloak="">
              <thead>
                <tr>
                  <th>Dato</th>
                  <th>Tid</th>
                  <th>Kamp</th>
                  <th>Sted</th>
                  <th>Turnering</th>
                  <th>Dommere</th>
                </tr>
              </thead>
              <tbody>
                <tr ng-repeat="match in (matches | filter:search | filter:dateFilter) as filtered">
                  <td>{"{{match.date | date:'dd.MM.yy'}}"}</td>
                  <td>{"{{match.date | date:'HH:mm'}}"}</td>
                  <td>
                    <a href={"/fiks/mymatches/{{match.fiksId}}/"}>{"{{match.teams}}"}</a>
                  </td>
                  <td>{"{{match.venue}}"}</td>
                  <td>{"{{match.competition}}"}</td>
                  <td>
                    <span ng-repeat="ref in match.officials" class="official">{"({{ref.role}}) {{ref.name}}"}<span class="phone" ng-if={"isSet(ref.mobile)"}> Mobil: <a href={"tel:{{ref.mobile}}"}>{"{{ref.mobile}}"}</a></span><span class="phone" ng-if={"ref.home != null"}> Tlf: <a href={"tel:{{ref.home}}"}>{"{{ref.home}}"}</a></span></span>
                    <a href={"/fiks/mymatches/{{match.fiksId}}/yield?cancellationId={{match.cancellationId}}"} ng-if="isSet(match.cancellationId)">Meld forfall</a>
                  </td>
                </tr>
              </tbody>
            </table>
            <div class="filter-count" ng-cloak="">Viser {"{{filtered.length}}"} av {"{{matches.length}} kamper"}</div>
          </div>
          <div>
            <div class="text-center loading" ng-if="isLoading">
              <span class="glyphicon glyphicon-refresh glyphicon-refresh-animate"></span>
              <span>Laster kamper..</span>
            </div>
          </div>
          <div class="alert alert-warning" ng-if="isTimeout" ng-cloak="">
            <p><h2>Fiks sliter...</h2></p>
             Det hender dessverre rett som det er at stedet vi henter data fra, fiks.fotball.no sliter, slik at vi ikke får hente data på 30 sekunder. Dette jobber de helt sikkert med, siden det også betyr at hele fotballnorge (også de som bruker fiks.fotball.no direkte) ikke får tilgang til data.
             Du kan prøve på nytt ved å trykke på knappen under. Forhåpentligvis går det bedre da, men mest sannsynlig er dette et problem vi må vente på at folkene NFF bruker for å utvikle og drifte fiks.fotball.no får ryddet opp i.
             Erfaringsmessig tar dette alt fra noen timer til noen dager.
            <p>
              <button type="button" class="btn btn-default" ng-click="reloadMatches()">
                <span class="glyphicon glyphicon-repeat" aria-hidden="true"></span><span>&nbsp;Last kamper på nytt</span>
              </button>
            </p>
          </div>
        </div>
      </div>
  }

  def invoiceTableSPA = {
    val years = (2014 to LocalDate.now.getYear).reverse
    <div ng-app="invoiceapp">
      <div ng-controller="ctrl" data-ng-init={s"loadInvoices(${years.head})"}>
        <div class="col-md-12 row table-filter-row">
          <div class="btn-group btn-group-sm pull-left">
            {
              years.map(y=> <button class="btn btn-default" ng-click={s"loadInvoices($y)"} ng-class={s"{active: isShowingFor($y)}"} >{y}</button> )
            }
          </div>
          <div class="pull-right">
            <input type="search" class="input-sm form-control" name="search" ng-model="search" id="search" placeholder="Filtrer.."></input>
          </div>
        </div>
        <div class="col-md-12 row">
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
              <td><a href={"/invoice/{{invoice.id}}"}>{"{{invoice.match.home}} - {{invoice.match.away}}"}</a></td>
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
    </div>
  }

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
    <form id="invoice" method="post" class="form-horizontal">
      <div class="form-group">
        <label class="col-sm-2 control-label" for="matchFee">Kamphonorar</label>
        <div class="col-sm-2">
          <input type="number" id="matchFee" name="matchFee" class="form-control" placeholder="" required="required" value={i.map(_.matchFee.toString).getOrElse("")}/>
        </div>
        <p class="help-block"></p>
      </div>
      <div class="form-group">
        <label class="control-label col-sm-2" for="millageAllowance">Kilometergodtgjørelse</label>
        <div class="col-sm-2">
          <input type="number" class="form-control" id="km" name="km" placeholder="ant km" step="0.01" value={i.flatMap(_.km.map(_.toString)).getOrElse("")}/>
        </div>
        <div class="col-sm-2">
          <input type="number" id="millageAllowance" name="millageAllowance" class="form-control" placeholder="sum" step="0.01" value={i.flatMap(_.millageAllowance.map(_.toString)).getOrElse("")}/>
        </div>
        <p class="help-block"></p>
      </div>
      <div class="form-group">
        <label class="control-label col-sm-2" for="millageAllowance">Passasjertillegg</label>
        <div class="col-sm-2">
          <input type="number" class="form-control" id="passengers" name="passengers" placeholder="Antall passasjerer" step="1" value={i.flatMap(_.passengerAllowance.map(_.pax.toString)).getOrElse("")}/>
        </div>
        <div class="col-sm-2">
          <input type="number" id="passengerKm" name="passengerKm" class="form-control" placeholder="Kilometer" step="0.01" value={i.flatMap(_.passengerAllowance.map(_.km.toString)).getOrElse("")}/>
        </div>
        <p class="help-block"></p>
      </div>
      <div class="form-group">
        <label class="control-label col-sm-2" for="toll">Bompenger</label>
        <div class="col-sm-2">
          <input type="number" step="0.01" id="toll" placeholder=" " name="toll" class="form-control" value={i.flatMap(_.toll.map(_.toString)).getOrElse("")}/>
        </div>
        <p class="help-block"></p>
      </div>
      <div class="form-group">
        <label class="control-label col-sm-2" for="otherExpenses">Andre utlegg (parkering, ferge etc)</label>
        <div class="col-sm-2">
          <input type="number" step="0.01" id="otherExpenses" placeholder=" " name="otherExpenses" class="form-control" value={i.flatMap(_.otherExpenses.map(_.toString)).getOrElse("")}/>
        </div>
        <p class="help-block"></p>
      </div>
      <div class="form-group">
        <label class="control-label col-sm-2" for="perDiem">Diett</label>
        <div class="col-sm-2">
          <input type="number" id="perDiem" name="perDiem" placeholder=" " class="form-control" value={i.flatMap(_.perDiem.map(_.toString)).getOrElse("")}/>
        </div>
        <p class="help-block"></p>
      </div>
      <div class="form-group">
        <label class="control-label col-sm-2" for="total">Total</label>
        <div class="col-sm-2">
          <input type="number" step="0.01" id="total" placeholder=" " name="total" class="form-control" value={i.map(_.total.toString).getOrElse("")}/>
        </div>
        <p class="help-block"></p>
      </div>
      <div class="form-group">
        <div class="col-sm-6 col-sm-offset-2">
          <button type="submit" class="btn btn-primary">Lagre</button>
          {
            if(i.isDefined){
              (
              <button type="button" id="delete" class="btn btn-danger"><i class="glyphicon glyphicon-trash icon-white"></i> Slett</button>
              )
            }
          }
        </div>
      </div>
      <div class="form-group">
        <div class="col-sm-6 col-sm-offset-2">
          {
            if(i.isDefined){
              if(i.get.reminder.isDefined)
                <button type="button" id="reminder" class="btn btn-warning">Purret</button>
              else
                <button type="button" id="reminder" class="btn btn-default">Merk purret</button>
            }
          }
          {
            if(i.isDefined){
              if(i.get.settled.isDefined)
                <button type="button" id="settled" class="btn btn-success">Betalt</button>
              else
                <button type="button" id="settled" class="btn btn-default">Merk betalt</button>
            }
          }
          </div>
        </div>
      {if(i.isDefined) {
        <div class="form-group">
          <div class="col-sm-8 col-sm-offset-2">
            <div class="btn-group">
              <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-expanded="false">
                <span class="glyphicon glyphicon-download-alt" aria-hidden="true"></span> Last ned <span class="caret"></span>
              </button>
              <ul class="dropdown-menu" role="menu">
                <li>
                  <a href={s"/invoice/${i.get.id.get.toString}?export=nffbredde"}>NFF Bredderegning (xslx)</a>
                </li>
                <li>
                  <a href={s"/invoice/${i.get.id.get.toString}?export=ofk"}>OFK Bredderegning (xslx)</a>
                </li>
                <li>
                  <a href={s"/invoice/${i.get.id.get.toString}?export=nfftopp"}>NFF Toppfotball (xslx)</a>
                </li>
              </ul>
            </div>
          </div>
        </div>
        <div class="col-sm-8 col-sm-offset-2">
          <span class="badge">NB!</span> Legg inn mer informasjon om deg selv i din <a href="/user">brukerprofil</a>
          for å få den automatisk lagt inn i dommerregningen du laster ned.
        </div>
    }}
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
    (<script src="//cdnjs.cloudflare.com/ajax/libs/jquery-validate/1.11.0/jquery.validate.min.js" type="text/javascript"></script>
     ++angularJs++
      <script src="/js/invoice.js" type="text/javascript"></script> )  ++lodashJS
  }

  def availableMatchesScripts = {
    angularJs ++ (<script src="/js/availablematches.js" type="text/javascript"></script>) ++ lodashJS ++ momentJS
  }

  def matchesScripts = {
    angularJs ++ (<script src="/js/matches.js" type="text/javascript"></script>) ++ lodashJS ++ momentJS
  }

  def angularJs = <script src="//cdnjs.cloudflare.com/ajax/libs/angular.js/1.3.13/angular.min.js" type="text/javascript"></script>


def tableOfAvailableMatches = {
  <div ng-app="availablematchesapp">
    <div ng-controller="ctrl" data-ng-init="loadMatches()">
      <table class="table table-striped table-bordered table-condensed" ng-show="!isLoading" ng-cloak="">
      <thead>
        <tr>
          <th>Tid</th>
          <th>
            <select class="input-sm form-control" ng-model="tournament">
              <option selected="selected" value="">Turnering</option>
              <option ng-repeat="t in uniqueTournaments()">{"{{t}}"}</option>
            </select>
          </th>
          <th>Kamp</th>
          <th>Sted</th>
          <th>
            <select class="input-sm form-control" name="role" ng-model="role">
              <option selected="selected" value="">Type</option>
              <option value="Dommer">Dommer</option>
              <option value="AD">AD</option>
            </select>
          </th>
          <th>Meld interesse</th>
        </tr>
      </thead>
      <tbody>
          <tr ng-repeat="match in (matches | filter:filterByType | filter:filterByTournament)">
            <td>
              {"{{match.date | date:'dd.MM.yy HH:mm'}}"}
            </td>
            <td>
              {"{{match.tournament}}"}
            </td>
            <td>
              {"{{match.teams}}"}
            </td>
            <td>
              {"{{match.venue}}"}
            </td>
            <td>
              {"{{match.role}}"}
            </td>
            <td ng-if="_.isNull(match.availabilityId)">
              Interesse meldt
            </td>
            <td ng-if="!(_.isNull(match.availabilityId))">
              {<a href="availablematches?matchid={{match.availabilityId}}">Meld interesse</a>}
            </td>
          </tr>
      </tbody>
    </table>
      <div>
        <div class="text-center loading" ng-if="isLoading">
          <span class="glyphicon glyphicon-refresh glyphicon-refresh-animate"></span>
          <span>Henter tilgjengelige kamper..</span>
        </div>
      </div>
   </div>
  </div>
  }

  def loginMessages
  (messageParams: Map[String, Seq[String]]) = {
    messageParams.get("message") match {
      case Some(msg) => msg.mkString("") match {
        case "loginRequired" => <div class="alert alert-info">Du må logge inn for å få tilgang til denne siden</div>
        case "loginFailed" => <div class="alert alert-danger">Login feilet, prøv igjen</div>
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
              <div class="alert alert-danger">
                Ugyldig brukernavn/passord, prøv igjen
              </div>
            }
          }
        </div>
        {
          if(validationErrors.contains("username")){
            <div class="form-group has-error">
            <label for="username" class="control-label">Brukernavn</label>
            <input type="text" name="username" class="form-control" id="inputError"/>
            <span class="help-block">Brukernavn må fylles ut</span>
            </div>
          }else{
            <div class="form-group">
            <label for="username" class="control-label">Brukernavn</label>
            <input type="text" name="username" class="form-control"/>
            </div>
          }
        }

        {
          if(validationErrors.contains("password")){
          <div class="form-group has-error">
              <label for="password" class="control-label">Passord</label>
              <input type="password" class="form-control" name="password" id="inputError"/>
            <span class="help-block">Passord må fylles ut</span>
          </div>
          }else{
            <div class="form-group">
              <label for="password" class="control-label">Passord</label>
              <input type="password" class="form-control" name="password"/>
            </div>
          }
        }

        {
        if(validationErrors.contains("email")){
          <div class="form-group has-error">
            <label for="email" class="control-label">E-post</label>
            <input type="email" name="email" class="form-control" id="inputError"/>
            <span class="help-inline">E-post må fylles ut</span>
          </div>
        }else{
          <div class="form-group">
            <label for="email" class="control-label">E-post</label>
            <input type="email" name="email" class="form-control"/>
            <span class="help-inline">Brukes <strong>kun</strong> dersom Dommer-FIKS har viktige meldinger til deg om tjenesten</span>
          </div>
          }
        }
        {
          if(validationErrors.contains("terms")){
            <div class="form-group has-error">
              <div class="checkbox">
                <label class="control-label">
                    <input type="checkbox" name="terms"/>
                  Jeg godtar at Dommer-FIKS oppbevarer mitt brukernavn og passord (påkrevd)
                </label>
              </div>
            </div>
          }else{
            <div class="checkbox">
              <label><input type="checkbox" name="terms"/>
              Jeg godtar at Dommer-FIKS oppbevarer mitt brukernavn og passord
            </label>
            </div>
          }
        }
        <button type="submit" class="btn btn-primary">Sett opp kalender</button>
      </form>
  }

  def donateButton = {
    <form action="https://www.paypal.com/cgi-bin/webscr" method="post">
      <input type="hidden" name="cmd" value="_s-xclick" />
      <input type="hidden" name="encrypted" value="-----BEGIN PKCS7-----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-----END PKCS7-----"/>
      <input type="image" src="https://www.paypalobjects.com/en_US/i/btn/btn_donate_LG.gif" border="0" name="submit" alt="PayPal - The safer, easier way to pay online!" />
      <img alt="" border="0" src="https://www.paypalobjects.com/en_US/i/scr/pixel.gif" width="1" height="1" />
    </form>
  }

  def userForm(user: Option[User], errors:Option[List[String]]) = {
    val name = user.flatMap(_.invoiceData.flatMap(_.name)).getOrElse("")
    val adr = user.flatMap(_.invoiceData.flatMap(_.address)).getOrElse("")
    val zip = user.flatMap(_.invoiceData.flatMap(_.postalCode)).getOrElse("")
    val city = user.flatMap(_.invoiceData.flatMap(_.city)).getOrElse("")
    val account = user.flatMap(_.invoiceData.flatMap(_.accountNumber)).getOrElse("")
    val mun = user.flatMap(_.invoiceData.flatMap(_.taxMuncipal)).getOrElse("")
    val email = user.map(_.email).getOrElse("")
    val pwd = user.flatMap(_.password).getOrElse("")
    val phone = user.map(_.phoneForInvoice).getOrElse("")

    (
    <div class="col-md-8">
      <legend>Om brukerprofil</legend>
      <p>
      Her kan du legge inn informasjon om deg selv. Du trenger ikke å legge inn noe her for å bruke Dommer-FIKS, men
        det vil gi deg ekstra funksjonalitet.
      </p>
      <p>
      Personalia brukes får å fylle inn mer informasjon når du laster ned dommerregning.
      Passord trengs kun dersom du ønsker å bruke kalenderfunksjonaliteten
      </p>
    </div>
    <div class="col-md-8">
    <form class="form-horizontal" method="post">
      <fieldset>
        <!-- Form Name -->
        <legend>Informasjon for dommerregning</legend>

        {if(errors.isDefined)
        <div class="alert alert-danger" role="alert">
          <ul>
          {errors.get.map(e => <li>{e}</li>)}
          </ul>
        </div>
        }
        <!-- Text input-->
        <div class="form-group">
          <label class="col-md-4 control-label" for="name">Navn</label>
          <div class="col-md-4">
            <input id="name" name="name" type="text" placeholder="Fullt navn" class="form-control input-md" value={name}></input>

          </div>
        </div>

        <!-- Text input-->
        <div class="form-group">
          <label class="col-md-4 control-label" for="address">Hjemmeadresse</label>
          <div class="col-md-4">
            <input id="address" name="address" type="text" placeholder="Gatenavn og nummer" value={adr} class="form-control
            input-md"></input>

          </div>
        </div>

        <!-- Text input-->
        <div class="form-group">
          <label class="col-md-4 control-label" for="zip">Postnummer</label>
          <div class="col-md-4">
            <input id="zip" name="zip" type="text" placeholder=" " class="form-control input-md" value={zip}></input>

          </div>
        </div>

        <!-- Text input-->
        <div class="form-group">
          <label class="col-md-4 control-label" for="city">Poststed</label>
          <div class="col-md-4">
            <input id="city" name="city" type="text" placeholder=" " class="form-control input-md" value={city}></input>
          </div>
        </div>

        <!-- Text input-->
        <div class="form-group">
          <label class="col-md-4 control-label" for="email">Telefonnummer</label>
          <div class="col-md-4">
            <input id="phone" name="phone" type="tel" placeholder=" " class="form-control input-md" value={phone} required="
            "></input>
          </div>
        </div>
        <!-- Text input-->
        <div class="form-group">
          <label class="col-md-4 control-label" for="email">E-post</label>
          <div class="col-md-4">
            <input id="email" name="email" type="text" placeholder=" " class="form-control input-md" value={email} required="
            "></input>
          </div>
        </div>

        <!-- Text input-->
        <div class="form-group">
          <label class="col-md-4 control-label" for="accountno">Kontonummer</label>
          <div class="col-md-4">
            <input id="accountNo" name="accountNo" type="text" placeholder=" " value={account} class="form-control input-md"></input>
          </div>
        </div>

        <!-- Text input-->
        <div class="form-group">
          <label class="col-md-4 control-label" for="taxMuncipal">Skattekommune</label>
          <div class="col-md-4">
            <input id="taxMuncipal" name="taxMuncipal" type="text" placeholder=" " value={mun} class="form-control
            input-md"></input>
          </div>
        </div>
      </fieldset>

      <fieldset>
        <legend>Informasjon for kalender</legend>
        <!-- Password input-->
        <div class="form-group">
          <label class="col-md-4 control-label" for="password">FIKS passord</label>
          <div class="col-md-4">
            <input id="password" name="password" type="password" placeholder=" " value={pwd} class="form-control input-md"></input>
            <span class="help-block">Passord trengs kun dersom du ønsker å sette opp kalender med automatiske
              oppdateringer</span>
          </div>
        </div>
      </fieldset>
      <div class="form-group">
        <div class="col-md-4 col-md-offset-4">
          <button id="submit" class="btn btn-default" name="submit">Lagre</button>
        </div>
      </div>
    </form>
    </div>)
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

   private def formGroup(fields:Map[String, FormField], parameterNames:String*):String = {
    if(fields.filterKeys(parameterNames.contains(_)).values.exists(_.isError)){
      "form-group has-error"
    }else{
      "form-group"
    }
  }
  private def errorInGroup(fields:Map[String, FormField], parameterNames:String*):Boolean = {
    fields.filterKeys(parameterNames.contains(_)).values.exists(_.isError)
  }


  private def momentAngularJS = <script src="//cdnjs.cloudflare.com/ajax/libs/angular-moment/0.9.0/angular-moment.min.js" type="text/javascript"></script>
  def momentJS = <script src="//cdnjs.cloudflare.com/ajax/libs/moment.js/2.9.0/moment.min.js" type="text/javascript"></script>
  def lodashJS = <script src="//cdnjs.cloudflare.com/ajax/libs/lodash.js/3.3.0/lodash.min.js" type="text/javascript"></script>

}
