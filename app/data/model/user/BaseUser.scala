package data.model.user

import com.github.nscala_time.time.Imports._

/**
 * Base trait for all users in application
 */
trait BaseUser extends CouchDbModel {
  def personalData: PersonalData
  def registered: Boolean
  def registrationDate: Option[DateTime]
  def lastLoginDate: Option[DateTime]
  def userName: String
  def email: String
}

case class PersonalData(userName: String, email: String, firstName: Option[String], lastName: Option[String])

case class UserAppData(id: String, registrationDate: Option[DateTime], lastLoginDate: Option[DateTime])