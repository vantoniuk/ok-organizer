package models

import com.mohiva.play.silhouette.impl.util.BCryptPasswordHasher
import utils.silhouette.IdentitySilhouette

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
object UserRole extends Enumeration {
  val MASTER = Value(0, "master")
  val ADMIN = Value(10, "admin")
  val USER = Value(100, "user")
  val GUEST = Value(1000, "user")

  def byName(in: String): UserRole.Value = values.find(_.toString == in).getOrElse(GUEST)
  def byId(in: Int): UserRole.Value = values.find(_.id == in).getOrElse(GUEST)
}

case class UserId(id: Int) extends AnyVal

object UserId {
  val empty = UserId(-1)
}

case class User(
    id: UserId,
    email: String,
    emailConfirmed: Boolean,
    password: String,
    nick: String,
    firstName: String,
    lastName: String,
		role: UserRole) extends IdentitySilhouette {
  def key = email
  def fullName: String = firstName + " " + lastName
}