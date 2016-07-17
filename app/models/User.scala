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

object User {
  val users = scala.collection.mutable.HashMap[Int, User](
    1 -> User(UserId(1), "master@myweb.com", true, (new BCryptPasswordHasher()).hash("123123").password, "Eddy", "Eddard", "Stark", UserRole.MASTER),
    2 -> User(UserId(2), "a@myweb.com", true, (new BCryptPasswordHasher()).hash("123123").password, "Maggy", "Margaery", "Tyrell", UserRole.ADMIN),
    3 -> User(UserId(3), "b@myweb.com", true, (new BCryptPasswordHasher()).hash("123123").password, "Petyr", "Petyr", "Baelish", UserRole.USER)
  )

  def findByEmail(email: String): Future[Option[User]] = Future.successful(users.find(_._2.email == email).map(_._2))
  //	def findByEmailMap[A] (email: String)(f: User => A): Future[Option[A]] = findByEmail(email).map(_.map(f))

  def save(user: User): Future[User] = {
    // A rudimentary auto-increment feature...
    def nextId: Int = users.maxBy(_._1)._1 + 1

    val theUser = if (user.id.id > 0) user else user.copy(id = UserId(nextId))
    users += (theUser.id.id -> theUser)
    Future.successful(theUser)
  }

  def remove(email: String): Future[Unit] = findByEmail(email).map(_.map(u => users.remove(u.id.id)))
}