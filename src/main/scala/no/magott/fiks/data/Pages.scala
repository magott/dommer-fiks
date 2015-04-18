package no.magott.fiks.data

import javax.servlet.http.HttpServletRequest
import no.magott.fiks.user.User

import xml.NodeSeq
import unfiltered.request.{Host, HttpRequest}
import no.magott.fiks.HerokuRedirect.XForwardProto
import java.net.SocketTimeoutException
import validation.FormField
import no.magott.fiks.invoice.{InvoiceTotals, Invoice}

case class Pages[T <: HttpServletRequest](req: HttpRequest[T]) {

  val snippets = Snippets(req)
  import snippets._

  def loginForm(messageParams: Map[String, Seq[String]]) = {
    emptyPage(
      <legend>Logg inn</legend>
        <div>
          {loginMessages(messageParams)}
        </div>
        <p>
          Logg inn med ditt Fiks brukernavn og passord.
            <br/>
          Du skal bruke samme brukernavn og passord som du bruker på
          <a href="http://fiks.fotball.no">offisielle fiks</a>
        </p>
        <form class="form-horizontal" action="login" method="post">
          <div class="form-group">
            <label class="col-sm-2 control-label" for="username">Brukernavn</label>
            <div class="col-sm-6">
              <input type="text" name="username" placeholder="Brukernavn" required=""/>
            </div>
          </div>
          <div class="form-group">
            <label class="col-sm-2 control-label" for="password">Password</label>
            <div class="col-sm-6">
              <input type="password" name="password" placeholder="Passord" required=""/>
            </div>
          </div>
            <div class="form-group">
              <div class="col-sm-offset-2 col-sm-6">
                <button type="submit" class="btn btn-primary">Logg inn</button>
              </div>
            </div>
        </form>
        <div class="well">
          Har Dommer-FIKS gjort hverdagen din enklere? Hva med å vise din takknemlighet med en liten donasjon via PayPal?
          {donateButton}
        </div>
    )
  }

  def about = {
    emptyPage(
      <h3>Om denne siden</h3>
        <p>
          Denne siden er utviklet av Morten Andersen-Gott. Dersom du har innspill til forbedringer eller vil rapportere
          en feil kan du gjøre dette
          <a href="http://www.facebook.com/dommerfiks">her</a>
        </p>

        <h3>Sikkerhet</h3>
        <p>
          Dommer-FIKS trenger ditt brukernavn og passord for fiks for å hente ut informasjon om dine kamper.
          Brukernavnet og passordet sendes kryptert fra deg til Dommer-FIKS og fra Dommer-FIKS til Fiks.
          Dersom du velger å benytte deg av Dommer-FIKS kalenderfunksjon for å kunne synkronisere
          dine kamper med Outlook, iCal, Google Calendar eller lignende vil ditt passord bli kryptert
          og lagret av Dommer-FIKS, mer informasjon om dette får du før oppretter en slik kalender. Dersom du ikke
          oppretter kalender vil ditt brukernavn og passord ikke bli lagret av Dommer-FIKS.
        </p>
        <h3>Støtt siden</h3>
        <p>
          Bruker du Dommer-FIKS flittig? Fornøyd med kalenderfunksjonaliteten? Lyst til å støtte utvikleren av siden?
          Det er ingen direkte kostnader knyttet til utviklingen av Dommer-FIKS, men funksjonalitet som kalender og resultatrapportering
          tar tid å utvikle. Nå kan du gi et lite bidrag, for å vise at du setter pris på siden, dette kan gjøres via PayPal. Klikk 'donate'

          {donateButton}

        </p>
        <h3>Kildekode</h3>
        <p>
          Dommer-FIKS er tilgjengelig som åpen kildekode. Kildekoden finner du på <a href="http://github.com/magott/dommer-fiks">GitHub</a>
          <p>
            Selve applikasjonen kjører på Heroku Europa.
          </p>
        </p>
    )
  }

  def betaOnly = {
    emptyPage(
      <div class="alert alert-info">Denne funksjonen er kun tilgjengelig for beta-brukere.
        Meld din interesse via <a href="http://www.facebook.com/dommerfiks">Facebook</a> dersom du er interessert
      </div>
    )
  }

  def reportInterestIn(availableMatch: AvailableMatch) = {
    emptyPage(reportInterestForm(availableMatch))
  }


  def assignedMatchInfo(m:AssignedMatch) = emptyPage(assignedMatchDetailsTable(m),
  Some(
    <script src="/js/fiks.js"></script>
    <script src="//cdnjs.cloudflare.com/ajax/libs/spin.js/1.2.7/spin.min.js"></script>
      <script type="text/javascript">
        { """
          $(document).ready(fetchForecast);
          $(document).ready(fetchStadiumLink);
          $(document).ready(bindSmsButton);


          """}
      </script>
  )
  )

  def assignedMatchResult(r:MatchResult, inputFields: Map[String, FormField] = Map.empty) = emptyPage(assignedMatchResultForm(r, inputFields))

  def availableMatches(availableMatches: List[AvailableMatch]) = {
    emptyPage(tableOfAvailableMatches, Some(availableMatchesScripts))
  }

  def calendarSignup(missingFields: Seq[String] = Nil) = {
    emptyPage(
      calendarSignupForm(missingFields)
    )
  }

  def userProfile(userOpt: Option[User], errors:Option[List[String]]) = {
    emptyPage(userForm(userOpt, errors), Some(userProfileScripts))
  }

  def calendarSignUpInfo = {
    emptyPage(
      (
    <legend>Kalender</legend>
    <div class="col-md-6">
      <p>
      Dommer-FIKS tilbyr funksjonalitet der kamper legges automatisk inn i din kalender på telefon og/eller på PC.
    </p>
    <p>
      For å kunne lage en kalender du kan legge til på telefonen din, i Outlook eller iCalendar, trengs det litt informasjon om deg.
      Dette legger du inn under din <a href="/user">brukerprofil</a>. Når du har gjort dette, gå til denne siden.
      Du vil da se knapper du kan trykke på for å legge til kalenderen automatisk på din telefon.
    </p>
      <p>
        <a class="btn btn-default" href="/user" role="button">Opprett kalender <span aria-hidden="true">&rarr;</span></a>
      </p>
    </div>)
    )
  }

  def calendarInfo(calendarId:String) = {
    val Host(host) = req
    val scheme = XForwardProto.unapply(req)
    val url = host +"/calendar?id="+ calendarId
    val schemeAndUrl = scheme.getOrElse("http") +"://" + url
    val deleteCalUrl = schemeAndUrl+"&action=delete"
    val resetCalIdUrl = schemeAndUrl+"&action=reset"
    val webcalUrl =  "webcal://"+url
    val googleCalUrl = "http://www.google.com/calendar/render?cid=%s".format(webcalUrl)
    emptyPage(
      <legend>Din kalender</legend>
        <p>
        Din kalender er nå konfigurert. Bruk knappene under for å importere den direkte på ditt Appleprodukt, nyere versjon av Outlook eller Google Calendar.
        Dersom du sletter kalenderen vil dine kalendere ikke lenger få oppdateringer av ny/endrede kamper.
        Du kan også laste ned kalenderfilen for hele kampprogrammet ditt og åpne den med din kalender, men da vil du ikke få automatiske
        oppdateringer, men må gjenta prosessen hver gang nye kamper kommer til for å holde kalenderen oppdatert.

        </p>
        <p>
          <ul>
            <li><strong>Slett kalender:</strong> Sletter din kalender, du vil ikke lenger kunne synkronisere dine kamper</li>
            <li><strong>Generer ny kalender:</strong> Det genereres en ny adresse for kalenderen din. Du må oppdatere din kalender til å peke på den nye adressen</li>
          </ul>
        </p>
      <div class="calendar-controls">
        <div>
         <input type="text" value={schemeAndUrl} class="form-control"></input>
        </div>
          <div class="btn-group">
            <a class="btn btn-danger" href={deleteCalUrl}><i class="glyphicon glyphicon-trash"></i> Slett kalender</a>
            <a class="btn btn-default" href={resetCalIdUrl}><i class="glyphicon glyphicon-refresh icon-white"></i> Generer ny adresse</a>
            <a class="btn btn-default" href={schemeAndUrl}><i class="glyphicon glyphicon-download"></i> Last ned</a>
            <a class="btn btn-default" href={webcalUrl}><i class="glyphicon glyphicon-plus"></i> Abonnér i Outlook/iCal/iPhone</a>
            <a class="btn btn-default" href={googleCalUrl} target="_blank"><i class="glyphicon glyphicon-plus"></i> Abonnér i Google Calendar</a>
          </div>
      </div>
    )
  }


  def invoiceInfoPage(invoice:Option[Invoice], am:Option[AssignedMatch]) = {
    val navbar:Option[NodeSeq] = am.map(invoiceNavBar)
    val matchDataPanel:Option[NodeSeq] = invoice.map(i => invoiceMatchDataPanel(i.matchData))
    val form:NodeSeq = invoiceForm(invoice)
    emptyPage(navbar.getOrElse(NodeSeq.Empty) ++ form ++ matchDataPanel.getOrElse(NodeSeq.Empty), Some(invoiceScripts))
  }

  def matchesSPA = {
    emptyPage(matchesSPATable, Some(matchesScripts))
  }

  def invoiceSAP = {
    emptyPage(
    invoiceTableSPA, Some(invoiceScripts ++ momentJS)
    )
  }

  def yieldMatch(m:AssignedMatch) = {
    emptyPage(
      yieldMatchForm(m)
    )
  }

  def notFound = emptyPage( <div class="alert alert-danger">
    <h4 class="alert-heading">Auda!</h4>
    Fant ikke siden du forsøkte å gå til. Bruk menyen over for å navigere. Eller gå direkte til <a href="/login">innloggingen</a>
  </div>)

  def forbidden = emptyPage( <div class="alert alert-danger">
    <h4 class="alert-heading">Ingen tilgang</h4>
    Du har ikke tilgang til denne siden
  </div>)

  def error(n:NodeSeq) = emptyPage(
    <div class="alert alert-danger">{n}</div>)

  def error(e: Exception) = {
    e.printStackTrace()
    e match {
      case ex: SocketTimeoutException => {
        emptyPage(
          <div class="alert alert-danger">
            <p>
              <strong>Det tok for lang tid å hente data fra fiks:</strong> Du er allerede logget inn og trenger <strong><em>ikke</em></strong> gjøre det på nytt.
              Forhåpentligvis går det bedre om du prøver på nytt. Bruk menyen over for å prøve igjen. Eller klikk <a href="/fiks/mymatches">her</a>
              for å gå til dine kamper.
            </p>
          </div>
        )
      }
      case _ => {
        emptyPage(
          <div class="alert alert-danger">
            <p>
              <strong>Her skjedde det en uventet feil:</strong> Forhåpentligvis går det bedre om du prøver på nytt. Bruk menyen over for å prøve igjen.
                      Hvis ikke kan du rapportere feilen på <a href="http://www.facebook.com/dommerfiks">Facebooksidene</a>
            </p>
            <p>{e.getMessage}</p>
            <p>{e.getStackTraceString}</p>
          </div>
        )
      }
    }
  }

  def submitStadium(stadiumName:String, fiksMatchId:String) = {
    emptyPage(
      <legend>Hvor ligger {stadiumName}?</legend>
      <p>
        Hjelp Dommer-FIKS med å plassere de forskjellige banene i Norge.
        Det er tatt utgangspunkt i regjerningens liste over forskjellige idrettsannlegg i Norge, men det kommer stadig
        nye baner i Norge, de skifter navn, eller det brukes andre navn i FIKS enn i listen til regjerningen. Derfor trenger
        Dommer-FIKS hjelp av deg for å legge inn baners plassering. Dette gjør at vi kan gi et kart og værmelding for banen.
      </p>
      <p>
        Fyll inn skjema med så mye informasjon som mulig om banen. Etter informasjonen er mottatt kan det ta noen dager før Dommer-FIKS er
        oppdatert med informasjonen.
      </p>
      <form class="form-horizontal" action={req.uri} method="POST">
        <div class="form-group">
            <label class="control-label col-sm-2" for="stadiumName">Banenavn</label>
          <div class="col-sm-6">
            <input class="form-control" id="stadiumName" name="stadiumName" readonly="true" value={stadiumName}></input>
          </div>
        </div>
        <div class="form-group">
            <label class="control-label col-sm-2" for="email">Email</label>
          <div class="col-sm-6">
            <input type="email" class="form-control" name="email" id="email" placeholder="Email" required="required"/>
            <p class="help-block">Dersom Dommer-FIKS trenger å kontakte deg for mer informasjon</p>
          </div>
        </div>
        <div class="form-group">
          <label class="control-label col-sm-2" for="description">Beskrivelse av banen</label>
          <div class="col-sm-6">
            <textarea class="form-control" rows="3" placeholder="Beskrivelse av banens plassering (adresse etc)" name="description" id="description" required="required"></textarea>
            <p class="help-block">Beskriv så godt som mulig hvor banen ligger.
              Bruk gjerne gataadresse om du har det. Du kan også legge inn linker til Google maps eller lignende.</p>
          </div>
        </div>
        <div class="form-group">
          <div class="col-sm-2 col-sm-offset-2">
            <button type="submit" class="btn">Send inn</button>
          </div>
        </div>
      </form>
    )
  }
}