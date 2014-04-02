package no.magott.fiks.data

import javax.servlet.http.HttpServletRequest
import xml.NodeSeq
import unfiltered.request.{Host, HttpRequest}
import no.magott.fiks.HerokuRedirect.XForwardProto
import java.net.SocketTimeoutException
import validation.FormField
import no.magott.fiks.invoice.Invoice

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
          <a href="http://www.fiks.fotball.no">offisielle fiks</a>
        </p>
        <form class="form-horizontal" action="login" method="post">
          <div class="control-group">
            <label class="control-label" for="username">Brukernavn</label>
            <div class="controls">
              <input type="text" name="username" placeholder="Brukernavn"/>
            </div>
          </div>
          <div class="control-group">
            <label class="control-label" for="password">Password</label>
            <div class="controls">
              <input type="password" name="password" placeholder="Passord"/>
            </div>
          </div>
            <div class="control-group">
              <div class="controls">
                <label class="checkbox">
                  <input type="checkbox" name="RememberMe"/> <a href="#" data-toggle="tooltip" title="Kryss av her for å forbli pålogget. Du vil ikke automatisk logges av og slipper å logge inn hver gang du besøker Dommer-FIKS">
                  Forbli pålogget</a>
                  </label>
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

  def assignedMatches(assignedMatches: List[AssignedMatch]) = {
    emptyPage(tableOfAssignedMatches(assignedMatches))
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
    emptyPage(tableOfAvailableMatches(availableMatches))
  }

  def calendarSignup(missingFields: Seq[String] = Nil) = {
    emptyPage(
      calendarSignupForm(missingFields)
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
      <div>
        <div class="control-group">
        <div class="input-append">
         <input type="text" value={schemeAndUrl} class="input-xxlarge"></input>
           <a class="btn btn-inverse" href={resetCalIdUrl}><i class="icon-refresh icon-white"></i> Generer ny adresse</a>
           <a class="btn btn-danger" href={deleteCalUrl}><i class="icon-trash"></i> Slett kalender</a>
        </div>
        </div>
        <div class="control-group">
        <div class="btn-group">
          <a class="btn" href={schemeAndUrl}><i class="icon-download"></i> Last ned</a>
          <a class="btn" href={webcalUrl}><i class="icon-plus"></i> Abonnér i Outlook/iCal/iPhone</a>
          <a class="btn" href={googleCalUrl} target="_blank"><i class="icon-plus"></i> Abonnér i Google Calendar</a>
        </div>
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

  def invoices(invoices:Iterator[Invoice]) = {
    emptyPage(
      invoiceTable(invoices)
    )
  }

  def notFound = emptyPage( <div class="alert alert-block">
    <h4 class="alert-heading">Auda!</h4>
    Fant ikke siden du forsøkte å gå til. Bruk menyen over for å navigere. Eller gå direkte til <a href="/login">innloggingen</a>
  </div>)

  def forbidden = emptyPage( <div class="alert alert-block">
    <h4 class="alert-heading">Ingen tilgang</h4>
    Du har ikke tilgang til denne siden
  </div>)

  def error(n:NodeSeq) = emptyPage(
    <div class="alert alert-error">{n}</div>)

  def error(e: Exception) = {
    e.printStackTrace()
    e match {
      case ex: SocketTimeoutException => {
        emptyPage(
          <div class="alert alert-error">
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
          <div class="alert alert-error">
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
        <div class="control-group">
          <label class="control-label">Banenavn</label>
          <div class="controls">
            <span class="input-large uneditable-input">{stadiumName}</span>
          </div>
        </div>
        <div class="control-group">
          <label class="control-label" for="inputEmail">Email</label>
          <div class="controls">
            <input type="email" name="email" id="inputEmail" placeholder="Email" required="required"/>
            <span class="help-inline">Dersom Dommer-FIKS trenger å kontakte deg for mer informasjon</span>
          </div>
        </div>
        <div class="control-group">
          <label class="control-label" for="inputPassword">Beskrivelse av banen</label>
          <div class="controls">
            <textarea rows="3" placeholder="Beskrivelse av banens plassering (adresse etc)" name="description" required="required"></textarea>
            <span class="help-block">Beskriv så godt som mulig hvor banen ligger.
              Bruk gjerne gataadresse om du har det. Du kan også legge inn linker til Google maps eller lignende.</span>
          </div>
        </div>
        <div class="control-group">
          <div class="controls">
            <button type="submit" class="btn">Send inn</button>
          </div>
        </div>
      </form>
    )
  }
}