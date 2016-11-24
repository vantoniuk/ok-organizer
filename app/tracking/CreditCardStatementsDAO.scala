package tracking

import models.UserId
import models.db.Implicits._
import models.db.MyPostgresDriver.api.{Tag => DBTag, _}
import models.db.{Implicits, MyPostgresDriver, Users}
import org.joda.time.{DateTime, Interval}

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class CreditCardStatement(id: Int, userId: UserId, creditCardId: CreditCardId, available: Int, amountPaid: Int, timestamp: DateTime)
case class RichCreditCardStatement(id: Int, creditCardVendor: String, creditCardName: String, available: Int, amountPaid: Int, timestamp: DateTime)

class CreditCardStatements(tag: DBTag) extends Table[CreditCardStatement](tag, "credit_card_statements") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def userId = column[UserId]("user_id")
  def creditCardId = column[CreditCardId]("card_id")
  def available = column[Int]("available")
  def amountPaid = column[Int]("paid")
  def timestamp = column[DateTime]("timestamp")

  def * = (id, userId, creditCardId, available, amountPaid, timestamp) <> (CreditCardStatement.apply _ tupled, CreditCardStatement.unapply)

  def cardIdFK = foreignKey("credit_card_statement_card_id_fk", creditCardId, CreditCards.query)(_.creditCardId, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)
  def userIdFK = foreignKey("credit_card_statement_uid_fk", userId, Users.query)(_.id, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)
}

object CreditCardStatements {
  val query = TableQuery[CreditCardStatements]
}

case class CreditCardPaymentSummary(cardVendor: String, cardName: String, from: DateTime, to: DateTime, paidAmount: Int)

object CreditCardPaymentSummary {
  def weekly(cardVendor: String, cardName: String, weekStart: DateTime, paidAmount: Int): CreditCardPaymentSummary = {
    CreditCardPaymentSummary(cardVendor, cardName, weekStart, weekStart.plusDays(7), paidAmount)
  }
}

case class PaymentSummary(from: DateTime, to: DateTime, paidAmount: Int)

object PaymentSummary {
  def weekly(weekStart: DateTime, paidAmount: Int): PaymentSummary = {
    PaymentSummary(weekStart, weekStart.plusDays(7), paidAmount)
  }

  implicit val summaryWrites = (
    (JsPath \ "from").write[Long].contramap[DateTime](_.getMillis) ~
    (JsPath \ "to").write[Long].contramap[DateTime](_.getMillis) ~
    (JsPath \ "amount").write[Double].contramap[Int](_.toDouble / 100)
  )(unlift(PaymentSummary.unapply))
}

trait CreditCardStatementsDAO {
  def findByUserId(userId: UserId, interval: Interval): Future[Seq[CreditCardStatement]]
  def findRichByUserId(userId: UserId, interval: Interval): Future[Seq[RichCreditCardStatement]]
  def findRichByCardId(cardId: CreditCardId, interval: Interval): Future[Seq[RichCreditCardStatement]]
  def saveStatement(creditCard: CreditCardStatement): Future[Boolean]
  def weeklySummary(userId: UserId, interval: Interval): Future[Seq[PaymentSummary]]
}

class PostgresCreditCardStatementsDAO(database: Database) extends CreditCardStatementsDAO {
  val date_trunc = SimpleFunction.binary[String, DateTime, DateTime]("date_trunc")
  private def byUserInterval(userId: Rep[UserId], from: Rep[DateTime], to: Rep[DateTime]) = {
    for {
      statement <- CreditCardStatements.query.sortBy(_.timestamp) if statement.userId === userId && statement.timestamp >= from && statement.timestamp <= to
    } yield statement
  }
  private def byCardInterval(creditCardId: Rep[CreditCardId], from: Rep[DateTime], to: Rep[DateTime]) = {
    for {
      statement <- CreditCardStatements.query.sortBy(_.timestamp) if statement.creditCardId === creditCardId && statement.timestamp >= from && statement.timestamp <= to
    } yield statement
  }

  private def byUserIntervalRich(userId: Rep[UserId], from: Rep[DateTime], to: Rep[DateTime]) = {
    byUserInterval(userId, from, to).join(CreditCards.query).on(_.creditCardId === _.creditCardId).map({
      case (statement, creditCard) =>
        (statement.id, creditCard.vendor, creditCard.name, statement.available, statement.amountPaid, statement.timestamp)
    })
  }

  private def byCardIntervalRich(cardId: Rep[CreditCardId], from: Rep[DateTime], to: Rep[DateTime]) = {
    byCardInterval(cardId, from, to).join(CreditCards.query).on(_.creditCardId === _.creditCardId).map({
      case (statement, creditCard) =>
        (statement.id, creditCard.vendor, creditCard.name, statement.available, statement.amountPaid, statement.timestamp)
    })
  }

  private def weeklySummaryQuery(userId: Rep[UserId], from: Rep[DateTime], to: Rep[DateTime]) = {
    byUserIntervalRich(userId, from, to)
      .groupBy(tuple => date_trunc("week", tuple._6))
      .map({case(weekStart, seq) => (weekStart, seq.map(_._5).sum.getOrElse(0))})
  }

  private val byUserIntervalCompiled = Compiled(byUserInterval _)
  private val byUserIntervalRichCompiled = Compiled(byUserIntervalRich _)
  private val byCardIntervalRichCompiled = Compiled(byCardIntervalRich _)
  private val weeklySummaryQueryCompiled = Compiled(weeklySummaryQuery _)

  def findByUserId(userId: UserId, interval: Interval): Future[Seq[CreditCardStatement]] = {
    database.run(byUserIntervalCompiled(userId, interval.getStart, interval.getEnd).result)
  }

  def findRichByUserId(userId: UserId, interval: Interval): Future[Seq[RichCreditCardStatement]] = {
    database
      .run(byUserIntervalRichCompiled(userId, interval.getStart, interval.getEnd).result)
      .map(_.map(RichCreditCardStatement.apply _ tupled))
  }

  def findRichByCardId(cardId: CreditCardId, interval: Interval): Future[Seq[RichCreditCardStatement]] = {
    database
      .run(byCardIntervalRichCompiled(cardId, interval.getStart, interval.getEnd).result)
      .map(_.map(RichCreditCardStatement.apply _ tupled))
  }

  def saveStatement(creditCard: CreditCardStatement): Future[Boolean] = {
    database.run(CreditCardStatements.query.+=(creditCard)).map(_ > 0)
  }

  def weeklySummary(userId: UserId, interval: Interval): Future[Seq[PaymentSummary]] = {
    database
      .run(weeklySummaryQueryCompiled(userId, interval.getStart, interval.getEnd).result)
      .map(_.map(PaymentSummary.weekly _ tupled))
  }
}
