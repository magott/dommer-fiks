package validation

sealed trait FormField{
  val fieldName:String
  val value:Option[String]
  def isValid:Boolean
  def isError = !isValid
  def errorMessage:Option[String]
  def toTuple = (fieldName -> this)
  def and(i:FormField)(f:(FormField, FormField) => Map[String,FormField])={
    f(this, i)
  }
}
case class InputFail(fieldName:String,value:Option[String], m:String) extends FormField{
  def isValid = false
  def errorMessage = Some(m)
}

case class InputOk(fieldName:String, value:Option[String]) extends FormField{
  def isValid = true
  def errorMessage = None
}

object Validators {
  def isNonEmpty(fieldName:String, value:String, errorMessage:String):FormField = {
    if(value.isEmpty) InputFail(fieldName,Some(value),errorMessage) else InputOk(fieldName,Some(value))
  }

  def isBlankOrInt(fieldName:String, value:String, errorMessage:String):FormField = {
    val trimmed = value.trim
    if(trimmed.isEmpty) InputOk(fieldName,None) else if(trimmed.forall(_.isDigit)) InputOk(fieldName, Some(trimmed)) else InputFail(fieldName,Some(trimmed),errorMessage)
  }

  def bothSetOrUnset(errorMsg:String)(first:FormField,second:FormField): Map[String,FormField] = {
    if(first.isValid && second.isValid){
      if(first.value.isDefined != second.value.isDefined){
        Map(InputFail(first.fieldName,first.value,errorMsg).toTuple, InputFail(second.fieldName,second.value,errorMsg).toTuple)
      }else{
        Map(first.toTuple,second.toTuple)
      }
    }else{
      Map(first.toTuple,second.toTuple)
    }
  }

  def intLiteralToOption(s:String) = if(s.trim.isEmpty) None else Some(s.trim.toInt)

}
