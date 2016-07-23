package models.db

import models._
import Implicits._
import MyPostgresDriver.api.{Tag => DBTag, _}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits._

class Services(tag: DBTag) extends Table[Service](tag, "service") {
  def id = column[ServiceId]("id", O.PrimaryKey, O.AutoInc)
  def title = column[String]("title")
  def description = column[String]("description")
  def url = column[String]("url")
  def roleRequired = column[UserRole]("role")

  def * = (id, title, description, url, roleRequired) <> (Service.apply _ tupled, Service.unapply)

  def titleIdx = index("title_idx", title, unique = true)
}

object Services {
  val query = TableQuery[Services]
}

class PostgresServiceDAO(database: Database) extends ServiceDAO {
  private def findByIdQueryRaw(id: Rep[ServiceId]) = {
    Services.query.filter(_.id === id)
  }

  private val findByIdCompiled = Compiled(findByIdQueryRaw _)

  def find(id: ServiceId): Future[Option[Service]] = {
    database.run(findByIdCompiled(id).result).map(_.headOption)
  }

  def delete(id: ServiceId): Future[Int] = {
    database.run(findByIdCompiled(id).delete)
  }

  def save(service: Service): Future[Service] = {
    val insertAction = (Services.query returning Services.query.map(_.id) into ((service, id) => service.copy(id = id))) insertOrUpdate service

    database.run(insertAction).map(_.getOrElse(throw new IllegalStateException("Failed to save service!! " + service)))
  }

  override def getAll: Future[Seq[Service]] = database.run(Services.query.result)
}
