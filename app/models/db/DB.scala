package models.db

import play.api.Play.current
import play.api.db.{DB => PlayDB}
import MyPostgresDriver.api._

object DB {
  type DATABASE = MyPostgresDriver.backend.Database

  class PostgresDAOProvider(db: DATABASE) extends DAOProvider {
    def withTransaction[T](doWork: (DAOProvider) => T): T = ???

    def userDAO: UserDAO = ???
  }

  object PostgresDAO {
    val database = Database.forConfig("database")
  }

}
