package models

import com.google.inject.ImplementedBy
import models.db.DB.PostgresDAOProvider

import scala.concurrent.Future

package object db {

  @ImplementedBy(classOf[PostgresDAOProvider])
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
