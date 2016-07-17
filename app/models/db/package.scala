package models

import scala.concurrent.Future

package object db {

  trait DAOProvider {
    def withTransaction[T](doWork: DAOProvider => T): T

    def userDAO: UserDAO
  }

  trait UserDAO {
    def findByEmail(email: String): Future[Option[User]]

    def save(user: User): Future[User]

    def delete(email: String): Future[Int]
  }

}
