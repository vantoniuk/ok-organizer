package data.basic

import utils.imports._

trait BasicConvertions {
  implicit def stringToOptionalString(input: String): OptionalString = OptionalString(input)
  implicit def optionalStringToString(str: OptionalString): String = str.toString

}
