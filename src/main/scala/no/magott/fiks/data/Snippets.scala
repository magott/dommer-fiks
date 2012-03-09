package no.magott.fiks.data

import xml.NodeSeq

object Snippets {

//  def matches(matches: List){
//
//  }

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

          <h1>Bootstrap starter template</h1>
          <p>Use this document as a way to quick start any new project. <br/>
            All you get is this message and a barebones HTML document.</p>

          {body}

        </div> <!-- /container -->
        <footer>Morten Andersen-Gott (c) 2012</footer>


        <!-- Placed at the end of the document so the pages load faster -->
        <script src="/js/jquery.js"></script>
        <script src="/js/bootstrap.js"></script>
      </body>
    </html>


}
