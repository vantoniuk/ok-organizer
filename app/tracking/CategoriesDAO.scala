package tracking

import models.UserId
import models.db.Implicits._
import models.db.MyPostgresDriver.api.{Tag => DBTag, _}
import models.db.{Implicits, MyPostgresDriver, Users}

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

case class SpendCategory(userId: UserId, name: String, description: String, recommendedLimit: Int)

class SpendCategories(tag: DBTag) extends Table[SpendCategory](tag, "spend_categories") {
  def userId = column[UserId]("user_id")
  def name = column[String]("name")
  def description = column[String]("description")
  def recommendedLimit = column[Int]("recommended_limit")

  def * = (userId, name, description, recommendedLimit) <> (SpendCategory.apply _ tupled, SpendCategory.unapply)

  def pk = primaryKey("credit_card_pk", (userId, name))
  def userIdFK = foreignKey("credit_card_uid_fk", userId, Users.query)(_.id, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)
}

object SpendCategories {
  val query = TableQuery[SpendCategories]
}

trait SpendCategoriesDAO {
  def findByUserId(userId: UserId): Future[Seq[SpendCategory]]
  def saveSpendCategory(category: SpendCategory): Future[Boolean]
}

class PostgresSpendCategoriesDAO(database: Database) extends SpendCategoriesDAO {
  private def byUserName(userId: Rep[UserId], name: Rep[String]) = {
    for {
      category <- SpendCategories.query if category.userId === userId && category.name === name
    } yield category
  }

  private def byUser(userId: Rep[UserId]) = {
    for {
      card <- SpendCategories.query if card.userId === userId
    } yield card
  }

  private val byUserNameCompiled = Compiled(byUserName _)
  private val byUserCompiled = Compiled(byUser _)

  def findByUserId(userId: UserId): Future[Seq[SpendCategory]] = {
    database.run(byUserCompiled(userId).result)
  }

  def saveSpendCategory(category: SpendCategory): Future[Boolean] = {
    val cardQuery = byUserNameCompiled(category.userId, category.name)
    val action = cardQuery.result.flatMap{
      case r if r.nonEmpty => cardQuery.update(category)
      case _ => SpendCategories.query.+=(category)
    }

    database.run(action).map(_ > 0)

  }
}
