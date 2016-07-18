package models.db

import com.google.inject.Singleton
import models.note.db.{PostgresNodeDAO, NodeDAO}
import play.api.Play.current
import play.api.db.{DB => PlayDB}
import MyPostgresDriver.api._

object DB {
  type DATABASE = MyPostgresDriver.backend.Database

  @Singleton
  class PostgresDAOProvider(db: DATABASE) extends DAOProvider {
    def this() = this(PostgresDAOProvider.database)
    def withTransaction[T](doWork: (DAOProvider) => T): T = ???

    def userDAO: UserDAO = new PostgresUserDAO(db)
    def nodeDAO: NodeDAO = new PostgresNodeDAO(db)
  }

  object PostgresDAOProvider {
    val database = Database.forConfig("database")
  }

}
