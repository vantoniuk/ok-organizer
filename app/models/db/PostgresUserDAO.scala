package models.db

import models.{UserId, UserRole, User}
import Implicits._
import MyPostgresDriver.api.{Tag => DBTag, _}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits._

class Users(tag: DBTag) extends Table[User](tag, "users") {
  def id = column[UserId]("id", O.PrimaryKey, O.AutoInc)
  def email = column[String]("email")
  def emailConfirmed = column[Boolean]("confirmed")
  def password = column[String]("password")
  def nick = column[String]("nick")
  def firstName = column[String]("first name")
  def lastName = column[String]("last name")
  def role = column[UserRole]("role")

  def * = (id, email, emailConfirmed, password, nick, firstName, lastName, role) <> (User.apply _ tupled, User.unapply)

  def emailIdx = index("email_idx", email, unique = true)
}

object Users {
  val query = TableQuery[Users]
}

class PostgresUserDAO(database: Database) extends UserDAO {
  private def findByEmailQueryRaw(email: Rep[String]) = {
    Users.query.filter(_.email === email)
  }

  private val findByEmailQuery = Compiled(findByEmailQueryRaw _)

  def findByEmail(email: String): Future[Option[User]] = {
    database.run(findByEmailQuery(email).result).map(_.headOption)
  }

  def delete(email: String): Future[Int] = database.run(findByEmailQuery(email).delete)

  def save(user: User): Future[User] = {
    val insertAction = (Users.query returning Users.query.map(_.id) into ((user, id) => user.copy(id = id))) insertOrUpdate user

    database.run(insertAction).map(_.getOrElse(throw new IllegalStateException("Failed to save user!! " + user)))
  }
}
