package validation

import org.scalatest.FunSuite
import validation.Validators._

class ValidationTest extends FunSuite{

  test("Non-empty string is valid"){
    val nonEmpty = isNonEmpty("field", "data", "cannot be blank")
    assert(nonEmpty.isValid)
  }

  test("Empty string is invalid"){
    val empty = isNonEmpty("field", "", "cannot be blank")
    assert(empty.isError)
    assert(empty.errorMessage.isDefined)
    assert(empty.errorMessage.get == "cannot be blank")
  }

  test("Integer string is valid"){
    val empty = isBlankOrInt("field", "10", "cannot be blank")
    assert(empty.isValid)
    assert(empty.errorMessage.isEmpty)
  }

  test("Empty string is valid integer string"){
    val empty = isBlankOrInt("field", "", "cannot be blank")
    assert(empty.isValid)
    assert(empty.errorMessage.isEmpty)
  }

  test("Alpha character string is invalid integer string"){
    val empty = isBlankOrInt("field", "a", "is not a valid number")
    assert(empty.isError)
    assert(empty.errorMessage.get == "is not a valid number")
  }

}
