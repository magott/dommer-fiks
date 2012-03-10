package no.magott.fiks.data

import no.magott.fiks.data.Snippets._
import no.magott.fiks.data.MatchScraper.AssignedMatch

object Pages {
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
          <a href="http://www.fiks.fotball.no">orginale fiks</a>
        </p>
        <form class="form-horizontal" action="login" method="post">
            <input type="text" name="username"/>
            <input type="password" name="password"/>
          <button type="submit" class="btn btn-primary">Logg inn</button>
        </form>
    )
  }

  def assignedMatches(assignedMatches: Iterator[AssignedMatch]) = {
    {
      emptyPage(tableOfAssignedMatches(assignedMatches))
    }
  }

  def about = {
    <h2>Om denne siden</h2>
      <p>
        Denne siden er utviklet av Morten Andersen-Gott. Dersom du har innspill til forbedringer eller vil rapportere
        en feil kan du gjøre dette <a href="https://github.com/magott/ofk-fiks/issues">her</a>
      </p>

      <h2>Sikkerhet</h2>
      <p>
        Dommer-FIKS trenger ditt brukernavn og passord for fiks for å hente ut og oppdatere informasjon om deg.
        I alphaversjon sendes brukernavn og passord i klartekst. Dette betyr at det er enkelt for kyndige personer å lese
        passord og brukernavnet ditt. I fremtiden vil passord og brukernavn gå over HTTPS slik at dette ikke er noe problem.
      </p>
    <p>
      Dommer-FIKS ser ditt brukernavn og passord, men hverken lagrer ikke dette.
      Det finnes ingen steder i Dommer-FIKS etter innlogging er fortetatt
    </p>

  }

}