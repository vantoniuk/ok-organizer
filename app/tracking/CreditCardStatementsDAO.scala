package tracking

import models.db.MyPostgresDriver.MyAPI._
import models.db.{Users, MyPostgresDriver, Implicits}
import models.{User, UserId, UserRole}
import org.joda.time.{Interval, DateTime}
import slick.dbio.DBIOAction

import scala.concurrent.Future
import models.{UserId, UserRole, User}
import Implicits._
import MyPostgresDriver.api.{Tag => DBTag, _}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits._

case class CreditCardStatement(id: Int, userId: UserId, creditCardId: CreditCardId, availableCredit: Int, timestamp: DateTime)

class CreditCardStatements(tag: DBTag) extends Table[CreditCardStatement](tag, "credit_cards") {
  def id = column[Int]("id")
  def userId = column[UserId]("user_id")
  def creditCardId = column[CreditCardId]("card_id")
  def availableCredit = column[Int]("available_credit")
  def timestamp = column[DateTime]("timestamp")

  def * = (id, userId, creditCardId, availableCredit, timestamp) <> (CreditCardStatement.apply _ tupled, CreditCardStatement.unapply)

  def cardIdFK = foreignKey("credit_card_statement_card_id_fk", creditCardId, CreditCards.query)(_.creditCardId, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)
  def userIdFK = foreignKey("credit_card_statement_uid_fk", userId, Users.query)(_.id, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)
}

object CreditCardStatements {
  val query = TableQuery[CreditCardStatements]
}

trait CreditCardStatementsDAO {
  def findByUserId(userId: UserId, interval: Interval): Future[Seq[CreditCardStatement]]
  def saveStatement(creditCard: CreditCardStatement): Future[Boolean]
}

class PostgresCreditCardStatementsDAO(database: Database) extends CreditCardStatementsDAO {
  private def byUserInterval(userId: Rep[UserId], from: Rep[DateTime], to: Rep[DateTime]) = {
    for {
      statement <- CreditCardStatements.query if statement.userId === userId && statement.timestamp >= from && statement.timestamp <= to
    } yield statement
  }

  private val byUserIntervalCompiled = Compiled(byUserInterval _)

  def findByUserId(userId: UserId, interval: Interval): Future[Seq[CreditCardStatement]] = {
    database.run(byUserIntervalCompiled(userId, interval.getStart, interval.getEnd).result)
  }

  def saveStatement(creditCard: CreditCardStatement): Future[Boolean] = {
    database.run(CreditCardStatements.query.+=(creditCard)).map(_ > 0)

  }
}
