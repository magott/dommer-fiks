package no.magott.fiks.data
import org.scalatest.FunSuite
class MatchResultTest extends FunSuite{

  test("Correctly identifies required deletions"){
    val m1 = MatchResult(fiksId="12",teams="",matchId="", halfTimeScore = Some(Score(1,2)),resultReports = Set(ResultReport(ResultType.HalfTime,Score(1,1),"","")))
    assert(!m1.requiredDeletions.isEmpty)
    val m2 = MatchResult(fiksId="12",teams="",matchId="", finalScore = Some(Score(1,2)),resultReports = Set(ResultReport(ResultType.HalfTime,Score(1,1),"","")))
    assert(!m2.requiredDeletions.isEmpty)
    val m3 = MatchResult(fiksId="12",teams="",matchId="", halfTimeScore = Some(Score(1,1)) ,finalScore = Some(Score(1,1)),resultReports = Set(ResultReport(ResultType.HalfTime,Score(1,1),"",""),ResultReport(ResultType.FinalResult,Score(1,1),"","")))
    assert(m3.requiredDeletions.isEmpty)
    val m4 = MatchResult(fiksId="12",teams="",matchId="", halfTimeScore = Some(Score(0,0)) ,finalScore = Some(Score(1,2)),resultReports = Set(ResultReport(ResultType.HalfTime,Score(1,1),"",""),ResultReport(ResultType.FinalResult,Score(1,1),"","")))
    assert(m4.requiredDeletions.size == 2)
    val m5 = MatchResult(fiksId="12",teams="",matchId="",resultReports = Set(ResultReport(ResultType.HalfTime,Score(1,1),"",""),ResultReport(ResultType.FinalResult,Score(1,1),"","")))
    assert(m5.requiredDeletions.size == 2)
  }

}
