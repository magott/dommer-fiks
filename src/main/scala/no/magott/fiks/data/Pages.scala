package no.magott.fiks.data

import no.magott.fiks.data.Snippets._

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
      Dommer-FIKS ser ditt brukernavn og passord, men lagrer ikke dette på noen måte.
      Det finnes ingen steder i Dommer-FIKS etter innlogging er fortetatt
    </p>
    ,Some("about"))
  }

  def reportInterestIn(availableMatch: AvailableMatch) = {
    emptyPage(reportInterestForm(availableMatch))
  }

  def assignedMatches(assignedMatches: List[AssignedMatch]) = {
    emptyPage(tableOfAssignedMatches(assignedMatches),Some("mymatches"))
  }

  def availableMatches(availableMatches: List[AvailableMatch]) = {
    emptyPage(tableOfAvailableMatches(availableMatches),Some("availablematches"))
  }

}