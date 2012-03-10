package no.magott.fiks.data

import no.magott.fiks.data.MatchScraper.AssignedMatch
import org.joda.time.LocalDateTime
import xml.{XML, NodeSeq}

object Snippets {

  def emptyPage(body: NodeSeq): NodeSeq =
    <html lang="en">
      <head>
          <meta charset="utf-8"/>
        <title>Fiks fix</title>
          <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
          <meta name="description" content=" "/>
          <meta name="author" content=" "/>

        <!-- Le styles -->
          <link href="/css/bootstrap.css" rel="stylesheet"/>
        <style type="text/css">
          {"""body
        {padding-top: 60px; /* 60px to make the container go all the way to the bottom of the topbar */}
        """}
        </style>
          <link href="/css/bootstrap-responsive.css" rel="stylesheet"/>

        <!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
        <!--[if lt IE 9]>
    <script src="//html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->

      </head>
      <body>
        <div class="navbar navbar-fixed-top">
          <div class="navbar-inner">
            <div class="container">
              <a class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
              </a>
              <a class="brand" href="#">Dommer-FIKS</a>
              <div class="nav-collapse">
                <ul class="nav">
                  <li class="active">
                    <a href="#">Dine oppdrag</a>
                  </li>
                  <li>
                    <a href="#available">Ledige oppdrag</a>
                  </li>
                  <li>
                    <a href="#about">Om</a>
                  </li>
                </ul>
              </div> <!--/.nav-collapse -->
            </div>
          </div>
        </div>


        <div class="container">

          {body}

        </div> <!-- /container -->
        <div class="alert alert-info">Denne siden er under utvikling og kan derfor være noe ustabil mens mer funksjonalitet utvikles. </div>
        <footer>Morten Andersen-Gott (c) 2012</footer>


        <!-- Placed at the end of the document so the pages load faster -->
        <script src="/js/jquery.js"></script>
        <script src="/js/bootstrap.js"></script>
      </body>
    </html>

  def tableOfAssignedMatches(assignedMatches: Iterator[AssignedMatch]) = {
    <table class="table table-striped table-bordered table-condensed">
      <thead>
        <tr>
          <th>Dato</th>
          <th>Tid</th>
          <th>Turnering</th>
          <th>Kamp</th>
          <th>Sted</th>
          <th>Dommere</th>
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
              {m.teams}
            </td>
            <td>
              {m.venue}
            </td>
            <td>
              {m.referees}
            </td>
          </tr>
      }}
      </tbody>
    </table>
  }

  def loginMessages(messageParams: Map[String, Seq[String]]) = {

    messageParams.get("message") match {
      case Some(msg) => msg.mkString("") match {
        case "loginRequired" => <div class="alert alert-info">Du må logge inn for å få tilgang til denne siden</div>
        case "loginFailed" => <div class="alert alert-error">Login feilet, prøv igjen</div>
        case "sessionTimeout" => <div class="alert alert">Sesjonen din er for gammel, du må logge inn på nytt</div>
        case _ =>
      }
      case None =>
    }
  }

  def googleCalendarLink(start: LocalDateTime, heading:String, location:String, details:String):NodeSeq = {
    val timeString = start.toString("yyyyMMdd'T'HHmmss'Z") +"/" +start.plusHours(2).toString("yyyyMMdd'T'HHmmss'Z")
    val xmlString = """<a href="http://www.google.com/calendar/event?action=TEMPLATE&amp;text=""" +
      heading + """&amp;dates="""+ timeString + """&amp;details=Kampdata&amp;location=Fjompesletta&amp;trp=false&amp;sprop=&amp;sprop=name:" target="_blank"><img src="//www.google.com/calendar/images/ext/gc_button1_no.gif" border="0"/></a>"""
    XML.loadString(xmlString)
  }

}
