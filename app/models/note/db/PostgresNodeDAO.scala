package models.note.db

import models.db.MyPostgresDriver.api.{Tag => DBTag, _}
import models.db.Users
import models.note.{Node, NodeId, NodePriority, NodeType}
import models.{User, UserId}
import org.joda.time.DateTime
import models.db.Implicits._
import Implicits._
import scala.concurrent.ExecutionContext.Implicits._

import scala.concurrent.Future

class Nodes(tag: DBTag) extends Table[Node](tag, "nodes") {
  def id = column[NodeId]("id", O.PrimaryKey, O.AutoInc)
  def parentId = column[Option[NodeId]]("parent_id")
  def nodeType = column[NodeType]("type")
  def title = column[String]("title", O.Length(255, varying = true))
  def description = column[String]("description")
  def icon = column[Option[String]]("icon", O.Length(255, varying = true))
  def priority = column[NodePriority]("priority")
  def rating = column[Int]("rating")
  def author = column[UserId]("author")
  def created = column[DateTime]("created")

  def * = (id, parentId, nodeType, title, description, icon, priority, rating, author, created) <> (Node.apply _ tupled, Node.unapply)

  def userFK = foreignKey("user_id_fk", author, Users.query)(_.id, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)
}

object Nodes {
  val query = TableQuery[Nodes]
}

class PostgresUserDAO(database: Database) extends NodeDAO {
  private def findById(id: Rep[NodeId]) = {
    Nodes.query.filter(_.id === id)
  }
  private def findAuthorAndType(author: Rep[UserId], nodeType: Rep[NodeType]) = {
    Nodes.query.filter(_.author === author).filter(_.nodeType === nodeType)
  }

  private val findByIdCompiled = Compiled(findById _)
  private val findByAuthorAndTypeCompiled = Compiled(findAuthorAndType _)

  def find(id: NodeId): Future[Option[Node]] = {
    database.run(findByIdCompiled(id).result).map(_.headOption)
  }

  def update(node: Node): Future[Node] = {
    database.run(findByIdCompiled(node.id).update(node)).map(_ => node)
  }

  def delete(nodeId: NodeId): Future[Boolean] = {
    database.run(findByIdCompiled(nodeId).delete).map(_ => true)
  }

  def save(node: Node): Future[Node] = {
    val insertAction = (Nodes.query returning Nodes.query.map(_.id) into ((node, id) => node.copy(id = id))) insertOrUpdate node

    database.run(insertAction).map(_.getOrElse(throw new IllegalStateException("Failed to save user!! " + node)))
  }

  def find(author: UserId, nodeType: NodeType): Future[List[Node]] = {
    database.run(findByAuthorAndTypeCompiled(author, nodeType).result).map(_.toList)
  }
}
