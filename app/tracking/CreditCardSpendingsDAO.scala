package tracking

import models.UserId
import models.db.Implicits._
import models.db.MyPostgresDriver.api.{Tag => DBTag, _}
import models.db.{Implicits, MyPostgresDriver, Users}
import org.joda.time.{DateTime, Interval}

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

case class CreditCardSpending(id: Int, userId: UserId, creditCardId: CreditCardId, categoryId: SpendCategoryId, amount: Int, timestamp: DateTime)
case class RichCreditCardSpending(creditCardVendor: String, creditCardName: String, categoryName: String, amount: Int, timestamp: DateTime)

class CreditCardSpendings(tag: DBTag) extends Table[CreditCardSpending](tag, "credit_card_spendings") {
  def id = column[Int]("id")
  def userId = column[UserId]("user_id")
  def creditCardId = column[CreditCardId]("card_id")
  def categoryId = column[SpendCategoryId]("category_id")
  def amount = column[Int]("amount")
  def timestamp = column[DateTime]("timestamp")

  def * = (id, userId, creditCardId, categoryId, amount, timestamp) <> (CreditCardSpending.apply _ tupled, CreditCardSpending.unapply)

  def cardIdFK = foreignKey("credit_card_spendings_card_id_fk", creditCardId, CreditCards.query)(_.creditCardId, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)
  def catIdFK = foreignKey("credit_card_spendings_cat_id_fk", categoryId, SpendCategories.query)(_.id, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)
  def userIdFK = foreignKey("credit_card_spendings_uid_fk", userId, Users.query)(_.id, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)
}

object CreditCardSpendings {
  val query = TableQuery[CreditCardSpendings]
}

trait CreditCardSpendingsDAO {
  def findByUserId(userId: UserId, interval: Interval): Future[Seq[CreditCardSpending]]
  def findRichByUserId(userId: UserId, interval: Interval): Future[Seq[RichCreditCardSpending]]
  def saveStatement(creditCard: CreditCardSpending): Future[Boolean]
}

class PostgresCreditCardSpendingsDAO(database: Database) extends CreditCardSpendingsDAO {
  private def byUserInterval(userId: Rep[UserId], from: Rep[DateTime], to: Rep[DateTime]) = {
    for {
      statement <- CreditCardSpendings.query if statement.userId === userId && statement.timestamp >= from && statement.timestamp <= to
    } yield statement
  }

  private def byUserIntervalRich(userId: Rep[UserId], from: Rep[DateTime], to: Rep[DateTime]) = {
    byUserInterval(userId, from, to).join(SpendCategories.query).on(_.categoryId === _.id).map({
      case (statement, category) =>
        (statement.creditCardId, category.name, statement.amount, statement.timestamp)
    }).join(CreditCards.query).on(_._1 === _.creditCardId).map({
      case ((_, categoryName, amount, timestamp), creditCard) =>
        (creditCard.vendor, creditCard.name, categoryName, amount, timestamp)
    })
  }

  private val byUserIntervalCompiled = Compiled(byUserInterval _)
  private val byUserIntervalRichCompiled = Compiled(byUserIntervalRich _)

  def findByUserId(userId: UserId, interval: Interval): Future[Seq[CreditCardSpending]] = {
    database.run(byUserIntervalCompiled(userId, interval.getStart, interval.getEnd).result)
  }

  def findRichByUserId(userId: UserId, interval: Interval): Future[Seq[RichCreditCardSpending]] = {
    database
      .run(byUserIntervalRichCompiled(userId, interval.getStart, interval.getEnd).result)
      .map(_.map(RichCreditCardSpending.apply _ tupled))
  }

  def saveStatement(creditCard: CreditCardSpending): Future[Boolean] = {
    database.run(CreditCardSpendings.query.+=(creditCard)).map(_ > 0)

  }
}
