package models.db

import models.{UserId, UserRole, User}
import slick.driver.PostgresDriver.api._
import slick.jdbc.JdbcBackend.Database
import Implicits._
import MyPostgresDriver.api.{Tag => DBTag, _}

class Users(tag: Tag) extends Table[User](tag, "users") {
  def id = column[UserId]("id", O.PrimaryKey, O.AutoInc)
  def email = column[String]("email")
  def emailConfirmed = column[Boolean]("confirmed")
  def password = column[String]("password")
  def nick = column[String]("nick")
  def firstName = column[String]("first name")
  def lastName = column[String]("last name")
  def role = column[UserRole]("role")

  def * = (id, email, emailConfirmed, password, nick, firstName, lastName, role) <> (User.apply _ tupled, User.unapply)
}

object Users {
  val query = TableQuery[Users]
  query.schema.create
}

object UserDAO {
  val a = Database.forConfig("database")
}
