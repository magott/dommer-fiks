package no.magott.fiks

import unfiltered.request.Params

object MatchIdParameter extends Params.Extract("matchid", Params.first)

object ResultParameter extends Params.Extract("result", Params.first)

object CommentParameter extends Params.Extract("comment", Params.first)

object ActionParameter extends Params.Extract("action", Params.first)
