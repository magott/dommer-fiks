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

  }

}