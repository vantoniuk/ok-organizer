package data.db

import play.api.libs.json._
import play.api.libs.functional.syntax._
import utils.imports._

trait DbJsonConversions {
  implicit val createdOkFormat = (
    (__ \ "id").format[String] and
    (__ \ "rev").format[String]
  )(CreatedOk.apply, unlift(CreatedOk.unapply))

  implicit val createdErrorFormat = (
    (__ \ "error").format[String] and
    (__ \ "reason").format[String]
  )(CreatedError.apply, unlift(CreatedError.unapply))
}
