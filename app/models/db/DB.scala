package models.db

import com.google.inject.Singleton
import models.note.db.{PostgresNodeDAO, NodeDAO}
import play.api.Play.current
import play.api.db.{DB => PlayDB}
import MyPostgresDriver.api._
import tracking._

object DB {
  type DATABASE = MyPostgresDriver.backend.Database

  @Singleton
  class PostgresDAOProvider(db: DATABASE) extends DAOProvider {
    def this() = this(PostgresDAOProvider.database)
    def withTransaction[T](doWork: (DAOProvider) => T): T = ???

    lazy val userDAO: UserDAO = new PostgresUserDAO(db)
    lazy val nodeDAO: NodeDAO = new PostgresNodeDAO(db)
    lazy val serviceDAO: ServiceDAO = new PostgresServiceDAO(db)

    lazy val creditCardsDAO: CreditCardsDAO = new PostgresCreditCardsDAO(db)
    lazy val spendCategoriesDAO: SpendCategoriesDAO = new PostgresSpendCategoriesDAO(db)
    lazy val creditCardStatementsDAO: CreditCardStatementsDAO = new PostgresCreditCardStatementsDAO(db)
  }

  object PostgresDAOProvider {
    val database = Database.forConfig("database")
  }

}
