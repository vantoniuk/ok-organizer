package models

import com.google.inject.ImplementedBy
import models.db.DB.PostgresDAOProvider
import models.note.db.NodeDAO

import scala.concurrent.Future

package object db {

  @ImplementedBy(classOf[PostgresDAOProvider])
  trait DAOProvider {
    def withTransaction[T](doWork: DAOProvider => T): T

    def userDAO: UserDAO
    def nodeDAO: NodeDAO
    def serviceDAO: ServiceDAO
  }

  trait UserDAO {
    def findByEmail(email: String): Future[Option[User]]

    def save(user: User): Future[User]

    def delete(email: String): Future[Int]
  }

  trait ServiceDAO {
    def getAll: Future[Seq[Service]]

    def find(id: ServiceId): Future[Option[Service]]

    def save(service: Service): Future[Service]

    def delete(id: ServiceId): Future[Int]
  }

}
