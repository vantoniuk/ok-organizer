package models.note.db

import models.db.MyPostgresDriver.api.{Tag => DBTag, _}
import models.db.{Services, Users}
import models.note.{Node, NodeId, NodePriority, NodeType}
import models.{ServiceId, User, UserId}
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
  def expired = column[Option[DateTime]]("expired")
  def service = column[ServiceId]("service")

  def * = (id, parentId, nodeType, title, description, icon, priority, rating, author, created, expired, service) <> (Node.apply _ tupled, Node.unapply)

  def userFK = foreignKey("user_id_fk", author, Users.query)(_.id, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)
  def serviceFK = foreignKey("service_id_fk", service, Services.query)(_.id, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)

  def authorNodeTypeIdx = index("author_type_idx", (author, nodeType))
  def serviceNodeTypeIdx = index("service_type_idx", (service, nodeType))
  def parentIdIdx = index("parent_id_idx", parentId)
}

object Nodes {
  val query = TableQuery[Nodes]
  query.schema.createStatements.foreach(println)

  val ratingSortedQuery = query.sortBy(_.rating)
}

class PostgresNodeDAO(database: Database) extends NodeDAO {
  private def findById(id: Rep[NodeId]) = {
    Nodes.query.filter(_.id === id)
  }
  private def findByParentId(id: Rep[Option[NodeId]]) = {
    Nodes.ratingSortedQuery.sortBy(_.rating).filter(_.parentId === id)
  }
  private def findAuthorAndType(author: Rep[UserId], nodeType: Rep[NodeType]) = {
    Nodes.ratingSortedQuery.filter(_.author === author).filter(_.nodeType === nodeType)
  }
  private def findServiceAndType(service: Rep[ServiceId], nodeType: Rep[NodeType]) = {
    Nodes.ratingSortedQuery.filter(_.service === service).filter(_.nodeType === nodeType)
  }

  private val findByIdCompiled = Compiled(findById _)
  private val findByParentIdCompiled = Compiled(findByParentId _)
  private val findByAuthorAndTypeCompiled = Compiled(findAuthorAndType _)
  private val findByServiceAndTypeCompiled = Compiled(findServiceAndType _)

  def find(id: NodeId): Future[Option[Node]] = {
    database.run(findByIdCompiled(id).result).map(_.headOption)
  }

  def findSubNodes(id: NodeId): Future[List[Node]] = {
    database.run(findByParentIdCompiled(Some(id)).result).map(_.toList)
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

  def findByUser(author: UserId, nodeType: NodeType): Future[List[Node]] = {
    database.run(findByAuthorAndTypeCompiled(author, nodeType).result).map(_.toList)
  }

  def findByService(serviceId: ServiceId, nodeType: NodeType): Future[List[Node]] = {
    database.run(findByServiceAndTypeCompiled(serviceId, nodeType).result).map(_.toList)
  }
}
