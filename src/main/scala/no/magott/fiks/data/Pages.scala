package no.magott.fiks.data

import javax.servlet.http.HttpServletRequest
import xml.NodeSeq
import unfiltered.request.{Host, HttpRequest}
import no.magott.fiks.HerokuRedirect.XForwardProto
import java.net.SocketTimeoutException
import validation.FormField

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
            <input type="text" name="username"/>
            <input type="password" name="password"/>
          <button type="submit" class="btn btn-primary">Logg inn</button>
        </form>
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

          <form action="https://www.paypal.com/cgi-bin/webscr" method="post">
            <input type="hidden" name="cmd" value="_s-xclick" />
            <input type="hidden" name="encrypted" value="-----BEGIN PKCS7-----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-----END PKCS7-----"/>
            <input type="image" src="https://www.paypalobjects.com/en_US/i/btn/btn_donate_LG.gif" border="0" name="submit" alt="PayPal - The safer, easier way to pay online!" />
            <img alt="" border="0" src="https://www.paypalobjects.com/en_US/i/scr/pixel.gif" width="1" height="1" />
          </form>
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
    emptyPage(tableOfAssignedMatches(assignedMatches), Some("mymatches"))
  }

  def assignedMatchInfo(m:AssignedMatch) = emptyPage(assignedMatchDetailsTable(m))

  def assignedMatchResult(r:MatchResult, inputFields: Map[String, FormField] = Map.empty) = emptyPage(assignedMatchResultForm(r, inputFields))

  def availableMatches(availableMatches: List[AvailableMatch]) = {
    emptyPage(tableOfAvailableMatches(availableMatches), Some("availablematches"))
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
    val webcalUrl = if(XForwardProto.unapply(req).isDefined) "webcal://"+url else "webcal://+url"
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
        <div class="input-append">
         <input type="text" value={schemeAndUrl} class="input-xxlarge"></input>
           <a class="btn btn-inverse" href={resetCalIdUrl}><i class="icon-refresh icon-white"></i> Generer ny adresse</a>
           <a class="btn btn-danger" href={deleteCalUrl}><i class="icon-trash"></i> Slett kalender</a>
        </div>
        <div class="btn-group">
          <a class="btn" href={schemeAndUrl}><i class="icon-download"></i> Last ned</a>
          <a class="btn" href={webcalUrl}><i class="icon-plus"></i> Abonnér i Outlook/iCal/iPhone</a>
          <a class="btn" href={googleCalUrl} target="_blank"><i class="icon-plus"></i> Abonnér i Google Calendar</a>
        </div>
      </div>
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

  def error(e: Exception) = {
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

}