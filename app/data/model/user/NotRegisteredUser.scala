package data.model.user

import com.github.nscala_time.time.Imports._
import play.api.libs.json.Json
import utils.imports._
import scala.concurrent.Future
import java.util.UUID
import data.db.DbResponses.DataResponseOk


case class NotRegisteredUser (personalData: PersonalData, source: Service) extends BaseUser {
  def registered: Boolean = false
  def registrationDate: Option[DateTime] = None
  def lastLoginDate: Option[DateTime] = None

  def userName: String = personalData.userName
  def email: String = personalData.email

  def save(): Future[DbAction] = NotRegisteredUser.save(this)
}

object NotRegisteredUser {
  import AppMain.dbClient

  private def dbActionToUserOpt(action: DbAction): Option[NotRegisteredUser] = action match {
    case DbSuccess(CouchDbClient.OK, DataResponseOk(user: NotRegisteredUser)) =>
      Some(user)
    case _ => None
  }

  def objType: DataType = DataTypes.User

  def fromDb[user](personalData: PersonalData, source: Service, objType: DataType, registered: Boolean): NotRegisteredUser =
    new NotRegisteredUser(personalData, source)

  def toDb(user: NotRegisteredUser): Option[(PersonalData, Service, DataType, Boolean)] = Some((user.personalData, user.source, objType, false))

  def save(user: NotRegisteredUser): Future[DbAction] = dbClient.save(UUID.randomUUID.toString, Json.toJson(user))
  def save(id: String, user: NotRegisteredUser): Future[DbAction] = dbClient.save(id, Json.toJson(user))

  def get(id: String): Future[Option[NotRegisteredUser]] = (dbClient.find[NotRegisteredUser](id)) map (dbActionToUserOpt)

}