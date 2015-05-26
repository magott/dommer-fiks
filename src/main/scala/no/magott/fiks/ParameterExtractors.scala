package no.magott.fiks

import unfiltered.request.Params

object MatchIdParameter extends Params.Extract("matchid", Params.first)

object TournamentParameter extends Params.Extract("tournament", Params.first)

object ResultParameter extends Params.Extract("result", Params.first)

object CommentParameter extends Params.Extract("comment", Params.first)

object ActionParameter extends Params.Extract("action", Params.first)

object CancellationIdParameter extends Params.Extract("cancellationId", Params.first)

object ReasonParameter extends Params.Extract("reason", Params.first)

object ExportParameter extends Params.Extract("export", Params.first)

object IdParameter extends Params.Extract("id", Params.first)
