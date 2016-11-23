package tracking

import models.UserId
import models.db.Implicits._
import models.db.MyPostgresDriver.api.{Tag => DBTag, _}
import models.db.{Implicits, MyPostgresDriver, Users}
import org.joda.time.{DateTime, Interval}

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

case class CreditCardStatement(id: Int, userId: UserId, creditCardId: CreditCardId, amount: Int, timestamp: DateTime)
case class RichCreditCardStatement(creditCardVendor: String, creditCardName: String, amount: Int, timestamp: DateTime)

class CreditCardStatements(tag: DBTag) extends Table[CreditCardStatement](tag, "credit_card_statements") {
  def id = column[Int]("id")
  def userId = column[UserId]("user_id")
  def creditCardId = column[CreditCardId]("card_id")
  def amount = column[Int]("amount")
  def timestamp = column[DateTime]("timestamp")

  def * = (id, userId, creditCardId, amount, timestamp) <> (CreditCardStatement.apply _ tupled, CreditCardStatement.unapply)

  def cardIdFK = foreignKey("credit_card_statement_card_id_fk", creditCardId, CreditCards.query)(_.creditCardId, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)
  def userIdFK = foreignKey("credit_card_statement_uid_fk", userId, Users.query)(_.id, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)
}

object CreditCardStatements {
  val query = TableQuery[CreditCardStatements]
}

trait CreditCardStatementsDAO {
  def findByUserId(userId: UserId, interval: Interval): Future[Seq[CreditCardStatement]]
  def findRichByUserId(userId: UserId, interval: Interval): Future[Seq[RichCreditCardStatement]]
  def saveStatement(creditCard: CreditCardStatement): Future[Boolean]
}

class PostgresCreditCardStatementsDAO(database: Database) extends CreditCardStatementsDAO {
  private def byUserInterval(userId: Rep[UserId], from: Rep[DateTime], to: Rep[DateTime]) = {
    for {
      statement <- CreditCardStatements.query if statement.userId === userId && statement.timestamp >= from && statement.timestamp <= to
    } yield statement
  }

  private def byUserIntervalRich(userId: Rep[UserId], from: Rep[DateTime], to: Rep[DateTime]) = {
    byUserInterval(userId, from, to).join(CreditCards.query).on(_.creditCardId === _.creditCardId).map({
      case (statement, creditCard) =>
        (creditCard.vendor, creditCard.name, statement.amount, statement.timestamp)
    })
  }

  private val byUserIntervalCompiled = Compiled(byUserInterval _)
  private val byUserIntervalRichCompiled = Compiled(byUserIntervalRich _)

  def findByUserId(userId: UserId, interval: Interval): Future[Seq[CreditCardStatement]] = {
    database.run(byUserIntervalCompiled(userId, interval.getStart, interval.getEnd).result)
  }

  def findRichByUserId(userId: UserId, interval: Interval): Future[Seq[RichCreditCardStatement]] = {
    database
      .run(byUserIntervalRichCompiled(userId, interval.getStart, interval.getEnd).result)
      .map(_.map(RichCreditCardStatement.apply _ tupled))
  }

  def saveStatement(creditCard: CreditCardStatement): Future[Boolean] = {
    database.run(CreditCardStatements.query.+=(creditCard)).map(_ > 0)

  }
}
