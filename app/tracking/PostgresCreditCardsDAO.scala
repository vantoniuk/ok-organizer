package tracking

import models.db.MyPostgresDriver.MyAPI._
import models.db.{Users, MyPostgresDriver, Implicits}
import models.{User, UserId, UserRole}
import org.joda.time.DateTime
import slick.dbio.DBIOAction

import scala.concurrent.Future
import models.{UserId, UserRole, User}
import Implicits._
import MyPostgresDriver.api.{Tag => DBTag, _}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits._

case class CreditCard(userId: UserId, vendor: String, name: String, description: String, availableCredit: Int, total: Int, addedAt: DateTime)

class CreditCards(tag: DBTag) extends Table[CreditCard](tag, "credit_cards") {
  def userId = column[UserId]("user_id")
  def vendor = column[String]("vendor")
  def name = column[String]("name")
  def description = column[String]("description")
  def availableCredit = column[Int]("available_credit")
  def total = column[Int]("total")
  def addedAt = column[DateTime]("added_at")

  def * = (userId, vendor, name, description, availableCredit, total, addedAt) <> (CreditCard.apply _ tupled, CreditCard.unapply)

  def pk = primaryKey("credit_card_pk", (userId, vendor, name))
  def userIdFK = foreignKey("credit_card_uid_fk", userId, Users.query)(_.id, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)
}

object CreditCards {
  val query = TableQuery[CreditCards]
}

trait CreditCardsDAO {
  def findByUserId(userId: UserId): Future[Seq[CreditCard]]
  def saveCreditCard(creditCard: CreditCard): Future[Boolean]
}

class PostgresCreditCardsDAO(database: Database) extends CreditCardsDAO {
  private def byUserVendorName(userId: Rep[UserId], vendor: Rep[String], name: Rep[String]) = {
    for {
      card <- CreditCards.query if card.userId === userId && card.vendor === vendor && card.name === name
    } yield card
  }

  private def byUser(userId: Rep[UserId]) = {
    for {
      card <- CreditCards.query if card.userId === userId
    } yield card
  }

  private val byUserVendorNameCompiled = Compiled(byUserVendorName _)
  private val byUserCompiled = Compiled(byUser _)

  def findByUserId(userId: UserId): Future[Seq[CreditCard]] = {
    database.run(byUserCompiled(userId).result)
  }

  def saveCreditCard(creditCard: CreditCard): Future[Boolean] = {
    val cardQuery = byUserVendorNameCompiled(creditCard.userId, creditCard.vendor, creditCard.name)
    val action = cardQuery.result.flatMap{
      case r if r.nonEmpty => cardQuery.update(creditCard)
      case _ => CreditCards.query.+=(creditCard)
    }

    database.run(action).map(_ > 0)

  }
}
