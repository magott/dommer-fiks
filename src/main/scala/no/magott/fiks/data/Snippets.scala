package no.magott.fiks.data

import xml.{XML, NodeSeq}
import org.joda.time.{DateTimeZone, DateTime, LocalDateTime}

object Snippets {

  val calendarFormatString = "yyyyMMdd'T'HHmmss'Z"

  def emptyPage(body: NodeSeq, page: Option[String] = None): NodeSeq =
    <html lang="en">
      <head>
          <meta charset="utf-8"/>
        <title>Fiks fix</title>
          <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
          <meta name="description" content="Fiks, without the #fail"/>
          <meta name="author" content="Morten Andersen-Gott"/>
          <meta name="google-site-verification" content="ptF2AFWdgpfQFz8_Uu2o_kDR704noD60eKR4nHC3uT8"/>

        <!-- Le styles -->
          <link href="/css/bootstrap.min.css" rel="stylesheet"/>
        <style type="text/css">
          {"""body
        {padding-top: 60px; /* 60px to make the container go all the way to the bottom of the topbar */}
        """}
        </style>
          <link href="/css/bootstrap-responsive.min.css" rel="stylesheet"/>
          <link href="/css/fiks.css" rel="stylesheet"/>

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
              <a class="brand" href="/">Dommer-FIKS</a>
              <div class="nav-collapse">
                <ul class="nav">
                  <li class={if (page.getOrElse("").contains("mymatches")) "active" else "inactive"}>
                    <a href="/fiks/mymatches">Dine oppdrag</a>
                  </li>
                  <li class={if (page.getOrElse("").contains("availablematches")) "active" else "inactive"}>
                    <a href="/fiks/availablematches">Ledige oppdrag</a>
                  </li>
                  <li class={if (page.getOrElse("").contains("about")) "active" else "inactive"}>
                    <a href="/fiks/about">Om</a>
                  </li>
                </ul>
              </div> <!--/.nav-collapse -->
            </div>
          </div>
        </div>


        <div class="container">

          {body}

        </div> <!-- /container -->
        <div class="alert alert-success">
          Dommer-FIKS har nå den funksjonaliteten den i utgangspunktet var tiltenkt.
          Send inn forslag via Facebook dersom det er noe du savner. Gi også beskjed om du opplever noen problemer.
        </div>
        <footer>
          <p>
            <a href="http:///www.andersen-gott.com">Morten Andersen-Gott</a>
            (c) 2012</p>
          <p>
            <a href="http://www.facebook.com/dommerfiks">Foreslå forbedringer eller rapportér feil</a>
          </p>
        </footer>


        <!-- Placed at the end of the document so the pages load faster -->
        <script src="/js/jquery.js"></script>
        <script src="/js/bootstrap.min.js"></script>
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
      </body>
    </html>

  def tableOfAssignedMatches(assignedMatches: List[AssignedMatch]) = {
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
              {m.teams}
            </td>
            <td>
              {m.venue}
            </td>
            <td>
              {m.referees}
            </td>
            <td>
              {googleCalendarLink(m.date, m.teams, m.venue, m.referees)}
              |
              {icsLink(m.date, m.teams, m.venue, m.referees)}
            </td>
          </tr>
      }}
      </tbody>
    </table>
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
                {
                m.availabilityId match{
                  case Some(id) => <a href={"""availablematches?matchid=""" + m.availabilityId.get + """"""}>Meld interesse</a>
                  case None => <div>Meldt inn</div>
                }
                }
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
      <form class="well" action={"""availablematches?matchid="""+availableMatch.availabilityId.get} method="post">
        <tr>
          <th>Kommentarer</th>
          <td>
              <textarea name="comment" id="comment"/>
          </td>
        </tr>
        <tr>
          <td/>
          <td><button type="submit" class="btn btn-primary">Meld interesse</button></td>
        </tr>
      </form>
    </table>
  }

  def googleCalendarLink(start: LocalDateTime, heading: String, location: String, details: String): NodeSeq = {
    val utcStart = toUTC(start)
    val timeString = utcStart.toString(calendarFormatString) + "/" + utcStart.plusHours(2).toString(calendarFormatString)
    var linkString = """http://www.google.com/calendar/event?action=TEMPLATE&amp;text=""" +
      heading + """&amp;dates=""" + timeString + """&amp;details=""" + details + """&amp;location=""" + location + """&amp;trp=false&amp;sprop=&amp;sprop=name:"""
    val xmlString = """<a href="""" + santitizeURL(linkString) + """" target="_blank">Google</a>"""
    XML.loadString(xmlString)
  }


  def isc(uriParams: Map[String, Seq[String]]) = {
    val heading = uriParams.getOrElse("heading", Seq("")).head
    val startTime = uriParams.getOrElse("startTime", Seq("")).head.toLong
    val details = uriParams.getOrElse("details", Seq("")).head
    val location = uriParams.getOrElse("location", Seq("")).head
    val start = toUTC(new LocalDateTime(startTime))
    "BEGIN:VCALENDAR\n" +
      "VERSION:2.0\n" +
      "PRODID:-//hacksw/handcal//NONSGML v1.0//EN\n" +
      "BEGIN:VEVENT\n" +
      "LOCATION:" + location + "\n" +
      "DTSTART:" + start.toString(calendarFormatString) + "\n" +
      "DTEND:" + start.plusHours(2).toString(calendarFormatString) + "\n" +
      "SUMMARY:" + heading + "\n" +
      "DESCRIPTION:" + details + "\n" +
      "END:VEVENT\n" +
      "END:VCALENDAR\n"
  }

  private def santitizeURL(url: String) = {
    url.replaceAllLiterally(" ", "%20")
    url.replaceAllLiterally("-", "%2D")
      .replaceAllLiterally("æ", "%C3%A6")
      .replaceAllLiterally("Æ", "%C3%86")
      .replaceAllLiterally("ø", "%C3%B8")
      .replaceAllLiterally("Ø", "%C3%98")
      .replaceAllLiterally("å", "%C3%A5")
      .replaceAllLiterally("Å", "%C3%85")
      .replaceAllLiterally("<br/>", "%0A")
      .replaceAllLiterally("<br>", "%0A")
      .replaceAllLiterally("</br>", "%0A")
  }

  private def toUTC(dateTime: LocalDateTime) = {
    dateTime.toDateTime(DateTimeZone.forID("Europe/Oslo")).withZone(DateTimeZone.UTC).toLocalDateTime
  }

  private def icsLink(start: LocalDateTime, heading: String, location: String, details: String) = {
    val url = "/match.ics?startTime=" + start.toDate.getTime + "&heading=" + heading + "&location=" + location + "&details=" + details;
    <a href={santitizeURL(url)}>Outlook/iCal</a>
  }
}
