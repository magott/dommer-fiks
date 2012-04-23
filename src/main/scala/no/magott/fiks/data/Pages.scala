package no.magott.fiks.data

import javax.servlet.http.HttpServletRequest
import xml.NodeSeq
import unfiltered.request.{Host, HttpRequest}
import no.magott.fiks.HerokuRedirect.XForwardProto

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
      , Some("about"))
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
    val googleCalUrl = "http://www.google.com/calendar/render?cid=%s".format(schemeAndUrl)
    val appleUrl = "webcal://"+url
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
         <input type="text" value={schemeAndUrl}></input>
           <a class="btn btn-inverse" href={resetCalIdUrl}><i class="icon-refresh icon-white"></i> Generer ny adresse</a>
           <a class="btn btn-danger" href={deleteCalUrl}><i class="icon-trash"></i> Slett kalender</a>
        </div>
        <div class="btn-group">
          <a class="btn" href={schemeAndUrl}><i class="icon-download"></i> Last ned</a>
          <a class="btn" href={appleUrl}><i class="icon-plus"></i> Abonnèr i Outlook/iCal/iPhone</a>
          <a class="btn" href={googleCalUrl} target="_blank"><i class="icon-plus"></i> Abonnér i Google Calendar</a>
        </div>
      </div>
    )
  }

  def notFound = emptyPage( <div class="alert alert-block">
    <h4 class="alert-heading">Auda!</h4>
    Fant ikke siden du forsøkte å gå til. Bruk menyen over for å navigere. Eller gå direkte til <a href="/login">innloggingen</a>
  </div>)

  def error(e: Exception) = {
    emptyPage(
      <div class="alert alert-error">
        <p>
            <strong>Her skjedde det en uventet feil:</strong> Forhåpentligvis går det bedre om du prøver på nytt. Bruk menyen over for å prøve igjen.
        </p>
      </div>
    )
  }

}