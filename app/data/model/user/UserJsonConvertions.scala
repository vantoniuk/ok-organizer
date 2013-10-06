package data.model.user

import play.api.libs.json._
import play.api.libs.functional.syntax._
import utils.AppConfig._
import org.joda.time.DateTime
import utils.imports._

trait UserJsonConvertions {
  def dateTimeToString(date: DateTime): String = date.toString(dateFormat)
  def parseDateTime(date: String): DateTime = dateFormat.parseDateTime(date)

  def optDateTimeToString(dateOpt: Option[DateTime]): Option[String] = dateOpt map dateTimeToString
  def optParseDateTime(dateOpt: Option[String]): Option[DateTime] = dateOpt map parseDateTime

  implicit val personalDataFormat = (
    (__ \ "un").format[String] and
    (__ \ "e").format[String] and
    (__ \ "fn").formatNullable[String] and
    (__ \ "ln").formatNullable[String]
  )(PersonalData.apply, unlift(PersonalData.unapply))

  implicit val userDataFormat = (
    (__ \ "id").format[String] and
    (__ \ "rd").formatNullable[String].inmap(optParseDateTime, optDateTimeToString) and
    (__ \ "lld").formatNullable[String].inmap[Option[DateTime]](optParseDateTime, optDateTimeToString)
  )(UserAppData.apply, unlift(UserAppData.unapply))

  implicit val notRegisteredUserFormat = (
    (__ \ "pd").format[PersonalData] and
    (__ \ "s").format[Int].inmap[Service](Services(_), _.id) and
    (__ \ "type").format[Int].inmap[DataType](DataTypes(_), _.id) and
    (__ \ "r").format[Boolean]
  ) (NotRegisteredUser.fromDb, unlift(NotRegisteredUser.toDb))

}