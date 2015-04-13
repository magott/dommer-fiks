package no.magott.fiks.data
import org.scalatest.FunSuite
import org.joda.time.LocalDateTime

class MatchTest extends FunSuite{

  test("Only show dismissal link if head referee and match not in the future"){
    val m = AssignedMatch(LocalDateTime.now.minusMinutes(1), "3 div", "0301010101", "Home - Away", "A venue", "(Dommer) Morten  (AD1) Truls  Tlf.: mangler, Mobil:11111111 (AD2) Anton  Tlf.: mangler, Mobil:4444444", "12345", None)
    assert(m.displayDismissalReportLink)
    val mFutureDateKickoff = m.copy(date = LocalDateTime.now.plusMinutes(15))
    assert(!mFutureDateKickoff.displayDismissalReportLink)
    val notHeadReferee = m.copy(referees = "(Dommer) Truls Tlf.: mangler, Mobil:11111111 (AD1) Morten (AD2) Anton  Tlf.: mangler, Mobil:4444444")
    assert(!notHeadReferee.displayDismissalReportLink)
  }
  test("Able to extract referee first name and last name"){
    val m = AssignedMatch(LocalDateTime.now.minusMinutes(1), "3 div", "0301010101", "Home - Away", "A venue", "(Dommer) Morten Andersen-Gott (AD1) Truls  Tlf.: mangler, Mobil:11111111 (AD2) Anton  Tlf.: mangler, Mobil:4444444", "12345", None)
    assert(m.refereeFirstName == "Morten")
    assert(m.refereeLastName == "Andersen-Gott")
    val mRefWithMultipleFirstNames = m.copy(referees = "(Dommer) Morten Per Nilsen Trulsen (AD1) Truls  Tlf.: mangler, Mobil:11111111 (AD2) Anton  Tlf.: mangler, Mobil:4444444")
    assert(mRefWithMultipleFirstNames.refereeFirstName == "Morten Per Nilsen")
    assert(mRefWithMultipleFirstNames.refereeLastName == "Trulsen")
  }

  test("Dismissal report getters"){
    val m = AssignedMatch(LocalDateTime.now.minusMinutes(1), "3 div", "0301010101", "Home\u00A0-\u00A0Away", "A venue", "(Dommer) Morten Andersen-Gott (AD1) Truls  Tlf.: mangler, Mobil:11111111 (AD2) Anton  Tlf.: mangler, Mobil:4444444", "12345", None)
    assert(m.refereeLastName == "Andersen-Gott")
  }

  test("Can generate dismissal report url"){
    val m = AssignedMatch(LocalDateTime.now.minusMinutes(1), "3 div", "0301010101", "Home\u00A0-\u00A0Away", "A venue", "(Dommer) Morten Andersen-Gott (AD1) Truls  Tlf.: mangler, Mobil:11111111 (AD2) Anton  Tlf.: mangler, Mobil:4444444", "12345", None)
    print(m.dismissalUrl)
    assert(m.dismissalUrl.startsWith("http"))
  }

  test("Can create html with tel links of referee tupples"){
    val refString = "(Dommer) Monica Larsen  Tlf.: mangler, Mobil:94058108 (AD1) Morten Andersen-Gott  (AD2) Eirik Aasheim  Tlf.: mangler, Mobil:91159497 (4. dommer) Resa Hauge  Tlf.: mangler, Mobil:97000098 (Dommerveileder) Jane Therese Sæves Myran  Tlf.:69886806, Mobil: mangler (Kampdelegat) Robert Valdemar Iversen  Tlf.:70131941, Mobil:48116530"

  }

  test("Can extract name, phone number and mobile"){
    val NameMobilePhone(n1, p1, m1) = "Monica Larsen Tlf.: mangler, Mobil:94058108"
    assert(m1.isDefined)
    assert(p1.isEmpty)
    assert(!n1.contains(":"))
    val NameMobilePhone(n2, p2, m2) = "Morten Taraldsvik Olafsen  Tlf.:91304863, Mobil:91304863"
    assert(!n2.contains(":"))
    assert(m2.isDefined)
    assert(p2.isDefined)
    val NameMobilePhone(n3, p3, m3) = "Reservert Oslo  Tlf.: mangler, Mobil: mangler"
    assert(!n3.contains(":"))
    assert(m3.isEmpty)
    assert(p3.isEmpty)
    val NameMobilePhone(n4, p4, m4) = "Kim Minh Nguyen  Tlf.: mangler, Mobil:962 00 016"
    assert(m4.isDefined)
    assert(p4.isEmpty)
    assert(m4.forall(! _.contains(" ")))
    val NameMobilePhone(n5, p5, m5) = "Kim Minh Nguyen  Tlf.: mangler, Mobil:+47 962 00 016"
    assert(m5.exists(_ == "+4796200016"))
  }

  test("Can extract official"){
    val refString = "(Dommer) Monica Larsen  Tlf.: mangler, Mobil:94058108 (AD1) Morten Andersen-Gott  (AD2) Eirik Aasheim  Tlf.: mangler, Mobil:91159497 (4. dommer) Resa Hauge  Tlf.: mangler, Mobil:97000098 (Dommerveileder) Jane Therese Sæves Myran  Tlf.:69886806, Mobil: mangler (Kampdelegat) Robert Valdemar Iversen  Tlf.:70131941, Mobil:48116530"
    val m = AssignedMatch(LocalDateTime.now, "3. div", "123", "a - b", "venue", refString, "23", None)
    val officials = m.officials
    assert(officials.find(_.role == AssistentDommer1).isDefined)
  }




}
